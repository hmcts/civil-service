package uk.gov.hmcts.reform.civil.scheduler.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "scheduler")
public class DefaultBackPressureConfiguration {

    private ScheduledTaskBackPressureConfiguration defaultBackPressure;
}
