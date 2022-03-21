drop database if exists Spring_DA;
create schema Spring_DA default character set 'utf8' collate 'utf8_bin';

use Spring_DA;

drop table if exists blog;
create table blog
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    content    varchar(45),
    authorId   int,
    authorName varchar(45)
) comment '博客表';
insert into blog (content, authorId, authorName)
values ('content11', 1, 'zhang'),
       ('content12', 1, 'zhang'),
       ('content2', 2, 'wang'),
       ('content3', 3, 'li');

drop table if exists author;
create table author
(
    id      INT PRIMARY KEY AUTO_INCREMENT,
    name    varchar(45)
) comment '作者表';
insert into author (name)
values ('zhang'),
       ('wang'),
       ('li');


# delete from Spring_DA.blog where id>=5;
# ALTER TABLE Spring_DA.blog auto_increment=5;


