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
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.HearingReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.bankholidays.PublicHolidaysCollection;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    HearingScheduledHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class,
})
public class HearingScheduledHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private HearingScheduledHandler handler;
    @MockBean
    private LocationRefDataService locationRefDataService;
    @MockBean
    private HearingReferenceNumberRepository hearingReferenceNumberRepository;
    @MockBean
    private PublicHolidaysCollection publicHolidaysCollection;

    @Nested
    class MidEventCheckLocationListCallback {

        private static final String PAGE_ID = "locationName";

        @Test
        void shouldReturnLocationList_whenLocationsAreQueried() {
            List<LocationRefData> locations = new ArrayList<>();
            locations.add(LocationRefData.builder().siteName("Site Name").courtAddress("Address").postcode("28000")
                              .build());
            when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(((Map)((ArrayList)((Map)(response.getData().get("hearingLocation"))).get("list_items")).get(0))
                           .get("label")).isEqualTo("Site Name - Address - 28000");
        }
    }

    @Nested
    class MidEventCheckPastDateCallback {

        private static final String PAGE_ID = "checkPastDate";

        @Test
        void shouldReturnError_whenDateFromDateEqualToPresentDateProvided() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .dateOfApplication(LocalDate.now())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("The Date must be in the past");
        }

        @Test
        void shouldReturnOk_whenDateFromDateNotGreaterThanPresentDateProvided() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .dateOfApplication(LocalDate.now().minusDays(1))
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

    }

    @Nested
    class MidEventCheckFutureDateCallback {

        private static final String PAGE_ID = "checkFutureDate";

        @Test
        void shouldReturnError_whenDateFromDateNotTwentyFourHoursAfterPresentDateProvided() {
            LocalDateTime localDateTime = LocalDateTime.now();
            String hours = "0" + (localDateTime.getHour());
            String minutes = "0" + localDateTime.getMinute();
            hours = hours.substring(hours.length() - 2);
            minutes = minutes.substring(minutes.length() - 2);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .hearingDate(LocalDate.from(localDateTime)).hearingTimeHourMinute(hours + minutes).build();

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors().get(0)).isEqualTo("The Date & Time must be 24hs in advance from now");
        }

        @Test
        void shouldNotReturnError_whenDateFromDateIsTwentyFourHoursAfterOfPresentDateProvided() {
            LocalDateTime localDateTime = LocalDateTime.now().plusHours(24).plusMinutes(1);
            String hours = "0" + (localDateTime.getHour());
            String minutes = "0" + String.valueOf(localDateTime.getMinute());
            hours = hours.substring(hours.length() - 2);
            minutes = minutes.substring(minutes.length() - 2);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .hearingDate(LocalDate.from(localDateTime)).hearingTimeHourMinute(hours + minutes).build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldGetDueDateAndFeeSmallClaim_whenAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .hearingDate(LocalDate.now().plusWeeks(5))
                .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Set<LocalDate> publicHolidays = new HashSet<>();
            publicHolidays.add(LocalDate.now().plusDays(3));
            when(publicHolidaysCollection.getPublicHolidays()).thenReturn(publicHolidays);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHearingFee()).isEqualTo(
                Fee.builder().calculatedAmountInPence(new BigDecimal(545)).build());
        }

        @Test
        void shouldGetDueDateAndFeeMultiClaim_whenAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .hearingDate(LocalDate.now().plusWeeks(5))
                .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Set<LocalDate> publicHolidays = new HashSet<>();
            publicHolidays.add(LocalDate.now().plusDays(3));
            when(publicHolidaysCollection.getPublicHolidays()).thenReturn(publicHolidays);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHearingFee()).isEqualTo(
                Fee.builder().calculatedAmountInPence(new BigDecimal(1175)).build());
        }

        @Test
        void shouldGetDueDateAndFeeFastClaim_whenAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addRespondent2(NO)
                .listingOrRelisting(ListingOrRelisting.LISTING)
                .hearingDate(LocalDate.now().plusWeeks(5))
                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            Set<LocalDate> publicHolidays = new HashSet<>();
            publicHolidays.add(LocalDate.now().plusDays(3));
            when(publicHolidaysCollection.getPublicHolidays()).thenReturn(publicHolidays);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
            assertThat(updatedData.getHearingFee()).isEqualTo(
                Fee.builder().calculatedAmountInPence(new BigDecimal(27)).build());
        }
    }

    /*   @Nested

class SubmittedCallback {

        @Test
        void shouldReturnHearingNoticeCreated_WhenSubmitted() {
            when(hearingReferenceNumberRepository.getHearingReferenceNumber()).thenReturn("000HN001");

            String header = "# Hearing notice created\n"
                + "# Your reference number\n" + "# 000HN001";

            String body = "%n%n You may need to complete other tasks for the hearing"
                + ", for example, book an interpreter.";

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .build();

            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                          .confirmationHeader(header)
                                                                          .confirmationBody(String.format(body))
                                                                          .build());
        }
    }*/
}


