create table activation_tokens
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  null,
    expires_at datetime(6)  not null,
    member_id  bigint       not null,
    token      varchar(255) not null,
    constraint UKa0emb8v14vdpreuo97gwil8tm
        unique (member_id)
);

create table comments
(
    id                bigint auto_increment
        primary key,
    author_member_id  bigint                        not null,
    author_nickname   varchar(20)                   not null,
    content_text      text                          not null,
    created_at        datetime(6)                   not null,
    parent_comment_id bigint                        null,
    post_id           bigint                        not null,
    reply_count       bigint                        not null,
    status            enum ('DELETED', 'PUBLISHED') not null,
    updated_at        datetime(6)                   not null
);

create index idx_comment_author_id
    on comments (author_member_id);

create index idx_comment_created_at
    on comments (created_at);

create index idx_comment_parent_id
    on comments (parent_comment_id);

create index idx_comment_post_id
    on comments (post_id);

create index idx_comment_status
    on comments (status);

create table follows
(
    id                 bigint auto_increment
        primary key,
    created_at         datetime(6)                              not null,
    follower_id        bigint                                   not null,
    follower_nickname  varchar(50)                              not null,
    following_id       bigint                                   not null,
    following_nickname varchar(50)                              not null,
    status             enum ('ACTIVE', 'BLOCKED', 'UNFOLLOWED') not null,
    updated_at         datetime(6)                              not null,
    constraint UK4faelgsm2rxl2jf3iyjy981ro
        unique (follower_id, following_id)
);

create index idx_follow_created_at
    on follows (created_at);

create index idx_follow_follower_id
    on follows (follower_id);

create index idx_follow_following_id
    on follows (following_id);

create index idx_follow_status
    on follows (status);

create table friendships
(
    id               bigint auto_increment
        primary key,
    created_at       datetime(6)                                            not null,
    friend_member_id bigint                                                 not null,
    friend_nickname  varchar(50)                                            null,
    member_id        bigint                                                 not null,
    status           enum ('ACCEPTED', 'PENDING', 'REJECTED', 'UNFRIENDED') not null,
    updated_at       datetime(6)                                            not null,
    constraint UKphu6nmq16if8s5ot2g4j1frrb
        unique (member_id, friend_member_id)
);

create index idx_friendship_created_at
    on friendships (created_at);

create index idx_friendship_friend_member_id
    on friendships (friend_member_id);

create index idx_friendship_friend_nickname
    on friendships (friend_nickname);

create index idx_friendship_member_id
    on friendships (member_id);

create index idx_friendship_status
    on friendships (status);

create table images
(
    id          bigint auto_increment
        primary key,
    file_key    varchar(255)                                      not null,
    image_type  enum ('POST_IMAGE', 'PROFILE_IMAGE', 'THUMBNAIL') not null,
    is_deleted  bit                                               not null,
    uploaded_by bigint                                            not null,
    constraint UKj8m5brmvrpg2i7whte0spvwkx
        unique (file_key)
);

create index idx_image_type
    on images (image_type);

create index idx_uploaded_by
    on images (uploaded_by);

create table member_detail
(
    id              bigint auto_increment
        primary key,
    activated_at    datetime(6)  null,
    address         varchar(255) null,
    deactivated_at  datetime(6)  null,
    introduction    varchar(500) null,
    profile_address varchar(15)  null,
    registered_at   datetime(6)  null,
    constraint UKgw655ofqkjnixsrcqid0qvbqx
        unique (profile_address)
);

create table password_reset_tokens
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  not null,
    expires_at datetime(6)  not null,
    member_id  bigint       not null,
    token      varchar(255) not null,
    constraint UK71lqwbwtklmljk3qlsugr1mig
        unique (token)
);

create index idx_password_reset_member_id
    on password_reset_tokens (member_id);

create index idx_password_reset_token
    on password_reset_tokens (token);

