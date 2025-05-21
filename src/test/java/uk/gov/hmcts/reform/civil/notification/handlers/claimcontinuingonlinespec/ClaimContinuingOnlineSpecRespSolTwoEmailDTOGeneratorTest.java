package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecRespSolTwoEmailDTOGeneratorTest {

    private static final String TEMPLATE_ID = "resp-sol-two-template";
    private static final String ORG_NAME = "Respondent Two Org";
    public static final String CLAIM_CONTINUING_ONLINE_NOTIFICATION = "claim-continuing-online-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator emailGenerator;

    @Test
    void shouldReturnTemplateId() {
        when(notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec())
                .thenReturn(TEMPLATE_ID);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        String id = emailGenerator.getEmailTemplateId(caseData);
        assertThat(id).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        String ref = emailGenerator.getReferenceTemplate();
        assertThat(ref).isEqualTo(CLAIM_CONTINUING_ONLINE_NOTIFICATION);
    }

    @Test
    void shouldAddClaimLegalOrgNameToProperties() {
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        Map<String, String> props = emailGenerator.addCustomProperties(new HashMap<>(), caseData);
        assertThat(props).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME);
    }

    @Test
    void shouldNotify_whenOneVTwoTwoLegalRep() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();
        boolean result = emailGenerator.getShouldNotify(caseData);
        assertThat(result).isTrue();
    }

    @Test
    void shouldNotNotify_whenNotOneVTwoTwoLegalRep() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        boolean result = emailGenerator.getShouldNotify(caseData);
        assertThat(result).isFalse();
    }
}
