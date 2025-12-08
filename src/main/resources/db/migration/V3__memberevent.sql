-- v3__memberevent.sql

-- 1. member_event 테이블 생성
CREATE TABLE member_event
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    member_id          BIGINT                NOT NULL,
    event_type         VARCHAR(30)           NOT NULL,
    title              VARCHAR(100)          NOT NULL,
    message            VARCHAR(500)          NOT NULL,
    related_member_id  BIGINT                NULL,
    related_post_id    BIGINT                NULL,
    related_comment_id BIGINT                NULL,
    is_read            BIT(1)                NOT NULL,
    created_at         datetime              NOT NULL,
    CONSTRAINT pk_member_event PRIMARY KEY (id)
);

CREATE INDEX idx_member_event_member_id ON member_event (member_id);
CREATE INDEX idx_member_event_created_at ON member_event (created_at);
CREATE INDEX idx_member_event_event_type ON member_event (event_type);
CREATE INDEX idx_member_event_is_read ON member_event (is_read);
CREATE INDEX idx_member_event_member_unread ON member_event (member_id, is_read, created_at);

-- 2. posts에 author_image_id 추가
ALTER TABLE posts
    ADD author_image_id BIGINT NULL;
UPDATE posts
SET author_image_id = 0
WHERE author_image_id IS NULL;
ALTER TABLE posts
    MODIFY author_image_id BIGINT NOT NULL;

-- 3. friendships에 이미지 ID 추가
ALTER TABLE friendships
    ADD friend_image_id BIGINT NULL;
ALTER TABLE friendships
    ADD image_id BIGINT NULL;

UPDATE friendships
SET friend_image_id = 0
WHERE friend_image_id IS NULL;
UPDATE friendships
SET image_id = 0
WHERE image_id IS NULL;

ALTER TABLE friendships
    MODIFY friend_image_id BIGINT NOT NULL;
ALTER TABLE friendships
    MODIFY image_id BIGINT NOT NULL;
