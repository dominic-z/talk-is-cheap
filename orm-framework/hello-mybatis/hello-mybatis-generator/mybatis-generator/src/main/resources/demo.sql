insert into student(name,sex,age,photo) values
    ('宋远桥', '男', 40, '/imgs/1.png'),
    ('俞莲舟', '男', 38, '/imgs/2.png'),
    ('俞岱岩', '男', 32, '/imgs/3.png'),
    ('张松溪', '男', 27, '/imgs/4.png'),
    ('张翠山', '男', 24, '/imgs/5.png'),
    ('殷梨亭', '男', 19, '/imgs/6.png'),
    ('莫声谷', '男', 17, '/imgs/7.png');

insert into user(id,username,password) values
    (1, 'admin', '123'),
    (2, 'zhang', '123'),
    (3, 'li', '123');

insert into user_info(username, name, sex) values
    ('admin', '管理员', '男'),
    ('zhang', '张三', '男'),
    ('li', '李四', '男');

insert into role(id, name) values
    (10, '管理员'),
    (11, '运维'),
    (12, '顾客');


insert into menu(id, label, pid, icon, route_path, route_element) values
    (101, '菜单1',  0,   'EditOutlined',          '/student',    'A8MainStudent'),
    (102, '菜单2',  0,   'AreaChartOutlined',    '/teacher',    'A8MainTeacher'),
    (103, '菜单3',  0,   'AccountBookOutlined',  null,     null) ,
    (104, '菜单31', 103, 'AlertOutlined',         '/user1',   'A8MainUser'),
    (105, '菜单32', 103, 'AppstoreOutlined',      '/user2',   'A8MainUser');

insert into func(id, name) values
    (200, '功能1'),
    (201, '功能2'),
    (202, '功能3');

insert into user_role values
    (1,10),
    (2,11),
    (3,12);

insert into role_menu values
    (10, 101),
    (10, 102),
    (10, 103),
    (10, 104),
    (10, 105),
    (11, 101),
    (11, 102),
    (12, 103),
    (12, 104);
   
   
   
/* dql操作数据库 */
SELECT @@character_set_client, @@character_set_connection, @@character_set_results;

SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

insert into student(name,sex,age,photo) values
    ('张三人你', '1', 40, '/imgs/1.png');
select * from student s  WHERE name ='张三人你';   


SET character_set_client = utf8mb4;
SET character_set_connection = gbk;
SET character_set_results = utf8mb4;
ALTER TABLE student ADD INDEX idx_name (name);
insert into student(name,sex,age,photo) values
    ('李四', '男', 40, '/imgs/1.png');
explain select * from student where name='李四';
select * from student where name='𠮷';
explain SELECT * from student s ;


use java_web;
drop table if exists student;
create table student (
    id int primary key auto_increment,
    name varchar(32) not null,
    sex varchar(2),
    age tinyint,
    photo varchar(32)
) ;

SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;
select * from student ;
