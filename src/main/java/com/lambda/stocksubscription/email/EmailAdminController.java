package com.lambda.stocksubscription.email;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 이메일 발송 관련 API 컨트롤러
 * 관리자 기능으로 사용
 */
@RestController
@RequestMapping("/api/admin/email")
@RequiredArgsConstructor
@Slf4j
public class EmailAdminController {

    private final EmailService emailService;

    /**
     * 특정 사용자에게 테스트 이메일 발송
     */
    @PostMapping("/test")
    public ResponseEntity<String> sendTestEmail(@RequestParam String email) {
        try {
            emailService.sendTestEmail(email);
            return ResponseEntity.ok("테스트 이메일이 성공적으로 발송되었습니다: " + email);
        } catch (MessagingException e) {
            log.error("테스트 이메일 발송 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("이메일 발송 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("테스트 이메일 발송 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("오류 발생: " + e.getMessage());
        }
    }

    /**
     * 모든 구독자에게 즉시 이메일 발송 트리거
     */
    @PostMapping("/send-to-all")
    public ResponseEntity<String> sendEmailToAllSubscribers() {
        try {
            emailService.sendDailyClosingPriceEmails();
            return ResponseEntity.ok("모든 구독자에게 이메일 발송이 시작되었습니다.");
        } catch (Exception e) {
            log.error("일괄 이메일 발송 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("오류 발생: " + e.getMessage());
        }
    }
}
