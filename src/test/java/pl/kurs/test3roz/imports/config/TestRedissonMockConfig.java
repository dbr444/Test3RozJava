package pl.kurs.test3roz.imports.config;

import org.mockito.Mockito;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestRedissonMockConfig {

    @Bean
    public RedissonClient redissonClient() {
        RLock mockLock = Mockito.mock(RLock.class);

        Mockito.when(mockLock.tryLock()).thenReturn(true);
        Mockito.when(mockLock.isLocked()).thenReturn(false);
        Mockito.when(mockLock.isHeldByCurrentThread()).thenReturn(true);

        @SuppressWarnings("unchecked")
        RBlockingQueue<Object> mockQueue = (RBlockingQueue<Object>) Mockito.mock(RBlockingQueue.class);

        RedissonClient client = Mockito.mock(RedissonClient.class);
        Mockito.when(client.getLock(Mockito.anyString())).thenReturn(mockLock);
        Mockito.when(client.getBlockingQueue(Mockito.anyString())).thenReturn(mockQueue);
        return client;
    }
}
