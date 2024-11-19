package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingTypes;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RequestAHearingCallbackHandler.LISTING_REQUESTED;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RequestAHearingCallbackHandler.LISTING_REQUESTED_TASKS;

@ExtendWith(MockitoExtension.class)
class RequestAHearingCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private RequestAHearingCallbackHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new RequestAHearingCallbackHandler(objectMapper);
    }

    @Test
    void shouldClearData_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .requestHearingNoticeIntermediate(HearingTypes.CASE_MANAGEMENT_CONFERENCE)
            .requestAnotherHearing(YES)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getData().get("requestHearingNoticeIntermediate")).isNull();
        assertThat(response.getData().get("requestAnotherHearing")).isNull();
    }

    @Test
    void shouldCreateConfirmationScreen_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .requestHearingNoticeMulti(HearingTypes.CASE_MANAGEMENT_CONFERENCE)
            .requestAnotherHearing(YES)
            .build();

        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

        assertEquals(LISTING_REQUESTED, response.getConfirmationHeader());
        assertEquals(LISTING_REQUESTED_TASKS, response.getConfirmationBody());
    }
}
