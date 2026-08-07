package uk.gov.hmcts.reform.civil.workflow.ga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaFlowState;
import uk.gov.hmcts.reform.civil.ga.service.flowstate.GaStateFlowEngine;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.workflow.ga.fixture.GaLifecycleFixtures;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({"java:S5960", "java:S6813"})
class RespondToApplicationWorkflowTest extends GAWorkflowIntegrationTest {

    @Autowired
    private GaStateFlowEngine stateFlowEngine;

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private GeneralAppLocationRefDataService locationRefDataService;

    @MockBean
    private DocUploadDashboardNotificationService dashboardNotificationService;

    @BeforeEach
    void setUpExternalDependencies() {
        when(idamClient.getUserInfo(BEARER_TOKEN)).thenReturn(UserInfo.builder()
                                                                 .uid(GaLifecycleFixtures.RESPONDENT_ID)
                                                                 .sub(GaLifecycleFixtures.RESPONDENT_EMAIL)
                                                                 .build());
        when(coreCaseDataService.getCase(Long.parseLong(GaLifecycleFixtures.PARENT_CASE_REFERENCE)))
            .thenReturn(CaseDetails.builder()
                            .id(Long.parseLong(GaLifecycleFixtures.PARENT_CASE_REFERENCE))
                            .data(GaLifecycleFixtures.responseParentCase().toMap(objectMapper))
                            .build());
        when(locationRefDataService.getCourtLocations(anyString(), anyString())).thenReturn(List.of(
            new LocationRefData()
                .setSiteName("Central London County Court")
                .setCourtAddress("Thomas More Building")
                .setPostcode("EC4A 3TR")
        ));
    }

    @Test
    void shouldInitialiseTheResponseWithCourtLocations() throws Exception {
        startWorkflow(GaLifecycleFixtures.responseInput())
            .eventId(CaseEvent.RESPOND_TO_APPLICATION)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getGeneralAppVaryJudgementType()).isEqualTo(YesOrNo.NO);
                assertThat(result.caseData().getHearingDetailsResp().getHearingPreferredLocation().getListItems())
                    .extracting("label")
                    .containsExactly("Central London County Court - Thomas More Building - EC4A 3TR");
            });
    }

    @Test
    void shouldPersistTheResponseMoveEvidenceAndReturnConfirmation() throws Exception {
        startWorkflow(GaLifecycleFixtures.responseInput())
            .eventId(CaseEvent.RESPOND_TO_APPLICATION)
            .aboutToSubmit()
            .then(result -> {
                GARespondentResponse response = result.caseData().getRespondentsResponses().getFirst().getValue();
                assertThat(response.getGaRespondentDetails()).isEqualTo(GaLifecycleFixtures.RESPONDENT_ID);
                assertThat(response.getGeneralAppRespondent1Representative()).isEqualTo(YesOrNo.NO);
                assertThat(response.getGaRespondentResponseReason())
                    .isEqualTo("The application should not be granted");
                assertThat(response.getGaHearingDetails().getRespondentResponsePartyName())
                    .isEqualTo("Respondent One - Defendant");

                assertThat(result.caseData().getGaAddlDoc()).singleElement().satisfies(document -> {
                    assertThat(document.getValue().getDocumentName()).isEqualTo("Respond evidence");
                    assertThat(document.getValue().getCreatedBy()).isEqualTo("Respondent One");
                    assertThat(document.getValue().getDocumentLink().getDocumentFileName())
                        .isEqualTo("response-evidence.pdf");
                    assertThat(document.getValue().getDocumentLink().getCategoryID()).isEqualTo("applications");
                });
                assertThat(result.caseData().getGaAddlDocRespondentSol()).hasSize(1);
                assertThat(result.caseData().getGaAddlDocStaff()).hasSize(1);
                assertThat(result.caseData().getBusinessProcess())
                    .extracting("status", "camundaEvent")
                    .containsExactly(BusinessProcessStatus.READY, CaseEvent.RESPOND_TO_APPLICATION.name());
                assertThat(stateFlowEngine.evaluate(result.caseData()).getStateHistory().getLast().getName())
                    .isEqualTo(GaFlowState.Main.PROCEED_GENERAL_APPLICATION.fullName());
            })
            .submitted()
            .then(result -> {
                assertThat(result.submittedResponse().path("confirmation_header").asText())
                    .isEqualTo("# You have provided the requested information");
                assertThat(result.submittedResponse().path("confirmation_body").asText())
                    .contains("Summary judgment")
                    .contains("reviewed by a Judge");
            });

        verifyNoInteractions(dashboardNotificationService);
    }
}
