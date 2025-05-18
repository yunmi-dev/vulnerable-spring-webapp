package com.securitydemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/debug")
public class SecurityMisconfigurationController {

    // 취약점: 디버그 모든 정보를 노출
    @GetMapping("/info")
    @ResponseBody
    public String debugInfo() {
        // 취약점: 시스템 정보 노출
        StringBuilder info = new StringBuilder();
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("OS: ").append(System.getProperty("os.name")).append("\n");
        info.append("User: ").append(System.getProperty("user.name")).append("\n");
        info.append("Working Directory: ").append(System.getProperty("user.dir")).append("\n");

        return info.toString();
    }

    // 취약점: 의도적인 에러 발생
    @GetMapping("/error")
    public String triggerError() {
        // 취약점: 의도적으로 예외를 발생시켜 스택 트레이스를 노출
        throw new RuntimeException("This is a test exception to demonstrate stack trace exposure");
    }
}
