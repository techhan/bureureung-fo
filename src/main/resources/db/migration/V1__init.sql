CREATE TABLE fo_user (
                         id bigint NOT NULL AUTO_INCREMENT,
                         email varchar(100) NOT NULL,
                         password varchar(255) NOT NULL,
                         nickname varchar(20) NOT NULL,
                         phone varchar(20) NOT NULL,
                         profile_image_url varchar(500) DEFAULT NULL,
                         grade enum('BRONZE','GOLD','SILVER','VIP') NOT NULL,
                         status enum('ACTIVE','DELETED') NOT NULL,
                         created_at datetime(6) NOT NULL,
                         updated_at datetime(6) NOT NULL,
                         deleted_at datetime(6) DEFAULT NULL,
                         PRIMARY KEY (id),
                         UNIQUE KEY uk_fo_user_email (email),
                         UNIQUE KEY uk_fo_user_nickname (nickname)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE fo_user_terms (
                               id bigint NOT NULL AUTO_INCREMENT,
                               fo_user_id bigint NOT NULL,
                               terms_type enum('MARKETING','NIGHT_MARKETING','PRIVACY','TERMS') NOT NULL,
                               is_agreed bit(1) NOT NULL,
                               created_at datetime(6) NOT NULL,
                               updated_at datetime(6) NOT NULL,
                               PRIMARY KEY (id),
                               UNIQUE KEY uk_fo_user_terms (fo_user_id, terms_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE fo_user_terms_history (
                                       id bigint NOT NULL AUTO_INCREMENT,
                                       fo_user_id bigint NOT NULL,
                                       terms_type enum('MARKETING','NIGHT_MARKETING','PRIVACY','TERMS') NOT NULL,
                                       is_agreed bit(1) NOT NULL,
                                       created_at datetime(6) NOT NULL,
                                       PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE withdrawal_history (
                                    id bigint NOT NULL AUTO_INCREMENT,
                                    fo_user_id bigint NOT NULL,
                                    reason enum('APP_ERROR','DELIVERY_FEE','FEW_RESTAURANTS','OTHER','PRIVACY_CONCERN','RARELY_USE','RE_SIGNUP','SERVICE_DISSATISFIED') NOT NULL,
                                    detail varchar(500) DEFAULT NULL,
                                    withdrawn_at datetime(6) NOT NULL,
                                    created_at datetime(6) NOT NULL,
                                    updated_at datetime(6) NOT NULL,
                                    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;