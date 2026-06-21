
SELECT count(1) from t_test_data ttd ;


-- 会使用到index
explain select * from t_test_data ttd order by user_id limit 10,10;
-- 不会使用到index
explain select * from t_test_data ttd order by user_id limit 900000,10;


-- 几十毫秒返回
select * from t_test_data ttd order by user_id limit 10,10;


/*
 对上述sql进行优化，比如子查询
 */
-- 需要十几秒
select * from t_test_data ttd order by user_id limit 900000,10;
-- 使用子查询进行优化
select * from t_test_data ttd WHERE id in (SELECT sub.id from (select id from t_test_data order by user_id limit 900000,10) as sub);

-- search after翻页优化deep-page，除了排序字段，还需要带上主键索引维持排序稳定
select * from t_test_data ttd order by user_id,id limit 900000,10;
select * from t_test_data ttd WHERE (user_id='U301989' and id>=239013) or user_id>'U301989' order by user_id,id limit 10;
explain select * from t_test_data ttd WHERE (user_id='U301989' and id>=239013) or user_id>'U301989' order by user_id,id limit 10;

/*
 * 网上说，深度分页的问题在于limit m,n需要找到m+n条然后丢弃前m条，所以很慢，只要不让他查前m+n条就快了；还有说法是说找到m+n条需要回表m+n次，所以很慢，只要不回表就不会慢了；
 * 那我的问题是，对于上面未优化的sql，究竟是查了m+n条->丢弃前m条->回表n次，还是先查了m+n条->回表m+n次->丢弃前m条。究竟是丢弃m条慢，还是回表m+n次慢？
 * 结论是：这两个都，但是回表m+n次更慢，深度分页的主要罪魁祸首。
 * 从上述子查询优化的sql可以看出，确实没有频繁回表了，他解决了回表m+n次的问题，速度快了，但是他解决查m+n丢弃m次问题了么？答案是没有；
 * 可以看下列两个sql，第一个sql明显比第二个sql块，也就是说，即使没回表，查m+n后丢弃m的行为都会随着m的变大而耗费更多时间，但是耗费的时间的增长没有那么快
 * */
select id from t_test_data ttd order by user_id limit 100000,10; 
select id from t_test_data ttd order by user_id limit 3000000,10;

 




/*
 * 使用游标进行深度分页，前提得是能找到一个唯一的组合作为order by中，很明显，user_id不唯一，因此需要user_id和id联合作为order by的语句
 * */
-- 这个sql需要30s左右
select * from t_test_data ttd order by user_id,id limit 900000,10;
-- 对深度分页sql进行优化，比如使用游标，比如说上一页的user_id是U30199，id是887277（从上述结果的第一条抄来的），结果应该就是上述sql的后9条。
SELECT * from t_test_data ttd where (user_id='U301989' and id>239013) or user_id >'U301989' order by user_id LIMIT 9;

 

