package org.example;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static io.restassured.RestAssured.given;
import static io.smallrye.mutiny.helpers.spies.Spy.onFailure;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ReplaceWithTest {
    private static final Logger log = Logger.getLogger(ReplaceWithTest.class);
    private final List<String> orders = new ArrayList<>();

    @Test
    void notWorking() {
        awaitWithDeadline(stepOne()
                .replaceWith(stepTwo())
                .replaceWith(stepThree())
        );
        log.info("orders: " + orders);
        Assertions.assertEquals(List.of("1", "1.1", "2", "3"), orders);
    }

    @Test
    void working() {
        awaitWithDeadline(stepOne()
                .call(this::stepTwo)
                .call(this::stepThree)
        );
        log.info("orders: " + orders);
        Assertions.assertEquals(List.of("1", "1.1", "2", "3"), orders);
    }

    void awaitWithDeadline(Uni<Void> uni) {
        uni.await()
                .atMost(Duration.ofSeconds(10));
    }

    Uni<Void> stepOne() {
        CompletableFuture<Void> future = getVoidCompletableFuture();
        orders.add("1");
        return Uni.createFrom().completionStage(future);
    }

    public CompletableFuture<Void> getVoidCompletableFuture() {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            Thread.sleep(5000);
            orders.add("1.1");
            completableFuture.complete(null);
            return null;
        });

        return completableFuture;
    }

    Uni<Void> stepTwo() {
        orders.add("2");
        return Uni.createFrom().voidItem();
    }
    Uni<Void> stepThree() {
        orders.add("3");
        return Uni.createFrom().voidItem();
    }
}