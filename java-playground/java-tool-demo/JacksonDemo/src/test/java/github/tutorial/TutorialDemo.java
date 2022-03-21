package github.tutorial;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://github.com/FasterXML/jackson-databind下tutorial
 */

public class TutorialDemo {

    private Apple get(int id) {
        Apple apple1 = new Apple();
        apple1.setI(id);
        apple1.setBananaList(new ArrayList<>());

        Banana banana = new Banana();
        banana.setName("a banana");
        apple1.getBananaList().add(banana);
        return apple1;
    }

    ObjectMapper defaultMapper;
    Apple apple1;
    Apple apple2;

    @Before
    public void init() {
        defaultMapper = new ObjectMapper();
        apple1 = get(1);
        apple2 = get(2);
    }

    @Test
    public void basicUse() throws Exception {
        //Object to JSON in String
        String appleJsonInString = defaultMapper.writeValueAsString(apple1);
        System.out.println(appleJsonInString);

        System.out.println("apples==========================");
        List<Apple> apples = new ArrayList<>();
        apples.add(apple1);
        apples.add(apple2);
        String appleJSONString = defaultMapper.writeValueAsString(apples);
        System.out.println(appleJSONString);

        System.out.println("appleMap==========================");
        Map<String, Apple> appleMap = new HashMap<>();
        appleMap.put("apple1", apple1);
        appleMap.put("apple2", apple2);
        String appleMapJSONString = defaultMapper.writeValueAsString(appleMap);
        System.out.println(appleMapJSONString);

        System.out.println("appleMapMap==========================");
        Map<String,Map<String,Apple>> appleMapMap=new HashMap<>();
        appleMapMap.put("appleMap",appleMap);
        String appleMapMapString = defaultMapper.writeValueAsString(appleMapMap);
        System.out.println(appleMapMapString);

        System.out.println("newApple1==========================");
//        JSON in string to Object
        Apple newApple1 = defaultMapper.readValue(appleJsonInString, Apple.class);
        System.out.println(newApple1);
        System.out.println("newAppleMap1 and newAppleMap2==========================");
        Map<?, ?> newAppleMap1 = defaultMapper.readValue(appleMapJSONString, Map.class);
        System.out.println(newAppleMap1.get("apple1"));
        Map<String, Apple> newAppleMap2 = defaultMapper.readValue(appleMapJSONString, new TypeReference<Map<String, Apple>>() {
        });
        System.out.println(newAppleMap2);
        System.out.println("newAppleMapMap==========================");
        Map<?,?> newAppleMapMap=defaultMapper.readValue(appleMapMapString,Map.class);
        System.out.println(newAppleMapMap);
        Map<?,?> appleMap1 = (Map<?, ?>) newAppleMapMap.get("appleMap");
        System.out.println(appleMap1);

    }

    @Test
    public void streamingDemo() throws IOException {
//相当于把每个json看作一行流数据，从左到右去读取和处理
        JsonFactory f = defaultMapper.getFactory(); // may alternatively construct directly too

// First: write simple JSON output
        File jsonFile = new File("test.json");
        JsonGenerator g = f.createGenerator(jsonFile, JsonEncoding.UTF8);
// write JSON: { "message" : "Hello world!" }
        g.writeStartObject();
        g.writeStringField("message", "Hello world!");
        g.writeObjectField("apple", apple1);
        g.writeEndObject();
        g.close();

// Second: read file back
        JsonParser p = f.createParser(jsonFile);
        JsonToken t = p.nextToken(); // Should be JsonToken.START_OBJECT
        t = p.nextToken(); // JsonToken.FIELD_NAME
        if ((t != JsonToken.FIELD_NAME) || !"message".equals(p.getCurrentName())) {
            // handle error
        }
        t = p.nextToken();// JsonToken.VALUE_STRING
        if (t != JsonToken.VALUE_STRING) {
            // similarly
        }
        String msg = p.getText();
        System.out.printf("My message to you is: %s!\n", msg);

        t = p.nextToken();// JsonToken.FIELD_NAME
        System.out.println(t);

        t = p.nextToken();// JsonToken.START_OBJECT
        System.out.println(t);

        t = p.nextToken();// JsonToken.FIELD_NAME
        System.out.println(t + ":" + p.getText());
        t = p.nextToken();// JsonToken.VALUE_NUMBER_INT
        System.out.println(t + ":" + p.getText());
        p.close();

    }

    ObjectMapper configurableMapper;

