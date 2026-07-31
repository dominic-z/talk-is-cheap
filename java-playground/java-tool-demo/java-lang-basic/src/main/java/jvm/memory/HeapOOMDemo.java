package jvm.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 堆内存溢出（OutOfMemoryError: Java heap space）示例
 *
 * <p>原理：持续创建对象并持有引用（不让 GC 回收），直到堆内存耗尽，JVM 抛出 OOM。</p>
 *
 * <p>本示例模拟真实内存泄漏场景：大小对象混合泄漏，
 * 比如业务缓存不断增长（小对象）+ 文件/图片缓存未清理（大对象）。</p>
 *
 * <h3>推荐 JVM 启动参数（Java 17）：</h3>
 * <pre>
 * -Xms128m -Xmx128m
 * -XX:+UseG1GC
 * -XX:MaxGCPauseMillis=200
 * -Xlog:gc*:file=/tmp/gc-oom.log:time,uptime,level,tags
 * -XX:+HeapDumpOnOutOfMemoryError
 * -XX:HeapDumpPath=/tmp/heapdump-oom.hprof
 * </pre>
 *
 * <p>注意：如果使用相对路径，文件会生成在 JVM 启动时的工作目录下，
 * 从 IDE 运行时工作目录可能不是你以为的目录，建议用绝对路径。</p>
 *
 * <h3>堆转储文件分析：</h3>
 * <ul>
 *   <li>OOM 时自动生成 /tmp/heapdump-oom.hprof 文件</li>
 *   <li>使用 Eclipse MAT (Memory Analyzer Tool) 打开分析</li>
 *   <li>使用 VisualVM 打开查看对象分布</li>
 *   <li>关注 Dominator Tree 和 Leak Suspects 报告</li>
 *   <li>观察小对象（HashMap、byte[]）和大对象（byte[1MB]）的占比</li>
 * </ul>
 *
 * <h3>使用 jstat 观察（在 OOM 之前）：</h3>
 * <pre>
 * jstat -gc &lt;PID&gt; 500
 * # 观察 OU（Old Used）持续增长直到接近 OC（Old Capacity）
 * # 观察 FGC 次数增加但 OU 不下降（因为对象都有强引用）
 * </pre>
 *
 * <h3>GC 日志分析要点：</h3>
 * <ul>
 *   <li>观察 G1 的 Mixed GC / Full GC 后 Region 几乎没有被回收（对象都存活）</li>
 *   <li>最终出现 OutOfMemoryError: Java heap space</li>
 *   <li>GC 频率越来越高，暂停时间越来越长（GC 风暴）</li>
 *   <li>G1 可能先尝试 Mixed GC 回收，失败后退化为 Full GC</li>
 * </ul>
 *
 * @author dominiczhu
 */
public class HeapOOMDemo {

    public static void main(String[] args) {
        long pid = ProcessHandle.current().pid();
        System.out.println("=== Heap OOM Demo (G1) ===");
        System.out.println("PID: " + pid);
        System.out.println("jstat 命令: jstat -gc " + pid + " 500");
        System.out.println("堆大小 128MB，使用 G1 收集器");
        System.out.println("模拟内存泄漏：小对象 (HashMap+byte[256~2KB]) + 大对象 (byte[1MB]) 持续增长");
        System.out.println("OOM 时会自动生成堆转储文件：/tmp/heapdump-oom.hprof");
        System.out.println();

        // 模拟内存泄漏：两个不断增长的缓存
        List<Object> businessCache = new ArrayList<>();   // 小对象泄漏：业务缓存
        List<byte[]> fileCache = new ArrayList<>();       // 大对象泄漏：文件缓存
        int smallCount = 0;
        int largeCount = 0;

        try {
            while (true) {
                // 小对象泄漏：模拟业务数据不断累积（如未清理的 Session、未过期的缓存）
                for (int i = 0; i < 100; i++) {
                    Map<String, Object> entry = new HashMap<>(16);
                    entry.put("key", "item-" + smallCount);
                    entry.put("data", new byte[256 + (smallCount % 1792)]); // 256B ~ 2KB
                    entry.put("createTime", System.currentTimeMillis());
                    businessCache.add(entry);
                    smallCount++;
                }

                // 大对象泄漏：模拟文件/图片缓存未清理
                byte[] fileData = new byte[1024 * 1024]; // 1MB, Humongous
                fileData[0] = (byte) largeCount;
                fileCache.add(fileData);
                largeCount++;

                if ((smallCount + largeCount) % 50 == 0) {
                    System.out.println("已泄漏: 小对象 " + smallCount + " 个, 大对象 " + largeCount + " 个 (约 " + largeCount + " MB)");
                }
            }
        } catch (OutOfMemoryError e) {
            System.out.println();
            System.out.println("========== OOM 发生 ==========");
            System.out.println("小对象泄漏: " + smallCount + " 个 (HashMap + byte[256~2KB])");
            System.out.println("大对象泄漏: " + largeCount + " 个 (byte[1MB], 约 " + largeCount + " MB)");
            System.out.println("异常信息: " + e.getMessage());
            System.out.println();
            System.out.println("堆转储文件已生成: /tmp/heapdump-oom.hprof");
            System.out.println("分析工具推荐：");
            System.out.println("  1. Eclipse MAT: 打开 hprof 文件，查看 Leak Suspects");
            System.out.println("  2. VisualVM: 打开 hprof 文件，查看对象直方图");
            System.out.println("  3. 关注 byte[] 和 HashMap 的对象数量和占用大小");
            System.out.println("  4. 查看 Dominator Tree 找到最大的内存持有者");
            System.out.println();
            System.out.println("GC 日志分析：查看 /tmp/gc-oom.log 中 Full GC 后 Region 是否被回收");

            // 释放引用，让程序可以正常退出
            businessCache = null;
            fileCache = null;
        }
    }
}
