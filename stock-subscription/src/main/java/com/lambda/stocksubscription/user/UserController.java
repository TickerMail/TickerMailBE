package com.lambda.stocksubscription.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 설정 (프로덕션에서는 구체적인 도메인으로 제한하세요)
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        System.out.println(userDTO.toString());
        UserDTO createdUser = userService.createUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{email}/stocks")
    public ResponseEntity<UserDTO> updateUserStocks(
        @PathVariable String email,
        @RequestBody List<String> stocks) {
        UserDTO updatedUser = userService.updateUserStocks(email, stocks);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{email}/toggle-subscription")
    public ResponseEntity<UserDTO> toggleSubscription(@PathVariable String email) {
        UserDTO updatedUser = userService.toggleSubscription(email);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/subscribed")
    public ResponseEntity<List<UserDTO>> getAllSubscribedUsers() {
        List<UserDTO> users = userService.getAllSubscribedUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, Object>> unsubscribe(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        // TODO: token validation
        try {
            String email = request.get("email");
            if (email == null || email.isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일 주소가 제공되지 않았습니다");
                return ResponseEntity.badRequest().body(response);
            }
            userService.toggleSubscription(email);
            response.put("success", true);
            response.put("message", "구독이 성공적으로 취소되었습니다");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "구독 취소 처리 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}