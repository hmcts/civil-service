package uk.gov.hmcts.reform.hearings.hearingnotice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.hearings.hearingnotice.client.HearingNoticeApi;
import uk.gov.hmcts.reform.hearings.hearingnotice.exception.GetHearingException;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.CaseDetailsHearing;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.HearingDetails;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.HearingGetResponse;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.HearingRequestDetails;
import uk.gov.hmcts.reform.hearings.hearingnotice.model.HearingResponse;
import uk.gov.hmcts.reform.hearings.hearingrequest.model.PartyDetailsModel;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class HearingNoticeServiceTest {

    @Mock
    private HearingNoticeApi hearingNoticeApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private HearingNoticeService hearingNoticeService;

    private static final String AUTHORIZATION = "auth";
    private static final String SERVICE_AUTHORIZATION = "service auth";
    private static final String HEARING_ID = "HER123123";

    @Nested
    class HearingGetResponses {
        @BeforeEach
        void setUp() {
            when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        }

        @Test
        void shouldGetHearingRequest() throws GetHearingException {
            HearingGetResponse response = HearingGetResponse.builder()
                .requestDetails(HearingRequestDetails.builder().build())
                .hearingDetails(HearingDetails.builder().build())
                .caseDetails(CaseDetailsHearing.builder().build())
                .partyDetails(List.of(PartyDetailsModel.builder().build()))
                .hearingResponse(HearingResponse.builder().build())
                .build();

            when(hearingNoticeApi.getHearingRequest(
                AUTHORIZATION, SERVICE_AUTHORIZATION,
                HEARING_ID, null))
                .thenReturn(response);

            HearingGetResponse actualResponse =
                hearingNoticeService.getHearingResponse(AUTHORIZATION, HEARING_ID);

            assertThat(actualResponse).isEqualTo(response);
        }

        @Test
        void shouldThrowExceptionWhenGetHearingRequestIsNull() {
            when(hearingNoticeApi.getHearingRequest(
                AUTHORIZATION, SERVICE_AUTHORIZATION,
                HEARING_ID, null))
                .thenReturn(null);

            assertThrows(GetHearingException.class,
                         () -> hearingNoticeService.getHearingResponse(AUTHORIZATION, HEARING_ID));
        }
    }
}
