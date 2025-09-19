package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;

public class CreateSDOAppSolOneEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CreateSDOAppSolOneEmailDTOGenerator emailDTOGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    void shouldReturnCorrectEmailTemplateIdWhenSpec() {
//        String baseLocation = "base location";
//        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation(baseLocation).build();
//        CaseData caseData = CaseData.builder().caseAccessCategory(SPEC_CLAIM).caseManagementLocation(caseLocation).build();
//
//        String expectedTemplateId = "template-id";
//        when(notificationsProperties.getSdoOrderedSpec()).thenReturn(expectedTemplateId);
//        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(baseLocation)).thenReturn(false);
//
//        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);
//
//        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
//    }

    @Test
    void shouldReturnCorrectEmailTemplateIdWhenSpecAndEA() {
        String baseLocation = "base location";
        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation(baseLocation).build();
        CaseData caseData = CaseData.builder().caseAccessCategory(SPEC_CLAIM).caseManagementLocation(caseLocation).build();

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

        assertThat(referenceTemplate).isEqualTo("create-sdo-applicants-notification-%s");
    }
}
