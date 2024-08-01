package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;
import java.time.format.DateTimeFormatter;

import static com.launchdarkly.shaded.kotlin.jvm.internal.TypeIntrinsics.castToMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
public class ResetPinCUICallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @InjectMocks
    private ResetPinCUICallbackHandler handler;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new ResetPinCUICallbackHandler(defendantPinToPostLRspecService, objectMapper);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().addRespondent1PinToPostLRspec(givenPin)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            Object respondentPinObj = response.getData().get("respondent1PinToPostLRspec");
            assertThat(respondentPinObj).isNotNull().isInstanceOf(java.util.Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> respondentPinMap = (Map<String, Object>) respondentPinObj;

            Object expiryDateObj = respondentPinMap.get("expiryDate");
            assertThat(expiryDateObj).isNotNull().isInstanceOf(java.util.List.class);

            List<?> expiryDateList = (List<?>) expiryDateObj;

            List<Integer> dateComponents = expiryDateList.stream()
                .map(o -> Integer.parseInt(o.toString())).toList();

            LocalDate expiryDate = LocalDate.of(
                dateComponents.get(0), dateComponents.get(1), dateComponents.get(2)
            );

            String formattedExpiryDate = expiryDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String expectedExpiryDate = pin.getExpiryDate().format(DateTimeFormatter.ISO_LOCAL_DATE);

            assertThat(formattedExpiryDate).isEqualTo(expectedExpiryDate);
            verify(defendantPinToPostLRspecService, times(1)).resetPinExpiryDate(givenPin);
        }
    }

}
