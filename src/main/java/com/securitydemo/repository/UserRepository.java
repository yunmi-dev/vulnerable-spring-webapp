package com.securitydemo.repository;

import com.securitydemo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setAdmin(rs.getBoolean("is_admin"));
        user.setBirthYear(rs.getInt("birth_year"));
        user.setProfileUrl(rs.getString("profile_url"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    };

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }

    public Optional<User> findById(Integer id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE id = ?", userRowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findByUsername(String username) {
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE username = ?", userRowMapper, username);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    // SQL Injection 취약점: 안전하지 않은 쿼리 실행을 위한 메서드
    public User findByUsernameUnsafe(String username) {
        // 취약점: 파라미터화된 쿼리 대신 문자열 연결 사용
        String sql = "SELECT * FROM users WHERE username = '" + username + "'";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);
        return users.isEmpty() ? null : users.get(0);
    }

    public User save(User user) {
        if (user.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO users (username, password, email, is_admin, birth_year, profile_url) VALUES (?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPassword());
                ps.setString(3, user.getEmail());
                ps.setBoolean(4, user.isAdmin());
                ps.setInt(5, user.getBirthYear() != null ? user.getBirthYear() : 0);
                ps.setString(6, user.getProfileUrl());
                return ps;
            }, keyHolder);
            user.setId(keyHolder.getKey().intValue());
            return user;
        } else {
            jdbcTemplate.update(
                    "UPDATE users SET username = ?, password = ?, email = ?, is_admin = ?, birth_year = ?, profile_url = ? WHERE id = ?",
                    user.getUsername(), user.getPassword(), user.getEmail(), user.isAdmin(), user.getBirthYear(), user.getProfileUrl(), user.getId()
            );
            return user;
        }
    }

    public boolean delete(Integer id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id = ?", id) > 0;
    }
}
