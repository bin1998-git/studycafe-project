CREATE DATABASE study_cafe_db;

USE study_cafe_db;

-- 1. MEMBER 신규 회원 등록 및 정보 관리에 사용되는 기본 데이터
CREATE TABLE MEMBER (
    member_id   INT PRIMARY KEY NOT NULL AUTO_INCREMENT, -- 회원 식별용 고유 키(PK)
    name        VARCHAR(50)     NOT NULL,
    phone       VARCHAR(20)     UNIQUE, -- UNIQUE 제약이 걸려 있어 동일한 연락처나 이메일로 중복 가입하는 것을 차단
    email       VARCHAR(100)    UNIQUE, -- UNIQUE 제약이 걸려 있어 동일한 연락처나 이메일로 중복 가입하는 것을 차단
    password    VARCHAR(255)    NOT NULL,
    created_at  DATETIME        DEFAULT NOW()
);

-- 2. TICKET 스터디 카페에서 판매하는 '메뉴판' 역할을 하며, 상품의 기준 정보를 담음 
CREATE TABLE TICKET (
    ticket_id       INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL,-- 화면에 표시될 상품명
    type            ENUM('TIME','DAY') NOT NULL, -- 시간권인지 기간권인지 구분
    duration_value  INT             NOT NULL COMMENT '시간권=분, 기간권=일', -- 실제 회원에게 부여할 이용량을 계산
    price           INT             NOT NULL, -- 결제 시 청구할 금액
    description     TEXT -- 상세 설명 텍스트
);

-- 3. MEMBER_TICKET 회원이 결제 후 실제로 발급받아 소유하게 된 이용권 인스턴스 이며, 결제할 때마다 새 레코드가 생성됩니다
CREATE TABLE MEMBER_TICKET (
    member_ticket_id    INT         NOT NULL AUTO_INCREMENT,
    member_id           INT         NOT NULL,
    ticket_id           INT         NOT NULL,
    started_at          DATETIME,
    expired_at          DATETIME,
    status              ENUM('UNUSED','ACTIVE','EXPIRED') DEFAULT 'UNUSED',
    PRIMARY KEY (member_ticket_id),
    FOREIGN KEY (member_id)  REFERENCES MEMBER(member_id),
    FOREIGN KEY (ticket_id)  REFERENCES TICKET(ticket_id) -- 누가(FK), 어떤 상품(FK)을 구매했는지 연결
);

-- 4. SEAT
CREATE TABLE SEAT (
    seat_id     INT             NOT NULL AUTO_INCREMENT,
    seat_number VARCHAR(20)     NOT NULL UNIQUE,
    seat_type   ENUM('STANDARD','PREMIUM') NOT NULL COMMENT '일반석=시간권, 프리미엄석=기간권',
    status      ENUM('AVAILABLE','IN_USE','DISABLED') DEFAULT 'AVAILABLE',
    zone        VARCHAR(50),
    PRIMARY KEY (seat_id)
);

-- 5. SEAT_USAGE
CREATE TABLE SEAT_USAGE (
    usage_id            INT         NOT NULL AUTO_INCREMENT,
    member_id           INT         NOT NULL,
    seat_id             INT         NOT NULL,
    member_ticket_id    INT         NOT NULL,
    started_at          DATETIME    NOT NULL,
    ended_at            DATETIME,
    PRIMARY KEY (usage_id),
    FOREIGN KEY (member_id)          REFERENCES MEMBER(member_id),
    FOREIGN KEY (seat_id)            REFERENCES SEAT(seat_id),
    FOREIGN KEY (member_ticket_id)   REFERENCES MEMBER_TICKET(member_ticket_id)
);

-- 6. PAYMENT
CREATE TABLE PAYMENT (
    payment_id  INT         NOT NULL AUTO_INCREMENT,
    member_id   INT         NOT NULL,
    ticket_id   INT         NOT NULL,
    amount      INT         NOT NULL,
    method      ENUM('CARD','CASH','TRANSFER') NOT NULL,
    status      ENUM('SUCCESS','FAIL','REFUND') DEFAULT 'SUCCESS',
    paid_at     DATETIME    DEFAULT NOW(),
    PRIMARY KEY (payment_id),
    FOREIGN KEY (member_id)  REFERENCES MEMBER(member_id),
    FOREIGN KEY (ticket_id)  REFERENCES TICKET(ticket_id)
);

-- 7. NOTIFICATION
CREATE TABLE NOTIFICATION (
    notification_id INT         NOT NULL AUTO_INCREMENT,
    member_id       INT         NOT NULL,
    type            ENUM('SEAT_START','SEAT_END','PAYMENT_DONE') NOT NULL,
    message         TEXT        NOT NULL,
    related_id      INT         COMMENT 'usage_id 또는 payment_id',
    is_read         BOOLEAN     DEFAULT FALSE,
    created_at      DATETIME    DEFAULT NOW(),
    PRIMARY KEY (notification_id),
    FOREIGN KEY (member_id)  REFERENCES MEMBER(member_id)
);

-- 8. ticket 샘플 데이터
INSERT INTO TICKET (name, type, duration_value, price, description)
VALUES ('30시간권', 'TIME', 1800, 50000, '총 30시간 이용 가능한 시간 정액권'),
       ('50시간권', 'TIME', 3000, 80000, '총 50시간 이용 가능한 시간 정액권'),
       ('100시간권', 'TIME', 6000, 150000, '총 100시간 이용 가능한 시간 정액권'),

       ('14일권', 'DAY', 14, 100000, '14일 동안 자유롭게 이용 가능한 기간권'),
       ('30일권', 'DAY', 30, 150000, '30일 동안 자유롭게 이용 가능한 기간권'),
       ('60일권', 'DAY', 60, 200000, '60일 동안 자유롭게 이용 가능한 기간권');
   
