package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

public class CaseProceedsInCasemanRespSolOneEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CaseProceedsInCasemanRespSolOneEmailDTOGenerator emailDTOGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenIsNotLipvLROneVOne() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenIsLipvLROneVOne() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(NO)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getSolicitorCaseTakenOfflineForSpec()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("case-proceeds-in-caseman-respondent-notification-%s");
    }
}
