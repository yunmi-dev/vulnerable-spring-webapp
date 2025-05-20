package com.securitydemo.service;

import com.securitydemo.model.User;
import com.securitydemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final Map<String, User> sessionStore = new HashMap<>();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;

        // 테스트용 사용자 추가
        try {
            User testUser = new User();
            testUser.setUsername("test");
            testUser.setPassword(hashMD5("test"));
            testUser.setEmail("test@example.com");
            testUser.setAdmin(false);
            userRepository.save(testUser);
            System.out.println("Test user added successfully");
        } catch (Exception e) {
            System.out.println("Error adding test user: " + e.getMessage());
        }
    }

    public User signUp(String username, String password, String email, Integer birthYear) {
        System.out.println("SignUp attempt - Username: " + username);

        try {
            // 취약점: 평문 비밀번호를 MD5로 해싱 (안전하지 않은 방식)
            String hashedPassword = hashMD5(password);

            User user = new User();
            user.setUsername(username);
            user.setPassword(hashedPassword); // 취약점: 솔트 없이 MD5로만 해싱
            user.setEmail(email);
            user.setBirthYear(birthYear);
            user.setAdmin(false);

            User savedUser = userRepository.save(user);
            System.out.println("User registered successfully: " + savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            System.out.println("SignUp error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public User login(String username, String password) {
        System.out.println("Login attempt - Username: " + username);

        try {
            // 취약점: 안전하지 않은 비밀번호 비교
            String hashedPassword = hashMD5(password);

            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                System.out.println("User found: " + user.getUsername() + ", Hash: " + user.getPassword());

                if (hashedPassword.equals(user.getPassword())) {
                    System.out.println("Password match - login successful");
                    String token = username + "-" + System.currentTimeMillis();
                    sessionStore.put(token, user);
                    System.out.println("Session token created: " + token);
                    return user;
                } else {
                    System.out.println("Password mismatch: Expected " + user.getPassword() + ", Got " + hashedPassword);
                }
            } else {
                System.out.println("User not found: " + username);
            }

            return null;
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String createSession(User user) {
        String token = user.getUsername() + "-" + System.currentTimeMillis();
        sessionStore.put(token, user);
        System.out.println("New session created: " + token + " for user: " + user.getUsername());
        return token;
    }

    public User getUserBySessionToken(String token) {
        User user = sessionStore.get(token);
        System.out.println("Session lookup - Token: " + token + ", User: " + (user != null ? user.getUsername() : "null"));
        return user;
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Dangerous Eval 취약점 시뮬레이션: 생년월일로 나이 계산
    public Map<String, Object> calculateAge(String birthYearInput) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 명령어 실행 시뮬레이션 (위험한 코드)
            if (birthYearInput.contains(";") || birthYearInput.contains("|") ||
                    birthYearInput.contains("$(") || birthYearInput.contains("`")) {

                // 명령어 추출 (보안을 위해 실제 실행은 하지 않음)
                String command = birthYearInput.replaceAll("^\\d+\\)\\s*;\\s*", "");

                // 명령어 실행 시뮬레이션
                result.put("age", -1);
                result.put("command_detected", command);
                return result;
            }

            // 일반적인 계산 수행
            try {
                int birthYear = Integer.parseInt(birthYearInput);
                int age = 2025 - birthYear;
                result.put("age", age);
            } catch (NumberFormatException e) {
                result.put("age", -1);
                result.put("error", "Invalid birth year");
            }

            return result;
        } catch (Exception e) {
            result.put("age", -1);
            result.put("error", e.getMessage());
            return result;
        }
    }

    // 사용자 프로필 URL 업데이트 (SSRF 취약점)
    public User updateProfileUrl(Integer userId, String profileUrl) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // 취약점: URL 검증 없이 저장
            user.setProfileUrl(profileUrl); // SSRF 취약점: 내부 URL 접근 가능
            return userRepository.save(user);
        }
        return null;
    }

    // MD5 해싱 함수 (비밀번호 해싱에 사용)
    private String hashMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해싱 오류", e);
        }
    }
}
