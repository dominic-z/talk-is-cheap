package others;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title SimpleUse
 * @date 2021/5/10 下午2:30
 */
public class SimpleUse {

    private FruitBasket get(int id) {
        FruitBasket fruitBasket=new FruitBasket();
        Apple apple1 = new Apple();
        apple1.setI(id);
        fruitBasket.setApples(new ArrayList<>());
        fruitBasket.getApples().add(apple1);

        Banana aBanana = new Banana();
        aBanana.setName("a banana");
        fruitBasket.setBananas(new ArrayList<>());
        fruitBasket.getBananas().add(aBanana);
        Banana bBanana = new Banana();
        bBanana.setName("b banana");
        fruitBasket.getBananas().add(bBanana);
//        fruitBasket.setBananaMap(Map.of(bBanana,1));
        return fruitBasket;
    }

    ObjectMapper defaultMapper;
    FruitBasket fruitBasket;
    Child child;

    @Before
    public void init() {
        defaultMapper = new ObjectMapper();
        fruitBasket = get(1);

        child=new Child();
        child.setFruitBasket(fruitBasket);
        child.setName("kid");
        child.setSuccess(true);
        child.setIsGood(true);
    }

    @Test
    public void writeJson() throws JsonProcessingException {
        String  childJson=defaultMapper.writeValueAsString(child);
        System.out.println(childJson);
    }

    @Test
    public void readJson() throws JsonProcessingException {
        String childJson=defaultMapper.writeValueAsString(child);
        Child newChild = defaultMapper.readValue(childJson, Child.class);
        System.out.println(newChild);

        Map<?, ?> newChildMap = defaultMapper.readValue(childJson, Map.class);
        System.out.println(newChildMap);
        Map<?, ?> newFruitBasket= (Map<?, ?>) newChildMap.get("fruitBasket");

        List<?> bananas= (List<?>) newFruitBasket.get("bananas");
        System.out.println(bananas.get(0));
    }
}

@Data
class Child {
    private String name;
    private FruitBasket fruitBasket;
    private Date birthDay;
    private boolean isSuccess; // 用来测试pojo对象里应不应该带is，结论是，boolean对象不应该带is
    private Boolean isGood; // 包装类型会相对缓解问题，因为针对包装类提供的getter和setter都是直接加get和set
}

@Data
class FruitBasket {
    private List<Banana> bananas;
    private List<Apple> apples;
    /*
    这种map没有办法反序列化，需要配置
     */
//    private Map<Banana,Integer> bananaMap;
}

@Data
class Apple {
    private int i;
}

@Data
class Banana {
    private String name;
}