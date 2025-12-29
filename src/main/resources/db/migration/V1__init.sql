-- PostgreSQL용 테이블 생성 스크립트

-- ENUM 타입 정의 (중복 문자열 리터럴 해결)
CREATE
TYPE content_status AS ENUM ('DELETED', 'PUBLISHED');
CREATE
TYPE follow_status AS ENUM ('ACTIVE', 'BLOCKED', 'UNFOLLOWED');
CREATE
TYPE friendship_status AS ENUM ('ACCEPTED', 'PENDING', 'REJECTED', 'UNFRIENDED');
CREATE
TYPE privacy_type AS ENUM ('PRIVATE', 'PUBLIC');
CREATE
TYPE image_type AS ENUM ('POST_IMAGE', 'PROFILE_IMAGE', 'THUMBNAIL');
CREATE
TYPE member_status AS ENUM ('ACTIVE', 'DEACTIVATED', 'PENDING');
CREATE
TYPE trust_level AS ENUM ('BRONZE', 'SILVER', 'GOLD', 'DIAMOND');
CREATE
TYPE role_type AS ENUM ('ADMIN', 'MANAGER', 'USER');

-- activation_tokens 테이블
CREATE TABLE activation_tokens
(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6),
    expires_at TIMESTAMP(6) NOT NULL,
    member_id  BIGINT       NOT NULL,
    token      VARCHAR(255) NOT NULL,
    CONSTRAINT UKa0emb8v14vdpreuo97gwil8tm UNIQUE (member_id)
);

