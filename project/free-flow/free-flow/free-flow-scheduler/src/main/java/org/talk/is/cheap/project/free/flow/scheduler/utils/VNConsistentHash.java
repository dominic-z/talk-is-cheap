package org.talk.is.cheap.project.free.flow.scheduler.utils;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.talk.is.cheap.project.free.flow.common.utils.VerifyUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class VNConsistentHash {
    @Getter
    private final int vnNum;
    private final TreeMap<Long,String> hashRing = new TreeMap<>();
    private static final HashFunction HASH_FUNCTION = Hashing.murmur3_32_fixed();


    public VNConsistentHash(int vnNum){
        VerifyUtil.requireTrue(vnNum>0,"vnNum需要大于0");
        this.vnNum = vnNum;


    }

    public void addNode(String node){
        int vnNodeIdNumLength = String.format("%d", vnNum).length();
        String vnNodeIdFormat = "%s#VN%0"+ vnNodeIdNumLength + "d";

        for (int i = 0; i < vnNum; i++) {
            String vnNode = String.format(vnNodeIdFormat, node, i); // 确保格式相同，避免hash冲突
            long hash = HASH_FUNCTION.hashString(vnNode, StandardCharsets.UTF_8).asLong();
            hashRing.put(hash,node);
        }
    }

    public void deleteNode(String node){
        for (int i = 0; i < vnNum; i++) {
            String vnNode = String.format("%s#VN%d", node, i);
            long hash = HASH_FUNCTION.hashString(vnNode, StandardCharsets.UTF_8).asLong();
            hashRing.remove(hash);
        }
    }


    public String getNode(long dataHash){
        Map.Entry<Long, String> entry = this.hashRing.ceilingEntry(dataHash);
        if(entry==null){
            return this.hashRing.firstEntry().getValue();
        }
        return entry.getValue();
    }



}
