package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CONFIRM_REVIEW_ORDER_EVENT;

@ExtendWith(MockitoExtension.class)
class UpdateConfirmOrderReviewCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private UpdateConfirmOrderReviewCallbackHandler handler;

    @Test
    void shouldReturnCorrectActivityId_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertThat(handler.camundaActivityId(params)).isEqualTo("UpdateConfirmOrderReviewEvent");
    }

    @Test
    void shouldReturnCorrectEventList_whenRequested() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var events = List.of(UPDATE_CONFIRM_REVIEW_ORDER_EVENT);

        assertThat(handler.handledEvents()).isEqualTo(events);
    }
}
