drop database if exists how2mybatis;
create schema how2mybatis default character set utf8 collate utf8_general_ci;

use how2mybatis;

drop table if exists blog;
create table blog
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    content    varchar(45),
    author_id   int,
    author_name varchar(45)
) comment '博客表';
insert into blog (content, author_id, author_name)
values ('zhang_content11', 1, 'zhang'),
       ('zhang_content12', 1, 'zhang'),
       ('wang_content2', 2, 'wang'),
       ('li_content3', 3, 'li');

drop table if exists author;
create table author
(
    id      INT PRIMARY KEY AUTO_INCREMENT,
    name    varchar(45),
    age     int,
    height  double,
    sex_value int comment '0为男，1为女',
    sex     enum ('MALE','FEMALE')
) comment '作者表';
insert into author (name, age, sex_value, sex)
values ('zhang', 18.5, 0, 'MALE'),
       ('wang', 21.2, 1, 'FEMALE'),
       ('li', 21.7, 1, 'FEMALE');


# delete from how2mybatis.blog where id>=5;
# ALTER TABLE how2mybatis.blog auto_increment=5;


