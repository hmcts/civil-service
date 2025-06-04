package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;

public class CaseProceedsInCasemanClaimantEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

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
    void shouldNotNotifyWhenNotLipVLipOneVOne() {
        CaseData caseData = CaseData.builder().applicant1Represented(YES).respondent1Represented(YES).build();
        boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldNotNotifyWhenLipVLipNotEnabled() {
        CaseData caseData = CaseData.builder().applicant1Represented(NO).respondent1Represented(NO).build();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isFalse();
    }

    @Test
    void shouldNotifyWhenLipVLipOneVOneAndLipVLipEnabled() {
        CaseData caseData = CaseData.builder().applicant1Represented(NO).respondent1Represented(NO).build();
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
        boolean shouldNotify = emailDTOGenerator.getShouldNotify(caseData);

        assertThat(shouldNotify).isTrue();
    }
}
