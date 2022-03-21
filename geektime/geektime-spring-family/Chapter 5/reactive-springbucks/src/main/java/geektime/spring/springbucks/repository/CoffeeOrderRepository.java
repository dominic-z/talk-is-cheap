package geektime.spring.springbucks.repository;

import geektime.spring.springbucks.model.CoffeeOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;

@Repository
public class CoffeeOrderRepository {
    @Autowired
    private DatabaseClient databaseClient;

    public Mono<Long> save(CoffeeOrder order) {
        return databaseClient.insert().into("t_order")
                .value("customer", order.getCustomer())
                .value("state", order.getState().ordinal())
                .value("create_time", new Timestamp(order.getCreateTime().getTime()))
                .value("update_time", new Timestamp(order.getUpdateTime().getTime()))
                .fetch()
                .first()
//                .map(m -> (Long) m.get("ID")) 其实也用不着flatMap，flatMap传入的是一个a->flux 然后把多个flux拍扁
                .flatMap(m -> Mono.just((Long) m.get("ID")))//返回的是插入之后的id
                .flatMap(id -> Flux.fromIterable(order.getItems())
                        .flatMap(c -> databaseClient.insert().into("t_order_coffee")
                                .value("coffee_order_id", id)
                                .value("items_id", c.getId())
                                .then()).then(Mono.just(id)));
    }
}
