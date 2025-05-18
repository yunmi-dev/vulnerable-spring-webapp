package com.securitydemo.controller;

import com.securitydemo.model.Article;
import com.securitydemo.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    private final ArticleService articleService;

    @Autowired
    public SearchController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // 게시글 검색 (SQL Injection 취약점)
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model) {
        if (keyword != null && !keyword.isEmpty()) {
            // 취약점: 안전하지 않은 SQL 쿼리 실행
            List<Article> results = articleService.searchByTitle(keyword);
            model.addAttribute("results", results);
            model.addAttribute("keyword", keyword);

            // 취약점: SQL 쿼리를 위해 사용된 키워드를 그대로 보여줌
            model.addAttribute("sqlQuery", "SELECT * FROM articles WHERE title LIKE '%" + keyword + "%'");
        }

        return "search";
    }
}
