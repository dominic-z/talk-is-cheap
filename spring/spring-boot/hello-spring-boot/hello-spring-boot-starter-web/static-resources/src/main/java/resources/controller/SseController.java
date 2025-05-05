package resources.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(path = "/sse")
public class SseController {

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Integer>> streamEvents() {
        AtomicInteger counter = new AtomicInteger(0);
        return Flux.interval(Duration.ofSeconds(1))
                .map(seq -> ServerSentEvent.<Integer>builder()
                        .data(counter.incrementAndGet())
                        .build());
    }
}