package uk.gov.hmcts.reform.civil.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.civil.config.properties.AsyncHandlerProperties;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncHandlerConfiguration implements AsyncConfigurer {

    @Bean(name = "asyncHandlerExecutor")
    public Executor asyncHandlerExecutor(AsyncHandlerProperties props) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(props.getCorePoolSize());
        executor.setMaxPoolSize(props.getMaxPoolSize());
        executor.setQueueCapacity(props.getQueueCapacity());
        executor.setThreadNamePrefix("HandlerExecutor-");
        executor.initialize();
        return executor;
    }

}
