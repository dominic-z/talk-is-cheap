package jvm.memory;

import java.util.HashMap;
import java.util.Map;

/**
 * 触发多次 Young GC 的示例
 *
 * <p>原理：Young GC 在 Eden 区满时触发。通过不断创建短生命周期对象填满 Eden 区，即可触发 Young GC。</p>
 *
 * <p>本示例模拟真实生产环境：大部分是普通小对象（走正常 Eden → Survivor 晋升路径），
 * 少部分是 Humongous 大对象（>= Region 50%，直接进入 Old 区的 Humongous Region）。</p>
 *
 * <p>G1 Region 大小说明：256MB 堆 → Region 约 1MB → Humongous 阈值约 512KB</p>
 *
 * <h3>推荐 JVM 启动参数（Java 17）：</h3>
 * <pre>
 * -Xms256m -Xmx256m
 * -XX:+UseG1GC
 * -XX:MaxGCPauseMillis=200
 * -Xlog:gc*:file=./gc-young.log:time,uptime,level,tags
 * </pre>
 *
 * <p>注意：如果使用相对路径，文件会生成在 JVM 启动时的工作目录下，
 * 从 IDE 运行时工作目录可能不是你以为的目录，建议用绝对路径。</p>
 *
 * <h3>使用 jstat 观察：</h3>
 * <pre>
 * jstat -gc &lt;PID&gt; 1000 10
 * # 关注 YGC（Young GC次数）和 YGCT（Young GC耗时）列
 * </pre>
 *
 * <h3>GC 日志分析要点：</h3>
 * <ul>
 *   <li>搜索 "Pause Young (Normal)" 关键字定位 G1 的 Young GC</li>
 *   <li>观察 Eden Region 从满到清空的频率</li>
 *   <li>观察 Survivor Region 数量和对象年龄变化</li>
 *   <li>小对象走正常 Eden→Survivor 晋升，大对象直接进入 Humongous Region</li>
 *   <li>G1 的 Young 区大小是动态调整的，由 MaxGCPauseMillis 目标驱动</li>
 * </ul>
 *
 * @author dominiczhu
 */
public class YoungGCDemo {

    // 点击 Modify options → 勾选 Add VM options，输入框才会出现
    public static void main(String[] args) throws InterruptedException {
        long pid = ProcessHandle.current().pid();
        System.out.println("=== Young GC Demo (G1) ===");
        System.out.println("PID: " + pid);
        System.out.println("jstat 命令: jstat -gc " + pid + " 1000");
        System.out.println("堆大小 256MB，G1 自动管理 Young 区大小");
        System.out.println("对象分布：90% 小对象 (128B~4KB) + 10% Humongous 大对象 (1MB)");
        System.out.println();
        Thread.sleep(10000);

        // 模拟真实业务场景：大量小对象 + 少量大对象，全部短生命周期
        for (int batch = 1; batch <= 10; batch++) {
            // 每批模拟 1000 个“HTTP 请求”产生的临时对象
            for (int i = 0; i < 1000; i++) {
                // === 小对象（占 90%）：正常在 Eden 区分配，Young GC 时回收 ===
                // 模拟请求头/参数解析
                byte[] requestBody = new byte[256 + (i % 512)];  // 256B ~ 768B
                // 模拟 JSON 序列化结果
                String json = "{\"userId\":" + i + ",\"action\":\"query\",\"ts\":" + System.nanoTime() + "}";
                // 模拟业务处理中间结果
                Map<String, Object> context = new HashMap<>(16);
                context.put("id", i);
                context.put("data", requestBody);
                context.put("result", json);

                // === 大对象（占 10%）：Humongous 对象，直接进入 Old 区的 Humongous Region ===
                if (i % 10 == 0) {
                    // 模拟文件上传/大报文响应
                    byte[] largePayload = new byte[1024 * 1024]; // 1MB，超过 Region 50%，是 Humongous
                    largePayload[0] = (byte) i; // 防止被 JIT 优化掉
                }
            }

            System.out.println("第 " + batch + " 批处理完成（1000 个请求），等待观察 GC...");
            Thread.sleep(500);
        }

        System.out.println();
        System.out.println("分配完成，共处理 10000 个模拟请求");
        System.out.println("  - 小对象：~9000 个 (byte[256~768], String, HashMap)");
        System.out.println("  - Humongous 大对象：~1000 个 (byte[1MB])");
        System.out.println("请使用 jstat -gc " + pid + " 1000 查看 YGC 次数");
        System.out.println("请查看 ./gc-young.log 分析 Young GC 日志");

        // 保持进程存活，方便 jstat 连接
        Thread.sleep(30_000);
    }
}
