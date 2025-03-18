package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class EmailTemplateFooterConfiguration {
    private final String phone;
    private final String openingTime;
    private final String smartSurveyUrl;

    public EmailTemplateFooterConfiguration(
        @Value("${email-template.phone}") String phone,
        @Value("${email-template.openingTiming}") String openingTime,
        @Value("${email-template.smartSurveyUrl}") String smartSurveyUrl
    ) {
        this.phone = phone;
        this.openingTime = openingTime;
        this.smartSurveyUrl = smartSurveyUrl;
    }
}