-- MEMBER 샘플 데이터 
INSERT INTO MEMBER(name, phone, email, password) VALUES
("아령","010-1111-1111","a@naver.com","aaa111"),
("덤벨","010-2222-2222","b@naver.com","bbb222"),
("벤치","010-3333-3333","c@naver.com","ccc333"),
("스쿼트","010-4444-4444","d@naver.com","ddd444"),
("데드리프트","010-5555-5555","e@naver.com","eee555");

-- 8. ticket 샘플 데이터
INSERT INTO TICKET (name, type, duration_value, price, description)
VALUES ('30시간권', 'TIME', 1800, 50000, '총 30시간 이용 가능한 시간 정액권'),
       ('50시간권', 'TIME', 3000, 80000, '총 50시간 이용 가능한 시간 정액권'),
       ('100시간권', 'TIME', 6000, 150000, '총 100시간 이용 가능한 시간 정액권'),

       ('14일권', 'DAY', 14, 100000, '14일 동안 자유롭게 이용 가능한 기간권'),
       ('30일권', 'DAY', 30, 150000, '30일 동안 자유롭게 이용 가능한 기간권'),
       ('60일권', 'DAY', 60, 200000, '60일 동안 자유롭게 이용 가능한 기간권');
       
-- MEMBER_TICKET 샘플 데이터 
-- 1~3번은 시간권, 4~6번은 기간권 구매
INSERT INTO MEMBER_TICKET (member_id, ticket_id) VALUES
    (1, 1),  -- 아령: 30시간권 (시간권 -> 일반석 이용 가능)
    (2, 4),  -- 덤벨: 14일권 (기간권 -> 프리미엄석 이용 가능)
    (3, 2),  -- 벤치: 50시간권 (시간권 -> 일반석 이용 가능)
    (4, 5),  -- 스쿼트: 30일권 (기간권 -> 프리미엄석 이용 가능)
    (5, 6);  -- 데드리프트: 60일권 (기간권 -> 프리미엄석 이용 가능)
    
-- SEAT 샘플 데이터 
INSERT INTO SEAT (seat_number, seat_type, status, zone) VALUES
    ('A01', 'STANDARD', 'AVAILABLE', '집중존'),
    ('A02', 'STANDARD', 'IN_USE', '집중존'),
    ('A03', 'STANDARD', 'DISABLED', '집중존'),
    ('B01', 'STANDARD', 'AVAILABLE', '노트북존'),
    ('B02', 'STANDARD', 'IN_USE', '노트북존'),
    ('B03', 'STANDARD', 'AVAILABLE', '노트북존'),
    ('P01', 'PREMIUM', 'AVAILABLE', '프라이빗존'),
    ('P02', 'PREMIUM', 'IN_USE', '프라이빗존'),
    ('P03', 'PREMIUM', 'AVAILABLE', '프라이빗존'),
    ('P04', 'PREMIUM', 'IN_USE', '프라이빗존');
    
SELECT * FROM seat WHERE status = 'AVAILABLE';

INSERT INTO SEAT (seat_number, seat_type, status, zone) VALUES
    ('A01', 'STANDARD', 'AVAILABLE', '집중존');
    
-- UPDATE seat SET status = ? WHERE seat_id = ?;     

SELECT * FROM seat_usage; 

-- SEAT_USAGE 샘플 데이터 
INSERT INTO SEAT_USAGE (member_id, seat_id, member_ticket_id, started_at, ended_at) VALUES
    -- [과거 이용 및 퇴실 완료]
    -- 아령(1): 시간권(1)으로 일반석(1번) 이용
    (1, 1, 1, '2026-04-13 09:00:00', '2026-04-13 13:00:00'),
    
    -- 덤벨(2): 기간권(4)으로 프리미엄석(7번) 이용
    (2, 7, 2, '2026-04-13 14:00:00', '2026-04-13 18:30:00'),
    
    -- 벤치(3): 시간권(2)으로 일반석(4번) 이용
    (3, 4, 3, '2026-04-14 10:00:00', '2026-04-14 15:00:00'),
    
    -- [현재 좌석 사용 중 (ended_at = NULL)]
    -- 스쿼트(4): 기간권(5)으로 프리미엄석(8번) 이용 중
    (4, 8, 4, '2026-04-14 18:00:00', NULL),
    
    -- 데드리프트(5): 기간권(6)으로 프리미엄석(9번) 이용 중
    (5, 9, 5, '2026-04-14 19:30:00', NULL),
    
    -- [재방문] 아령(1)이 다른 시간에 일반석(2번)을 다시 이용 중
    (1, 2, 1, '2026-04-14 20:00:00', NULL);

-- payment 샘플 데이터
INSERT INTO PAYMENT (member_id, ticket_id, amount, method, status, paid_at)
VALUES (1, 10, 50000, 'CARD', 'SUCCESS', NOW()),
       (2, 11, 30000, 'CASH', 'SUCCESS', NOW()),
       (3, 12, 80000, 'TRANSFER', 'SUCCESS', NOW()),
       (4, 13, 100000, 'CARD', 'SUCCESS', NOW()),
       (5, 14, 150000, 'CARD', 'REFUND', '2026-04-10 14:30:00');