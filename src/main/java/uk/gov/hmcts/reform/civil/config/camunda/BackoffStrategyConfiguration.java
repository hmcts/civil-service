package uk.gov.hmcts.reform.civil.config.camunda;

import org.camunda.bpm.client.backoff.BackoffStrategy;
import org.camunda.bpm.client.task.ExternalTask;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BackoffStrategyConfiguration implements BackoffStrategy {

    @Override
    public void reconfigure(List<ExternalTask> externalTasks) {
    }

    @Override
    public long calculateBackoffTime() {
        return 0;
    }
}
