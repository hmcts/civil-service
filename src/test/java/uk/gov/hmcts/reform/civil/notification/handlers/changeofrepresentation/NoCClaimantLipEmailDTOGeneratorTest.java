package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class NoCClaimantLipEmailDTOGeneratorTest {
    private static final String ENGLISH_TEMPLATE_ID = "english-template-id";
    private static final String BILINGUAL_TEMPLATE_ID = "bilingual-template-id";
    private static final String CLAIMANT_EMAIL = "claimant@example.com";
    private static final String CASE_REFERENCE = "1234567890";

    private NotificationsProperties notificationsProperties;
    private NoCHelper noCHelper;
    private NoCClaimantLipEmailDTOGenerator generator;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        noCHelper = mock(NoCHelper.class);
        generator = new NoCClaimantLipEmailDTOGenerator(notificationsProperties, noCHelper);

        caseData = mock(CaseData.class);
    }

    @Test
    void shouldReturnTrueWhenIsAppliantLipForRespondentSolicitorChange() {
        mockStatic(NocNotificationUtils.class).when(() ->
                         NocNotificationUtils.isAppliantLipForRespondentSolicitorChange(caseData)
        ).thenReturn(true);

        assertThat(generator.getShouldNotify(caseData)).isTrue();
    }

    @Test
    void shouldReturnEmailDTOWithCorrectValuesWhenNonBilingual() {
        when(caseData.isClaimantBilingual()).thenReturn(false);
        when(caseData.getApplicant1Email()).thenReturn(CLAIMANT_EMAIL);
        when(notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate())
            .thenReturn(ENGLISH_TEMPLATE_ID);
        when(caseData.getLegacyCaseReference()).thenReturn(CASE_REFERENCE);
        when(noCHelper.getClaimantLipProperties(caseData)).thenReturn(Map.of("key1", "val1"));

        EmailDTO result = generator.buildEmailDTO(caseData);

        assertThat(result.getTargetEmail()).isEqualTo(CLAIMANT_EMAIL);
        assertThat(result.getEmailTemplate()).isEqualTo(ENGLISH_TEMPLATE_ID);
        assertThat(result.getReference()).isEqualTo("notice-of-change-" + CASE_REFERENCE);
        assertThat(result.getParameters()).containsEntry("key1", "val1");
    }

    @Test
    void shouldReturnEmailDTOWithCorrectValuesWhenBilingual() {
        when(caseData.isClaimantBilingual()).thenReturn(true);
        when(caseData.getApplicant1Email()).thenReturn(CLAIMANT_EMAIL);
        when(notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC())
            .thenReturn(BILINGUAL_TEMPLATE_ID);
        when(caseData.getLegacyCaseReference()).thenReturn(CASE_REFERENCE);
        when(noCHelper.getClaimantLipProperties(caseData)).thenReturn(Map.of("key2", "val2"));

        EmailDTO result = generator.buildEmailDTO(caseData);

        assertThat(result.getTargetEmail()).isEqualTo(CLAIMANT_EMAIL);
        assertThat(result.getEmailTemplate()).isEqualTo(BILINGUAL_TEMPLATE_ID);
        assertThat(result.getReference()).isEqualTo("notice-of-change-" + CASE_REFERENCE);
        assertThat(result.getParameters()).containsEntry("key2", "val2");
    }
}
