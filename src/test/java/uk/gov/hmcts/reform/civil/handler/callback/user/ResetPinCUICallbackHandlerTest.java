package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.notification.handlers.resetpin.ResetPinDefendantLipNotifier;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
public class ResetPinCUICallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @Mock
    private ResetPinDefendantLipNotifier resetPinDefendantLipNotifier;

    private ObjectMapper objectMapper = new ObjectMapper();

    private ResetPinCUICallbackHandler handler;

    @BeforeEach
    public void setUpTest() {
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        handler = new ResetPinCUICallbackHandler(
            defendantPinToPostLRspecService,
            resetPinDefendantLipNotifier,
            objectMapper
        );
    }

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
            given(resetPinDefendantLipNotifier.notifyParties(any())).willReturn(List.of());
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

        @Test
        void shouldHandleErrorsFromResetPinExpiryDate() {
            given(defendantPinToPostLRspecService.resetPinExpiryDate(any())).willReturn(pin);
            given(resetPinDefendantLipNotifier.notifyParties(any())).willReturn(List.of("An error occured"));
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().addRespondent1PinToPostLRspec(givenPin)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors().get(0))
                .isEqualTo("An error occured");
            verify(defendantPinToPostLRspecService, times(1)).resetPinExpiryDate(givenPin);
        }
    }

}
