package uk.gov.hmcts.reform.civil.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.hmcts.reform.civil.config.properties.AsyncHandlerProperties;
import uk.gov.hmcts.reform.civil.request.RequestData;
import uk.gov.hmcts.reform.civil.request.RequestDataCache;
import uk.gov.hmcts.reform.civil.request.SimpleRequestData;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncHandlerConfiguration implements AsyncConfigurer {

    private final ApplicationContext context;

    @Bean(name = "asyncHandlerExecutor")
    public Executor asyncHandlerExecutor(AsyncHandlerProperties props) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(props.getCorePoolSize());
        executor.setMaxPoolSize(props.getMaxPoolSize());
        executor.setQueueCapacity(props.getQueueCapacity());
        executor.setThreadNamePrefix("HandlerExecutor-");
        executor.setTaskDecorator(new AsyncTaskDecorator(context));
        executor.initialize();
        return executor;
    }

    static class AsyncTaskDecorator implements TaskDecorator {

        private final ApplicationContext context;

        AsyncTaskDecorator(ApplicationContext context) {
            this.context = context;
        }

        @Override
        public Runnable decorate(@Nonnull Runnable task) {
            return () -> {
                try {
                    SimpleRequestData requestData = new SimpleRequestData(context.getBean(RequestData.class));
                    RequestDataCache.add(requestData);
                } catch (IllegalStateException e) {
                    log.debug("Request data not provided. Skipping adding to request cache.");
                }

                try {
                    task.run();
                } finally {
                    RequestDataCache.remove();
                }
            };
        }
    }

}
