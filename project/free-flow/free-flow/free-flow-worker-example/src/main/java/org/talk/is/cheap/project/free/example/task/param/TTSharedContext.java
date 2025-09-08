package org.talk.is.cheap.project.free.example.task.param;

import lombok.Data;
import org.talk.is.cheap.project.free.flow.common.task.definition.codec.JsonInputCodec;

@Data
public class TTSharedContext {

    public static class TTSharedContextInputClass extends JsonInputCodec<TTSharedContext> {

    }
    @Data
     public static class  TTSharedContextInnerData{
         private String address;
     }

    private int num;
    private String name;
    private TTSharedContextInnerData ttSharedContextInnerData;
}
