package es.org.talk.is.cheap.domain;


import lombok.Data;

import java.util.List;

@Data
public class HotelDocCompletion {
    private Long id;
    private String name;
    private String address;
    private Integer price;
    private Integer score;
    private String brand;
    private String city;
    private String starName;
    private String business;
    private String location;
    private String pic;
    private List<String> suggestion;

}
