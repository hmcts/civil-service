package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.AssignCaseToRespondentSolHelper;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.GaLifecycleFixtures;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({"java:S5960", "java:S6813"})
class MakeDecisionWorkflowTest extends GAWorkflowIntegrationTest {

    @Autowired
    private GaStateFlowEngine stateFlowEngine;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private GeneralAppLocationRefDataService locationRefDataService;

    @MockBean(name = "deadlinesCalculator")
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private AssignCaseToRespondentSolHelper assignCaseToRespondentSolHelper;

    @BeforeEach
    void setUpExternalDependencies() {
        when(idamClient.getUserInfo(BEARER_TOKEN)).thenReturn(UserInfo.builder()
                                                                 .givenName("Jamie")
                                                                 .familyName("Judge")
                                                                 .build());
        when(locationRefDataService.getCourtLocations(anyString(), anyString())).thenReturn(List.of(
            new LocationRefData()
                .setSiteName("Central London County Court")
                .setCourtAddress("Thomas More Building")
                .setPostcode("EC4A 3TR")
        ));
        when(deadlinesCalculator.getJudicialOrderDeadlineDate(any(LocalDateTime.class), anyInt()))
            .thenReturn(LocalDate.of(2026, 8, 14));
    }

    @Test
    void shouldInitialiseJudicialDecisionDataAndCourtLocations() throws Exception {
        GeneralApplicationCaseData caseData = GaLifecycleFixtures.decisionStartInput().copy()
            .businessProcess(null)
            .build();

        startWorkflow(caseData)
            .eventId(CaseEvent.MAKE_DECISION)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getJudgeTitle()).isEqualTo("Jamie Judge");
                assertThat(result.caseData().getApplicationIsCloaked()).isEqualTo(YesOrNo.NO);
                assertThat(result.caseData().getJudicialDecisionMakeOrder().getOrderText())
                    .isEqualTo("Extend the deadline");
                assertThat(result.caseData().getJudicialDecisionMakeOrder().getDismissalOrderText())
                    .contains("This application is dismissed");
                assertThat(result.caseData().getJudicialDecisionRequestMoreInfo().getIsWithNotice())
                    .isEqualTo(YesOrNo.YES);
                assertThat(result.caseData().getJudicialListForHearing().getHearingPreferredLocation().getListItems())
                    .extracting("label")
                    .containsExactly("Central London County Court - Thomas More Building - EC4A 3TR");
            });
    }

    @Test
    void shouldSetTheDecisionBusinessProcessVisibilityStateAndReturnConfirmation() throws Exception {
        GeneralApplicationCaseData caseData = GaLifecycleFixtures.decision(GAJudgeDecisionOption.FREE_FORM_ORDER).copy()
            .businessProcess(null)
            .build();

        startWorkflow(caseData)
            .eventId(CaseEvent.MAKE_DECISION)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, CaseEvent.MAKE_DECISION.name());
                assertThat(result.caseData().getApplicationIsCloaked()).isEqualTo(YesOrNo.NO);
                assertThat(stateFlowEngine.evaluate(result.caseData()).getStateHistory().getLast().getName())
                    .isEqualTo(GaFlowState.Main.ORDER_MADE.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .isEqualTo("# Your order has been made");
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .isEqualTo("<br/><br/>");
            });

        verifyNoInteractions(assignCaseToRespondentSolHelper);
    }

    @Test
    void shouldConfirmTheDeadlineWhenTheJudgeRequestsMoreInformation() throws Exception {
        GeneralApplicationCaseData caseData = GaLifecycleFixtures.requestMoreInformation();
        String expectedDate = caseData.getJudicialDecisionRequestMoreInfo().getJudgeRequestMoreInfoByDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        startWorkflow(caseData)
            .eventId(CaseEvent.MAKE_DECISION)
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .isEqualTo("# You have requested more information");
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains("provide a response by " + expectedDate);
            });
    }
}
