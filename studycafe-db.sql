-- ================================================================
--  StudyCafe DB  (MySQL 8+)
--  - 여러 번 실행해도 오류가 나지 않도록 DROP ... CREATE 순으로 작성
--  - Java 코드(TicketType: TIME/PERIOD, Status: AVAILABLE/IN_USE/DISABLED,
--    PaymentStatus: SUCCESS/FAIL/REFUND)와 컬럼/ENUM 값을 일치시킴
-- ================================================================

CREATE DATABASE IF NOT EXISTS study_cafe_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE study_cafe_db;

-- 재실행 가능성을 위해 자식 → 부모 순으로 DROP
DROP TABLE IF EXISTS NOTIFICATION;
DROP TABLE IF EXISTS SEAT_USAGE;
DROP TABLE IF EXISTS PAYMENT;
DROP TABLE IF EXISTS MEMBER_TICKET;
DROP TABLE IF EXISTS SEAT;
DROP TABLE IF EXISTS TICKET;
DROP TABLE IF EXISTS MEMBER;

-- ──────────────────────────────────────────────────────────────
-- 1. MEMBER
-- ──────────────────────────────────────────────────────────────
CREATE TABLE MEMBER
(
    member_id  INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name       VARCHAR(50)     NOT NULL,
    phone      VARCHAR(20) UNIQUE,
    email      VARCHAR(100) UNIQUE,
    password   VARCHAR(255)    NOT NULL,
    created_at DATETIME DEFAULT NOW()
);

-- ──────────────────────────────────────────────────────────────
-- 2. TICKET
--   Java TicketType: TIME / PERIOD
-- ──────────────────────────────────────────────────────────────
CREATE TABLE TICKET
(
    ticket_id      INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name           VARCHAR(100)           NOT NULL,
    type           ENUM ('TIME','PERIOD') NOT NULL,
    duration_value INT                    NOT NULL COMMENT '시간권=분, 기간권=일',
    price          INT                    NOT NULL,
    description    TEXT
);

-- ──────────────────────────────────────────────────────────────
-- 3. MEMBER_TICKET
--   Java MemberTicketDTO.remainingMinutes 와 일치
-- ──────────────────────────────────────────────────────────────
CREATE TABLE MEMBER_TICKET
(
    member_ticket_id  INT NOT NULL AUTO_INCREMENT,
    member_id         INT NOT NULL,
    ticket_id         INT NOT NULL,
    started_at        DATETIME,
    expired_at        DATETIME,
    status            ENUM ('UNUSED','ACTIVE','EXPIRED') DEFAULT 'UNUSED',
    remaining_minutes INT                                DEFAULT 0 COMMENT '남은 시간(분)',
    PRIMARY KEY (member_ticket_id),
    FOREIGN KEY (member_id) REFERENCES MEMBER (member_id),
    FOREIGN KEY (ticket_id) REFERENCES TICKET (ticket_id)
);

-- ──────────────────────────────────────────────────────────────
-- 4. SEAT
--   Java Status: AVAILABLE / IN_USE / DISABLED
-- ──────────────────────────────────────────────────────────────
CREATE TABLE SEAT
(
    seat_id     INT                                     NOT NULL AUTO_INCREMENT,
    seat_number VARCHAR(20)                             NOT NULL UNIQUE,
    seat_type   ENUM ('STANDARD','PREMIUM')             NOT NULL COMMENT '일반석=시간권, 프리미엄석=기간권',
    status      ENUM ('AVAILABLE','IN_USE','DISABLED') DEFAULT 'AVAILABLE',
    zone        VARCHAR(50),
    PRIMARY KEY (seat_id)
);

-- ──────────────────────────────────────────────────────────────
-- 5. SEAT_USAGE
-- ──────────────────────────────────────────────────────────────
CREATE TABLE SEAT_USAGE
(
    usage_id         INT      NOT NULL AUTO_INCREMENT,
    member_id        INT      NOT NULL,
    seat_id          INT      NOT NULL,
    member_ticket_id INT      NOT NULL,
    started_at       DATETIME NOT NULL,
    ended_at         DATETIME,
    PRIMARY KEY (usage_id),
    FOREIGN KEY (member_id) REFERENCES MEMBER (member_id),
    FOREIGN KEY (seat_id) REFERENCES SEAT (seat_id),
    FOREIGN KEY (member_ticket_id) REFERENCES MEMBER_TICKET (member_ticket_id)
);

-- ──────────────────────────────────────────────────────────────
-- 6. PAYMENT
-- ──────────────────────────────────────────────────────────────
CREATE TABLE PAYMENT
(
    payment_id INT NOT NULL AUTO_INCREMENT,
    member_id  INT NOT NULL,
    ticket_id  INT NOT NULL,
    amount     INT NOT NULL,
    method     ENUM ('CARD','CASH','TRANSFER')   NOT NULL,
    status     ENUM ('SUCCESS','FAIL','REFUND') DEFAULT 'SUCCESS',
    paid_at    DATETIME                         DEFAULT NOW(),
    PRIMARY KEY (payment_id),
    FOREIGN KEY (member_id) REFERENCES MEMBER (member_id),
    FOREIGN KEY (ticket_id) REFERENCES TICKET (ticket_id)
);

-- ──────────────────────────────────────────────────────────────
-- 7. NOTIFICATION
-- ──────────────────────────────────────────────────────────────
CREATE TABLE NOTIFICATION
(
    notification_id INT  NOT NULL AUTO_INCREMENT,
    member_id       INT  NOT NULL,
    type            ENUM ('SEAT_START','SEAT_END','PAYMENT_DONE') NOT NULL,
    message         TEXT NOT NULL,
    related_id      INT COMMENT 'usage_id 또는 payment_id',
    is_read         BOOLEAN  DEFAULT FALSE,
    created_at      DATETIME DEFAULT NOW(),
    PRIMARY KEY (notification_id),
    FOREIGN KEY (member_id) REFERENCES MEMBER (member_id)
);

