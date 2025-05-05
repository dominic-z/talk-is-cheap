package resources.controller;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/async")
@Slf4j
public class AsyncController implements ApplicationContextAware {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping(value = "/deferred")
    public DeferredResult<String> handleDeferredRequest() {
        // 创建一个 DeferredResult 对象，设置超时时间为 5 秒
        DeferredResult<String> deferredResult = new DeferredResult<>(5000L);

        // 模拟异步处理
        executorService.submit(() -> {
            try {
                log.info("deffer begin to wait");
                // 模拟耗时操作
                Thread.sleep(3000);
                // 设置结果
                deferredResult.setResult("Async operation completed successfully!");
            } catch (InterruptedException e) {
                // 处理异常，设置错误结果
                deferredResult.setErrorResult("An error occurred during async operation.");
            }
        });

        // 处理超时情况
        deferredResult.onTimeout(() -> {
            deferredResult.setErrorResult("Request timed out.");
        });

        return deferredResult;
    }

    @GetMapping(value = "/async")
    public CompletableFuture<String> asyncRequest() throws InterruptedException {
        // 获取代理类用于异步
        return ac.getBean(this.getClass()).asyncMethod();
    }

    @Async("asyncExecutor")
    public CompletableFuture<String> asyncMethod() throws InterruptedException {
        Thread.sleep(3000); // 模拟耗时操作
        return CompletableFuture.completedFuture("Async method result");
    }

    private ApplicationContext ac;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        log.info("set ac");
        ac = applicationContext;
    }
}
