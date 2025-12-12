-- ============================================================
-- RMRT 성능 테스트 데이터 초기화 + 생성 (50명)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET @TEST_EMAIL_PATTERN = 'test%@example.com';

-- ------------------------------------------------------------
-- 1. 기존 테스트 데이터 완전 삭제 (순서 중요!)
-- ------------------------------------------------------------

-- trust_score_id를 먼저 저장
CREATE TEMPORARY TABLE temp_trust_ids AS
SELECT trust_score_id
FROM members
WHERE email LIKE @TEST_EMAIL_PATTERN;

CREATE TEMPORARY TABLE temp_member_ids AS
SELECT id
FROM members
WHERE email LIKE @TEST_EMAIL_PATTERN;

-- 자식 테이블들 삭제
DELETE
FROM post_hearts
WHERE member_id IN (SELECT id FROM temp_member_ids);
DELETE
FROM comments
WHERE author_member_id IN (SELECT id FROM temp_member_ids);
DELETE
FROM collection_posts
WHERE collection_id IN (SELECT id FROM post_collections WHERE owner_member_id IN (SELECT id FROM temp_member_ids));
DELETE
FROM post_collections
WHERE owner_member_id IN (SELECT id FROM temp_member_ids);
DELETE
FROM post_images
WHERE post_id IN (SELECT id FROM posts WHERE author_member_id IN (SELECT id FROM temp_member_ids));
DELETE
FROM posts
WHERE author_member_id IN (SELECT id FROM temp_member_ids);
DELETE
FROM follows
WHERE follower_id IN (SELECT id FROM temp_member_ids);
DELETE
FROM friendships
WHERE member_id IN (SELECT id FROM temp_member_ids);
DELETE
FROM member_event
WHERE member_id IN (SELECT id FROM temp_member_ids);
DELETE
FROM activation_tokens
WHERE member_id IN (SELECT id FROM temp_member_ids);
DELETE
FROM member_roles
WHERE member_id IN (SELECT id FROM temp_member_ids);

-- members 삭제
DELETE
FROM members
WHERE email LIKE @TEST_EMAIL_PATTERN;

-- trust_score 삭제 (저장해둔 ID 사용)
DELETE
FROM trust_score
WHERE id IN (SELECT trust_score_id FROM temp_trust_ids);

-- member_detail 삭제
DELETE
FROM member_detail
WHERE profile_address LIKE 'testuser%';

-- 임시 테이블 삭제
DROP TEMPORARY TABLE IF EXISTS temp_trust_ids;
DROP TEMPORARY TABLE IF EXISTS temp_member_ids;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 2. 테스트용 회원 생성 (50명)
-- ------------------------------------------------------------

-- 변수 초기화
SET @i = 0;
SET @d = 0;
SET @row = 0;

-- 2-1. trust_score 50개 생성
INSERT INTO trust_score (trust_score, trust_level, real_money_review_count, ad_review_count)
SELECT 100 + ((n - 1) % 5) * 50,
       CASE ((n - 1) % 3)
           WHEN 0 THEN 'BRONZE'
           WHEN 1 THEN 'SILVER'
           ELSE 'GOLD'
           END,
       ((n - 1) % 20),
       ((n - 1) % 10)
FROM (SELECT @i := @i + 1 AS n
      FROM information_schema.columns
      LIMIT 50) t;

-- 2-2. member_detail 50개 생성
SET @d = 0;
INSERT INTO member_detail (activated_at, registered_at, introduction, profile_address, address, image_id)
SELECT NOW(),
       NOW(),
       CONCAT('성능 테스트용 계정 ', n),
       CONCAT('testuser', n),
       CONCAT('서울시 성능로 ', n),
       0
FROM (SELECT @d := @d + 1 AS n
      FROM information_schema.columns
      LIMIT 50) t;

-- 생성된 ID 범위 저장
SET @min_trust_id = (SELECT MIN(id)
                     FROM trust_score
                     ORDER BY id DESC
                     LIMIT 50);
SET @min_detail_id = (SELECT MIN(id)
                      FROM member_detail
                      WHERE profile_address LIKE 'testuser%');

-- 2-3. members 50명 생성
SET @row = 0;
INSERT INTO members (email, nickname, password_hash, status,
                     follower_count, following_count, post_count,
                     detail_id, trust_score_id, updated_at)
SELECT CONCAT('test', n, '@example.com'),
       CONCAT('TestUser', n),
       '비번', # !!!실제 hash 비번
       'ACTIVE',
       0,
       0,
       0,
       @min_detail_id + n - 1,
       @min_trust_id + n - 1,
       NOW()
FROM (SELECT @row := @row + 1 AS n
      FROM information_schema.columns
      LIMIT 50) seq;

-- ------------------------------------------------------------
-- 3. 테스트 포스트 100개 생성
-- ------------------------------------------------------------

SET @first_member_id = (SELECT MIN(id)
                        FROM members
                        WHERE email LIKE @TEST_EMAIL_PATTERN);
SET @row = 0;

INSERT INTO posts (author_member_id,
                   author_nickname,
                   author_introduction,
                   author_image_id,
                   restaurant_name,
                   restaurant_address,
                   restaurant_latitude,
                   restaurant_longitude,
                   content_text,
                   rating,
                   status,
                   view_count,
                   heart_count,
                   comment_count,
                   created_at,
                   updated_at)
SELECT @first_member_id + ((n - 1) % 50),
       CONCAT('TestUser', ((n - 1) % 50) + 1),
       CONCAT('성능 테스트용 계정 ', ((n - 1) % 50) + 1),
       0,
       CONCAT('맛집_', n),
       CONCAT('서울시 강남구 테스트로 ', n, '번길'),
       37.4979 + (n * 0.001),
       127.0276 + (n * 0.001),
       CONCAT('정말 맛있는 식당입니다! 성능 테스트용 포스트 #', n),
       1 + ((n - 1) % 5),
       'PUBLISHED',
       n * 10,
       n * 2,
       ((n - 1) % 10),
       DATE_SUB(NOW(), INTERVAL n HOUR),
       DATE_SUB(NOW(), INTERVAL n HOUR)
FROM (SELECT @row := @row + 1 AS n
      FROM information_schema.columns
      LIMIT 100) t;

-- ------------------------------------------------------------
-- 4. 결과 확인
-- ------------------------------------------------------------

SELECT '테스트 회원' AS type, COUNT(*) AS cnt
FROM members
WHERE email LIKE @TEST_EMAIL_PATTERN
UNION ALL
SELECT '테스트 포스트', COUNT(*)
FROM posts
WHERE author_member_id >= @first_member_id;

SELECT '✅ 테스트 데이터 생성 완료' AS message;
