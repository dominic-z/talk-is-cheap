select * from t_test_data ttd  limit 4000000,10;
DROP TABLE IF EXISTS t_test_data;

explain select * from t_test_data ttd  limit 10,10;
explain select * from t_test_data ttd  limit 900000,10;


select * from t_test_data ttd  limit 900000,10;