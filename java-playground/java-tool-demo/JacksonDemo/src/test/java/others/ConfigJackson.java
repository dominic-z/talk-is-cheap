package others;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author dominiczhu
 * @version 1.0
 * @title ConfigJackson
 * @date 2021/5/10 下午2:28
 */
public class ConfigJackson {

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

    ObjectMapper configurableMapper;
    FruitBasket fruitBasket;
    Child child;

    @Before
    public void init() {
        configurableMapper = new ObjectMapper();
        configurableMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES,true);
        configurableMapper.setDateFormat(new SimpleDateFormat("yyyyMMdd"));
        fruitBasket = get(1);

        child=new Child();
        child.setFruitBasket(fruitBasket);
        child.setName("kid");
        child.setBirthDay(new Date());
    }

    @Test
    public void writeJson() throws JsonProcessingException {
        String  childJson=configurableMapper.writeValueAsString(child);
        System.out.println(childJson);
    }

    @Test
    public void readJson() throws JsonProcessingException {
        String  childJson=configurableMapper.writeValueAsString(child).replace("\"","'");
        System.out.println(childJson);
        Map<?, ?> newChild = configurableMapper.readValue(childJson, Map.class);
        System.out.println(newChild);
        Map<?, ?> newFruitBasket= (Map<?, ?>) newChild.get("fruitBasket");

        List<?> bananas= (List<?>) newFruitBasket.get("bananas");
        System.out.println(bananas.get(0));
    }

}
