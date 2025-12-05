-- This code is sql code for the bankapp

-- @block
use bankapp;

-- @block
create table users (
    id int auto_increment primary key,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    email varchar(100) not null unique,
    created_at timestamp default current_timestamp
);

-- @block
create table accounts (
    id int auto_increment primary key,
    user_id int not null,
    account_number varchar(20) not null unique,
    account_type varchar(20) not null check (account_type in ('savings', 'current')),
    balance decimal(10, 2) default 0.00,
    created_at timestamp default current_timestamp,
    foreign key (user_id) references users(id)
);

-- @block
create table transactions (
    id int auto_increment primary key,
    account_id int not null,
    transaction_type varchar(20) not null check (transaction_type in ('deposit', 'withdrawal', 'transfer')),
    amount decimal(10, 2) not null,
    currency varchar(10) not null,
    reference varchar(50) not null unique,
    description text,
    status varchar(20) not null check (status in ('pending', 'success', 'failed')),
    transaction_date timestamp default current_timestamp,
    foreign key (account_id) references accounts(id)
);

-- @block
select * from users;


--@block
ALTER TABLE transactions
ADD COLUMN to_account_id INT NULL AFTER account_id,
ADD COLUMN converted_amount DECIMAL(10, 2) NULL AFTER amount,
ADD COLUMN to_currency VARCHAR(10) NULL AFTER currency,
ADD COLUMN exchange_rate DECIMAL(10, 6) NULL AFTER to_currency,

--@block
ALTER TABLE transactions
MODIFY COLUMN transaction_type VARCHAR(20) NOT NULL;



--@block
ALTER TABLE transactions
ADD CONSTRAINT chk_transaction_type 
CHECK (transaction_type IN ('deposit', 'withdrawal', 'transfer_in', 'transfer_out')),
ADD CONSTRAINT fk_transactions_to_account 
FOREIGN KEY (to_account_id) REFERENCES accounts(id);

--@block
SELECT CONSTRAINT_NAME, CHECK_CLAUSE 
FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS 
WHERE CONSTRAINT_NAME = 'transactions_chk_1';

--@block
ALTER TABLE transactions 
DROP CONSTRAINT transactions_chk_1;

--@block
ALTER TABLE transactions
ADD CONSTRAINT transactions_chk_1 
CHECK (transaction_type IN ('deposit', 'withdrawal', 'transfer_in', 'transfer_out'));

--@block
ALTER TABLE transactions DROP INDEX reference;  

--@block
SELECT * FROM Users

--@block
ALTER TABLE Users 
ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT 'password';


--@block
ALTER TABLE users ADD COLUMN refresh_token VARCHAR(255);


--@block
ALTER TABLE users MODIFY COLUMN refresh_token VARCHAR(1024);