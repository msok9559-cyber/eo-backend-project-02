-- 1. 유저 테이블
CREATE TABLE `users` (
                         `id`          BIGINT       NOT NULL AUTO_INCREMENT,
                         `username`    VARCHAR(50)  NOT NULL UNIQUE,
                         `password`    VARCHAR(255) NOT NULL,
                         `nickname`    VARCHAR(50)  NOT NULL,
                         `email`       VARCHAR(100) NOT NULL UNIQUE,
                         `role`        VARCHAR(20)  NOT NULL,
                         `enabled`     SMALLINT     NOT NULL DEFAULT 1,
                         `created_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`)
);

-- 2. 게시판 종류
CREATE TABLE `boards` (
                          `id`    BIGINT       NOT NULL AUTO_INCREMENT,
                          `title` VARCHAR(100) NOT NULL,
                          PRIMARY KEY (`id`)
);

-- 3. 게시글 테이블
CREATE TABLE `posts` (
                         `id`             BIGINT       NOT NULL AUTO_INCREMENT,
                         `user_id`        BIGINT       NOT NULL,
                         `board_id`       BIGINT       NOT NULL,
                         `post_title`     VARCHAR(150) NOT NULL,
                         `content`        TEXT         NOT NULL,
                         `view_count`     INT          NOT NULL DEFAULT 0,
                         `comments_count` INT          NOT NULL DEFAULT 0,
                         `post_type`      SMALLINT     DEFAULT 0,
                         `fixed`          SMALLINT     DEFAULT 0,
                         `created_at`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
                         FOREIGN KEY (`board_id`) REFERENCES `boards`(`id`)
);

-- 4. 댓글 테이블
CREATE TABLE `comments` (
                            `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                            `user_id`    BIGINT       NOT NULL,
                            `post_id`    BIGINT       NOT NULL,
                            `r_content`  VARCHAR(200) NOT NULL,
                            `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            FOREIGN KEY (`user_id`) REFERENCES `users`(`id`),
                            FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`)
);

-- 5. 파일 테이블
CREATE TABLE `files` (
                         `id`          BIGINT       NOT NULL AUTO_INCREMENT,
                         `post_id`     BIGINT       NOT NULL, -- posts_id에서 post_id로 통일 추천
                         `stored_name` VARCHAR(255) NOT NULL,
                         `extension`   VARCHAR(10)  NOT NULL,
                         `file_size`   BIGINT       NOT NULL,
                         `created_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         PRIMARY KEY (`id`),
                         FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`)
);

-- 6. 해시태그
CREATE TABLE `hashtags` (
                            `id`       BIGINT      NOT NULL AUTO_INCREMENT,
                            `tag_name` VARCHAR(30) NOT NULL UNIQUE, -- 태그 중복 방지
                            PRIMARY KEY (`id`)
);

-- 7. 게시글-해시태그 매핑 (N:M 관계)
CREATE TABLE `post_hashtag` (
                                `tag_id`  BIGINT NOT NULL,
                                `post_id` BIGINT NOT NULL,
                                PRIMARY KEY (`tag_id`, `post_id`),
                                FOREIGN KEY (`tag_id`) REFERENCES `hashtags`(`id`),
                                FOREIGN KEY (`post_id`) REFERENCES `posts`(`id`)
);

-- 8. 쪽지함
CREATE TABLE `messages` (
                            `id`                    BIGINT    NOT NULL AUTO_INCREMENT,
                            `sender_id`             BIGINT    NOT NULL,
                            `receiver_id`           BIGINT    NOT NULL,
                            `m_title`               VARCHAR(50) NOT NULL,
                            `content`               TEXT      NOT NULL,
                            `is_read`               SMALLINT  NOT NULL DEFAULT 0,
                            `created_at`            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `read_at`               TIMESTAMP NULL, -- readed_at 대신 read_at 추천
                            `sender_delete_state`   SMALLINT  NOT NULL DEFAULT 0,
                            `receiver_delete_state` SMALLINT  NOT NULL DEFAULT 0,
                            PRIMARY KEY (`id`),
                            FOREIGN KEY (`sender_id`) REFERENCES `users`(`id`),
                            FOREIGN KEY (`receiver_id`) REFERENCES `users`(`id`)
);