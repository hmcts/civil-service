package uk.gov.hmcts.reform.civil.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan("uk.gov.hmcts.reform.civil")
public class AopConfig {

}
