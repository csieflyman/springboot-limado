import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import util.RedisUtils;

import javax.annotation.PostConstruct;

/**
 * @author csieflyman
 */
@Configuration
public class StaticInjectionConfig {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    private void init() {
        RedisUtils.setRedisTemplate(redisTemplate);
    }

}
