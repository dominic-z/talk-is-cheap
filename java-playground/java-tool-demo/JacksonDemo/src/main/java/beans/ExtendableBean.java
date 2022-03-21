package beans;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dominiczhu
 * @date 2020/9/8 6:37 下午
 */
public class ExtendableBean {
    public String name;
    private Map<String, String> properties;

    public ExtendableBean(String name){
        this.name=name;
        this.properties=new HashMap<>();
    }

    @JsonAnyGetter
    public Map<String, String> getProperties() {
        return properties;
    }

    public void add(String k,String v){
        properties.put(k,v);
    }
}