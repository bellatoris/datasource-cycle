package com.doogie.datasourcecycle

import org.reactivestreams.Publisher
import org.reactivestreams.Subscription
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.retry.Retry
import java.time.Duration


@SpringBootApplication
class DatasourceCycleApplication {
    @Bean
    fun runApplicion(): ApplicationRunner {
        return ApplicationRunner {
            val a = Retry
                .anyOf<Unit>(RuntimeException::class.java)
                .retryMax(5)
                .timeout(Duration.ofHours(1)).doOnRetry { println("h") }

            val b = object : Retry<Unit> by a {
                override fun apply(t: Flux<Throwable>?): Publisher<Long> {
                    return Flux.from(a.apply(t)).doOnError {
                        println("hello")
                    }
                }
            }

            val hi = Flux.fromIterable(listOf(1, 2, 3, 4, 5))
                .concatMap {
                    if (it > 3) throw RuntimeException()
                    Flux.fromIterable(listOf(1, 2, 3, 4, 5))
                }
                .retryWhen(b)
                .limitRate(1)
                .limitRequest(10)
                .subscribeOn(Schedulers.elastic())

//            hi.subscribe({ println(it) }, { e -> println(e) }, { println("done") }, { sub -> sub.request(Long.MAX_VALUE) })
//            hi.subscribe(SimpleSubscriber)

        }
    }

    object SimpleSubscriber : BaseSubscriber<Int>() {
        override fun hookOnSubscribe(subscription: Subscription) {
            println("Subscribed")
            request(1)
        }

        override fun hookOnNext(value: Int) {
            System.out.println(value)
            request(1)
        }

        override fun hookOnError(throwable: Throwable) {
            println("haha")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<DatasourceCycleApplication>(*args)
}
