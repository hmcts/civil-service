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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseTakenOfflineAppSolOne1EmailDTOGeneratorTest {

    public static final String CASE_TAKEN_OFFLINE_APPLICANT_NOTIFICATION = "case-taken-offline-applicant-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CaseTakenOfflineAppSolOneEmailDTOGenerator generator;

    @Test
    void shouldUseNoApplicantResponseTemplate_whenAllConditionsTrue_applicantYes() {
        CaseData data = CaseData.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.YES)
                .applicant1ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

        when(notificationsProperties.getSolicitorCaseTakenOfflineNoApplicantResponse())
                .thenReturn("no-response-id");

        try (MockedStatic<MultiPartyScenario> mocked = mockStatic(MultiPartyScenario.class)) {
            mocked.when(() -> MultiPartyScenario.getMultiPartyScenario(data))
                    .thenReturn(MultiPartyScenario.ONE_V_ONE);

            String templateId = generator.getEmailTemplateId(data);
            assertThat(templateId).isEqualTo("no-response-id");
        }
    }

    @Test
    void shouldUseNoApplicantResponseTemplate_whenApplicantRepresentedNull() {
        CaseData data = CaseData.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(null)
                .applicant1ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

        when(notificationsProperties.getSolicitorCaseTakenOfflineNoApplicantResponse())
                .thenReturn("no-response-null-applicant");

        try (MockedStatic<MultiPartyScenario> mocked = mockStatic(MultiPartyScenario.class)) {
            mocked.when(() -> MultiPartyScenario.getMultiPartyScenario(data))
                    .thenReturn(MultiPartyScenario.ONE_V_ONE);

            String templateId = generator.getEmailTemplateId(data);
            assertThat(templateId).isEqualTo("no-response-null-applicant");
        }
    }

    @Test
    void shouldUseDefaultTemplate_whenDeadlineMissing() {
        CaseData data = CaseData.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.YES)
                .applicant1ResponseDeadline(null)
                .build();

        when(notificationsProperties.getSolicitorCaseTakenOffline())
                .thenReturn("default-no-deadline");

        try (MockedStatic<MultiPartyScenario> mocked = mockStatic(MultiPartyScenario.class)) {
            mocked.when(() -> MultiPartyScenario.getMultiPartyScenario(data))
                    .thenReturn(MultiPartyScenario.ONE_V_ONE);

            String templateId = generator.getEmailTemplateId(data);
            assertThat(templateId).isEqualTo("default-no-deadline");
        }
    }

    @Test
    void shouldUseDefaultTemplate_whenRespondentNotRepresented() {
        CaseData data = CaseData.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.YES)
                .applicant1ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

        when(notificationsProperties.getSolicitorCaseTakenOffline())
                .thenReturn("default-respondent-no");

        try (MockedStatic<MultiPartyScenario> mocked = mockStatic(MultiPartyScenario.class)) {
            mocked.when(() -> MultiPartyScenario.getMultiPartyScenario(data))
                    .thenReturn(MultiPartyScenario.ONE_V_ONE);

            String templateId = generator.getEmailTemplateId(data);
            assertThat(templateId).isEqualTo("default-respondent-no");
        }
    }

    @Test
    void shouldUseDefaultTemplate_whenApplicantNotRepresented() {
        CaseData data = CaseData.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .applicant1ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

        when(notificationsProperties.getSolicitorCaseTakenOffline())
                .thenReturn("default-applicant-no");

        try (MockedStatic<MultiPartyScenario> mocked = mockStatic(MultiPartyScenario.class)) {
            mocked.when(() -> MultiPartyScenario.getMultiPartyScenario(data))
                    .thenReturn(MultiPartyScenario.ONE_V_ONE);

            String templateId = generator.getEmailTemplateId(data);
            assertThat(templateId).isEqualTo("default-applicant-no");
        }
    }

    @Test
    void shouldUseDefaultTemplate_whenScenarioNotOneVOne() {
        CaseData data = CaseData.builder()
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.YES)
                .applicant1ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

        when(notificationsProperties.getSolicitorCaseTakenOffline())
                .thenReturn("default-scenario-no");

        try (MockedStatic<MultiPartyScenario> mocked = mockStatic(MultiPartyScenario.class)) {
            mocked.when(() -> MultiPartyScenario.getMultiPartyScenario(data))
                    .thenReturn(MultiPartyScenario.TWO_V_ONE);

            String templateId = generator.getEmailTemplateId(data);
            assertThat(templateId).isEqualTo("default-scenario-no");
        }
    }

    @Test
    void shouldUseDefaultTemplate_whenCategoryNotSpecClaim() {
        CaseData data = CaseData.builder()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.YES)
                .applicant1ResponseDeadline(LocalDateTime.now().plusDays(1))
                .build();

        when(notificationsProperties.getSolicitorCaseTakenOffline())
                .thenReturn("default-category-no");

        try (MockedStatic<MultiPartyScenario> mocked = mockStatic(MultiPartyScenario.class)) {
            mocked.when(() -> MultiPartyScenario.getMultiPartyScenario(data))
                    .thenReturn(MultiPartyScenario.ONE_V_ONE);

            String templateId = generator.getEmailTemplateId(data);
            assertThat(templateId).isEqualTo("default-category-no");
        }
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
                .isEqualTo(CASE_TAKEN_OFFLINE_APPLICANT_NOTIFICATION);
    }
}
