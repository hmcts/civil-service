package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT;

@ExtendWith(MockitoExtension.class)
public class ConfirmOrderReviewClaimantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ConfirmOrderReviewClaimantNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private FeatureToggleService featureToggleService;

    public static final String TASK_ID = "UpdateTaskListConfirmOrderReviewClaimant";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

    @Test
    void shouldConfigureDashboardNotificationsStayCase() {

        when(featureToggleService.isCaseEventsEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build()
            .toBuilder().applicant1Represented(YesOrNo.NO)
            .isFinalOrder(YesOrNo.YES)
            .build();

        CallbackParams callbackParams = CallbackParamsBuilder.builder()
            .of(ABOUT_TO_SUBMIT, caseData)
            .build();

        handler.handle(callbackParams);

        verify(dashboardApiClient).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT",
            "BEARER_TOKEN"
        );
    }

}
