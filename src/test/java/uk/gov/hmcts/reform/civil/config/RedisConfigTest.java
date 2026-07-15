package uk.gov.hmcts.reform.civil.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisConfig Tests")
class RedisConfigTest {

    @InjectMocks
    private RedisConfig redisConfig;

    @Test
    void shouldCreateRedisConnectionFactory_withDefaultValues() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost", 6379, "", false, Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
        assertThat(factory.getStandaloneConfiguration().getHostName()).isEqualTo("localhost");
        assertThat(factory.getStandaloneConfiguration().getPort()).isEqualTo(6379);
    }

    @Test
    void shouldCreateRedisConnectionFactory_withCustomHostAndPort() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "redis.prod.example.com", 10000, "", false, Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
        assertThat(factory.getStandaloneConfiguration().getHostName()).isEqualTo("redis.prod.example.com");
        assertThat(factory.getStandaloneConfiguration().getPort()).isEqualTo(10000);
    }

    @Test
    void shouldCreateRedisConnectionFactory_withPassword() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost", 6379, "secure-password-123", false, Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
        assertThat(factory.getStandaloneConfiguration().getPassword()).isNotNull();
    }

    @Test
    void shouldCreateRedisConnectionFactory_withoutPassword_whenEmpty() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost", 6379, "", false, Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
    }

    @Test
    void shouldCreateRedisConnectionFactory_withoutPassword_whenNull() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost", 6379, null, false, Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
    }

    @Test
    void shouldCreateRedisConnectionFactory_withSSL_enabled() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost", 6379, "", true, Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
    }

    @Test
    void shouldCreateRedisConnectionFactory_withCustomTimeout() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost", 6379, "", false, Duration.ofMillis(5000)
        );

        assertThat(factory).isNotNull();
    }

    @Test
    void shouldCreateStringRedisTemplate_withConnectionFactory() {
        LettuceConnectionFactory connectionFactory = redisConfig.redisConnectionFactory(
            "localhost", 6379, "", false, Duration.ofMillis(2000)
        );

        StringRedisTemplate template = redisConfig.stringRedisTemplate(connectionFactory);

        assertThat(template).isNotNull();
        assertThat(template.getConnectionFactory()).isEqualTo(connectionFactory);
    }
}
