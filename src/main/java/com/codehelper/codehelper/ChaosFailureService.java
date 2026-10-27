
package com.codehelper.codehelper;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ChaosFailureService {

    private final AtomicInteger failuresToInject = new AtomicInteger(0);
    private final AtomicInteger rateLimitErrorsToInject = new AtomicInteger(0);

    private volatile boolean selfHealingReady = false;

    public synchronized int arm(int count) {
        int safeCount = Math.max(0, Math.min(count, 3));
        rateLimitErrorsToInject.set(0);
        failuresToInject.set(safeCount);
        return safeCount;
    }

    public synchronized int armRateLimit(int count) {
        int safeCount = Math.max(0, Math.min(count, 3));
        failuresToInject.set(0);
        rateLimitErrorsToInject.set(safeCount);
        return safeCount;
    }

    public boolean consumeFailure() {
        return consumeCounter(failuresToInject);
    }

    public boolean consumeRateLimitError() {
        return consumeCounter(rateLimitErrorsToInject);
    }

    public boolean isSelfHealingReady() {
        return selfHealingReady;
    }

    public void setSelfHealingReady(boolean ready) {
        selfHealingReady = ready;
    }

    private boolean consumeCounter(AtomicInteger counter) {
        while (true) {
            int remaining = counter.get();

            if (remaining <= 0) {
                return false;
            }

            if (counter.compareAndSet(remaining, remaining - 1)) {
                return true;
            }
        }
    }
}
