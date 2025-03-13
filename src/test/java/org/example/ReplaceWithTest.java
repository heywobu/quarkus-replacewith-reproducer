package org.example;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static io.restassured.RestAssured.given;
import static io.smallrye.mutiny.helpers.spies.Spy.onFailure;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ReplaceWithTest {
    private static final Logger log = Logger.getLogger(ReplaceWithTest.class);

    @Test
    void testHelloEndpoint() {
        awaitWithDeadline(stepOne()
                .onFailure().invoke(throwable -> log.error("Error"))
                .replaceWith(stepTwo())
                .replaceWith(stepThree())
        );
    }

    void awaitWithDeadline(Uni<Void> uni) {
        uni.await()
                .atMost(Duration.ofSeconds(10));
    }

    Uni<Void> stepOne() {
        CompletableFuture<Void> future = getVoidCompletableFuture();
        log.info("1");
        return Uni.createFrom().completionStage(future);
    }

    public CompletableFuture<Void> getVoidCompletableFuture() {
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            Thread.sleep(5000);
            log.info("1.1");
            completableFuture.complete(null);
            return null;
        });

        return completableFuture;
    }

    Uni<Void> stepTwo() {
        log.infof("2");
        return Uni.createFrom().voidItem();
    }
    Uni<Void> stepThree() {
        log.infof("3");
        return Uni.createFrom().voidItem();
    }

}