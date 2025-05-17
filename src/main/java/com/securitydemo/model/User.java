package com.securitydemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String password;    // MD5 해싱된 비밀번호 저장 (취약점)
    private String email;
    private boolean isAdmin;
    private Integer birthYear;
    private String profileUrl;
    private LocalDateTime createdAt;
}
