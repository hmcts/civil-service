package uk.gov.hmcts.reform.unspec.handler.callback;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.config.MockDatabaseConfiguration;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.ClaimType.PERSONAL_INJURY_WORK;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;

@SpringBootTest(classes = {
    CreateClaimCallbackHandler.class,
    JacksonAutoConfiguration.class,
    MockDatabaseConfiguration.class},
    properties = {"reference.database.enabled=false"})
class CreateClaimCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private CreateClaimCallbackHandler handler;
    @Value("${unspecified.response-pack-url}")
    private String responsePackLink;

    @Nested
    class MidEventCallback {

        @Test
        void shouldReturnExpectedError_whenValuesAreInvalid() {
            Map<String, Object> data = new HashMap<>();
            data.put(
                "claimValue",
                ClaimValue.builder().higherValue(BigDecimal.valueOf(1)).lowerValue(BigDecimal.valueOf(10)).build()
            );

            CallbackParams params = callbackParamsOf(data, CallbackType.MID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors())
                .containsOnly("CONTENT TBC: Higher value must not be lower than the lower value.");
        }

        @Test
        void shouldReturnNoError_whenValuesAreValid() {
            Map<String, Object> data = new HashMap<>();
            data.put(
                "claimValue",
                ClaimValue.builder().higherValue(BigDecimal.valueOf(10)).lowerValue(BigDecimal.valueOf(1)).build()
            );
            data.put("claimType", PERSONAL_INJURY_WORK);

            CallbackParams params = callbackParamsOf(data, CallbackType.MID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
            assertThat(response.getData())
                .isEqualTo(
                    Map.of(
                        "claimValue", ClaimValue.builder()
                            .higherValue(BigDecimal.valueOf(10))
                            .lowerValue(BigDecimal.valueOf(1))
                            .build(),
                        "claimType", PERSONAL_INJURY_WORK,
                        "allocatedTrack", SMALL_CLAIM
                    ));
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldAddClaimIssuedDate_whenInvoked() {
            CallbackParams params = callbackParamsOf(new HashMap<>(), CallbackType.ABOUT_TO_SUBMIT);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getData()).containsEntry("claimIssuedDate", LocalDate.now());
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedResponse_whenInvoked() {
            CallbackParams params = callbackParamsOf(new HashMap<>(), CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            String documentLink = "https://www.google.com";
            LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
            String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);

            String body = format(
                "<br />Follow these steps to serve a claim:"
                    + "\n* [Download the sealed claim form](%s) (PDF, 123KB)"
                    + "\n* Send the form, particulars of claim and "
                    + "<a href=\"%s\" target=\"_blank\">a response pack</a> (PDF, 266 KB) to the defendant by %s"
                    + "\n* Confirm service online within 21 days of sending the form, particulars and response pack,"
                    + " before 4pm if you're doing this on the due day",
                documentLink,
                responsePackLink,
                formattedServiceDeadline
            );

            assertThat(response).isEqualToComparingFieldByField(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader("# Your claim has been issued\n## Claim number: TBC")
                    .confirmationBody(body)
                    .build());
        }
    }
}
