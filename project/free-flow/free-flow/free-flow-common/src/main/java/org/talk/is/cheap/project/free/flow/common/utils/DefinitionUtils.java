package org.talk.is.cheap.project.free.flow.common.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefinitionUtils {


    public static <V> Map<V, Set<V>> pointOutGraphToPointInGraph(Map<V,Set<V>> pointOutGraph){
        HashMap<V, Set<V>> pointInGraph = new HashMap<>();
        pointOutGraph.forEach((srcStage,targetStages)->{
            targetStages.forEach(targetStage->{
                pointInGraph.computeIfAbsent(targetStage,(k)->new HashSet<V>()).add(srcStage);
            });
        });
        return pointInGraph;
    }
}
