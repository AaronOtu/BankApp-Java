
-- @block
CREATE TABLE Users(
  id INT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL UNIQUE,
  bio TEXT,
  country VARCHAR(2)
);

-- @block
INSERT INTO Users(email, bio, country)
VALUES(
    'kofi@gmail.com',
    'My name is Kofi',
    'UK'
    
),
('obaasima','I am obaasima','US'),
('awurabena','Married','US');


-- @block
select * from Users;
-- @block
select * from Users
where country = 'UK'
order by id desc;

-- @block
create table Rooms(
    id INT AUTO_INCREMENT,
    street VARCHAR(255) ,
    owner_id INT NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(owner_id) REFERENCES Users(id) 
);


-- @block
INSERT INTO Rooms(street, owner_id)
VALUES(
    'Oxford Street',
    1
),
('Broadway', 1),
('5th Avenue', 1);

--@block 
select * from Rooms;
