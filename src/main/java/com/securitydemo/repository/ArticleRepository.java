package com.securitydemo.repository;

import com.securitydemo.model.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ArticleRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Article> articleRowMapper = (rs, rowNum) -> {
        Article article = new Article();
        article.setId(rs.getInt("id"));
        article.setUserId(rs.getInt("user_id"));
        article.setTitle(rs.getString("title"));
        article.setContent(rs.getString("content"));
        article.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        article.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return article;
    };

    @Autowired
    public ArticleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Article> findAll() {
        return jdbcTemplate.query(
                "SELECT a.*, u.username as author_username FROM articles a JOIN users u ON a.user_id = u.id ORDER BY a.created_at DESC",
                (rs, rowNum) -> {
                    Article article = articleRowMapper.mapRow(rs, rowNum);
                    article.setAuthorUsername(rs.getString("author_username"));
                    return article;
                }
        );
    }

    public Optional<Article> findById(Integer id) {
        List<Article> articles = jdbcTemplate.query(
                "SELECT a.*, u.username as author_username FROM articles a JOIN users u ON a.user_id = u.id WHERE a.id = ?",
                (rs, rowNum) -> {
                    Article article = articleRowMapper.mapRow(rs, rowNum);
                    article.setAuthorUsername(rs.getString("author_username"));
                    return article;
                },
                id
        );
        return articles.isEmpty() ? Optional.empty() : Optional.of(articles.get(0));
    }

    // SQL Injection 취약점: 안전하지 않은 검색 쿼리
    public List<Article> searchByTitleUnsafe(String keyword) {
        // 취약점: 파라미터화된 쿼리 대신 문자열 연결 사용
        String sql = "SELECT a.*, u.username as author_username FROM articles a " +
                "JOIN users u ON a.user_id = u.id " +
                "WHERE a.title LIKE '%" + keyword + "%' " +
                "ORDER BY a.created_at DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Article article = articleRowMapper.mapRow(rs, rowNum);
            article.setAuthorUsername(rs.getString("author_username"));
            return article;
        });
    }

    public Article save(Article article) {
        LocalDateTime now = LocalDateTime.now();

        if (article.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO articles (user_id, title, content, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setInt(1, article.getUserId());
                ps.setString(2, article.getTitle());
                ps.setString(3, article.getContent());
                ps.setTimestamp(4, Timestamp.valueOf(now));
                ps.setTimestamp(5, Timestamp.valueOf(now));
                return ps;
            }, keyHolder);
            article.setId(keyHolder.getKey().intValue());
            article.setCreatedAt(now);
            article.setUpdatedAt(now);
            return article;
        } else {
            jdbcTemplate.update(
                    "UPDATE articles SET title = ?, content = ?, updated_at = ? WHERE id = ?",
                    article.getTitle(), article.getContent(), now, article.getId()
            );
            article.setUpdatedAt(now);
            return article;
        }
    }

    public boolean delete(Integer id) {
        return jdbcTemplate.update("DELETE FROM articles WHERE id = ?", id) > 0;
    }
}
