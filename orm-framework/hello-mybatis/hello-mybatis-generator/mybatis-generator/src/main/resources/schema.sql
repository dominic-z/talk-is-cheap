use java_web;
drop table if exists student;
create table student (
    id int primary key auto_increment,
    name varchar(32) not null,
    sex char(1),
    age tinyint,
    photo varchar(32)
);

drop table if exists user_role;
drop table if exists role_menu;
drop table if exists role_func;
drop table if exists menu;
drop table if exists func;
drop table if exists role;
drop table if exists user;
drop table if exists user_info;

create table user (
    id int primary key auto_increment,
    username varchar(16) not null,
    password varchar(64) not null
);

create table user_info (
    username varchar(16) not null,
    name varchar(16),
    sex char(1)
);

create table role (
    id int primary key auto_increment,
    name varchar(16) not null
);

create table menu (
    id int primary key auto_increment,
    label varchar(16) not null,
    pid int not null,
    icon varchar(32),
    route_path varchar(64),
    route_element varchar(64)
);

create table func (
    id int primary key auto_increment,
    name varchar(16) not null
);

create table user_role (
    user_id int,
    role_id int,
    foreign key (user_id) references user(id),
    foreign key (role_id) references role(id)
);

create table role_menu (
    role_id int,
    menu_id int,
    foreign key (role_id) references role(id),
    foreign key (menu_id) references menu(id)
);

create table role_func (
    role_id int,
    func_id int,
    foreign key (role_id) references role(id),
    foreign key (func_id) references func(id)
);

