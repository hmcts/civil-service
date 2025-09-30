package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.GeneralAppLocationRefDataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    HearingScheduledEventCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
})
class HearingScheduledEventCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private HearingScheduledEventCallbackHandler handler;
    @MockBean
    private GeneralAppLocationRefDataService locationRefDataService;

    @Nested
    class AboutToStartCallbackHandling {

        @Test
        void shouldReturnLocationList_whenLocationsAreQueried() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("Site Name 1").courtAddress("Address1").postcode("18000")
                              .build());
            locations.add(LocationRefData.builder().siteName("Site Name 2").courtAddress("Address2").postcode("28000")
                              .build());
            when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);
            CaseData caseData = CaseDataBuilder.builder().ccdState(CaseState.LISTING_FOR_A_HEARING).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(((Map) ((ArrayList) ((Map) ((Map) (response.getData().get("gaHearingNoticeDetail")))
                .get("hearingLocation")).get("list_items")).get(0))
                           .get("label")).isEqualTo("Site Name 1 - Address1 - 18000");
        }

        @Test
        void shouldNotPrepopulateData_whenCcdStateIsOrderMade() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("Site Name 1").courtAddress("Address1").postcode("18000")
                              .build());
            locations.add(LocationRefData.builder().siteName("Site Name 2").courtAddress("Address2").postcode("28000")
                              .build());
            when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);
            CaseData caseData = CaseDataBuilder.builder().ccdState(CaseState.ORDER_MADE).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertNull(response.getData().get("gaHearingNoticeApplication"));
            assertNull(response.getData().get("gaHearingNoticeInformation"));
        }

        @Test
        void shouldReturnLocationList_with_preferredLocationSelected_whenLocationsAreQueried() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("Site Name 1").courtAddress("Address1").postcode("18000")
                    .build());
            locations.add(LocationRefData.builder().siteName("Site Name 2").courtAddress("Address2").postcode("28000")
                    .build());
            DynamicListElement location1 = DynamicListElement.builder()
                    .code(String.valueOf(UUID.randomUUID())).label("Site Name 2 - Address2 - 28000").build();

            when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);
            GAJudgesHearingListGAspec gaJudgesHearingListGAspec =
                    GAJudgesHearingListGAspec.builder().hearingPreferredLocation(DynamicList.builder()
                    .listItems(List.of(location1))
                    .value(location1).build()).build();
            CaseData caseData = CaseData.builder().ccdState(CaseState.LISTING_FOR_A_HEARING)
                .judicialListForHearing(gaJudgesHearingListGAspec).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            String label = ((Map)((Map)((Map)(response.getData().get("gaHearingNoticeDetail")))
                    .get("hearingLocation")).get("value"))
                    .get("label")
                    .toString();
            assertThat(label).isEqualTo("Site Name 2 - Address2 - 28000");
        }

        @Test
        void shouldNotReturnLocationList_with_preferredLocationSelected_whenLocationsAreQueried() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("Site Name 1").courtAddress("Address1").postcode("18000")
                              .build());
            locations.add(LocationRefData.builder().siteName("Site Name 2").courtAddress("Address2").postcode("28000")
                              .build());
            DynamicListElement location1 = DynamicListElement.builder()
                .code(String.valueOf(UUID.randomUUID())).label("Site Name 2 - Address2 - 28000").build();

            when(locationRefDataService.getCourtLocations(any())).thenReturn(locations);
            GAJudgesHearingListGAspec gaJudgesHearingListGAspec =
                GAJudgesHearingListGAspec.builder().hearingPreferredLocation(DynamicList.builder()
                                                                                 .listItems(List.of(location1))
                                                                                 .value(location1).build()).build();
            CaseData caseData = CaseData.builder().ccdState(CaseState.ORDER_MADE)
                .judicialListForHearing(gaJudgesHearingListGAspec).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(((Map) ((ArrayList) ((Map) ((Map) (response.getData().get("gaHearingNoticeDetail")))
                .get("hearingLocation")).get("list_items")).get(0))
                           .get("label")).isEqualTo("Site Name 1 - Address1 - 18000");
        }
    }

    @Nested
    class MidEventCheckFutureDateCallback {

        private static final String PAGE_ID = "hearing-check-date";

        @Test
        void shouldReturnError_whenDateFromDateNotTwentyFourHoursAfterPresentDateProvided() {
            LocalDateTime localDateTime = LocalDateTime.now();
            String hours = "0" + (localDateTime.getHour());
            String minutes = "0" + localDateTime.getMinute();
            hours = hours.substring(hours.length() - 2);
            minutes = minutes.substring(minutes.length() - 2);
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                    .gaHearingNoticeDetail(GAHearingNoticeDetail.builder()
                    .hearingDate(LocalDate.from(localDateTime))
                    .hearingTimeHourMinute(hours + minutes).build())
                    .build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("Hearing date must be in the future");
        }

        @Test
        void shouldNotReturnError_whenDateFromDateIsTwentyFourHoursAfterOfPresentDateProvided() {
            LocalDateTime localDateTime = LocalDateTime.now().plusHours(24).plusMinutes(1);
            String hours = "0" + (localDateTime.getHour());
            String minutes = "0" + String.valueOf(localDateTime.getMinute());
            hours = hours.substring(hours.length() - 2);
            minutes = minutes.substring(minutes.length() - 2);
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                    .gaHearingNoticeDetail(GAHearingNoticeDetail.builder()
                            .hearingDate(LocalDate.from(localDateTime))
                            .hearingTimeHourMinute(hours + minutes).build())
                    .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldGetDueDateAndFeeSmallClaim_whenAboutToSubmit() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("Site Name 1").courtAddress("Address1").postcode("18000")
                    .build());
            locations.add(LocationRefData.builder().siteName("Site Name 2").courtAddress("Address2").postcode("28000")
                    .build());
            DynamicListElement location1 = DynamicListElement.builder()
                    .code(String.valueOf(UUID.randomUUID())).label("Site Name 2 - Address2 - 28000").build();
            CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
                    .gaHearingNoticeDetail(GAHearingNoticeDetail.builder()
                            .hearingLocation(DynamicList.builder()
                                    .listItems(List.of(location1))
                                    .value(location1).build()).build())
                    .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getGaHearingNoticeDetail().getHearingLocation().getListItems()).isNull();
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnHearingNoticeCreated_WhenSubmitted() {

            String header = "# Hearing notice created\n"
                + "##### You may need to complete other tasks for the\n"
                + "##### hearing for example, book an interpreter.<br/>" + "<br/>";
            String body = "<br/> <br/>";
            CaseData caseData = CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.YES).build().toBuilder()
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(header)
                                                                          .confirmationBody(body)
                                                                          .build());
        }
    }
}
