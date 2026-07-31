package jvm.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 触发多次 Full GC 的示例
 *
 * <p>原理：Full GC 在以下情况触发：</p>
 * <ul>
 *   <li>Old 区空间不足（对象晋升失败 / 大对象直接进入 Old 区）</li>
 *   <li>Metaspace（方法区）空间不足</li>
 *   <li>调用 System.gc()（建议性，不保证）</li>
 *   <li>G1 中 Evacuation Failure（没有空闲 Region 可用）</li>
 * </ul>
 *
 * <p>本示例模拟真实场景：持续创建大小混合的长生命周期对象填满堆，
 * OOM 后释放部分对象（大小都释放），然后继续分配触发多次 Full GC。</p>
 *
 * <h3>推荐 JVM 启动参数（Java 17）：</h3>
 * <pre>
 * -Xms256m -Xmx256m
 * -XX:+UseG1GC
 * -XX:MaxGCPauseMillis=200
 * -Xlog:gc*:file=./gc-full.log:time,uptime,level,tags
 * -XX:+HeapDumpOnOutOfMemoryError
 * -XX:HeapDumpPath=./heapdump-full.hprof
 * </pre>
 *
 * <p>注意：如果使用相对路径，文件会生成在 JVM 启动时的工作目录下，
 * 从 IDE 运行时工作目录可能不是你以为的目录，建议用绝对路径。</p>
 *
 * <h3>使用 jstat 观察：</h3>
 * <pre>
 * jstat -gc &lt;PID&gt; 1000 20
 * # 关注 FGC（Full GC次数）和 FGCT（Full GC耗时）列
 * # 同时观察 OU（Old Used）在 Full GC 前后的变化
 * </pre>
 *
 * <h3>GC 日志分析要点：</h3>
 * <ul>
 *   <li>搜索 "Pause Full" 关键字（G1 日志中 Full GC 的标识）</li>
 *   <li>观察 Full GC 触发原因：Allocation Failure / Evacuation Failure / System.gc()</li>
 *   <li>对比 Full GC 前后各 Region 的大小变化（包括 Humongous Region）</li>
 *   <li>G1 的 Full GC 在 JDK10+ 是并行回收，关注暂停时间</li>
 * </ul>
 *
 * @author dominiczhu
 */
public class FullGCDemo {

    public static void main(String[] args) throws InterruptedException {
        long pid = ProcessHandle.current().pid();
        System.out.println("=== Full GC Demo (G1) ===");
        System.out.println("PID: " + pid);
        System.out.println("jstat 命令: jstat -gc " + pid + " 1000");
        System.out.println("堆大小 256MB，使用 G1 收集器");
        System.out.println("对象分布：小对象 (byte[256~2KB], HashMap) + 大对象 (byte[1MB])");
        System.out.println();
        Thread.sleep(10000);

        // 小对象容器：模拟业务数据累积
        List<Object> smallObjects = new ArrayList<>();
        // 大对象容器：模拟文件/图片累积
        List<byte[]> largeObjects = new ArrayList<>();

        for (int round = 1; round <= 5; round++) {
            System.out.println("[第 " + round + " 轮] 分配长生命周期对象...");

            try {
                // 每轮分配：3000 个小对象 + 30 个大对象
                for (int i = 0; i < 3000; i++) {
                    // 小对象：模拟订单数据、用户信息等
                    Map<String, Object> record = new HashMap<>(16);
                    record.put("id", i);
                    record.put("payload", new byte[256 + (i % 1792)]); // 256B ~ 2KB
                    record.put("status", "active");
                    smallObjects.add(record);

                    // 大对象：每 100 个小对象配一个 1MB 大对象
                    if (i % 100 == 0) {
                        byte[] fileData = new byte[1024 * 1024]; // 1MB, Humongous
                        fileData[0] = (byte) i;
                        largeObjects.add(fileData);
                    }
                }
                System.out.println("  已持有: 小对象 " + smallObjects.size() + " 个, 大对象 " + largeObjects.size() + " 个");
            } catch (OutOfMemoryError e) {
                System.out.println("  OOM! 释放部分对象后继续...");

                // 释放一半小对象
                int smallHalf = smallObjects.size() / 2;
                for (int i = 0; i < smallHalf; i++) {
                    smallObjects.set(i, null);
                }
                smallObjects.removeIf(obj -> obj == null);

                // 释放一半大对象
                int largeHalf = largeObjects.size() / 2;
                for (int i = 0; i < largeHalf; i++) {
                    largeObjects.set(i, null);
                }
                largeObjects.removeIf(obj -> obj == null);

                System.out.println("  释放后: 小对象 " + smallObjects.size() + " 个, 大对象 " + largeObjects.size() + " 个");

                // 触发 GC 回收释放的对象
                System.gc();
                Thread.sleep(1000);
            }

            Thread.sleep(1000); // 暂停方便 jstat 观察
        }

        System.out.println();
        System.out.println("=== 通过 System.gc() 主动触发 Full GC ===");
        System.gc();
        Thread.sleep(2000);

        System.out.println();
        System.out.println("执行完成！");
        System.out.println("请使用 jstat -gc " + pid + " 1000 查看 FGC 次数和 FGCT 耗时");
        System.out.println("请在 ./gc-full.log 中搜索 'Pause Full' 分析日志");

        Thread.sleep(30_000);
    }
}
