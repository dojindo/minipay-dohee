insert into member(email, password, name, created_date_time)
values('test@test.com', 'test12345', 'tester1', now());

insert into member(email, password, name, created_date_time)
values('test2@test.com', 'test12345', 'tester2', now());

insert into checking_account(account_number, member_id, balance, created_date_time)
values('8888-60-8082444', 1, 12000, now());

insert into checking_account(account_number, member_id, balance, created_date_time)
values('8888-44-8427001', 2, 0, now());

insert into setting(remit_type, member_id, created_date_time)
values('IMMEDIATE' , 1, now());

insert into setting(remit_type, member_id, created_date_time)
values('IMMEDIATE' , 2, now());