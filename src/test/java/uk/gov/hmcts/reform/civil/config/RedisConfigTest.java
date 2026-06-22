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
            "localhost",
            6379,
            "",
            false,
            Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
        assertThat(factory.getStandaloneConfiguration().getHostName()).isEqualTo("localhost");
        assertThat(factory.getStandaloneConfiguration().getPort()).isEqualTo(6379);
    }

    @Test
    void shouldCreateRedisConnectionFactory_withCustomHost() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "redis.prod.example.com",
            6379,
            "",
            false,
            Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
        assertThat(factory.getStandaloneConfiguration().getHostName()).isEqualTo("redis.prod.example.com");
    }

    @Test
    void shouldCreateRedisConnectionFactory_withCustomPort() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost",
            6380,
            "",
            false,
            Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
        assertThat(factory.getStandaloneConfiguration().getPort()).isEqualTo(6380);
    }

    @Test
    void shouldCreateRedisConnectionFactory_withPassword() {
        String password = "secure-password-123";
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost",
            6379,
            password,
            false,
            Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
        assertThat(factory.getStandaloneConfiguration().getPassword()).isNotNull();
    }

    @Test
    void shouldCreateRedisConnectionFactory_withoutPassword_whenEmpty() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost",
            6379,
            "",
            false,
            Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
    }

    @Test
    void shouldCreateRedisConnectionFactory_withoutPassword_whenNull() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost",
            6379,
            null,
            false,
            Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
    }

    @Test
    void shouldCreateRedisConnectionFactory_withSSL_enabled() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost",
            6379,
            "",
            true,
            Duration.ofMillis(2000)
        );

        assertThat(factory).isNotNull();
    }

    @Test
    void shouldCreateRedisConnectionFactory_withCustomTimeout() {
        Duration customTimeout = Duration.ofMillis(5000);
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
            "localhost",
            6379,
            "",
            false,
            customTimeout
        );

        assertThat(factory).isNotNull();
    }

    @Test
    void shouldCreateStringRedisTemplate_withConnectionFactory() {
        LettuceConnectionFactory connectionFactory = redisConfig.redisConnectionFactory(
            "localhost",
            6379,
            "",
            false,
            Duration.ofMillis(2000)
        );

        StringRedisTemplate template = redisConfig.stringRedisTemplate(connectionFactory);

        assertThat(template).isNotNull();
        assertThat(template.getConnectionFactory()).isEqualTo(connectionFactory);
    }

    @Test
    void shouldCreateStringRedisTemplate() {
        LettuceConnectionFactory connectionFactory = redisConfig.redisConnectionFactory(
            "localhost",
            6379,
            "",
            false,
            Duration.ofMillis(2000)
        );

        StringRedisTemplate template = redisConfig.stringRedisTemplate(connectionFactory);

        assertThat(template).isNotNull();
    }

    @Test
    void shouldHandleAllConfigurationCombinations() {
        // Test various combinations of parameters
        String[] hosts = {"localhost", "redis.example.com"};
        int[] ports = {6379, 6380, 6381};
        String[] passwords = {"", null, "secure-pass"};
        boolean[] sslFlags = {true, false};

        for (String host : hosts) {
            for (int port : ports) {
                for (String password : passwords) {
                    for (boolean ssl : sslFlags) {
                        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory(
                            host,
                            port,
                            password,
                            ssl,
                            Duration.ofMillis(2000)
                        );

                        assertThat(factory).isNotNull();
                        assertThat(factory.getStandaloneConfiguration().getHostName()).isEqualTo(host);
                        assertThat(factory.getStandaloneConfiguration().getPort()).isEqualTo(port);
                    }
                }
            }
        }
    }
}
