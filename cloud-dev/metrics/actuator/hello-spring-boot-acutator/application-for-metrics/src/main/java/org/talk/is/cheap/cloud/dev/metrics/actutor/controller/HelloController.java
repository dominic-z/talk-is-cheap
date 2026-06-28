package org.talk.is.cheap.cloud.dev.metrics.actutor.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterFilter;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


// http://localhost:8080/actuator/metrics 查看全部的指标
@RestController
@RequestMapping("/hello")
public class HelloController {
    // 计数器，只能增加不能减少
    // http://localhost:8080/actuator/metrics/user.hello.counter.total
    static final Counter userCounter = Metrics.counter("user.hello.test.total", "services", "demo");


    // 一个记录瞬时值的指标
    // Metrics.gauge(String name, Number value) 这个静态便捷方法，只有在 Gauge 尚未注册时才会创建并设置初始值。一旦 Gauge 被注册，后续调用该方法会直接忽略传入的新数值。
    //它不是一个“更新 Gauge 值”的方法，而是一个“注册 Gauge”的方法。
    // http://localhost:8080/actuator/metrics/user.hello.test.gauge
    // http://localhost:8080/actuator/metrics/user.hello.test.gauge?tag=tag1K:tag1V
    static final AtomicInteger gaugeNum = Metrics.gauge("user.hello.test.gauge", List.of(Tag.of("tag1K", "tag1V")), new AtomicInteger(0),
            AtomicInteger::doubleValue);


//    生产上在生产环境中，不建议直接使用 Metrics.globalRegistry，而应该通过 Spring 的依赖注入（DI）使用 MeterRegistry Bean。
    @Autowired
    private MeterRegistry meterRegistry;
    private Timer timer;
    private DistributionSummary summary;

    @PostConstruct
    public void init(){
        // 计时器
        // http://localhost:8080/actuator/metrics/user.hello.test.timer
         timer = meterRegistry.timer("user.hello.test.timer", "timer", "timersample");
//    . DistributionSummary 分布汇总（无时间维度的数值分布）
//    含义:和 Timer 结构一致（count、sum、max、百分位），但不代表时间，只记录任意数字大小分布。
//Timer = 耗时专用；DistributionSummary = 通用数值分布。
//适用：请求体大小、返回报文大小、单次订单金额、文件上传字节数。
        // 查询的时候会返回一个累计值和次数，这样就可以求出平均值了
        // http://localhost:8080/actuator/metrics/user.hello.test.summary

//    百分位的这个结果只能从http://localhost:8080/actuator/prometheus看到
        summary = DistributionSummary.builder("user.hello.test.summary")
                .tag("summary", "summarysample")
                .publishPercentiles(0.5, 0.95, 0.99)  // 开启后 max 也会被追踪
                .publishPercentileHistogram(true) // 开启分桶，用于prometheus
                .distributionStatisticExpiry(Duration.ofMinutes(10))  // 设置窗口即可激活 max
                .register(meterRegistry);
    }






    // http://localhost:8080/hello/gauge?num=10
    @GetMapping("/gauge")
    public void gauge(@RequestParam(name = "num") Integer num) {
        gaugeNum.set(num);
    }


    // http://localhost:8080/hello/count
    @GetMapping("/count")
    public void count() {
        userCounter.increment();
    }


    @GetMapping("/timer")
    public Integer timer() {
        return timer.record(() -> {
            Random random = new Random();
            int timeout = random.nextInt(5);
            try {
                TimeUnit.SECONDS.sleep(timeout);
            } catch (InterruptedException e) {
            }
            return timeout;
        });
    }


    @GetMapping("/summary")
    public List<Integer> summary() {
        Random random = new Random();
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int size = random.nextInt(20);
            res.add(size);
            summary.record(size);
        }

        return res;
    }
}