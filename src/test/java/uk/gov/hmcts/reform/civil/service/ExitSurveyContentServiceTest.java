package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExitSurveyContentServiceTest {

    private static final String FEEDBACK_LINK_TEMPLATE = "%n%n<br/><br/>This is a new service - your <a href=\"%s\" target=\"_blank\">feedback</a> will help us to improve it.";

    @InjectMocks
    private ExitSurveyContentService exitSurveyContentService;

    @Mock
    private ExitSurveyConfiguration exitSurveyConfiguration;

    @BeforeEach
    void setup() {
        exitSurveyContentService = new ExitSurveyContentService(exitSurveyConfiguration);
    }

    @Test
    void shouldReturnApplicantSurveyContents_whenInvoked() {
        when(exitSurveyConfiguration.getApplicantLink())
            .thenReturn("https://www.smartsurvey.co.uk/s/CivilDamages_ExitSurvey_Claimant/");

        assertThat(exitSurveyContentService.applicantSurvey()).isEqualTo(String.format(FEEDBACK_LINK_TEMPLATE,
                                                                                       "https://www.smartsurvey.co.uk/s/CivilDamages_ExitSurvey_Claimant/"));
    }

    @Test
    void shouldReturnRespondentSurveyContents_whenInvoked() {
        when(exitSurveyConfiguration.getRespondentLink())
            .thenReturn("https://www.smartsurvey.co.uk/s/CivilDamages_ExitSurvey_Defendant/");

        assertThat(exitSurveyContentService.respondentSurvey()).isEqualTo(String.format(FEEDBACK_LINK_TEMPLATE,
                                                                                        "https://www.smartsurvey.co.uk/s/CivilDamages_ExitSurvey_Defendant/"));
    }
}
