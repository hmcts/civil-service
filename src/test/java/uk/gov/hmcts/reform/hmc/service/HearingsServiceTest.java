package uk.gov.hmcts.reform.hmc.service;

import com.fasterxml.jackson.databind.JsonNode;
import feign.FeignException;
import feign.Request;
import org.assertj.core.api.Assertions;
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
import uk.gov.hmcts.reform.hmc.model.hearings.CaseHearing;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedServiceData;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
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
    private static final String HEARING_ID_2 = "hearing_id-2";
    private static final String HMC_STATUS = "Listed";
    private static final String HMCTS_SERVICE_CODE = "hmcts-service-code";
    private static final int VERSION_NUMBER = 1;
    private static final Long CASE_ID = new BigInteger("1234123412341234").longValue();

    private final FeignException notFoundFeignException = new FeignException.NotFound(
        "not found message",
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
        "not found response body".getBytes(UTF_8),
        Map.of());

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
    }

    @Nested
    class HearingGetResponses {
        @Test
        void shouldGetHearingRequest() throws HmcException {
            PartyDetailsModel partyDetails = new PartyDetailsModel();
            HearingGetResponse response = new HearingGetResponse()
                .setRequestDetails(new HearingRequestDetails())
                .setHearingDetails(new HearingDetails())
                .setCaseDetails(new CaseDetailsHearing())
                .setPartyDetails(List.of(partyDetails))
                .setHearingResponse(new HearingResponse());

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

    @Nested
    class GetPartiesNotifiedResponses {
        private final LocalDateTime time = LocalDateTime.of(2023, 5, 1, 15, 0);
        private final List<PartiesNotifiedResponse> listOfPartiesNotifiedResponses =
            List.of(getPartiesNotified(time.minusDays(2), 1, time, null),
                    getPartiesNotified(time.minusDays(3), 2, time, null));

        private PartiesNotifiedResponse getPartiesNotified(LocalDateTime responseReceivedDateTime, Integer requestVersion,
                                                           LocalDateTime partiesNotified, JsonNode serviceData) {
            return new PartiesNotifiedResponse().setResponseReceivedDateTime(responseReceivedDateTime)
                .setRequestVersion(requestVersion).setPartiesNotified(partiesNotified).setServiceData(
                    new PartiesNotifiedServiceData());
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

            Assertions.assertThat(result.getHearingID()).isEqualTo(HEARING_ID);
            Assertions.assertThat(result.getResponses()).isEqualTo(listOfPartiesNotifiedResponses);
        }

        @Test
        void shouldThrowException_whenExceptionError() {
            when(hearingNoticeApi.getPartiesNotifiedRequest(USER_TOKEN, SERVICE_TOKEN, HEARING_ID))
                .thenThrow(notFoundFeignException);

            Exception exception = assertThrows(HmcException.class, () -> hearingNoticeService
                .getPartiesNotifiedResponses(USER_TOKEN, HEARING_ID)
            );

            String expectedMessage = "Failed to retrieve data from HMC";
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class UpdatedPartiedNotifiedResponses {
        private final LocalDateTime time = LocalDateTime.of(2023, 5, 1, 15, 0);
        private final  PartiesNotified partiesNotified = new PartiesNotified().setServiceData(null);

        @Test
        void shouldUpdatePartiesResponses_whenInvoked() {
            // when
            hearingNoticeService.updatePartiesNotifiedResponse(USER_TOKEN, HEARING_ID, VERSION_NUMBER, time, partiesNotified);

            //then
            verify(hearingNoticeApi).updatePartiesNotifiedRequest(USER_TOKEN, SERVICE_TOKEN, partiesNotified, HEARING_ID, VERSION_NUMBER, time);
        }

        @Test
        void shouldThrowException_whenExceptionError() {

            when(hearingNoticeApi.updatePartiesNotifiedRequest(USER_TOKEN, SERVICE_TOKEN, partiesNotified, HEARING_ID, VERSION_NUMBER, time))
                .thenThrow(notFoundFeignException);

            Exception exception = assertThrows(HmcException.class, () -> hearingNoticeService
                .updatePartiesNotifiedResponse(USER_TOKEN, HEARING_ID, VERSION_NUMBER, time, partiesNotified)
            );

            String expectedMessage = "Failed to retrieve data from HMC";
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class GetUnNotifiedHearingResponses {
        private final List<String> listOfIds = List.of(HEARING_ID, HEARING_ID_2);
        private final LocalDateTime dateFrom = LocalDateTime.of(2023, 5, 1, 15, 0);
        private final LocalDateTime dateTo = LocalDateTime.of(2023, 5, 6, 15, 0);

        private UnNotifiedHearingResponse getUnNotifiedParties() {
            return new UnNotifiedHearingResponse(listOfIds, 2L);
        }

        @Test
        void shouldGetNotifiedHearingResponses_whenInvoked() {
            when(hearingNoticeApi.getUnNotifiedHearingRequest(USER_TOKEN, SERVICE_TOKEN, HMCTS_SERVICE_CODE, dateFrom, dateTo))
                .thenReturn(getUnNotifiedParties());

            UnNotifiedHearingResponse result = hearingNoticeService
                .getUnNotifiedHearingResponses(USER_TOKEN, HMCTS_SERVICE_CODE, dateFrom, dateTo);

            Assertions.assertThat(result.getTotalFound()).isEqualTo(2L);
            Assertions.assertThat(result.getHearingIds()).isEqualTo(listOfIds);
        }

        @Test
        void shouldThrowException_whenExceptionError() {
            when(hearingNoticeApi.getUnNotifiedHearingRequest(USER_TOKEN, SERVICE_TOKEN, HMCTS_SERVICE_CODE, dateFrom, dateTo))
                .thenThrow(notFoundFeignException);

            Exception exception = assertThrows(HmcException.class, () -> hearingNoticeService.getUnNotifiedHearingResponses(USER_TOKEN, HMCTS_SERVICE_CODE, dateFrom, dateTo));

            String expectedMessage = "Failed to retrieve data from HMC";
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }

    @Nested
    class GetHearings {

        @Test
        void shouldGetNotifiedHearingResponses_whenInvoked() {
            HearingsResponse hearings = new HearingsResponse()
                .setHmctsServiceCode(HMCTS_SERVICE_CODE)
                .setCaseRef(CASE_ID.toString())
                .setCaseHearings(List.of(new CaseHearing()));

            when(hearingNoticeApi.getHearings(USER_TOKEN, SERVICE_TOKEN, CASE_ID, HMC_STATUS))
                .thenReturn(hearings);

            HearingsResponse result = hearingNoticeService
                .getHearings(USER_TOKEN, CASE_ID, HMC_STATUS);

            Assertions.assertThat(result).isEqualTo(hearings);
        }

        @Test
        void shouldThrowException_whenExceptionError() {
            when(hearingNoticeApi.getHearings(SERVICE_TOKEN, SERVICE_TOKEN, CASE_ID, HMC_STATUS))
                .thenThrow(notFoundFeignException);

            Exception exception = assertThrows(HmcException.class, () -> hearingNoticeService.getHearings(SERVICE_TOKEN, CASE_ID, HMC_STATUS));

            String expectedMessage = "Failed to retrieve data from HMC";
            String actualMessage = exception.getMessage();

            assertTrue(actualMessage.contains(expectedMessage));
        }
    }
}
