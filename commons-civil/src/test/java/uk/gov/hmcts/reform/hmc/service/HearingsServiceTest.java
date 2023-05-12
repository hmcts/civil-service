package uk.gov.hmcts.reform.hmc.service;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hmc.client.HearingsApi;
import uk.gov.hmcts.reform.hmc.exception.HmcException;
import uk.gov.hmcts.reform.hmc.model.hearing.CaseDetailsHearing;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.PartyDetailsModel;

import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class HearingsServiceTest {

    @Mock
    private HearingsApi hearingNoticeApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private HearingsService hearingNoticeService;

    private static final String USER_TOKEN = "user_token";
    private static final String SERVICE_TOKEN = "service_token";
    private static final String HEARING_ID = "hearing_id";

    private final FeignException notFoundFeignException = new FeignException.NotFound(
        "not found message",
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
        "not found response body".getBytes(UTF_8));

    @Nested
    class HearingGetResponses {
        @BeforeEach
        void setUp() {
            when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        }

        @Test
        void shouldGetHearingRequest() throws HmcException {
            HearingGetResponse response = HearingGetResponse.builder()
                .requestDetails(HearingRequestDetails.builder().build())
                .hearingDetails(HearingDetails.builder().build())
                .caseDetails(CaseDetailsHearing.builder().build())
                .partyDetails(List.of(PartyDetailsModel.builder().build()))
                .hearingResponse(HearingResponse.builder().build())
                .build();

            when(hearingNoticeApi.getHearingRequest(
                USER_TOKEN, SERVICE_TOKEN,
                HEARING_ID, null))
                .thenReturn(response);

            HearingGetResponse actualResponse =
                hearingNoticeService.getHearingResponse(USER_TOKEN, HEARING_ID);

            assertThat(actualResponse).isEqualTo(response);
        }

        @Test
        void shouldThrowException_whenGetHearingRequestIsNull() {
            when(hearingNoticeApi.getHearingRequest(
                USER_TOKEN, SERVICE_TOKEN,
                HEARING_ID, null))
                .thenThrow(notFoundFeignException);

            Exception exception = assertThrows(
                HmcException.class,
                () -> hearingNoticeService.getHearingResponse(USER_TOKEN, HEARING_ID)
            );

            String expectedMessage = "Failed to retrieve data from HMC";
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }
}
