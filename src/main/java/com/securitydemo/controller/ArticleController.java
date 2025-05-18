package com.securitydemo.controller;

import com.securitydemo.model.Article;
import com.securitydemo.model.User;
import com.securitydemo.service.ArticleService;
import com.securitydemo.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class ArticleController {

    private final ArticleService articleService;
    private final UserService userService;

    @Autowired
    public ArticleController(ArticleService articleService, UserService userService) {
        this.articleService = articleService;
        this.userService = userService;
    }

    // 메인 페이지 (게시글 목록)
    @GetMapping("/")
    public String index(Model model) {
        List<Article> articles = articleService.findAll();
        model.addAttribute("articles", articles);
        return "index";
    }

    // 게시글 상세 보기 (XSS 취약점)
    @GetMapping("/posts/{id}")
    public String postDetail(@PathVariable Integer id, Model model) {
        Optional<Article> optionalArticle = articleService.findById(id);

        if (optionalArticle.isPresent()) {
            Article article = optionalArticle.get();
            model.addAttribute("article", article);

            // 취약점: 사용자 입력 HTML을 그대로 렌더링 (XSS 가능)
            model.addAttribute("content", article.getContent());

            return "post";
        } else {
            model.addAttribute("error", "게시글을 찾을 수 없습니다.");
            return "redirect:/";
        }
    }

    // 게시글 작성 페이지
    @GetMapping("/posts/new")
    public String newPost(HttpServletRequest request, Model model) {
        // 사용자 인증 확인
        User currentUser = getCurrentUser(request);

        if (currentUser == null) {
            return "redirect:/login";
        }

        return "new_post";
    }

    // 게시글 작성 처리 (XSS 취약점)
    @PostMapping("/posts/new")
    public String createPost(HttpServletRequest request,
                             @RequestParam String title,
                             @RequestParam String content,
                             Model model) {
        // 사용자 인증 확인
        User currentUser = getCurrentUser(request);

        if (currentUser == null) {
            model.addAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        // 취약점: 사용자 입력 HTML을 검증 없이 저장 (XSS 가능)
        Article article = articleService.create(currentUser.getId(), title, content);

        return "redirect:/posts/" + article.getId();
    }

    // 게시글 수정 페이지 (Broken Access Control 취약점)
    @GetMapping("/posts/{id}/edit")
    public String editPost(@PathVariable Integer id, Model model) {
        Optional<Article> optionalArticle = articleService.findById(id);

        if (optionalArticle.isPresent()) {
            Article article = optionalArticle.get();
            model.addAttribute("article", article);

            // 취약점: 작성자 확인 없이 수정 페이지 접근 허용
            return "edit_post";
        } else {
            model.addAttribute("error", "게시글을 찾을 수 없습니다.");
            return "redirect:/";
        }
    }

    // 게시글 수정 처리 (Broken Access Control 취약점)
    @PostMapping("/posts/{id}/edit")
    public String updatePost(@PathVariable Integer id,
                             @RequestParam String title,
                             @RequestParam String content) {

        // 취약점: 작성자 확인 없이 게시글 수정 허용
        articleService.update(id, title, content);

        return "redirect:/posts/" + id;
    }

    // 게시글 삭제 처리 (Broken Access Control 취약점)
    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Integer id) {

        // 취약점: 작성자 확인 없이 게시글 삭제 허용
        articleService.delete(id);

        return "redirect:/";
    }

    // 헬퍼 메서드: 현재 인증된 사용자 가져오기
    private User getCurrentUser(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session_token".equals(cookie.getName())) {
                    return userService.getUserBySessionToken(cookie.getValue());
                }
            }
        }

        return null;
    }
}
