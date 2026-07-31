package jvm.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 触发多次 Mixed GC 的示例（需要 G1 收集器）
 *
 * <p>原理：Mixed GC 是 G1 特有的 GC 类型，在并发标记（Concurrent Marking）完成后触发，
 * 会同时回收 Young 区和部分 Old 区（Garbage First 优先回收垃圾最多的 Region）。</p>
 *
 * <p>触发条件：Old 区占用达到 IHOP（InitiatingHeapOccupancyPercent，默认45%）→ 触发并发标记 → 标记完成后触发 Mixed GC</p>
 *
 * <p>本示例模拟真实场景：Old 区中既有普通小对象（经多次 Young GC 晋升而来），
 * 也有 Humongous 大对象（直接分配在 Old 区），释放时两种都释放一部分。</p>
 *
 * <h3>推荐 JVM 启动参数（Java 17）：</h3>
 * <pre>
 * -Xms512m -Xmx512m
 * -XX:+UseG1GC
 * -XX:MaxGCPauseMillis=200
 * -XX:InitiatingHeapOccupancyPercent=35
 * -XX:G1MixedGCCountTarget=4
 * -Xlog:gc*:file=./gc-mixed.log:time,uptime,level,tags
 * </pre>
 *
 * <p>注意：如果使用相对路径，文件会生成在 JVM 启动时的工作目录下，
 * 从 IDE 运行时工作目录可能不是你以为的目录，建议用绝对路径。</p>
 *
 * <h3>使用 jstat 观察：</h3>
 * <pre>
 * jstat -gc &lt;PID&gt; 1000 20
 * # 关注 YGC、FGC 列；Mixed GC 在 jstat 中计入 YGC
 * # 更详细的 Mixed GC 信息需要看 GC 日志
 * </pre>
 *
 * <h3>GC 日志分析要点：</h3>
 * <ul>
 *   <li>搜索 "Pause Young (Mixed)" 关键字定位 Mixed GC</li>
 *   <li>观察并发标记阶段：Concurrent Mark → Remark → Cleanup</li>
 *   <li>观察 Mixed GC 回收了哪些 Region（Young + Old + Humongous）</li>
 *   <li>G1MixedGCCountTarget=4 表示一次并发标记后分 4 次 Mixed GC 回收老年代</li>
 * </ul>
 *
 * @author dominiczhu
 */
public class MixedGCDemo {

    public static void main(String[] args) throws InterruptedException {
        long pid = ProcessHandle.current().pid();
        System.out.println("=== Mixed GC Demo (G1) ===");
        System.out.println("PID: " + pid);
        System.out.println("jstat 命令: jstat -gc " + pid + " 1000");
        System.out.println("堆大小 512MB，IHOP=35%，即 Old 区占用约 179MB 时触发并发标记");
        System.out.println("对象分布：小对象 (byte[128~4KB], HashMap) + 大对象 (byte[1MB])");
        System.out.println();
        Thread.sleep(10000);

        // === 阶段1：创建长生命周期对象，晋升到 Old 区，触发并发标记 ===
        System.out.println("[阶段1] 创建长生命周期对象，填满 Old 区以触发并发标记...");

        // 小对象容器：模拟业务缓存（经多次 Young GC 后晋升到 Old 区）
        List<Object> smallObjectCache = new ArrayList<>();
        // 大对象容器：模拟文件/图片缓存（Humongous，直接进入 Old 区）
        List<byte[]> largeObjectCache = new ArrayList<>();

        for (int i = 0; i < 2000; i++) {
            // 小对象：模拟用户会话缓存、配置对象等
            Map<String, Object> session = new HashMap<>(32);
            session.put("userId", i);
            session.put("token", "token-" + i + "-" + System.nanoTime());
            session.put("data", new byte[512 + (i % 1024)]); // 512B ~ 1.5KB
            smallObjectCache.add(session);

            // 大对象：每 10 个小对象配一个 1MB 大对象（模拟图片/文件缓存）
            if (i % 10 == 0) {
                byte[] imageCache = new byte[1024 * 1024]; // 1MB, Humongous
                imageCache[0] = (byte) i;
                largeObjectCache.add(imageCache);
            }

            if (i % 200 == 0) {
                System.out.println("  已分配: 小对象 " + smallObjectCache.size() + " 个, 大对象 " + largeObjectCache.size() + " 个");
                Thread.sleep(100);
            }
        }

        System.out.println();
        System.out.println("[阶段2] 继续创建短生命周期对象，触发 Young GC 并观察 Mixed GC...");

        // === 阶段2：短生命周期对象（大小混合），触发 Young GC 和 Mixed GC ===
        for (int round = 0; round < 3; round++) {
            System.out.println("  第 " + (round + 1) + " 轮短生命周期对象分配...");
            for (int i = 0; i < 5000; i++) {
                // 小对象：模拟请求处理临时数据
                byte[] buffer = new byte[256 + (i % 768)];
                String resp = "{\"code\":200,\"data\":" + i + "}";

                // 大对象：模拟临时大报文处理
                if (i % 20 == 0) {
                    byte[] tempLarge = new byte[1024 * 1024];
                    tempLarge[0] = (byte) i;
                }
            }
            Thread.sleep(1000);
        }

        // === 阶段3：释放部分 Old 区对象（大小都释放一部分），制造回收目标 ===
        System.out.println();
        System.out.println("[阶段3] 释放部分 Old 区对象，制造回收目标...");

        // 释放一半小对象缓存
        int smallHalf = smallObjectCache.size() / 2;
        for (int i = 0; i < smallHalf; i++) {
            smallObjectCache.set(i, null);
        }
        System.out.println("  释放小对象: " + smallHalf + " 个");

        // 释放一半大对象缓存
        int largeHalf = largeObjectCache.size() / 2;
        for (int i = 0; i < largeHalf; i++) {
            largeObjectCache.set(i, null);
        }
        System.out.println("  释放大对象: " + largeHalf + " 个 (约 " + largeHalf + " MB Humongous Region)");

        smallObjectCache = null;
        largeObjectCache = null;

        // === 阶段4：再次分配，触发 Mixed GC 回收 Old 区垃圾 ===
        System.out.println();
        System.out.println("[阶段4] 再次分配对象，触发 Mixed GC 回收 Old 区...");
        for (int i = 0; i < 8000; i++) {
            // 小对象
            byte[] temp = new byte[512 + (i % 512)];

            // 大对象
            if (i % 15 == 0) {
                byte[] tempLarge = new byte[1024 * 1024];
                tempLarge[0] = (byte) i;
            }

            if (i % 2000 == 0) {
                Thread.sleep(300);
            }
        }

        System.out.println();
        System.out.println("执行完成！");
        System.out.println("请在 ./gc-mixed.log 中搜索 'Pause Young (Mixed)' 查看 Mixed GC");
        System.out.println("请使用 jstat -gc " + pid + " 查看 GC 统计");

        Thread.sleep(30_000);
    }
}
