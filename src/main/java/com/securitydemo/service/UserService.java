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
    }

    public User signUp(String username, String password, String email, Integer birthYear) {
        // 취약점: 평문 비밀번호를 MD5로 해싱함 (안전하지 않은 방식)
        String hashedPassword = hashMD5(password);

        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEmail(email);
        user.setBirthYear(birthYear);
        user.setAdmin(false);

        return userRepository.save(user);
    }

    public Optional<User> login(String username, String password) {
        // 취약점: 안전하지 않은 비밀번호 비교
        String hashedPassword = hashMD5(password);

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent() && optionalUser.get().getPassword().equals(hashedPassword)) {
            User user = optionalUser.get();
            sessionStore.put(generateSessionToken(username), user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    // 세션 토큰 생성 (간단한 문자열 조합)
    private String generateSessionToken(String username) {
        return username + "-" + System.currentTimeMillis();
    }

    // 세션에서 사용자 가져오기
    public User getUserBySessionToken(String token) {
        return sessionStore.get(token);
    }

    // 다양한 서비스 메서드
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
            // 위험한 방식 - 사용자 입력으로부터 문자열 생성 후 계산
            String expression = "2025 - " + birthYearInput;

            // 명령어 실행 시뮬레이션 (위험)
            if (birthYearInput.contains(";") || birthYearInput.contains("|") ||
                    birthYearInput.contains("$(") || birthYearInput.contains("`")) {

                // 명령어 추출 (보안을 위해 실제 실행은 안함)
                String command = birthYearInput.replaceAll("^\\d+\\)\\s*;\\s*", "");

                // 명령어 실행 시뮬레이션
                result.put("age", -1);
                result.put("command_detected", command);

                // 위험: 실제로는 아래와 같은 코드가 실행될 수 있음 (시연 목적으로 주석 처리)
                // Process process = Runtime.getRuntime().exec(command);
                // BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                // String commandOutput = reader.lines().collect(Collectors.joining("\n"));
                // result.put("command_output", commandOutput);

                return result;
            }

            // 일반적인 계싼 수행
            if (expression.contains("-")) {
                String[] parts = expression.split(" - ");
                int currentYear = Integer.parseInt(parts[0]);
                int birthYear = Integer.parseInt(parts[1]);
                int age = currentYear - birthYear;
                result.put("age", age);
                return result;
            }

            result.put("age", -1);
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
            // 취약점: URL 검증 없이 저장함
            user.setProfileUrl(profileUrl); // SSRF 취약점: 내부 URL 접근 가능
            return userRepository.save(user);
        }
        return null;
    }

    // MD5 해싱 함수 (비밀번호 해싱에 사용함)
    private String hashMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();

            // byte 배열을 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해싱 오류", e);
        }
    }
}
