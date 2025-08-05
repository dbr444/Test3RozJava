package pl.kurs.test3roz.imports;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

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
        return lock.tryLock();
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
