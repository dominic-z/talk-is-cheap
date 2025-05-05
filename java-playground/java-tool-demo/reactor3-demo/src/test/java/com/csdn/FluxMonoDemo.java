package com.csdn;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author dominiczhu
 * @date 2020/9/3 8:51 上午
 */
public class FluxMonoDemo {

    @Test
    public void FluxMono() {
        Flux.just(1, 2, 3, 4, 5, 6).subscribe(System.out::print);
        System.out.println();
        Mono.just(1).subscribe(System.out::println);
        Flux.just(1, 2, 3, 4, 5, 6).subscribe(
                System.out::println,
                System.err::println,
                () -> System.out.println("Completed!"));

        Mono.error(new Exception("some error")).subscribe(
                System.out::println,
                System.err::println,
                () -> System.out.println("Completed!")
        );
    }


    private Flux<Integer> generateFluxFrom1To6() {
        return Flux.just(1, 2, 3, 4, 5, 6);
    }

    private Mono<Integer> generateMonoWithError() {
        return Mono.error(new Exception("some error"));
    }

    @Test
    public void testViaStepVerifier() {
        StepVerifier.create(generateFluxFrom1To6())
                .expectNext(1, 2, 3, 4, 5, 6)
                .expectComplete()
                .verify();
        StepVerifier.create(generateMonoWithError())
                .expectErrorMessage("some error")
                .verify();

        StepVerifier.create(generateMonoWithError())
                .expectErrorMessage("some error1")
                .verify();
    }


    @Test
    public void subscribe() {
//        Flux<Integer> ints = Flux.range(1, 4)
//                .map(i -> {
//                    if (i <= 3) return i;
//                    throw new RuntimeException("Got to 4");
//                });
//        ints.subscribe(i -> System.out.println(i),
//                error -> System.err.println("Error: " + error));


        class SampleSubscriber<T> extends BaseSubscriber<T> {

            public void hookOnSubscribe(Subscription subscription) {
                System.out.println("Subscribed");
                request(1);
            }

            public void hookOnNext(T value) {
                System.out.println("hook: " + value);
                // 请求1个元素，个人理解类似于next
                request(1);
            }

            @Override
            protected void hookOnComplete() {
                System.out.println("done");
            }
        }


        SampleSubscriber<Integer> ss = new SampleSubscriber<Integer>();
        Flux<Integer> ints = Flux.range(1, 4);
//
//        Because users tend to forget to request the subsciption. If the behavior is really needed, consider using subscribeWith
//        (Subscriber). To be removed in 3.5
//        ints.subscribe(i -> System.out.println(i),
//                error -> System.err.println("Error " + error),
//                () -> {System.out.println("Done");},
//                s -> ss.request(10));
        // 自定义一个subscriber
        ints.subscribeWith(ss);

    }

    /**
     * 创建一个数据流，通过叫做 sink（池） 的事件来创建一个数据流里的每个元素，以及控制数据流的终结
     */
    @Test
    public void makeSeq() {
        // generate
        Flux<String> flux = Flux.generate(
                () -> 0,
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3 * state);
                    if (state == 10) sink.complete();
                    return state + 1;
                });

        // create
        // 这个完全是
        abstract class EventListener<T> {
            abstract void onDataChunk(List<T> chunk);

            abstract void processComplete();
        }

        class EventProcessor<T> {
            EventListener<T> eventListener;

            public void register(EventListener<T> eventListener) {
                this.eventListener = eventListener;
            }

            public void sendData(List<T> data) {
                System.out.println(data);
                eventListener.onDataChunk(data);
            }

            public void complete() {
                System.out.println("complete");
                eventListener.processComplete();
            }

            public List<String> request(long n) {
                // 假设每次请求一个数据
                return List.of("done");
            }
        }

        EventProcessor<String> stringEventProcessor = new EventProcessor<>();
        Flux<Object> objectFlux = Flux.create(fluxSink -> {
            stringEventProcessor.register(
                    new EventListener<String>() {
                        // 这种方法对应着上游主动推送数据到下游
                        public void onDataChunk(List<String> chunk) {
                            for (String s : chunk) {
                                fluxSink.next(s);
                            }
                        }

                        public void processComplete() {
                            fluxSink.complete();
                        }
                    }
            );

            // 当订阅者主动请求数据的时候，上游通过这里的方法提供数据，对应着下游拉取数据
            fluxSink.onRequest(n -> {
                List<String> messages = stringEventProcessor.request(n);
                for (String s : messages) {
                    fluxSink.next(s);
                }
            });
        });
        // 需要先subsribe再sendData，否则create传入的lambda方法并不会执行，这是因为只有订阅了Flux流才会生成。先有消费者，生产者才会开始干活。
        // 这个例子模拟了通过自定义的senddata来创建一个流
        objectFlux.subscribeWith(new BaseSubscriber<Object>() {
            @Override
            protected void hookOnNext(Object value) {
                System.out.println(value);
                if (!"done".equals(value)) {
                    this.request(1);
                }
            }

            @Override
            protected void hookOnComplete() {
                System.out.println("complete");
            }
        });
        stringEventProcessor.sendData(List.of("push1", "push2", "push3"));
        stringEventProcessor.complete();



        Flux<Character> alphabet = Flux.just(-1, 30, 13, 9, 20)
                .handle((i, sink) -> {
                    Character letter = null;
                    if (i >=1 && i <= 26) {
                        letter = (char)('A' + i - 1);

                    }
                    if (letter != null)
                        sink.next(letter);
                });

        alphabet.subscribe(System.out::println);

    }


    @Test
    public void testOverflowStrategy() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final int[] a = {0};
        Flux.push(t -> {
                    for (int i = 0; i < 10; i++) {
                        t.next(a[0]++);
                        try {
                            TimeUnit.MICROSECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("generate thread:" + Thread.currentThread().getName());
                    t.complete();
                }, FluxSink.OverflowStrategy.ERROR)
                .publishOn(Schedulers.newSingle("publish-thread-"), 1)
                .subscribeOn(Schedulers.newSingle("subscribe-thread-"))
                .subscribe(new BaseSubscriber<Object>() {
                    private Subscription subscription = null;

                    @Override
                    public void hookOnSubscribe(Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(1);
                    }

                    @Override
                    public void hookOnNext(Object o) {
                        System.out.println(Thread.currentThread().getName() + ":消费数据:" + o);
                        try {
                            TimeUnit.MICROSECONDS.sleep(30);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        this.subscription.request(1);
                    }

                    @Override
                    public void hookOnError(Throwable throwable) {
                        System.out.println("出现错误");
                        throwable.printStackTrace();
                        countDownLatch.countDown();
                    }

                    @Override
                    public void hookOnComplete() {
                        System.out.println("Complete");
                        countDownLatch.countDown();
                    }
                });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
