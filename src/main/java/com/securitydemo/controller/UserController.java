package com.securitydemo.controller;

import com.securitydemo.model.User;
import com.securitydemo.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    // 회원가입 처리 (비밀번호 해싱 취약점)
    @PostMapping("/signup")
    public String signup(@RequestParam String username,
                         @RequestParam String password,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) Integer birthYear,
                         Model model) {
        try {
            User user = userService.signUp(username, password, email, birthYear);
            model.addAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "회원가입 중 오류 발생: " + e.getMessage());
            return "signup";
        }
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response,
                        Model model) {
        Optional<User> optionalUser = userService.login(username, password);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // 세션 토큰 생성 및 쿠키에 저장
            String sessionToken = username + "-" + System.currentTimeMillis();
            Cookie cookie = new Cookie("sesson_token", sessionToken);
            cookie.setPath("/");
            cookie.setHttpOnly(false);  // 취약점: JavaScript에서 쿠키 접근 가능
            response.addCookie(cookie);

            return "redirect:/";
        } else {
            model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "login";
        }
    }

    // 프로필 페이지
    @GetMapping("/profile")
    public String profilePage(HttpServletRequest request, Model model) {
        // 사용자 인증 검증 (세션 토큰)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session_token".equals(cookie.getName())) {
                    User user = userService.getUserBySessionToken(cookie.getValue());
                    if (user != null) {
                        model.addAttribute("user", user);
                        return "profile";
                    }
                }
            }
        }

        // 인증되지 않은 사용자는 로그인 페이지로 리다이렉트
        return "redirect:/login";
    }

    // Dangerous Eval 취약점: 생년월일 입력으로 나이 계산
    @PostMapping("/profile/calculate-age")
    @ResponseBody
    public Map<String, Object> calculateAge(@RequestParam String birthYear) {
        // 취약점: 사용자 입력을 검증 없이 계산식에 사용함
        return userService.calculateAge(birthYear);
    }

    // SSRF 취약점: 프로필 이미지 URL 설정
    @PostMapping("/profile/update-url")
    @ResponseBody
    public ResponseEntity<?> updateProfileUrl(HttpServletRequest request,
                                              @RequestParam String profileUrl) {
        // 사용자 인증
        Cookie[] cookies = request.getCookies();
        User currentUser = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session_token".equals(cookie.getName())) {
                    currentUser = userService.getUserBySessionToken(cookie.getValue());
                    break;
                }
            }
        }

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "인증되지 않은 사용자"));
        }

        // 취약점: URL 검증 없이 외부 요청 수행
        try {
            // SSRF 취약점: 사용자 입력 URL로 요청을 보냄
            // 내부 네트워크 (127.0.0.1, 로컬 서버) 등에 접근 가능
            String response = restTemplate.getForObject(profileUrl, String.class);

            // 응답이 성공하면 프로필 URL 업데이트
            User updatedUser = userService.updateProfileUrl(currentUser.getId(), profileUrl);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "프로필 URL이 업데이트되었습니다.",
                    "preview", response.substring(0, Math.min(response.length(), 100)) // 응답 미리보기
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "URL에 접근할 수 없습니다: " + e.getMessage()
                    ));
        }
    }

    // 프로필 업데이트
    @PostMapping("/profile")
    public String updateProfile(HttpServletRequest request,
                                @RequestParam(required = false) String profileUrl,
                                @RequestParam(required = false) Integer birthYear,
                                Model model) {
        // 사용자 인증
        Cookie[] cookies = request.getCookies();
        User currentUser = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session_token".equals(cookie.getName())) {
                    currentUser = userService.getUserBySessionToken(cookie.getValue());
                    break;
                }
            }
        }

        if (currentUser == null) {
            return "redirect:/login";
        }

        // 프로필 업데이트
        if (profileUrl != null && !profileUrl.isEmpty()) {
            currentUser.setProfileUrl(profileUrl);
        }

        if (birthYear != null) {
            currentUser.setBirthYear(birthYear);
        }

        userService.updateProfileUrl(currentUser.getId(), profileUrl);

        model.addAttribute("message", "프로필이 업데이트되었습니다.");
        model.addAttribute("user", currentUser);

        return "profile";
    }
}
