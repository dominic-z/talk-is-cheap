package org.talk.is.cheap.project.free.flow.starter.repository.service.derived;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talk.is.cheap.project.free.flow.starter.repository.domain.pojo.SeqGenerator;
import org.talk.is.cheap.project.free.flow.starter.repository.dao.mbg.query.example.SeqGeneratorExample;
import org.talk.is.cheap.project.free.flow.starter.repository.service.SeqGeneratorService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 序列生成器
 */
@Service
@Slf4j
public class SeqGeneratorUtil {

    @Autowired
    private SeqGeneratorService seqGeneratorService;

    private static final int step = 5;

    private final Map<String, AtomicLong> seqNextOne = new HashMap<>();
    private final Map<String, AtomicLong> seqBoundary = new HashMap<>();

    // 按序列名称来分配的锁对象
    private final Map<String, Object> lockMap = new HashMap<>();


    public long getNextId(String name) {
        if (!seqNextOne.containsKey(name)) {
            acquire(name);
        }

        AtomicLong nextAtomic = seqNextOne.get(name);
        AtomicLong boundaryAtomic = seqBoundary.get(name);
        while (true) {
            long next = nextAtomic.get();
            if (next < boundaryAtomic.get()) {
                if (nextAtomic.compareAndSet(next, next + 1)) {
                    return next + 1;
                }
//                log.info("自增并发失败");
            } else {
                acquire(name);
            }
        }

    }

    private Object getLock(String name) {
        if (lockMap.containsKey(name)) {
            return lockMap.get(name);
        }

        synchronized (this) {
            if (lockMap.containsKey(name)) {
                return lockMap.get(name);
            }
            lockMap.put(name, new Object());
        }
        return lockMap.get(name);
    }


    /**
     * 更新boundary
     * @param name
     */
    private void acquire(String name) {

        synchronized (getLock(name)) {

            if (!seqNextOne.containsKey(name)) {
                seqNextOne.put(name, new AtomicLong(0));
            }
            if (!seqBoundary.containsKey(name)) {
                seqBoundary.put(name, new AtomicLong(0));
            }

            AtomicLong nextAtomic = seqNextOne.get(name);
            AtomicLong boundaryAtomic = seqBoundary.get(name);
            if (nextAtomic.get() < boundaryAtomic.get()) {
                return;
            }

            while (true) {
                // todo: 目前是乐观锁并发控制，可以考虑支持悲观锁模式
                SeqGeneratorExample query = new SeqGeneratorExample();
                query.createCriteria().andSeqNameEqualTo(name);
                List<SeqGenerator> seqGenerators = seqGeneratorService.selectByExample(query);
                if (seqGenerators.isEmpty()) {
                    throw new RuntimeException("there is no seq named: " + name);
                }
                SeqGenerator seqGenerator = seqGenerators.get(0);
                BigDecimal next = new BigDecimal(seqGenerator.getNext());


                SeqGeneratorExample updateExample = new SeqGeneratorExample();
                // 并发控制
                updateExample.createCriteria().andSeqNameEqualTo(name).andRevisionEqualTo(seqGenerator.getRevision());

                BigDecimal boundary = next.add(new BigDecimal(step));
                seqGenerator.setNext(boundary.toString());
                seqGenerator.setCreateTime(null);
                seqGenerator.setUpdateTime(null);
                seqGenerator.setRevision(seqGenerator.getRevision() + 1);

                int updated = seqGeneratorService.updateByExampleSelective(seqGenerator, updateExample);
                if (updated != 0) {
                    boundaryAtomic.set(boundary.longValue());
                    return;
                }
//                log.info("修改数据库并发失败，重试");
            }
        }


    }


}