    @Before
    public void configMapper() {
        // SerializationFeature for changing how JSON is written
        configurableMapper = new ObjectMapper();
// to enable standard indentation ("pretty-printing"):
        configurableMapper.enable(SerializationFeature.INDENT_OUTPUT);
// to allow serialization of "empty" POJOs (no properties to serialize)
// (without this setting, an exception is thrown in those cases)
        configurableMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
// to write java.util.Date, Calendar as number (timestamp):
        configurableMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

// DeserializationFeature for changing how JSON is read as POJOs:

// to prevent exception when encountering unknown property:
        configurableMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
// to allow coercion of JSON empty String ("") to null Object value:
        configurableMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }



    AnnoBean bean1;
    AnnoBean bean2;

    @Test
    public void annotationDemo() throws IOException {
        File annoJsonFile = Paths.get("annoBean.json").toFile();
        AnnoBean annoBean = defaultMapper.readValue(annoJsonFile, AnnoBean.class);
        System.out.println(annoBean);

        String annoBeanString=defaultMapper.writeValueAsString(annoBean);
        System.out.println(annoBeanString);

        File annoCtorBeanJsonFile=Paths.get("annoCtorBean.json").toFile();
        AnnoCtorBean annoCtorBean=defaultMapper.readValue(annoCtorBeanJsonFile,AnnoCtorBean.class);
        System.out.println(annoCtorBean);

        System.out.println(defaultMapper.readValue("{\"name\":\"name\"}",FactoryBean.class));
    }


    @Test
    public void emptyJsonDemo()  {
        String jsonStr="{\"i\":0,\"b\":1}";
        Apple apple = null;
        try {
            apple = defaultMapper.readValue(jsonStr, Apple.class);
            System.out.println(apple);
            System.out.println(defaultMapper.writeValueAsString(apple));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            Apple apple2 = configurableMapper.readValue(jsonStr, Apple.class);
            System.out.println(apple2);
            System.out.println(configurableMapper.writeValueAsString(apple2));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}

class Apple {
    private int i;
    private List<Banana> bananaList;

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public List<Banana> getBananaList() {
        return bananaList;
    }

    public void setBananaList(List<Banana> bananaList) {
        this.bananaList = bananaList;
    }

    @Override
    public String toString() {
        return "github.tutorial.Apple{" +
                "i=" + i +
                ", bananaList=" + bananaList +
                '}';
    }
}

class Banana {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "github.tutorial.Banana{" +
                "name='" + name + '\'' +
                '}';
    }
}

// means that if we see "foo" or "bar" in JSON, they will be quietly skipped
// regardless of whether POJO has such properties
@JsonIgnoreProperties({"foo", "bar"})
class AnnoBean {
    private String _name;

    // without annotation, we'd get "theName", but we want "name":
    @JsonProperty("name")
    public String getTheName() {
        return _name;
    }

    // note: it is enough to add annotation on just getter OR setter;
    // so we can omit it here
    public void setTheName(String n) {
        _name = n;
    }

    // will not be written as JSON; nor assigned from JSON:
    @JsonIgnore
    private String internal;

    public String getInternal() {
        return internal;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    // no annotation, public field is read/written normally
    private String external;

    public String getExternal() {
        return external;
    }

    public void setExternal(String external) {
        this.external = external;
    }

    private int _code;

    @JsonIgnore
    public void setCode(int c) {
        _code = c;
    }

    // note: will also be ignored because setter has annotation!
    public int getCode() {
        return _code;
    }

    private String _nameOnlyForSet;

    @JsonProperty
    public void setNameOnlyForSet(String n) {
        _nameOnlyForSet = n;
    }

    @JsonIgnore
    public String getNameOnlyForSet() {
        return _nameOnlyForSet;
    }

    @JsonFormat(pattern = "yyyyMMdd")
    private Date today;

    public Date getToday() {
        return today;
    }

    public void setToday(Date today) {
        this.today = today;
    }

    @Override
    public String toString() {
        return "github.tutorial.AnnoBean{" +
                "_name='" + _name + '\'' +
                ", internal='" + internal + '\'' +
                ", external='" + external + '\'' +
                ", _code=" + _code +
                ", _nameOnlyForSet='" + _nameOnlyForSet + '\'' +
                '}';
    }
}


class AnnoCtorBean {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    private AnnoCtorBean(){}

    @JsonCreator // constructor can be public, private, whatever
    private AnnoCtorBean(@JsonProperty("name") String name,
                         @JsonProperty("age") int age) {
        System.out.println("constructor");
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "github.tutorial.AnnoCtorBean{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

class FactoryBean
{
    // fields etc omitted for brewity

    @JsonCreator
    public static FactoryBean create(@JsonProperty("name") String name) {
        // construct and return an instance
        System.out.println(name);
        return new FactoryBean();
    }
}