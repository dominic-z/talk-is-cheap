package hello.spring.boot3.spi.starter.starter.feign.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericData<T>{
    private Integer code;
    private T data;
    private String msg;
}
