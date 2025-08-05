package pl.kurs.test3roz.imports;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ImportLock {

    private final RedissonClient redissonClient;

    private final ImportProperties importProperties;

    private static final String LOCK_NAME = "import_lock";

    public boolean tryLock() {
        if (importProperties.isAllowParallelImports()) {
            return true;
        }
        RLock lock = redissonClient.getLock(LOCK_NAME);
        try {
            return lock.tryLock(5, -1, TimeUnit.SECONDS);//tutaj dorzucam bo znalazłem info, ze bez tego się automatycznie zwalnia lock po 30 sekundach, a teraz sprawdza i zwolni dopiero po zakończeniu importu
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock() {
        if (!importProperties.isAllowParallelImports()) {
            RLock lock = redissonClient.getLock(LOCK_NAME);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public boolean isLocked() {
        if (importProperties.isAllowParallelImports()) {
            return false;
        }
        RLock lock = redissonClient.getLock(LOCK_NAME);
        return lock.isLocked();
    }
}
