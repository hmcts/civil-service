package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class CaseTakenOfflineAppSolOneEmailDTOGeneratorTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @InjectMocks
    private CaseTakenOfflineAppSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenConditionsAreMet() {
        CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                //.multiPartyScenario(ONE_V_ONE)
                .respondent1Represented(YES)
                .applicant1Represented(YES)
                .applicant1ResponseDeadline(LocalDateTime.now())
                .build();

        when(notificationsProperties.getSolicitorCaseTakenOfflineNoApplicantResponse())
                .thenReturn("template-id-no-response");

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("template-id-no-response");
    }

    @Test
    void shouldReturnDefaultEmailTemplateIdWhenConditionsAreNotMet() {
        CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                //.multiPartyScenario(ONE_V_ONE)
                .respondent1Represented(YES)
                .applicant1Represented(null)
                .applicant1ResponseDeadline(null)
                .build();

        when(notificationsProperties.getSolicitorCaseTakenOffline())
                .thenReturn("default-template-id");

        String templateId = generator.getEmailTemplateId(caseData);

        assertThat(templateId).isEqualTo("default-template-id");
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = generator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("case-taken-offline-applicant-notification-%s");
    }
}