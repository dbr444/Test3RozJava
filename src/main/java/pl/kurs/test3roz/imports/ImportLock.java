package pl.kurs.test3roz.imports;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class ImportLock {
    private final AtomicBoolean lock = new AtomicBoolean(false);

    public boolean tryLock() {
        return lock.compareAndSet(false, true);
    }

    public void unlock() {
        lock.set(false);
    }

    public boolean isLocked() {
        return lock.get();
    }
}
