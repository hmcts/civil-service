package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.caseprogression.ConfirmListingTickBox;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.enums.caseprogression.ConfirmListingTickBox.CONFIRM_LISTING;
import static uk.gov.hmcts.reform.civil.handler.callback.user.ConfirmListingCompletedCallbackHandler.errorMessage;

@ExtendWith(MockitoExtension.class)
class ConfirmListingCompletedCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ConfirmListingCompletedCallbackHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new ConfirmListingCompletedCallbackHandler(objectMapper);
    }

    @Test
    void shouldReturnErrorMessageWhenNotConfirmed_WhenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder().build();
        CallbackParams params = callbackParamsOf(caseData, MID, "validate-confirmed");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).contains(errorMessage);
    }

    @Test
    void shouldNotReturnErrorMessageWhenConfirmed_WhenInvoked() {
        List<ConfirmListingTickBox> confirmList = new ArrayList<>();
        confirmList.add(CONFIRM_LISTING);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .confirmListingTickBox(confirmList)
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, "validate-confirmed");

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldClearData_whenInvoked() {
        List<ConfirmListingTickBox> confirmList = new ArrayList<>();
        confirmList.add(CONFIRM_LISTING);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .confirmListingTickBox(confirmList)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getData().get("confirmListingTickBox")).isNull();
    }

}
