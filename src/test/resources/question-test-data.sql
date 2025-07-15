INSERT INTO member (id, email, nickname, profile_image, member_type, created_at, updated_at)
VALUES (1, 'test@example.com', '테스터1', 'profile.jpg', 'MEMBER', NOW(), NOW());
INSERT INTO member (id, email, nickname, profile_image, member_type, created_at, updated_at)
VALUES (2, 'test@example.com', '테스터2', 'profile.jpg', 'MEMBER', NOW(), NOW());

INSERT INTO room (
    id, title, description, secret_code, room_status, room_type,
    emoji_count, participant_limit, ended_at, version, created_at, updated_at
)
VALUES (
           1, '테스트 방', '설명', 'ABC123', 'STARTED', 'PRIVATE',
           0, 100, NULL, 0, NOW(), NOW()
       );

INSERT INTO question (
    id, room_id, member_id, content, emoji_count, version, created_at, updated_at
)
VALUES
    (1, 1, 1, '질문 내용 1', 10, 0, '2024-01-01 10:00:00', '2024-01-01 10:00:00'),
    (2, 1, 1, '질문 내용 2', 9, 0, '2024-01-01 10:01:00', '2024-01-01 10:01:00'),
    (3, 1, 2, '질문 내용 3', 8, 0, '2024-01-01 10:02:00', '2024-01-01 10:02:00'),
    (4, 1, 1, '질문 내용 4', 7, 0, '2024-01-01 10:03:00', '2024-01-01 10:03:00'),
    (5, 1, 2, '질문 내용 5', 6, 0, '2024-01-01 10:04:00', '2024-01-01 10:04:00'),
    (6, 1, 1, '질문 내용 6', 5, 0, '2024-01-01 10:05:00', '2024-01-01 10:05:00'),
    (7, 1, 2, '질문 내용 7', 4, 0, '2024-01-01 10:06:00', '2024-01-01 10:06:00'),
    (8, 1, 1, '질문 내용 8', 3, 0, '2024-01-01 10:07:00', '2024-01-01 10:07:00'),
    (9, 1, 2, '질문 내용 9', 2, 0, '2024-01-01 10:08:00', '2024-01-01 10:08:00'),
    (10, 1, 1, '질문 내용 10', 1, 0, '2024-01-01 10:09:00', '2024-01-01 10:09:00');

