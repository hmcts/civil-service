package uk.gov.hmcts.reform.unspec.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan("uk.gov.hmcts.reform.unspec")
public class AopConfig {

}
