package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseTakenOfflineAppLipSolOne1EmailDTOGeneratorTest {

    public static final String CASE_TAKEN_OFFLINE_APPLICANT_NOTIFICATION = "case-taken-offline-applicant-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CaseTakenOfflineAppLipSolOneEmailDTOGenerator generator;

    @Test
    void shouldUseEnglishTemplate() {
        CaseData data = CaseData.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .applicant1Represented(YesOrNo.YES)
                .applicant1ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

        when(notificationsProperties.getClaimantLipClaimUpdatedTemplate())
                .thenReturn("no-response-id");

        try (MockedStatic<MultiPartyScenario> mocked = mockStatic(MultiPartyScenario.class)) {
            mocked.when(() -> MultiPartyScenario.getMultiPartyScenario(data))
                    .thenReturn(MultiPartyScenario.ONE_V_ONE);

            String templateId = generator.getEmailTemplateId(data);
            assertThat(templateId).isEqualTo("no-response-id");
        }
    }

    @Test
    void shouldUseWelshTemplate() {
        CaseData data = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1Represented(YesOrNo.YES)
            .claimantBilingualLanguagePreference(Language.WELSH.toString())
            .applicant1ResponseDeadline(LocalDateTime.now().plusDays(1))
            .build();

        when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate())
            .thenReturn("no-response-id");

        try (MockedStatic<MultiPartyScenario> mocked = mockStatic(MultiPartyScenario.class)) {
            mocked.when(() -> MultiPartyScenario.getMultiPartyScenario(data))
                .thenReturn(MultiPartyScenario.ONE_V_ONE);

            String templateId = generator.getEmailTemplateId(data);
            assertThat(templateId).isEqualTo("no-response-id");
        }
    }

}
