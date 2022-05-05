package uk.gov.hmcts.reform.civil.handler.tasks;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.FLOW_FLAGS;
import static uk.gov.hmcts.reform.civil.handler.tasks.StartBusinessProcessTaskHandler.FLOW_STATE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;

@SpringBootTest(classes = {
    CaseEventTaskHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class
})
@ExtendWith(SpringExtension.class)
class CaseEventTaskHandlerTest {

    private static final String CASE_ID = "1";

    @Mock
    private ExternalTask mockTask;

    @Mock
    private ExternalTaskService externalTaskService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Captor
    ArgumentCaptor<CaseDataContent> caseDataContentArgumentCaptor;

    @Autowired
    private CaseEventTaskHandler caseEventTaskHandler;

    @BeforeEach
    void init() {
        when(mockTask.getTopicName()).thenReturn("test");
        when(mockTask.getWorkerId()).thenReturn("worker");
        when(mockTask.getActivityId()).thenReturn("activityId");
    }

    @Nested
    class NotifyRespondent {

        @BeforeEach
        void init() {
            Map<String, Object> variables = Map.of(
                "caseId", CASE_ID,
                "caseEvent", NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE.name()
            );

            when(mockTask.getAllVariables()).thenReturn(variables);
            when(mockTask.getVariable(FLOW_STATE)).thenReturn(PENDING_CLAIM_ISSUED.fullName());
        }

        @Test
        void shouldTriggerCCDEvent_whenHandlerIsExecuted() {
            CaseData caseData = new CaseDataBuilder().atStateClaimDraft()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();
            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, "MAIN.DRAFT");
            variables.putValue(FLOW_FLAGS, Map.of());

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(coreCaseDataService.startUpdate(CASE_ID, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails).build());

