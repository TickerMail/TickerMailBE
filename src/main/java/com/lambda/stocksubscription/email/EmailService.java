package com.lambda.stocksubscription.email;

import com.lambda.stocksubscription.dollar.Dollar;
import com.lambda.stocksubscription.dollar.DollarFetchService;
import com.lambda.stocksubscription.stock.Stock;
import com.lambda.stocksubscription.stock.StockRepository;
import com.lambda.stocksubscription.stockprice.StockPrice;
import com.lambda.stocksubscription.stockprice.StockPriceRepository;
import com.lambda.stocksubscription.user.User;
import com.lambda.stocksubscription.user.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final TemplateEngine templateEngine;
    private final DollarFetchService dollarFetchService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.subject-prefix}")
    private String emailSubjectPrefix;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * 매일 오전 9시에 전일 종가 이메일 발송
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional(readOnly = true)
    public void sendDailyClosingPriceEmails() {
        if (!emailEnabled) {
            log.info("이메일 발송 기능이 비활성화되어 있습니다.");
            return;
        }

        log.info("일일 종가 이메일 발송 시작");

        // 구독 활성화된 모든 사용자 조회
        List<User> subscribedUsers = userRepository.findBySubscribedTrue();
        log.info("구독 중인 사용자 수: {}", subscribedUsers.size());

        // 전일 거래일 계산
        LocalDate previousTradingDate = getPreviousTradingDate();
        log.info("전일 거래일: {}", previousTradingDate);

        // 각 사용자별로 이메일 발송
        for (User user : subscribedUsers) {
            try {
                sendStockPriceEmailToUser(user, previousTradingDate);
                log.info("사용자 {}에게 이메일 발송 성공", user.getEmail());
            } catch (Exception e) {
                log.error("사용자 {}에게 이메일 발송 실패: {}", user.getEmail(), e.getMessage(), e);
            }
        }

        log.info("일일 종가 이메일 발송 완료");
    }

    /**
     * 특정 사용자에게 주식 가격 이메일 발송
     */
    @Transactional(readOnly = true)
    public void sendStockPriceEmailToUser(User user, LocalDate tradingDate) throws Exception {
        log.info("사용자 {}의 관심 종목 {}", user.getEmail(), user.getInterestedStocks());

        // 관심 종목이 없으면 이메일 발송 안함
        if (user.getInterestedStocks() == null || user.getInterestedStocks().isEmpty()) {
            log.info("사용자 {}의 관심 종목이 없습니다.", user.getEmail());
            return;
        }

        // 관심 종목에 대한 주식 정보와 가격 정보 조회
        List<String> symbols = user.getInterestedStocks();
        List<Stock> stocks = stockRepository.findBySymbolIn(symbols);

        // 심볼 -> Stock 맵 생성
        Map<String, Stock> stockMap = stocks.stream()
            .collect(Collectors.toMap(Stock::getSymbol, stock -> stock));

        // 해당 날짜의 주가 데이터 조회
        List<StockPrice> stockPrices = stockPriceRepository.findBySymbolInAndTradingDate(symbols, tradingDate);

        // 심볼 -> StockPrice 맵 생성
        Map<String, StockPrice> priceMap = stockPrices.stream()
            .collect(Collectors.toMap(StockPrice::getSymbol, price -> price));

        // 이메일 컨텍스트 생성
        Context context = new Context();
        context.setVariable("userName", user.getEmail() != null ? user.getEmail() : "투자자");
        context.setVariable("tradingDate", tradingDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));

        // 이메일에 포함할 주식 데이터 생성
        List<Map<String, Object>> stockDataList = new ArrayList<>();

        for (String symbol : symbols) {
            Map<String, Object> stockData = new HashMap<>();
            stockData.put("symbol", symbol);

            // 주식 기본 정보
            Stock stock = stockMap.get(symbol);
            if (stock != null) {
                stockData.put("companyName", stock.getCompanyName());
                stockData.put("exchange", stock.getExchange());
            } else {
                stockData.put("companyName", symbol);
                stockData.put("exchange", "");
            }

            // 주가 정보
            StockPrice price = priceMap.get(symbol);
            if (price != null) {
                stockData.put("closingPrice", price.getClosingPrice());
                stockData.put("changeAmount", price.getChangeAmount());
                stockData.put("changePercent", price.getChangePercent());
                stockData.put("positive", price.getChangeAmount().compareTo(BigDecimal.ZERO) >= 0);
            } else {
                stockData.put("closingPrice", "데이터 없음");
                stockData.put("changeAmount", "");
                stockData.put("changePercent", "");
                stockData.put("positive", true);
            }

            stockDataList.add(stockData);
        }

        context.setVariable("stocks", stockDataList);

        // 환율 데이터 넣기
        Map<String, Object> exchangeRateData = new HashMap<>();
        Dollar exchangeRate = dollarFetchService.fetchExchangeRates();
        exchangeRateData.put("date", exchangeRate.getSearchDate().toString());
        exchangeRateData.put("usdKrwBuy", exchangeRate.getBuyingRate());
        exchangeRateData.put("usdKrwSell", exchangeRate.getSellingRate());

        context.setVariable("exchangeRate", exchangeRate);

        // Thymeleaf 템플릿으로 이메일 본문 생성
        String emailContent = templateEngine.process("stock-price-email", context);

        // 이메일 발송
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(user.getEmail());
        helper.setSubject(String.format("[%s] %s 미국 주식 전일 종가 정보",
            emailSubjectPrefix,
            tradingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        helper.setText(emailContent, true);

        mailSender.send(message);
    }

    /**
     * 단일 사용자에게 테스트 이메일 발송 (관리자용)
     */
    public void sendTestEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
        log.info(getPreviousTradingDate().toString());
        sendStockPriceEmailToUser(user, getPreviousTradingDate());
        log.info("테스트 이메일 발송 완료: {}", email);
    }

    /**
     * 이전 거래일 계산 (주말 및 공휴일 고려)
     */
    private LocalDate getPreviousTradingDate() {
        LocalDate today = LocalDate.now();
        return today;
    }

    /**
     * 새로운 사용자 등록 환영 이메일 발송
     */
    public void sendWelcomeEmail(User user) throws MessagingException {
        Context context = new Context();
        context.setVariable("userName", user.getEmail() != null ? user.getEmail() : "투자자");

        String emailContent = templateEngine.process("welcome-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(user.getEmail());
        helper.setSubject(String.format("[%s] 가입을 환영합니다", emailSubjectPrefix));
        helper.setText(emailContent, true);

        mailSender.send(message);
        log.info("환영 이메일 발송 완료: {}", user.getEmail());
    }
}