INSERT INTO member (created_date_time, deleted, name) VALUES (NOW(), false, 'sender');
INSERT INTO member (created_date_time, deleted, name) VALUES (NOW(), false, 'receiver');
INSERT INTO member (created_date_time, deleted, name) VALUES (NOW(), false, 'sender2');
INSERT INTO member (created_date_time, deleted, name) VALUES (NOW(), false, 'receiver2');

INSERT INTO member_money (created_date_time, deleted, money_amount, money_limit, member_id) VALUES (NOW(), false, '10000KRW', '100000000KRW', 1);
INSERT INTO member_money (created_date_time, deleted, money_amount, money_limit, member_id) VALUES (NOW(), false, '10000KRW', '100000000KRW', 2);
INSERT INTO member_money (created_date_time, deleted, money_amount, money_limit, member_id) VALUES (NOW(), false, '10000000KRW', '100000000KRW', 3);
INSERT INTO member_money (created_date_time, deleted, money_amount, money_limit, member_id) VALUES (NOW(), false, '10000KRW', '30000KRW', 4);