-- comments 테이블
CREATE TABLE comments
(
    id BIGSERIAL PRIMARY KEY,
    author_member_id  BIGINT       NOT NULL,
    author_nickname   VARCHAR(20)  NOT NULL,
    content_text      TEXT         NOT NULL,
    created_at        TIMESTAMP(6) NOT NULL,
    parent_comment_id BIGINT,
    post_id           BIGINT       NOT NULL,
    reply_count       BIGINT       NOT NULL,
    status content_status NOT NULL,
    updated_at        TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_comment_author_id ON comments (author_member_id);
CREATE INDEX idx_comment_created_at ON comments (created_at);
CREATE INDEX idx_comment_parent_id ON comments (parent_comment_id);
CREATE INDEX idx_comment_post_id ON comments (post_id);
CREATE INDEX idx_comment_status ON comments (status);

-- follows 테이블
CREATE TABLE follows
(
    id BIGSERIAL PRIMARY KEY,
    created_at                 TIMESTAMP(6) NOT NULL,
    follower_id                BIGINT       NOT NULL,
    follower_nickname          VARCHAR(50)  NOT NULL,
    following_id               BIGINT       NOT NULL,
    following_nickname         VARCHAR(50)  NOT NULL,
    status follow_status NOT NULL,
    updated_at                 TIMESTAMP(6),
    follower_profile_image_id  BIGINT       NOT NULL,
    following_profile_image_id BIGINT       NOT NULL,
    CONSTRAINT UK4faelgsm2rxl2jf3iyjy981ro UNIQUE (follower_id, following_id)
);

CREATE INDEX idx_follow_created_at ON follows (created_at);
CREATE INDEX idx_follow_follower_id ON follows (follower_id);
CREATE INDEX idx_follow_following_id ON follows (following_id);
CREATE INDEX idx_follow_status ON follows (status);

-- friendships 테이블
CREATE TABLE friendships
(
    id BIGSERIAL PRIMARY KEY,
    created_at       TIMESTAMP(6) NOT NULL,
    friend_member_id BIGINT       NOT NULL,
    friend_nickname  VARCHAR(50),
    member_id        BIGINT       NOT NULL,
    member_nickname  VARCHAR(50),
    status friendship_status NOT NULL,
    updated_at       TIMESTAMP(6),
    friend_image_id  BIGINT       NOT NULL,
    image_id         BIGINT       NOT NULL,
    CONSTRAINT UKphu6nmq16if8s5ot2g4j1frrb UNIQUE (member_id, friend_member_id)
);

CREATE INDEX idx_friendship_created_at ON friendships (created_at);
CREATE INDEX idx_friendship_friend_member_id ON friendships (friend_member_id);
CREATE INDEX idx_friendship_friend_nickname ON friendships (friend_nickname);
CREATE INDEX idx_friendship_member_id ON friendships (member_id);
CREATE INDEX idx_friendship_status ON friendships (status);

-- images 테이블
CREATE TABLE images
(
    id BIGSERIAL PRIMARY KEY,
    file_key    VARCHAR(255) NOT NULL,
    image_type image_type NOT NULL,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    uploaded_by BIGINT       NOT NULL,
    CONSTRAINT UKj8m5brmvrpg2i7whte0spvwkx UNIQUE (file_key)
);

CREATE INDEX idx_image_type ON images (image_type);
CREATE INDEX idx_uploaded_by ON images (uploaded_by);

-- member_detail 테이블
CREATE TABLE member_detail
(
    id BIGSERIAL PRIMARY KEY,
    activated_at    TIMESTAMP(6),
    address         VARCHAR(255),
    deactivated_at  TIMESTAMP(6),
    introduction    VARCHAR(500),
    profile_address VARCHAR(15),
    registered_at   TIMESTAMP(6),
    image_id        BIGINT,
    CONSTRAINT UKgw655ofqkjnixsrcqid0qvbqx UNIQUE (profile_address)
);

-- password_reset_tokens 테이블
CREATE TABLE password_reset_tokens
(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    member_id  BIGINT       NOT NULL,
    token      VARCHAR(255) NOT NULL,
    CONSTRAINT UK71lqwbwtklmljk3qlsugr1mig UNIQUE (token)
);

CREATE INDEX idx_password_reset_member_id ON password_reset_tokens (member_id);
CREATE INDEX idx_password_reset_token ON password_reset_tokens (token);

-- post_collections 테이블
CREATE TABLE post_collections
(
    id BIGSERIAL PRIMARY KEY,
    created_at      TIMESTAMP(6) NOT NULL,
    cover_image_url VARCHAR(500),
    description     TEXT,
    name            VARCHAR(100) NOT NULL,
    owner_member_id BIGINT       NOT NULL,
    owner_nickname  VARCHAR(255) NOT NULL,
    privacy privacy_type NOT NULL,
    status content_status NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL
);

-- collection_posts 연결 테이블
CREATE TABLE collection_posts
(
    collection_id BIGINT  NOT NULL,
    post_id       BIGINT  NOT NULL,
    display_order INTEGER NOT NULL,
    PRIMARY KEY (collection_id, display_order),
    CONSTRAINT FKr71d636l9ctei4h0nnkkkag3e FOREIGN KEY (collection_id) REFERENCES post_collections (id)
);

CREATE INDEX idx_collection_created_at ON post_collections (created_at);
CREATE INDEX idx_collection_owner_member_id ON post_collections (owner_member_id);
CREATE INDEX idx_collection_privacy_status ON post_collections (privacy, status);
CREATE INDEX idx_collection_status ON post_collections (status);

-- post_hearts 테이블
CREATE TABLE post_hearts
(
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP(6) NOT NULL,
    member_id  BIGINT       NOT NULL,
    post_id    BIGINT       NOT NULL,
    CONSTRAINT uk_post_heart_post_member UNIQUE (post_id, member_id)
);

CREATE INDEX idx_post_heart_member_id ON post_hearts (member_id);
CREATE INDEX idx_post_heart_post_id ON post_hearts (post_id);

-- posts 테이블
CREATE TABLE posts
(
    id BIGSERIAL PRIMARY KEY,
    author_introduction  VARCHAR(500)     NOT NULL,
    author_member_id     BIGINT           NOT NULL,
    author_nickname      VARCHAR(20)      NOT NULL,
    comment_count        INTEGER          NOT NULL,
    rating               INTEGER          NOT NULL,
    content_text         TEXT             NOT NULL,
    created_at           TIMESTAMP(6)     NOT NULL,
    heart_count          INTEGER          NOT NULL,
    restaurant_address   VARCHAR(255)     NOT NULL,
    restaurant_latitude  DOUBLE PRECISION NOT NULL,
    restaurant_longitude DOUBLE PRECISION NOT NULL,
    restaurant_name      VARCHAR(100)     NOT NULL,
    status content_status NOT NULL,
    updated_at           TIMESTAMP(6)     NOT NULL,
    view_count           INTEGER          NOT NULL,
    author_image_id      BIGINT           NOT NULL
);

-- post_images 연결 테이블
CREATE TABLE post_images
(
    post_id     BIGINT  NOT NULL,
    image_id    BIGINT  NOT NULL,
    image_order INTEGER NOT NULL,
    PRIMARY KEY (post_id, image_order),
    CONSTRAINT FKo1i5va2d8de9mwq727vxh0s05 FOREIGN KEY (post_id) REFERENCES posts (id)
);

CREATE INDEX idx_post_author_id ON posts (author_member_id);
CREATE INDEX idx_post_created_at ON posts (created_at);
CREATE INDEX idx_post_restaurant_name ON posts (restaurant_name);
CREATE INDEX idx_post_status ON posts (status);

-- trust_score 테이블
CREATE TABLE trust_score
(
    id BIGSERIAL PRIMARY KEY,
    ad_review_count         INTEGER,
    trust_level trust_level,
    real_money_review_count INTEGER,
    trust_score             INTEGER
);

-- members 테이블
CREATE TABLE members
(
    id BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    follower_count  BIGINT       NOT NULL,
    following_count BIGINT       NOT NULL,
    nickname        VARCHAR(20)  NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    status member_status NOT NULL,
    updated_at      TIMESTAMP(6),
    detail_id       BIGINT       NOT NULL,
    trust_score_id  BIGINT,
    post_count      BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT UK1mess2qywlgcnemr4r1ldm14c UNIQUE (email),
    CONSTRAINT UK7q2ymaaa07yakjm7xgchec3v9 UNIQUE (detail_id),
    CONSTRAINT UKmvf9gg1s6tceoxlifwa6aewkn UNIQUE (trust_score_id),
    CONSTRAINT FKawl3m8yvo16wgowaam7fi3x1p FOREIGN KEY (detail_id) REFERENCES member_detail (id),
    CONSTRAINT FKh5w2qukccwyysqrxaxe0hy93t FOREIGN KEY (trust_score_id) REFERENCES trust_score (id)
);

CREATE INDEX idx_member_email ON members (email);
CREATE INDEX idx_member_nickname ON members (nickname);
CREATE INDEX idx_member_status ON members (status);

-- member_roles 테이블
CREATE TABLE member_roles
(
    member_id BIGINT NOT NULL,
    role role_type,
    CONSTRAINT FK431yrnsn5s4omvwjvl9dre1n0 FOREIGN KEY (member_id) REFERENCES members (id)
);

-- member_event 테이블
CREATE TABLE member_event
(
    id BIGSERIAL PRIMARY KEY,
    member_id          BIGINT       NOT NULL,
    event_type         VARCHAR(30)  NOT NULL,
    title              VARCHAR(100) NOT NULL,
    message            VARCHAR(500) NOT NULL,
    related_member_id  BIGINT,
    related_post_id    BIGINT,
    related_comment_id BIGINT,
    is_read            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP    NOT NULL
);

CREATE INDEX idx_member_event_member_id ON member_event (member_id);
CREATE INDEX idx_member_event_created_at ON member_event (created_at);
CREATE INDEX idx_member_event_event_type ON member_event (event_type);
CREATE INDEX idx_member_event_is_read ON member_event (is_read);
CREATE INDEX idx_member_event_member_unread ON member_event (member_id, is_read, created_at);
