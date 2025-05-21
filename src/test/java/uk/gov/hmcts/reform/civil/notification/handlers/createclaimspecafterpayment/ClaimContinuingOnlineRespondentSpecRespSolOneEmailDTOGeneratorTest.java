package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineRespondentSpecRespSolOneEmailDTOGeneratorTest {

    public static final String CLAIM_CONTINUING_ONLINE_NOTIFICATION = "claim-continuing-online-notification-%s";
    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimContinuingOnlineRespondentSpecRespSolOneEmailDTOGenerator generator;

    private static final String TEMPLATE_ID = "resp-sol-one-template";
    private static final String ORG_NAME = "Respondent Org";

    @Test
    void shouldReturnTemplateId() {
        when(notificationsProperties.getRespondentSolicitorClaimContinuingOnlineForSpec())
               .thenReturn(TEMPLATE_ID);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        assertThat(generator.getEmailTemplateId(caseData)).isEqualTo(TEMPLATE_ID);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate())
                .isEqualTo(CLAIM_CONTINUING_ONLINE_NOTIFICATION);
    }

    @Test
    void shouldAddDeadlineToProperties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        Map<String, String> props = generator.addProperties(caseData);

        assertThat(props)
                .containsEntry(
                        CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                        formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE));
    }

    @Test
    void shouldAddClaimLegalOrgNameToCustomProperties() {
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        Map<String, String> initial = new HashMap<>();

        Map<String, String> result = generator.addCustomProperties(initial, caseData);
        assertThat(result).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME);
    }
}