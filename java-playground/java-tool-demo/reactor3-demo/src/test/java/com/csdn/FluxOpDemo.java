package com.csdn;

import org.junit.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author dominiczhu
 * @date 2020/9/3 9:49 上午
 */
public class FluxOpDemo {
    private Flux<String> getZipDescFlux() {
        String desc = "Zip two sources together, that is to say wait for all the sources to emit one element and combine these elements once into a Tuple2.";
        return Flux.fromArray(desc.split("\\s+"));  // 1
    }

    @Test
    public void testOperator() throws InterruptedException {
        StepVerifier.create(Flux.range(1, 6)    // 1
                .map(i -> i * i))   // 2
                .expectNext(1, 4, 9, 16, 25, 36)    //3
                .expectComplete().verify();  // 4

        StepVerifier.create(
                Flux.just("flux", "mono")
                        .flatMap(s -> Flux.fromArray(s.split("\\s*"))   // 1 map的功能是对流里的每一个元素进行处理，而flatMap的功能是将每一个元素处理为一个Flux的流，然后把这些个flux拍扁拍成唯一一个流
                                .delayElements(Duration.ofMillis(100))) // 2
                        .doOnNext(System.out::print)) // 3
                .expectNextCount(8) // 4
                .verifyComplete();

        CountDownLatch countDownLatch = new CountDownLatch(1);  // 2
        Flux.zip(
                getZipDescFlux(),
                Flux.interval(Duration.ofMillis(200)))  // 3
                .subscribe(t -> System.out.println(t.getT1())
//                       , null, countDownLatch::countDown// 4
                );
        Thread.sleep(10000); //同样的效果，意思就是要维持当前主线程不关闭
//        countDownLatch.await(10, TimeUnit.SECONDS);     // 5 可以改成2看看

    }

    @Test
    public void testErrorHandling() throws InterruptedException {
        Flux.range(1, 6)
                .map(i -> 10 / (i - 3)) // 1
                .map(i -> i * i)
                .subscribe(System.out::println, System.err::println);

        Flux.range(1, 6)
                .map(i -> 10 / (i - 3))
                // 错误的时候返回缺省值
                .onErrorReturn(0)   // 1
                .map(i -> i * i)
                .subscribe(System.out::println, System.err::println);

        Flux.range(1, 6)
                .map(i -> 10 / (i - 3))   // 1
                // 捕获异常并重新抛出
                .onErrorMap(original -> new Exception("SLA exceeded", original)).subscribe(System.out::println, System.err::println); // 2

        LongAdder statsCancel = new LongAdder();    // 1
        Flux<String> flux =
                Flux.just("foo", "bar")
                        .doFinally(type -> {
                            if (type == SignalType.CANCEL)  // 2
                                statsCancel.increment();  // 3
                        })
                        .take(1);   // 4
        flux.subscribe();
        System.out.println("statsCancel.longValue(): " + statsCancel.longValue());

        Flux.range(1, 6)
                .map(i -> 10 / (3 - i))
                .retry(1)
                .subscribe(System.out::println, System.err::println);
        Thread.sleep(100);  // 确保序列执行完

    }

    @Test
    public void testBackpressure() {
        Flux.range(1, 6)    // 1
                .doOnRequest(n -> System.out.println("Request " + n + " values..."))    // 2
                .subscribe(new BaseSubscriber<Integer>() {  // 3
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) { // 4
                        System.out.println("Subscribed and make a request...");
                        request(1); // 5
                    }

                    @Override
                    protected void hookOnNext(Integer value) {  // 6
                        try {
                            TimeUnit.SECONDS.sleep(1);  // 7
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Get value [" + value + "]");    // 8
                        request(1); // 9
                    }
                });
    }


}
