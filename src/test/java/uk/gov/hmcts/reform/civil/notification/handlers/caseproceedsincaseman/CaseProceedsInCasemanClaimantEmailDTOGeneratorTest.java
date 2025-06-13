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
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;

public class CaseProceedsInCasemanClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CaseProceedsInCasemanClaimantEmailDTOGenerator emailDTOGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenBilingual() {
        CaseData caseData = CaseData.builder().claimantBilingualLanguagePreference(BOTH.toString()).build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenNotBilingual() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantLipClaimUpdatedTemplate()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("case-proceeds-in-caseman-applicant-notification-%s");
    }

    @Test
    void shouldNotNotifyWhenNotLipvLROneVOne() {
        CaseData caseData = CaseData.builder().applicant1Represented(NO).respondent1Represented(NO).build();
        boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldNotifyWhenLipvLROneVOne() {
        CaseData caseData = CaseData.builder().applicant1Represented(NO).respondent1Represented(YES).build();
        boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isTrue();
    }
}
