package uk.gov.hmcts.reform.civil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.civil.config.properties.AsyncHandlerProperties;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
@EnableAsync
public class AsyncHandlerConfiguration implements AsyncConfigurer {

    @Bean(name = "asyncHandlerRejectedCount")
    public AtomicLong asyncHandlerRejectedCount() {
        return new AtomicLong(0);
    }

    @Bean(name = "asyncHandlerExecutor")
    public ThreadPoolTaskExecutor asyncHandlerExecutor(AsyncHandlerProperties props, AtomicLong asyncHandlerRejectedCount) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(props.getCorePoolSize());
        executor.setMaxPoolSize(props.getMaxPoolSize());
        executor.setQueueCapacity(props.getQueueCapacity());
        executor.setThreadNamePrefix("HandlerExecutor-");

        executor.setRejectedExecutionHandler((r, executor1) -> {
            asyncHandlerRejectedCount.incrementAndGet();
            throw new java.util.concurrent.RejectedExecutionException("Task " + r.toString()
                + " rejected from " + executor1.toString());
        });

        executor.initialize();
        return executor;
    }
}
