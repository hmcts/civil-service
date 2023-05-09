package uk.gov.hmcts.reform.hearings.hearingnotice.service;

import com.fasterxml.jackson.databind.JsonNode;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hearings.hearingnotice.client.HearingNoticeApi;
import uk.gov.hmcts.reform.hearings.hearingnotice.exception.HearingNoticeException;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.UnNotifiedPartiesResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {HearingNoticeApi.class})
class HearingNoticeServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HearingNoticeApi hearingNoticeApi;

    @InjectMocks
    private HearingNoticeService hearingNoticeService;

    private static final String USER_TOKEN = "user_token";
    private static final String SERVICE_TOKEN = "service_token";
    private static final String HEARING_ID = "hearing_id";
    private static final String HEARING_ID_2 = "hearing_id-2";
    private static final String HMCTS_SERVICE_CODE = "hmcts-service-code";

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Nested
    class GetPartiesNotifiedResponses {
        private final LocalDateTime time = LocalDateTime.of(2023, 5, 1, 15, 0);
        private List<PartiesNotifiedResponse> listOfPartiesNotifiedResponses =
            List.of(getPartiesNotified(time.minusDays(2), 1, time, null),
                    getPartiesNotified(time.minusDays(3), 2, time, null));

        private PartiesNotifiedResponse getPartiesNotified(LocalDateTime responseReceivedDateTime, Integer requestVersion,
                                                           LocalDateTime partiesNotified, JsonNode serviceData) {
            return PartiesNotifiedResponse.builder().responseReceivedDateTime(responseReceivedDateTime)
                .requestVersion(requestVersion).partiesNotified(partiesNotified).serviceData(serviceData).build();
        }

        private PartiesNotifiedResponses getPartiesNotifiedResponse() {
            return new PartiesNotifiedResponses(HEARING_ID, listOfPartiesNotifiedResponses);
        }

        @Test
        void shouldGetPartiesResponses_whenInvoked() {
            when(hearingNoticeApi.getPartiesNotifiedRequest(USER_TOKEN, SERVICE_TOKEN, HEARING_ID))
                .thenReturn(getPartiesNotifiedResponse());
            PartiesNotifiedResponses result = hearingNoticeService
                .getPartiesNotifiedResponses(USER_TOKEN, HEARING_ID);

            assertThat(result.getHearingID()).isEqualTo(HEARING_ID);
            assertThat(result.getResponses()).isEqualTo(listOfPartiesNotifiedResponses);
        }

        @Test
        void shouldThrowException_whenExceptionError() {
            when(hearingNoticeApi.getPartiesNotifiedRequest(USER_TOKEN, SERVICE_TOKEN, HEARING_ID))
                .thenThrow(new FeignException.Forbidden(
                    "forbidden message",
                    Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
                    "forbidden response body".getBytes(UTF_8)));

            Exception exception = assertThrows(HearingNoticeException.class, () -> {
                hearingNoticeService
                    .getPartiesNotifiedResponses(USER_TOKEN, HEARING_ID);
            });

            String expectedMessage = "Failed to retrieve data from HMC";
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class GetUnNotifiedHearingResponses {
        private List<String> listOfIds = List.of(HEARING_ID, HEARING_ID_2);
        private final LocalDateTime dateFrom = LocalDateTime.of(2023, 5, 1, 15, 0);
        private final LocalDateTime dateTo = LocalDateTime.of(2023, 5, 6, 15, 0);

        private UnNotifiedPartiesResponse getUnNotifiedParties() {
            return new UnNotifiedPartiesResponse(listOfIds, 2L);
        }

        @Test
        void shouldGetNotifiedHearingResponses_whenInvoked() {
            when(hearingNoticeApi.getUnNotifiedHearingRequest(USER_TOKEN, SERVICE_TOKEN, HMCTS_SERVICE_CODE, dateFrom, dateTo))
                .thenReturn(getUnNotifiedParties());

            UnNotifiedPartiesResponse result = hearingNoticeService
                .getUnNotifiedHearingResponses(USER_TOKEN, HMCTS_SERVICE_CODE, dateFrom, dateTo);

            assertThat(result.getTotalFound()).isEqualTo(2L);
            assertThat(result.getHearingIds()).isEqualTo(listOfIds);
        }

        @Test
        void shouldThrowException_whenExceptionError() {
            when(hearingNoticeApi.getUnNotifiedHearingRequest(USER_TOKEN, SERVICE_TOKEN, HMCTS_SERVICE_CODE, dateFrom, dateTo))
                .thenThrow(new FeignException.Forbidden(
                    "forbidden message",
                    Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
                    "forbidden response body".getBytes(UTF_8)));

            Exception exception = assertThrows(HearingNoticeException.class, () -> {
                hearingNoticeService.getUnNotifiedHearingResponses(USER_TOKEN, HMCTS_SERVICE_CODE, dateFrom, dateTo);
            });

            String expectedMessage = "Failed to retrieve data from HMC";
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }
}
