package pl.kurs.securityconfig;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int BLOCK_MINUTES = 10;

    private record AttemptInfo(int count, LocalDateTime firstFailureTime, LocalDateTime blockedUntil) {}

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        AttemptInfo info = attempts.get(username);
        return info != null && info.blockedUntil() != null && info.blockedUntil().isAfter(LocalDateTime.now());
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    public void loginFailed(String username) {
        AttemptInfo current = attempts.get(username);
        LocalDateTime now = LocalDateTime.now();

        if (current == null || current.blockedUntil != null && now.isAfter(current.blockedUntil)) {
            attempts.put(username, new AttemptInfo(1, now, null));
        } else {
            int newCount = current.count + 1;
            if (newCount >= MAX_ATTEMPTS && now.isBefore(current.firstFailureTime.plusMinutes(5))) {
                attempts.put(username, new AttemptInfo(newCount, current.firstFailureTime, now.plusMinutes(BLOCK_MINUTES)));
            } else {
                attempts.put(username, new AttemptInfo(newCount, current.firstFailureTime, null));
            }
        }
    }
}
