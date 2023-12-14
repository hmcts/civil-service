package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    ResetPinCUICallbackHandler.class,
    JacksonAutoConfiguration.class
})
public class ResetPinCUICallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @Autowired
    private ResetPinCUICallbackHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private final DefendantPinToPostLRspec givenPin =
            DefendantPinToPostLRspec.builder()
                .expiryDate(LocalDate.of(
                                2021,
                                1,
                                1
                            )
                )
                .citizenCaseRole("citizen")
                .respondentCaseRole("citizen")
                .accessCode("123").build();
        private final DefendantPinToPostLRspec pin =
            DefendantPinToPostLRspec.builder()
                .expiryDate(LocalDate.now())
                .citizenCaseRole("citizen")
                .respondentCaseRole("citizen")
                .accessCode("123").build();

        @Test
        void shouldResetPinExpiryDate() {
            given(defendantPinToPostLRspecService.resetPinExpiryDate(any())).willReturn(pin);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().addRespondent1PinToPostLRspec(givenPin)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData())
                .extracting("respondent1PinToPostLRspec")
                .extracting("expiryDate")
                .isEqualTo(pin.getExpiryDate().toString());
            verify(defendantPinToPostLRspecService, times(1)).resetPinExpiryDate(givenPin);
        }
    }

}
