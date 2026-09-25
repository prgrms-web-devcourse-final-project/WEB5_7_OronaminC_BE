-- 스모크 테스트용 최소 데이터 (ID 고정)
-- IDENTITY 컬럼과 앱이 새로 만드는 행이 겹치지 않도록 1000번대 ID를 사용한다.
--
-- member  1001 발표자(MEMBER) / 1002 팀원(MEMBER) / 1003 질문 작성자(GUEST) / 1004 구독자(GUEST)
-- room    1001 (STARTED)
-- question 1001 (작성자 1003)
-- answer  1001 (작성자 1002, 팀원)

INSERT INTO member (id, email, nickname, profile_image, member_type, created_at, updated_at) VALUES
    (1001, 'presenter@loadtest.local', 'presenter', NULL, 'MEMBER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1002, 'team@loadtest.local', 'team', NULL, 'MEMBER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1003, NULL, 'guestq', NULL, 'GUEST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1004, NULL, 'guests', NULL, 'GUEST', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO room (id, title, description, secret_code, room_status, room_type, emoji_count,
                  participant_limit, ended_at, version, created_at, updated_at) VALUES
    (1001, 'loadtest room', 'k6 smoke', 'LT1001', 'STARTED', 'PUBLIC', 0,
     1000, TIMESTAMPADD(DAY, 1, CURRENT_TIMESTAMP), 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO participant (id, room_id, member_id, participant_type, exited_at, created_at, updated_at) VALUES
    (1001, 1001, 1001, 'PRESENTER', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1002, 1001, 1002, 'TEAM', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1003, 1001, 1003, 'GUEST', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (1004, 1001, 1004, 'GUEST', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO question (id, room_id, member_id, content, emoji_count, version, created_at, updated_at) VALUES
    (1001, 1001, 1003, 'loadtest question', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer (id, question_id, member_id, content, emoji_count, version, created_at, updated_at) VALUES
    (1001, 1001, 1002, 'seed answer', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
