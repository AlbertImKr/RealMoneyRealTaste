-- 1. 새 컬럼 추가 (NULL 허용)
ALTER TABLE follows
    ADD COLUMN follower_profile_image_id BIGINT NULL;

ALTER TABLE follows
    ADD COLUMN following_profile_image_id BIGINT NULL;

-- 기본값 설정
UPDATE follows
SET follower_profile_image_id = 0
WHERE follower_profile_image_id IS NULL;

UPDATE follows
SET following_profile_image_id = 0
WHERE following_profile_image_id IS NULL;

-- NOT NULL 제약 추가
ALTER TABLE follows
    MODIFY COLUMN follower_profile_image_id BIGINT NOT NULL;

ALTER TABLE follows
    MODIFY COLUMN following_profile_image_id BIGINT NOT NULL;

-- 2. member_detail에 image_id 추가
ALTER TABLE member_detail
    ADD COLUMN image_id BIGINT NULL;

-- 3. friendships에 member_nickname 추가
ALTER TABLE friendships
    ADD COLUMN member_nickname VARCHAR(50) NULL;

-- 4. members에 post_count 추가
ALTER TABLE members
    ADD COLUMN post_count BIGINT NOT NULL DEFAULT 0;

-- 5. ENUM을 VARCHAR로 변경 (DROP 없이 MODIFY 사용)
ALTER TABLE images
    MODIFY COLUMN image_type VARCHAR(255) NOT NULL;

ALTER TABLE post_collections
    MODIFY COLUMN privacy VARCHAR(255) NOT NULL;

-- ✅ status는 이미 존재하므로 MODIFY만 사용
ALTER TABLE post_collections
    MODIFY COLUMN status VARCHAR(255) NOT NULL;

ALTER TABLE member_roles
    MODIFY COLUMN `role` VARCHAR(255) NULL;

ALTER TABLE comments
    MODIFY COLUMN status VARCHAR(255) NOT NULL;

ALTER TABLE follows
    MODIFY COLUMN status VARCHAR(255) NOT NULL;

ALTER TABLE friendships
    MODIFY COLUMN status VARCHAR(20) NOT NULL;

ALTER TABLE members
    MODIFY COLUMN status VARCHAR(255) NOT NULL;

ALTER TABLE posts
    MODIFY COLUMN status VARCHAR(255) NOT NULL;

ALTER TABLE trust_score
    MODIFY COLUMN trust_level VARCHAR(255) NULL;

-- 6. updated_at NULL 허용
ALTER TABLE follows
    MODIFY COLUMN updated_at datetime NULL;
