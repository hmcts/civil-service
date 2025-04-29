package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUED_ON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
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

    private static final LocalDate ISSUE_DATE = LocalDate.of(2025, 4, 28);
    private static final LocalDateTime RESP1_DEADLINE = LocalDateTime.of(2025, 5, 12, 10, 30);
    public static final String TEMPLATE_1_V_1 = "template-1v1";
    public static final String TEMPLATE_1_V_2 = "template-1v2";
    public static final String TEST_NAME = "test name";
    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Doe";
    public static final String ID = "test id";
    public static final long CCD_CASE_REFERENCE = 1234L;
    public static final String LEGACY_CASE_REFERENCE = "000DC001";

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator emailGenerator;

    @Test
    void shouldChoose1v1Template_whenNoSecondRespondent() {
        when(notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec())
                .thenReturn(TEMPLATE_1_V_1);
        CaseData caseData = CaseData.builder()
                .issueDate(ISSUE_DATE)
                .respondent1ResponseDeadline(RESP1_DEADLINE)
                .build();

        assertThat(emailGenerator.getEmailTemplateId(caseData))
                .isEqualTo(TEMPLATE_1_V_1);
    }

    @Test
    void shouldChoose1v2Template_whenSecondRespondentPresent() {
        when(notificationsProperties.getClaimantSolicitorClaimContinuingOnline1v2ForSpec())
                .thenReturn(TEMPLATE_1_V_2);
        CaseData caseData = CaseData.builder()
                .issueDate(ISSUE_DATE)
                .respondent1ResponseDeadline(RESP1_DEADLINE)
                .respondent2ResponseDeadline(RESP1_DEADLINE.plusDays(1))
                .respondent2(uk.gov.hmcts.reform.civil.model.Party.builder().build())
                .build();

        assertThat(emailGenerator.getEmailTemplateId(caseData))
                .isEqualTo(TEMPLATE_1_V_2);
    }

    @Test
    void shouldIncludeAllCustomProperties_for1v1() {

        Party respondent1 = Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName(FIRST_NAME)
                .individualLastName(LAST_NAME)
                .build();

        Organisation organisation = Organisation.builder()
                .organisationID(ID)
                .build();

        CaseData caseData = CaseData.builder()
                .ccdCaseReference(CCD_CASE_REFERENCE)
                .legacyCaseReference(LEGACY_CASE_REFERENCE)
                .issueDate(ISSUE_DATE)
                .respondent1ResponseDeadline(RESP1_DEADLINE)
                .applicant1OrganisationPolicy((OrganisationPolicy.builder().organisation(organisation).build()))
                .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder().name(TEST_NAME).build())
                .respondent1(respondent1)
                .build();

        EmailDTO dto = emailGenerator.buildEmailDTO(caseData);
        Map<String, String> params = dto.getParameters();

        assertThat(params)
                .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, TEST_NAME)
                .containsEntry(CLAIM_DETAILS_NOTIFICATION_DEADLINE, formatLocalDate(RESP1_DEADLINE.toLocalDate(), DATE))
                .containsEntry(ISSUED_ON, formatLocalDate(ISSUE_DATE, DATE))
                .containsKey(PARTY_REFERENCES)
                .containsEntry(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()))
                .containsEntry(RESPONSE_DEADLINE, formatLocalDateTime(RESP1_DEADLINE, DATE_TIME_AT));
    }

    @Test
    void shouldIncludeBothRespondentNames_for1v2() {
        Organisation organisation = Organisation.builder()
                .organisationID(ID)
                .build();

        Party respondent1 = Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName(FIRST_NAME)
                .individualLastName(LAST_NAME)
                .build();

        Party respondent2 = Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualFirstName("Jane")
                .individualLastName(LAST_NAME)
                .build();

        CaseData caseData = CaseData.builder()
                .ccdCaseReference(CCD_CASE_REFERENCE)
                .legacyCaseReference(LEGACY_CASE_REFERENCE)
                .issueDate(ISSUE_DATE)
                .respondent1ResponseDeadline(RESP1_DEADLINE)
                .respondent2ResponseDeadline(RESP1_DEADLINE.plusDays(1))
                .applicant1OrganisationPolicy((OrganisationPolicy.builder().organisation(organisation).build()))
                .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder().name(TEST_NAME).build())
                .respondent1(respondent1)
                .respondent2(respondent2)
                .build();

        EmailDTO dto = emailGenerator.buildEmailDTO(caseData);
        Map<String, String> params = dto.getParameters();

        assertThat(params)
                .containsEntry(RESPONDENT_ONE_NAME, "John Doe")
                .containsEntry(RESPONDENT_TWO_NAME, "Jane Doe");
    }
}
