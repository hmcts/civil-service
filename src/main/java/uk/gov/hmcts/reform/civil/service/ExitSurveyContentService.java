package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;

@Service
@RequiredArgsConstructor
public class ExitSurveyContentService {

    private final ExitSurveyConfiguration exitSurveyConfiguration;
    private static final String feedbackLink = "%n%n<br/><br/>This is a new service - your <a href=\"%s\" target=\"_blank\">feedback</a> will help us to improve it.";

    public String applicantSurvey() {
        return String.format(feedbackLink,
                             exitSurveyConfiguration.getApplicantLink());
    }

    public String respondentSurvey() {
        return String.format(feedbackLink,
                              exitSurveyConfiguration.getRespondentLink());
    }
}
