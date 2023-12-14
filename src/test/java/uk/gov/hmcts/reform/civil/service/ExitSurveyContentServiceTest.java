package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ExitSurveyContentService.class,
    ExitSurveyConfiguration.class
    })
class ExitSurveyContentServiceTest {

    private static final String feedbackLink = "%n%n<br/><br/>This is a new service - your <a href=\"%s\" target=\"_blank\">feedback</a> will help us to improve it.";

    @Autowired
    ExitSurveyContentService exitSurveyContentService;

    @Test
    void shouldReturnApplicantSurveyContents_whenInvoked() {
        assertThat(exitSurveyContentService.applicantSurvey()).isEqualTo(String.format(feedbackLink,
                                                                                       "https://www.smartsurvey.co.uk/s/CivilDamages_ExitSurvey_Claimant/"));
    }

    @Test
    void shouldReturnRespondentSurveyContents_whenInvoked() {
        assertThat(exitSurveyContentService.respondentSurvey()).isEqualTo(String.format(feedbackLink,
                                                                                        "https://www.smartsurvey.co.uk/s/CivilDamages_ExitSurvey_Defendant/"));
    }
}
