package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;

public class CreateSDORespSolOneEmailDTOGeneratorTest {

    @InjectMocks
    private CreateSDORespSolOneEmailDTOGenerator emailDTOGenerator;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenSpecNotEANotBilingual() {
        String baseLocation = "base location";
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(baseLocation).build())
            .build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getSdoOrderedSpec()).thenReturn(expectedTemplateId);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(baseLocation)).thenReturn(false);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenSpecBilingual() {
        CaseDataLiP caseDataLiP = CaseDataLiP.builder()
            .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(BOTH.toString()).build())
            .build();
        CaseData caseData = CaseData.builder().caseAccessCategory(SPEC_CLAIM).caseDataLiP(caseDataLiP).build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getSdoOrderedSpecBilingual()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenSpecEA() {
        String baseLocation = "base location";
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(baseLocation).build())
            .build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getSdoOrderedSpecEa()).thenReturn(expectedTemplateId);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(baseLocation)).thenReturn(true);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenUnspec() {
        CaseData caseData = CaseData.builder().caseAccessCategory(UNSPEC_CLAIM).build();

        String expectedTemplateId = "template-id";
        when(notificationsProperties.getSdoOrdered()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("create-sdo-respondent-1-notification-%s");
    }
}
