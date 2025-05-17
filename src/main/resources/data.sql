-- MD5로 해싱된 비밀번호 ('password123' -> '482c811da5d5b4bc6d497ffa98491e38')
INSERT INTO users (username, password, email, is_admin, birth_year)
VALUES ('admin', '482c811da5d5b4bc6d497ffa98491e38', 'admin@example.com', TRUE, 1985);

INSERT INTO users (username, password, email, is_admin, birth_year)
VALUES ('user123', '482c811da5d5b4bc6d497ffa98491e38', 'user123@example.com', FALSE, 1990);

INSERT INTO users (username, password, email, is_admin, birth_year)
VALUES ('user456', '482c811da5d5b4bc6d497ffa98491e38', 'user456@example.com', FALSE, 1995);

-- 초기 게시글
INSERT INTO articles (user_id, title, content)
VALUES (2, '안녕하세요!', '<p>반갑습니다! 저는 user123입니다.</p>');

INSERT INTO articles (user_id, title, content)
VALUES (3, '해킹 공부 중', '<p>Spring Boot 보안 취약점 분석 중...</p><script>console.log("XSS 테스트")</script>');

INSERT INTO articles (user_id, title, content)
VALUES (1, '공지사항', '<p>이 사이트는 보안 학습용입니다.</p>');