create table post_collections
(
    id              bigint auto_increment
        primary key,
    created_at      datetime(6)                not null,
    cover_image_url varchar(500)               null,
    description     text                       null,
    name            varchar(100)               not null,
    owner_member_id bigint                     not null,
    owner_nickname  varchar(255)               not null,
    privacy         enum ('PRIVATE', 'PUBLIC') not null,
    status          enum ('ACTIVE', 'DELETED') not null,
    updated_at      datetime(6)                not null
);

create table collection_posts
(
    collection_id bigint not null,
    post_id       bigint not null,
    display_order int    not null,
    primary key (collection_id, display_order),
    constraint FKr71d636l9ctei4h0nnkkkag3e
        foreign key (collection_id) references post_collections (id)
);

create index idx_collection_created_at
    on post_collections (created_at);

create index idx_collection_owner_member_id
    on post_collections (owner_member_id);

create index idx_collection_privacy_status
    on post_collections (privacy, status);

create index idx_collection_status
    on post_collections (status);

create table post_hearts
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6) not null,
    member_id  bigint      not null,
    post_id    bigint      not null,
    constraint uk_post_heart_post_member
        unique (post_id, member_id)
);

create index idx_post_heart_member_id
    on post_hearts (member_id);

create index idx_post_heart_post_id
    on post_hearts (post_id);

create table posts
(
    id                   bigint auto_increment
        primary key,
    author_introduction  varchar(500)                  not null,
    author_member_id     bigint                        not null,
    author_nickname      varchar(20)                   not null,
    comment_count        int                           not null,
    rating               int                           not null,
    content_text         text                          not null,
    created_at           datetime(6)                   not null,
    heart_count          int                           not null,
    restaurant_address   varchar(255)                  not null,
    restaurant_latitude  double                        not null,
    restaurant_longitude double                        not null,
    restaurant_name      varchar(100)                  not null,
    status               enum ('DELETED', 'PUBLISHED') not null,
    updated_at           datetime(6)                   not null,
    view_count           int                           not null
);

create table post_images
(
    post_id     bigint not null,
    image_id    bigint not null,
    image_order int    not null,
    primary key (post_id, image_order),
    constraint FKo1i5va2d8de9mwq727vxh0s05
        foreign key (post_id) references posts (id)
);

create index idx_post_author_id
    on posts (author_member_id);

create index idx_post_created_at
    on posts (created_at);

create index idx_post_restaurant_name
    on posts (restaurant_name);

create index idx_post_status
    on posts (status);

create table trust_score
(
    id                      bigint auto_increment
        primary key,
    ad_review_count         int                                          null,
    trust_level             enum ('BRONZE', 'DIAMOND', 'GOLD', 'SILVER') null,
    real_money_review_count int                                          null,
    trust_score             int                                          null
);

create table members
(
    id              bigint auto_increment
        primary key,
    email           varchar(255)                              not null,
    follower_count  bigint                                    not null,
    following_count bigint                                    not null,
    nickname        varchar(20)                               not null,
    password_hash   varchar(255)                              not null,
    status          enum ('ACTIVE', 'DEACTIVATED', 'PENDING') not null,
    updated_at      datetime(6)                               null,
    detail_id       bigint                                    not null,
    trust_score_id  bigint                                    null,
    constraint UK1mess2qywlgcnemr4r1ldm14c
        unique (email),
    constraint UK7q2ymaaa07yakjm7xgchec3v9
        unique (detail_id),
    constraint UK9d30a9u1qpg8eou0otgkwrp5d
        unique (email),
    constraint UKmvf9gg1s6tceoxlifwa6aewkn
        unique (trust_score_id),
    constraint FKawl3m8yvo16wgowaam7fi3x1p
        foreign key (detail_id) references member_detail (id),
    constraint FKh5w2qukccwyysqrxaxe0hy93t
        foreign key (trust_score_id) references trust_score (id)
);

create table member_roles
(
    member_id bigint                            not null,
    role      enum ('ADMIN', 'MANAGER', 'USER') null,
    constraint FK431yrnsn5s4omvwjvl9dre1n0
        foreign key (member_id) references members (id)
);

create index idx_member_email
    on members (email);

create index idx_member_nickname
    on members (nickname);

create index idx_member_status
    on members (status);
