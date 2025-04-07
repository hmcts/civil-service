package uk.gov.hmcts.reform.civil.notification.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@ExtendWith(MockitoExtension.class)
class ClaimContinuingOnlineSpecNotifierTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @InjectMocks
    private ClaimContinuingOnlineSpecNotifier claimContinuingOnlineSpecNotifier;

    @Test
    void shouldNotifyApplicantAndRespondentSolicitor_whenInvoked() {
        CaseData caseData = createBaseCaseDataBuilder()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("OrgId1").build())
                        .build())
                .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder().name("test name").build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant@example.com").build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("OrgId2").build())
                        .build())
                .respondent1(Party.builder().partyEmail("respondent1@example.com")
                        .type(Party.Type.INDIVIDUAL).build())
                .build();

        setupStateFlow(caseData, false);

        Set<EmailDTO> partiesToNotify = claimContinuingOnlineSpecNotifier.getPartiesToNotify(caseData);
        assertThat(partiesToNotify.size()).isEqualTo(2);
    }

    @Test
    void shouldNotifyRespondent2Solicitor_whenInvoked() {
        CaseData caseData = createBaseCaseDataBuilder()
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("OrgId1").build())
                        .build())
                .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder().name("test name").build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("applicant@example.com").build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("OrgId2").build())
                        .build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("OrgId2").build())
                        .build())
                .respondent1(Party.builder().partyEmail("respondent1@example.com")
                        .type(Party.Type.INDIVIDUAL).build())
                .respondent2(Party.builder().partyEmail("respondent2@example.com")
                        .type(Party.Type.INDIVIDUAL).build())
                .respondentSolicitor2EmailAddress("respondent2@example.com")
                .build();

        setupStateFlow(caseData, true);

        Set<EmailDTO> partiesToNotify = claimContinuingOnlineSpecNotifier.getPartiesToNotify(caseData);
        assertThat(partiesToNotify.size()).isEqualTo(3);
    }

    @Test
    void shouldNotifyRespondentSolicitors_whenApplicant1IsUnrepresented() {
        CaseData caseData = createBaseCaseDataBuilder()
                .applicant1Represented(YesOrNo.NO)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("OrgId2").build())
                        .build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder().organisationID("OrgId2").build())
                        .build())
                .respondent1(Party.builder().partyEmail("respondent1@example.com")
                        .type(Party.Type.INDIVIDUAL).build())
                .respondent2(Party.builder().partyEmail("respondent2@example.com")
                        .type(Party.Type.INDIVIDUAL).build())
                .respondentSolicitor2EmailAddress("respondent2@example.com")
                .build();

        setupStateFlow(caseData, true);

        Set<EmailDTO> partiesToNotify = claimContinuingOnlineSpecNotifier.getPartiesToNotify(caseData);
        assertThat(partiesToNotify.size()).isEqualTo(2);
    }

    private CaseData.CaseDataBuilder<?, ?> createBaseCaseDataBuilder() {
        return CaseData.builder()
                .ccdCaseReference(1234L)
                .legacyCaseReference("LEGACY_REF")
                .issueDate(LocalDate.now())
                .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14));
    }

    private void setupStateFlow(CaseData caseData, boolean twoRespondents) {
        StateFlow stateFlow = mock(StateFlow.class);
        when(stateFlowEngine.evaluate(caseData)).thenReturn(stateFlow);
        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(twoRespondents);
    }
}
