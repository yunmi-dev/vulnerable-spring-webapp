package com.securitydemo.service;

import com.securitydemo.model.Article;
import com.securitydemo.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    @Autowired
    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    // 게시글 목록 조회
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    // 게시글 상세 조회
    public Optional<Article> findById(Integer id) {
        return articleRepository.findById(id);
    }

    // 게시글 저장/수정
    public Article save(Article article) {
        try {
            System.out.println("Saving article - Title: " + article.getTitle());
            System.out.println("Content: " + article.getContent().substring(0, Math.min(article.getContent().length(), 50)) + "...");
            return articleRepository.save(article);
        } catch (Exception e) {
            System.out.println("Error saving article: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 게시글 삭제
    public boolean delete(Integer id) {
        // 취약점: 작성자 검증 없이 삭제가 가능함
        return articleRepository.delete(id);
    }

    // 게시글 검색 (SQL Injection 취약점)
    public List<Article> searchByTitle(String keyword) {
        // 취약점: 안전 보증되지 않은 SQL 쿼리 실행
        return articleRepository.searchByTitleUnsafe(keyword);
    }

    // 취약점: 권한 검증 없이 게시글 수정
    public Article update(Integer id, String title, String content) {
        Optional<Article> optionalArticle = articleRepository.findById(id);
        if (optionalArticle.isPresent()) {
            Article article = optionalArticle.get();
            article.setTitle(title);
            article.setContent(content);    // 취약점: XSS 방지를 위한 HTML 이스케이프 없음
            return articleRepository.save(article);
        }
        return null;
    }

    // 게시글 작성
    public Article create(Integer userId, String title, String content) {
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);    // 취약점: XSS 방지를 위한 HTML 이스케이프 없음
        return articleRepository.save(article);
    }
}
