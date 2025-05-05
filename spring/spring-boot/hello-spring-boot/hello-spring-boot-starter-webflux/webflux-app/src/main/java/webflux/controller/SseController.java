package webflux.controller;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(path = "/sse")
public class SseController {

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamEvents() {
          AtomicInteger count = new AtomicInteger(1);

        Flux<ServerSentEvent<String>> textStream = Flux.<String>create(fluxSink -> {
                    while (count.get() % 5 != 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        fluxSink.next("some string" + count.get());
                        count.incrementAndGet();
                    }
                    fluxSink.next("done");
                    fluxSink.complete();
                })
                .map(seq -> ServerSentEvent.<String>builder()
                        .data(seq)
                        .build());

        // 模拟chatgpt一次输出一些话
        return textStream;
    }
}