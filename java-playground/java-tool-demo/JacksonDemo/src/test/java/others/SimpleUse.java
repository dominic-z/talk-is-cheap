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
    }

    @Test
    public void writeJson() throws JsonProcessingException {
        String  childJson=defaultMapper.writeValueAsString(child);
        System.out.println(childJson);
    }

    @Test
    public void readJson() throws JsonProcessingException {
        String  childJson=defaultMapper.writeValueAsString(child);
        Map<?, ?> newChild = defaultMapper.readValue(childJson, Map.class);
        System.out.println(newChild);
        Map<?, ?> newFruitBasket= (Map<?, ?>) newChild.get("fruitBasket");

        List<?> bananas= (List<?>) newFruitBasket.get("bananas");
        System.out.println(bananas.get(0));
    }
}

@Data
class Child {
    private String name;
    private FruitBasket fruitBasket;
    private Date birthDay;
}

@Data
class FruitBasket {
    private List<Banana> bananas;
    private List<Apple> apples;
}

@Data
class Apple {
    private int i;
}

@Data
class Banana {
    private String name;
}