            when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService).startUpdate(CASE_ID, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE);
            verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
            verify(externalTaskService).complete(mockTask, variables);
        }

        @Test
        void shouldCallHandleFailureMethod_whenExceptionFromBusinessLogic() {
            String errorMessage = "there was an error";

            when(mockTask.getRetries()).thenReturn(null);
            when(coreCaseDataService.startUpdate(CASE_ID, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE))
                .thenAnswer(invocation -> {
                    throw new Exception(errorMessage);
                });

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(externalTaskService, never()).complete(mockTask);
            verify(externalTaskService).handleFailure(
                eq(mockTask),
                eq(errorMessage),
                anyString(),
                eq(2),
                eq(500L)
            );
        }

        @Test
        void shouldCallHandleFailureMethod_whenFeignExceptionFromBusinessLogic() {
            String errorMessage = "there was an error";
            int status = 422;
            Request.HttpMethod requestType = Request.HttpMethod.POST;
            String exampleUrl = "example url";

            when(mockTask.getRetries()).thenReturn(null);
            when(coreCaseDataService.startUpdate(CASE_ID, NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE))
                .thenAnswer(invocation -> {
                    throw FeignException.errorStatus(errorMessage, Response.builder()
                        .request(
                            Request.create(
                                requestType,
                                exampleUrl,
                                new HashMap<>(), //this field is required for construtor//
                                null,
                                null,
                                null
                            ))
                        .status(status)
                        .build());
                });

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(externalTaskService, never()).complete(mockTask);
            verify(externalTaskService).handleFailure(
                eq(mockTask),
                eq(String.format("[%s] during [%s] to [%s] [%s]: []", status, requestType, exampleUrl, errorMessage)),
                anyString(),
                eq(2),
                eq(500L)
            );
        }

        @Test
        void shouldNotCallHandleFailureMethod_whenExceptionOnCompleteCall() {
            String errorMessage = "there was an error";

            doThrow(new NotFoundException(errorMessage)).when(externalTaskService).complete(mockTask);

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(externalTaskService, never()).handleFailure(
                any(ExternalTask.class),
                anyString(),
                anyString(),
                anyInt(),
                anyLong()
            );
        }
    }

    @Nested
    class TakeOfflineEvent {

        @BeforeEach
        void init() {
            Map<String, Object> variables = Map.of(
                "caseId", CASE_ID,
                "caseEvent", PROCEEDS_IN_HERITAGE_SYSTEM.name()
            );

            when(mockTask.getAllVariables()).thenReturn(variables);
        }

        @ParameterizedTest
        @EnumSource(
            value = FlowState.Main.class,
            names = {"FULL_ADMISSION", "PART_ADMISSION", "COUNTER_CLAIM",
                "PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT", "PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT",
                "PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT",
                "FULL_DEFENCE_PROCEED", "FULL_DEFENCE_NOT_PROCEED", "TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED",
                "TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED", "TAKEN_OFFLINE_BY_STAFF"})
        void shouldTriggerCCDEvent_whenClaimIsPendingUnRepresented(FlowState.Main state) {
            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, state.fullName());
            variables.putValue(
                FLOW_FLAGS,
                getFlowFlags(state)
            );

            when(mockTask.getVariable(FLOW_STATE)).thenReturn(state.fullName());

            CaseData caseData = getCaseData(state);
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

            when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService).startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM);
            verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
            verify(externalTaskService).complete(mockTask, variables);
        }

        @Test
        //Using an invalid flow state `PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA`
        void shouldThrowErrorMessage_whenInAnInvalidFlowStatePopulatingSummary() {
            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName());
            variables.putValue(FLOW_FLAGS,
                getFlowFlags(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA)
            );

            when(mockTask.getVariable(FLOW_STATE))
                .thenReturn(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA.fullName());

            assertThrows(IllegalStateException.class, () -> {
                getCaseData(PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE_AWAITING_CAMUNDA);
            });
        }

        @ParameterizedTest
        @EnumSource(
            value = ReasonForProceedingOnPaper.class,
            names = {"APPLICATION", "JUDGEMENT_REQUEST", "DEFENDANT_DOES_NOT_CONSENT", "CASE_SETTLED", "OTHER"})
        void shouldTriggerCCDEventWithDescription_whenClaimIsHandedOffline(ReasonForProceedingOnPaper reason) {
            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, TAKEN_OFFLINE_BY_STAFF.fullName());
            variables.putValue(
                FLOW_FLAGS,
                getFlowFlags(TAKEN_OFFLINE_BY_STAFF)
            );

            when(mockTask.getVariable(FLOW_STATE)).thenReturn(TAKEN_OFFLINE_BY_STAFF.fullName());

            CaseData caseData = getCaseData(TAKEN_OFFLINE_BY_STAFF);
            caseData.getClaimProceedsInCaseman().setReason(reason);
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());
            when(coreCaseDataService.submitUpdate(eq(CASE_ID), any(CaseDataContent.class))).thenReturn(caseData);

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            verify(coreCaseDataService).startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM);
            verify(coreCaseDataService).submitUpdate(eq(CASE_ID), any(CaseDataContent.class));
            verify(externalTaskService).complete(mockTask, variables);
        }

        @Test
        void shouldHaveCorrectDescription_whenInUnrepresentedDefendantState() {
            FlowState.Main state = PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, state.fullName());
            variables.putValue(
                FLOW_FLAGS,
                getFlowFlags(state)
            );

            when(mockTask.getVariable(FLOW_STATE)).thenReturn(state.fullName());

            CaseData caseData = getCaseData(state);
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

            when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
                .thenReturn(caseData);

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
            Event event = caseDataContent.getEvent();
            assertThat(event.getDescription()).isEqualTo("Unrepresented defendant: Mr. Sole Trader");
        }

        @Test
        void shouldHaveCorrectDescription_whenInUnregisteredSolicitorState() {
            FlowState.Main state = PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT;
            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, state.fullName());
            variables.putValue(
                FLOW_FLAGS,
                getFlowFlags(state)
            );

            when(mockTask.getVariable(FLOW_STATE)).thenReturn(state.fullName());

            CaseData caseData = getCaseData(state);
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

            when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
                .thenReturn(caseData);

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
            Event event = caseDataContent.getEvent();
            assertThat(event.getDescription()).isEqualTo("Unregistered defendant solicitor firm: Mr. Sole Trader");
        }

        @Test
        void shouldHaveCorrectDescription_whenInUnrepresentedDefendantAndUnregisteredSolicitorState() {
            FlowState.Main state = PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT;
            VariableMap variables = Variables.createVariables();
            variables.putValue(FLOW_STATE, state.fullName());
            variables.putValue(
                FLOW_FLAGS,
                getFlowFlags(state)
            );

            when(mockTask.getVariable(FLOW_STATE)).thenReturn(state.fullName());

            CaseData caseData = getCaseData(state);
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

            when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
                .thenReturn(caseData);

            caseEventTaskHandler.execute(mockTask, externalTaskService);

            CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
            Event event = caseDataContent.getEvent();
            assertThat(event.getDescription())
                .isEqualTo("Unrepresented defendant and unregistered defendant solicitor firm. "
                               + "Unrepresented defendant: Mr. John Rambo. "
                               + "Unregistered defendant solicitor firm: Mr. Sole Trader.");
        }

        @Nested
        class FullDefenceProceed {
            FlowState.Main state = FULL_DEFENCE_PROCEED;
            BusinessProcess businessProcess = BusinessProcess.builder().status(BusinessProcessStatus.READY).build();

            @BeforeEach
            void initForFullDefence() {
                VariableMap variables = Variables.createVariables();
                variables.putValue(FLOW_STATE, state.fullName());
                variables.putValue(FLOW_FLAGS, getFlowFlags(state));

                when(mockTask.getVariable(FLOW_STATE)).thenReturn(state.fullName());
            }

            @Nested
            class OneVOne {
                @Test
                void shouldHaveExpectedDescription() {
                    CaseData caseData = getCaseData(state);
                    CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

                    when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                        .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                        .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

                    when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
                        .thenReturn(caseData);

                    caseEventTaskHandler.execute(mockTask, externalTaskService);

                    CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
                    Event event = caseDataContent.getEvent();
                    assertThat(event.getSummary()).isEqualTo("RPA Reason: Claimant(s) proceeds.");
                    assertThat(event.getDescription())
                        .isEqualTo(null);
                }
            }

            @Nested
            class OneVTwo {
                @Test
                void shouldHaveExpectedDescription_WhenClaimantProceedsAgainstBothDefendants() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .atState(FlowState.Main.FULL_DEFENCE_PROCEED, MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                        .atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                        .businessProcess(businessProcess)
                        .build();
                    CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

                    when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                        .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                        .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

                    when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
                        .thenReturn(caseData);

                    caseEventTaskHandler.execute(mockTask, externalTaskService);

                    CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
                    Event event = caseDataContent.getEvent();
                    assertThat(event.getSummary()).isEqualTo("RPA Reason: Claimant(s) proceeds.");
                    assertThat(event.getDescription())
                        .isEqualTo("Claimant has provided intention: proceed against defendant: Mr. Sole Trader "
                                       + "and proceed against defendant: Mr. John Rambo");
                }

                @Test
                void shouldHaveExpectedDescription_WhenClaimantProceedsAgainstFirstDefendantOnly() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .atState(FlowState.Main.FULL_DEFENCE_PROCEED, MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                        .atStateApplicantRespondToDefenceAndProceedVsDefendant1Only_1v2()
                        .businessProcess(businessProcess)
                        .build();
                    CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

                    when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                        .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                        .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

                    when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
                        .thenReturn(caseData);

                    caseEventTaskHandler.execute(mockTask, externalTaskService);

                    CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
                    Event event = caseDataContent.getEvent();
                    assertThat(event.getSummary()).isEqualTo("RPA Reason: Claimant(s) proceeds.");
                    assertThat(event.getDescription())
                        .isEqualTo("Claimant has provided intention: proceed against defendant: Mr. Sole Trader "
                                       + "and not proceed against defendant: Mr. John Rambo");
                }

                @Test
                void shouldHaveExpectedDescription_WhenClaimantProceedsAgainstSecondDefendantOnly() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .atState(FlowState.Main.FULL_DEFENCE_PROCEED, MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                        .atStateApplicantRespondToDefenceAndProceedVsDefendant2Only_1v2()
                        .businessProcess(businessProcess)
                        .build();
                    CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

                    when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                        .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                        .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

                    when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
                        .thenReturn(caseData);

                    caseEventTaskHandler.execute(mockTask, externalTaskService);

                    CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
                    Event event = caseDataContent.getEvent();
                    assertThat(event.getSummary()).isEqualTo("RPA Reason: Claimant(s) proceeds.");
                    assertThat(event.getDescription())
                        .isEqualTo("Claimant has provided intention: not proceed against defendant: Mr. Sole Trader "
                                       + "and proceed against defendant: Mr. John Rambo");
                }
            }

            @Nested
            class TwoVOne {
                @Test
                void shouldHaveExpectedDescription_WhenBothClaimaintsProceed() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoApplicants()
                        .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
                        .businessProcess(businessProcess)
                        .build();
                    CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

                    when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                        .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                        .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

                    when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
                        .thenReturn(caseData);

                    caseEventTaskHandler.execute(mockTask, externalTaskService);

                    CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
                    Event event = caseDataContent.getEvent();
                    assertThat(event.getSummary()).isEqualTo("RPA Reason: Claimant(s) proceeds.");
                    assertThat(event.getDescription())
                        .isEqualTo("Claimant: Mr. John Rambo has provided intention: proceed. "
                                       + "Claimant: Mr. Jason Rambo has provided intention: proceed.");
                }

                @Test
                void shouldHaveExpectedDescription_WhenOnlyFirstClaimantProceeds() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoApplicants()
                        .atStateApplicant1RespondToDefenceAndProceed_2v1()
                        .businessProcess(businessProcess)
                        .build();
                    CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

                    when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                        .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                        .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

                    when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
                        .thenReturn(caseData);

                    caseEventTaskHandler.execute(mockTask, externalTaskService);

                    CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
                    Event event = caseDataContent.getEvent();
                    assertThat(event.getSummary()).isEqualTo("RPA Reason: Claimant(s) proceeds.");
                    assertThat(event.getDescription())
                        .isEqualTo("Claimant: Mr. John Rambo has provided intention: proceed. "
                                       + "Claimant: Mr. Jason Rambo has provided intention: not proceed.");
                }

                @Test
                void shouldHaveExpectedDescription_WhenOnlySecondClaimantProceeds() {
                    CaseData caseData = CaseDataBuilder.builder()
                        .multiPartyClaimTwoApplicants()
                        .atStateApplicant2RespondToDefenceAndProceed_2v1()
                        .businessProcess(businessProcess)
                        .build();
                    CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

                    when(coreCaseDataService.startUpdate(CASE_ID, PROCEEDS_IN_HERITAGE_SYSTEM))
                        .thenReturn(StartEventResponse.builder().caseDetails(caseDetails)
                                        .eventId(PROCEEDS_IN_HERITAGE_SYSTEM.name()).build());

                    when(coreCaseDataService.submitUpdate(eq(CASE_ID), caseDataContentArgumentCaptor.capture()))
                        .thenReturn(caseData);

                    caseEventTaskHandler.execute(mockTask, externalTaskService);

                    CaseDataContent caseDataContent = caseDataContentArgumentCaptor.getValue();
                    Event event = caseDataContent.getEvent();
                    assertThat(event.getSummary()).isEqualTo("RPA Reason: Claimant(s) proceeds.");
                    assertThat(event.getDescription())
                        .isEqualTo("Claimant: Mr. John Rambo has provided intention: not proceed. "
                                       + "Claimant: Mr. Jason Rambo has provided intention: proceed.");
                }
            }

        }

        @NotNull
        private Map<String, Boolean> getFlowFlags(FlowState.Main state) {
            if (state.equals(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED)
                || state.equals(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED)) {
                return Map.of("TWO_RESPONDENT_REPRESENTATIVES", true,
                              "ONE_RESPONDENT_REPRESENTATIVE", false,
                              "RPA_CONTINUOUS_FEED", false,
                              FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false,
                              FlowFlag.NOTICE_OF_CHANGE.name(), false
                );
            }
            return Map.of("ONE_RESPONDENT_REPRESENTATIVE", true, "RPA_CONTINUOUS_FEED", false,
                          FlowFlag.SPEC_RPA_CONTINUOUS_FEED.name(), false,
                          FlowFlag.NOTICE_OF_CHANGE.name(), false);
        }

        private CaseData getCaseData(FlowState.Main state) {
            BusinessProcess businessProcess = BusinessProcess.builder().status(BusinessProcessStatus.READY).build();
            CaseDataBuilder caseDataBuilder = new CaseDataBuilder().businessProcess(businessProcess);
            switch (state) {
                case FULL_ADMISSION:
                    caseDataBuilder.atStateRespondentFullAdmissionAfterNotifyDetails();
                    break;
                case PART_ADMISSION:
                    caseDataBuilder.atStateRespondentPartAdmissionAfterNotifyDetails();
                    break;
                case COUNTER_CLAIM:
                    caseDataBuilder.atStateRespondentCounterClaimAfterNotifyDetails();
                    break;
                case PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT:
                    caseDataBuilder.atStatePendingClaimIssuedUnrepresentedDefendant();
                    break;
                case PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT:
                    caseDataBuilder.atStatePendingClaimIssuedUnregisteredDefendant();
                    break;
                case PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT:
                    caseDataBuilder.atStatePendingClaimIssuedUnrepresentedUnregisteredDefendant();
                    break;
                case FULL_DEFENCE_PROCEED:
                    caseDataBuilder.atStateApplicantRespondToDefenceAndProceed();
                    break;
                case FULL_DEFENCE_NOT_PROCEED:
                    caseDataBuilder.atStateApplicantRespondToDefenceAndNotProceed();
                    break;
                case TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED:
                    caseDataBuilder.atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor();
                    break;
                case TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED:
                    caseDataBuilder.atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor();
                    break;
                case TAKEN_OFFLINE_BY_STAFF:
                    caseDataBuilder.atStateTakenOfflineByStaff();
                    break;
                default:
                    throw new IllegalStateException("Unexpected flow state " + state.fullName());
            }
            return caseDataBuilder.build();
        }
    }
}
