package uk.gov.hmcts.reform.unspec.bpmn;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.MAKE_PBA_PAYMENT;
import static uk.gov.hmcts.reform.unspec.handler.tasks.StartBusinessProcessTaskHandler.FLOW_STATE;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PAYMENT_FAILED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PAYMENT_SUCCESSFUL;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PROCEEDS_WITH_OFFLINE_JOURNEY;

class CreateClaimTest extends BpmnBaseTest {

    public static final String NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE
        = "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE_ACTIVITY_ID
        = "CreateClaimPaymentSuccessfulNotifyRespondentSolicitor1";
    public static final String NOTIFY_RESPONDENT_SOLICITOR_1_FAILED_PAYMENT
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_FAILED_PAYMENT";
    private static final String NOTIFY_RESPONDENT_SOLICITOR_1_FAILED_PAYMENT_ACTIVITY_ID
        = "CreateClaimPaymentFailedNotifyApplicantSolicitor1";
    private static final String MAKE_PAYMENT_ACTIVITY_ID = "CreateClaimMakePayment";
    public static final String PROCESS_PAYMENT = "processPayment";
    public static final String GENERATE_CLAIM_FORM = "GENERATE_CLAIM_FORM";
    public static final String CLAIM_FORM_ACTIVITY_ID = "GenerateClaimForm";
    public static final String NOTIFY_APPLICANT_SOLICITOR_1_CLAIM_PROCEEDS_OFFLINE
        = "NOTIFY_APPLICANT_SOLICITOR1_FOR_RESPONDENT_LITIGANT_IN_PERSON";
    public static final String NOTIFY_APPLICANT_SOLICITOR_1_CLAIM_PROCEEDS_OFFLINE_ACTIVITY_ID
        = "CreateClaimProceedsOfflineNotifyApplicantSolicitor1";

    public CreateClaimTest() {
        super("create_claim.bpmn", "CREATE_CLAIM_PROCESS_ID");
    }

    @Test
    void shouldSuccessfullyCompleteCreateClaim_whenPaymentWasSuccessful() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CREATE_CLAIM").getKey())
            .isEqualTo("CREATE_CLAIM_PROCESS_ID");

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_STATE, PENDING_CASE_ISSUED.fullName());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        variables.putValue(FLOW_STATE, PAYMENT_SUCCESSFUL.fullName());

        //complete the payment
        ExternalTask paymentTask = assertNextExternalTask(PROCESS_PAYMENT);
        assertCompleteExternalTask(
            paymentTask,
            PROCESS_PAYMENT,
            MAKE_PBA_PAYMENT.name(),
            MAKE_PAYMENT_ACTIVITY_ID,
            variables
        );

        //complete the document generation
        ExternalTask documentGeneration = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            documentGeneration,
            PROCESS_CASE_EVENT,
            GENERATE_CLAIM_FORM,
            CLAIM_FORM_ACTIVITY_ID
        );

        //complete the notification
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(notificationTask, PROCESS_CASE_EVENT, NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE,
                                   NOTIFY_RESPONDENT_SOLICITOR_1_CLAIM_ISSUE_ACTIVITY_ID
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteCreateClaim_whenPaymentFailed() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CREATE_CLAIM").getKey())
            .isEqualTo("CREATE_CLAIM_PROCESS_ID");

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_STATE, PENDING_CASE_ISSUED.fullName());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        variables.putValue(FLOW_STATE, PAYMENT_FAILED.fullName());

        //complete the payment
        ExternalTask paymentTask = assertNextExternalTask(PROCESS_PAYMENT);
        assertCompleteExternalTask(
            paymentTask,
            PROCESS_PAYMENT,
            MAKE_PBA_PAYMENT.name(),
            MAKE_PAYMENT_ACTIVITY_ID,
            variables
        );

        //complete the notification
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_RESPONDENT_SOLICITOR_1_FAILED_PAYMENT,
            NOTIFY_RESPONDENT_SOLICITOR_1_FAILED_PAYMENT_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }

    @Test
    void shouldSuccessfullyCompleteCreateClaim_whenClaimTakenOffline() {
        //assert process has started
        assertFalse(processInstance.isEnded());

        //assert message start event
        assertThat(getProcessDefinitionByMessage("CREATE_CLAIM").getKey())
            .isEqualTo("CREATE_CLAIM_PROCESS_ID");

        VariableMap variables = Variables.createVariables();
        variables.putValue(FLOW_STATE, PROCEEDS_WITH_OFFLINE_JOURNEY.fullName());

        //complete the start business process
        ExternalTask startBusiness = assertNextExternalTask(START_BUSINESS_TOPIC);
        assertCompleteExternalTask(
            startBusiness,
            START_BUSINESS_TOPIC,
            START_BUSINESS_EVENT,
            START_BUSINESS_ACTIVITY,
            variables
        );

        //complete the notification
        ExternalTask notificationTask = assertNextExternalTask(PROCESS_CASE_EVENT);
        assertCompleteExternalTask(
            notificationTask,
            PROCESS_CASE_EVENT,
            NOTIFY_APPLICANT_SOLICITOR_1_CLAIM_PROCEEDS_OFFLINE,
            NOTIFY_APPLICANT_SOLICITOR_1_CLAIM_PROCEEDS_OFFLINE_ACTIVITY_ID,
            variables
        );

        //end business process
        ExternalTask endBusinessProcess = assertNextExternalTask(END_BUSINESS_PROCESS);
        completeBusinessProcess(endBusinessProcess);

        assertNoExternalTasksLeft();
    }
}
