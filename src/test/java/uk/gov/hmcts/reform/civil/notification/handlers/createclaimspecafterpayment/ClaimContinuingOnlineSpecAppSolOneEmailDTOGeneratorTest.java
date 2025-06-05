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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecAppSolOneEmailDTOGeneratorTest {

    private static final String TEMPLATE_SINGLE = "template-single";
    private static final String TEMPLATE_MULTI = "template-multi";
    private static final String ORG_NAME = "Legal Org";
    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator generator;

    @Test
    void shouldReturnSingleTemplate_whenNoSecondDefendant() {
        when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec())
                .thenReturn(TEMPLATE_SINGLE);

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();

        String id = generator.getEmailTemplateId(caseData);
        assertThat(id).isEqualTo(TEMPLATE_SINGLE);
    }

    @Test
    void shouldReturnMultiTemplate_whenSecondDefendantPresent() {
        when(notificationsProperties.getClaimantSolicitorClaimContinuingOnline1v2ForSpec())
                .thenReturn(TEMPLATE_MULTI);

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

        String id = generator.getEmailTemplateId(caseData);
        assertThat(id).isEqualTo(TEMPLATE_MULTI);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldIncludeAllCustomPropertiesOneDefendant() {
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();

        Map<String, String> props = generator.addCustomProperties(new HashMap<>(), caseData);

        assertThat(props).containsAllEntriesOf(Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME,
                ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
                CLAIM_DETAILS_NOTIFICATION_DEADLINE, formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                RESPONSE_DEADLINE, formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT)
        ));
    }

    @Test
    void shouldIncludeAllCustomPropertiesTwoDefendants() {
        when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name(ORG_NAME).build()));

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

        Map<String, String> props = generator.addCustomProperties(new HashMap<>(), caseData);

        assertThat(props).containsAllEntriesOf(Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, ORG_NAME,
                ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE),
                CLAIM_DETAILS_NOTIFICATION_DEADLINE, formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE),
                RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2())
        ));
    }
}
