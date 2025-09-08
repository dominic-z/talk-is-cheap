package org.talk.is.cheap.project.free.example.task.param;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.task.definition.codec.JsonInputCodec;

@Data
public class TTMethod1Input {


    public static class TTMethod1InputCodec extends JsonInputCodec<TTMethod1Input> {

    }

    @Data
     public static class  TTMethod1InputInner{
         private String address;
     }

    private int num;
    private String name;
    private TTMethod1InputInner ttMethod1InputInner;
}