-- ================================================================
--  샘플 데이터
-- ================================================================

-- TICKET (ticket_id: 1~6)
INSERT INTO TICKET (name, type, duration_value, price, description)
VALUES ('30시간권',  'TIME',    1800,  50000, '총 30시간 이용 가능한 시간 정액권'),
       ('50시간권',  'TIME',    3000,  80000, '총 50시간 이용 가능한 시간 정액권'),
       ('100시간권', 'TIME',    6000, 150000, '총 100시간 이용 가능한 시간 정액권'),
       ('14일권',    'PERIOD',    14, 100000, '14일 동안 자유롭게 이용 가능한 기간권'),
       ('30일권',    'PERIOD',    30, 150000, '30일 동안 자유롭게 이용 가능한 기간권'),
       ('60일권',    'PERIOD',    60, 200000, '60일 동안 자유롭게 이용 가능한 기간권');

-- MEMBER (member_id: 1~5)
INSERT INTO MEMBER (name, phone, email, password)
VALUES ('아령',       '010-1111-1111', 'a@naver.com', 'aaa111'),
       ('덤벨',       '010-2222-2222', 'b@naver.com', 'bbb222'),
       ('벤치',       '010-3333-3333', 'c@naver.com', 'ccc333'),
       ('스쿼트',     '010-4444-4444', 'd@naver.com', 'ddd444'),
       ('데드리프트', '010-5555-5555', 'e@naver.com', 'eee555');

-- MEMBER_TICKET (member_ticket_id: 1~5)
-- 1~3번: 시간권(일반석 이용) / 4~5번: 기간권(프리미엄석 이용)
INSERT INTO MEMBER_TICKET (member_id, ticket_id, status, remaining_minutes)
VALUES (1, 1, 'ACTIVE', 1800), -- 아령: 30시간권
       (2, 4, 'ACTIVE',    0), -- 덤벨: 14일권
       (3, 2, 'ACTIVE', 3000), -- 벤치: 50시간권
       (4, 5, 'ACTIVE',    0), -- 스쿼트: 30일권
       (5, 6, 'ACTIVE',    0); -- 데드리프트: 60일권

-- SEAT (seat_id: 1~10)
-- SEAT_USAGE 의 미종료(ended_at IS NULL) 데이터와 일치하도록 상태 지정
INSERT INTO SEAT (seat_number, seat_type, status, zone)
VALUES ('A1', 'STANDARD', 'AVAILABLE', '집중존'),
       ('A2', 'STANDARD', 'IN_USE',    '집중존'),
       ('A3', 'STANDARD', 'DISABLED',  '집중존'),
       ('B1', 'STANDARD', 'AVAILABLE', '노트북존'),
       ('B2', 'STANDARD', 'AVAILABLE', '노트북존'),
       ('B3', 'STANDARD', 'AVAILABLE', '노트북존'),
       ('P1', 'PREMIUM',  'AVAILABLE', '프라이빗존'),
       ('P2', 'PREMIUM',  'IN_USE',    '프라이빗존'),
       ('P3', 'PREMIUM',  'IN_USE',    '프라이빗존'),
       ('P4', 'PREMIUM',  'AVAILABLE', '프라이빗존');

-- SEAT_USAGE
INSERT INTO SEAT_USAGE (member_id, seat_id, member_ticket_id, started_at, ended_at)
VALUES
    -- [과거 이용 및 퇴실 완료]
    (1, 1, 1, '2026-04-13 09:00:00', '2026-04-13 13:00:00'), -- 아령: A1
    (2, 7, 2, '2026-04-13 14:00:00', '2026-04-13 18:30:00'), -- 덤벨: P1
    (3, 4, 3, '2026-04-14 10:00:00', '2026-04-14 15:00:00'), -- 벤치: B1

    -- [현재 좌석 사용 중]
    (4, 8, 4, '2026-04-14 18:00:00', NULL), -- 스쿼트: P2
    (5, 9, 5, '2026-04-14 19:30:00', NULL), -- 데드리프트: P3
    (1, 2, 1, '2026-04-14 20:00:00', NULL); -- 아령(재방문): A2

-- PAYMENT
INSERT INTO PAYMENT (member_id, ticket_id, amount, method, status, paid_at)
VALUES (1, 1,  50000, 'CARD',     'SUCCESS', NOW()),
       (2, 4, 100000, 'CASH',     'SUCCESS', NOW()),
       (3, 2,  80000, 'TRANSFER', 'SUCCESS', NOW()),
       (4, 5, 150000, 'CARD',     'SUCCESS', NOW()),
       (5, 6, 200000, 'CARD',     'REFUND',  NOW());

-- NOTIFICATION
INSERT INTO NOTIFICATION (member_id, type, message, related_id, is_read)
VALUES (1, 'SEAT_START',   '아령 회원이 A1 좌석에 입실했습니다.',          1, FALSE),
       (2, 'SEAT_END',     '덤벨 회원이 P1 좌석에서 퇴실했습니다.',        2, FALSE),
       (3, 'PAYMENT_DONE', '벤치 회원의 50시간권 결제가 완료되었습니다.',   3, FALSE),
       (4, 'SEAT_START',   '스쿼트 회원이 P2 좌석에 입실했습니다.',         4, TRUE),
       (5, 'SEAT_START',   '데드리프트 회원이 P3 좌석에 입실했습니다.',     5, TRUE);
