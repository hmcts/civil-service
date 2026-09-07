package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.ConfirmOrderReviewFixtures;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONFIRM_ORDER_REVIEW;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;

@SuppressWarnings({"java:S5960", "java:S6813"})
class ConfirmOrderReviewWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private Time time;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(LocalDateTime.of(2026, 6, 1, 10, 0));
        when(userService.getUserDetails(anyString()))
            .thenReturn(UserDetails.builder()
                            .forename("Court")
                            .surname("Officer")
                            .email("officer@example.com")
                            .build());
    }

    @Test
    void shouldTransitionToAllFinalOrdersIssuedWhenFinalOrder() throws Exception {
        CaseData fixture = ConfirmOrderReviewFixtures.finalOrderFromDecisionOutcome();

        startWorkflow(fixture)
            .eventId(CONFIRM_ORDER_REVIEW)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(All_FINAL_ORDERS_ISSUED.toString());

                CaseData updated = result.caseData();
                assertThat(updated.getIsFinalOrder()).isEqualTo(YesOrNo.YES);
            });
    }

    @Test
    void shouldTransitionToCaseProgressionWhenNotFinalOrderFromDecisionOutcome() throws Exception {
        CaseData fixture = ConfirmOrderReviewFixtures.nonFinalOrderFromDecisionOutcome();

        startWorkflow(fixture)
            .eventId(CONFIRM_ORDER_REVIEW)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getState()).isEqualTo(CASE_PROGRESSION.toString());

                CaseData updated = result.caseData();
                assertThat(updated.getEnableUploadEvent()).isEqualTo(YesOrNo.YES);
            });
    }
}
