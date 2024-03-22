package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.handler.callback.user.CourtOfficerOrderHandler.HEADER;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    CourtOfficerOrderHandler.class,
    JacksonAutoConfiguration.class
})
public class CourtOfficerOrderHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private CourtOfficerOrderHandler handler;
    @MockBean
    private DocumentHearingLocationHelper locationHelper;
    @MockBean
    private IdamClient idamClient;
    @MockBean
    private WorkingDayIndicator workingDayIndicator;
    @MockBean
    private LocationRefDataService locationRefDataService;

    private static LocationRefData locationRefData =   LocationRefData.builder().siteName("A nice Site Name")
        .courtAddress("1").postcode("1")
        .courtName("Court Name example").region("Region").regionId("2").courtVenueId("666")
        .courtTypeId("10").courtLocationCode("121")
        .epimmsId("000000").build();

    @BeforeEach
    void setup() {
        when(locationHelper.getHearingLocation(any(),any(),any())).thenReturn(locationRefData);
        List<LocationRefData> locationRefDataList = new ArrayList<>();
        locationRefDataList.add(locationRefData);
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(locationRefDataList);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldPopulateValues_whenInvoked() {
            when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class))).thenReturn(LocalDate.now().plusDays(7));
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData().get("courtOfficerFurtherHearingComplex"))
                .extracting("datesToAvoidDateDropdown")
                .extracting("datesToAvoidDates").isEqualTo(LocalDate.now().plusDays(7).toString());
            assertThat(response.getData().get("courtOfficerFurtherHearingComplex"))
                .extracting("hearingLocationList").asString().contains("A nice Site Name");
            assertThat(response.getData().get("courtOfficerFurtherHearingComplex"))
                .extracting("alternativeHearingList").asString().contains("A nice Site Name");

        }

    }

    @Nested
    class MidEventShowCertifyConditionCallback {

        private static final String PAGE_ID = "validateValues";

        @Test
        void shouldNotReturnError_whenNoDate() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .courtOfficerFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                       .listFromDate(null).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
        @Test
        void shouldNoError_whenDateInFuture() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .courtOfficerFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                       .listFromDate(LocalDate.now().plusDays(7)).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldError_whenDateInPast() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .courtOfficerFurtherHearingComplex(FinalOrderFurtherHearing.builder()
                                                       .listFromDate(LocalDate.now().minusDays(7)).build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).contains("List from date cannot be in the past");
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldPopulateConfirmationHeader_WhenSubmitted() {
            // Given
            String confirmationHeader = format(HEADER, 1234567);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .ccdCaseReference(1234567L)
                .build();
            // When
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            var response = (SubmittedCallbackResponse) handler.handle(params);
            // Then
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(confirmationHeader)
                                                                          .build());
        }
    }

        @Test
    void handleEventsReturnsTheExpectedCallbackEvents() {
        assertThat(handler.handledEvents()).containsOnly(COURT_OFFICER_ORDER);
    }

}
