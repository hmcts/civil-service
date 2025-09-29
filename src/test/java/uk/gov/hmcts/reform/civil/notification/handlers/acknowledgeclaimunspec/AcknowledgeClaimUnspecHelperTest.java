package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimunspec;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_INTENTION;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.isAcknowledgeUserRespondentTwo;

class AcknowledgeClaimUnspecHelperTest {

    @Mock
    private OrganisationService organisationService;

    @Mock
    private Party respondent;

    @InjectMocks
    private AcknowledgeClaimUnspecHelper acknowledgeClaimUnspecHelper;

    private final LocalDateTime deadline = LocalDateTime.of(2029, 1, 1, 12, 0);

    @Test
    void shouldAddCorrectTemplateProperties_forRespondent1Acknowledged() {
        AcknowledgeClaimUnspecHelper helper = new AcknowledgeClaimUnspecHelper(organisationService);

        CaseData caseData = mock(CaseData.class);
        Map<String, String> inputProps = new HashMap<>();

        try (MockedStatic<NotificationUtils> notificationUtils = mockStatic(uk.gov.hmcts.reform.civil.utils.NotificationUtils.class);
             MockedStatic<uk.gov.hmcts.reform.civil.utils.PartyUtils> partyUtils = mockStatic(uk.gov.hmcts.reform.civil.utils.PartyUtils.class);
             MockedStatic<uk.gov.hmcts.reform.civil.enums.MultiPartyScenario> multiPartyMock = mockStatic(uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.class);
             MockedStatic<uk.gov.hmcts.reform.civil.helpers.DateFormatHelper> dateFormatMock = mockStatic(uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.class)) {

            when(caseData.getRespondent1()).thenReturn(respondent);
            when(caseData.getRespondent1ResponseDeadline()).thenReturn(deadline);
            multiPartyMock.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData))
                .thenReturn(MultiPartyScenario.ONE_V_ONE);
            partyUtils.when(() -> isAcknowledgeUserRespondentTwo(caseData)).thenReturn(false);
            partyUtils.when(() -> getPartyNameBasedOnType(respondent)).thenReturn("Resp Name");
            partyUtils.when(() ->
                                    PartyUtils.getResponseIntentionForEmail(caseData))
                .thenReturn("Response intention");

            notificationUtils.when(() ->
                                       getLegalOrganizationNameForRespondent(caseData, true, organisationService)
            ).thenReturn("Legal Org Name");

            dateFormatMock.when(() ->
                                    formatLocalDate(deadline.toLocalDate(), DateFormatHelper.DATE)
            ).thenReturn("01 Jan 2029");

            Map<String, String> result = helper.addTemplateProperties(inputProps, caseData);

            assertThat(result)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Legal Org Name")
                .containsEntry(RESPONDENT_NAME, "Resp Name")
                .containsEntry(RESPONSE_DEADLINE, "01 Jan 2029")
                .containsEntry(RESPONSE_INTENTION, "Response intention");
        }
    }

    @Test
    void shouldAddCorrectTemplateProperties_forRespondent2Acknowledged() {
        AcknowledgeClaimUnspecHelper helper = new AcknowledgeClaimUnspecHelper(organisationService);

        CaseData caseData = mock(CaseData.class);
        Map<String, String> inputProps = new HashMap<>();
        LocalDateTime respondent2Deadline = LocalDateTime.of(2030, 5, 15, 10, 30);

        try (MockedStatic<uk.gov.hmcts.reform.civil.utils.NotificationUtils> notificationUtils = mockStatic(uk.gov.hmcts.reform.civil.utils.NotificationUtils.class);
             MockedStatic<uk.gov.hmcts.reform.civil.utils.PartyUtils> partyUtils = mockStatic(uk.gov.hmcts.reform.civil.utils.PartyUtils.class);
             MockedStatic<uk.gov.hmcts.reform.civil.enums.MultiPartyScenario> multiPartyMock = mockStatic(uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.class);
             MockedStatic<uk.gov.hmcts.reform.civil.helpers.DateFormatHelper> dateFormatMock = mockStatic(uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.class)) {

            // Setup scenario: Respondent 2 acknowledged under ONE_V_TWO_TWO_LEGAL_REP
            when(caseData.getRespondent2()).thenReturn(respondent);
            when(caseData.getRespondent2ResponseDeadline()).thenReturn(respondent2Deadline);
            multiPartyMock.when(() -> MultiPartyScenario.getMultiPartyScenario(caseData))
                .thenReturn(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP);
            partyUtils.when(() -> isAcknowledgeUserRespondentTwo(caseData)).thenReturn(true);
            partyUtils.when(() -> getPartyNameBasedOnType(respondent)).thenReturn("Resp2 Name");

            notificationUtils.when(() ->
                                       getLegalOrganizationNameForRespondent(caseData, false, organisationService)
            ).thenReturn("Resp2 Legal Org");

            dateFormatMock.when(() ->
                                    formatLocalDate(respondent2Deadline.toLocalDate(), DateFormatHelper.DATE)
            ).thenReturn("15 May 2030");

            Map<String, String> result = helper.addTemplateProperties(inputProps, caseData);

            assertThat(result)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Resp2 Legal Org")
                .containsEntry(RESPONDENT_NAME, "Resp2 Name")
                .containsEntry(RESPONSE_DEADLINE, "15 May 2030");
        }
    }
}
