package uk.gov.hmcts.reform.unspec.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "exit-survey")
public class ExitSurveyConfiguration {

    @NotBlank
    private String applicantLink;
    @NotBlank
    private String respondentLink;
}
