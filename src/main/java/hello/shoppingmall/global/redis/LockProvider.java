package hello.shoppingmall.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LockProvider {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean tryLock(String key, long timeout) {
        return redisTemplate.opsForValue().setIfAbsent(key, "locked", timeout, TimeUnit.MILLISECONDS);
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
    }

    public String makeLockKey(String target, Long id) {
        return String.format("lock-%s:%s", target, id);
    }
}
