package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    HearingScheduledHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class
})
class HearingScheduledHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private HearingScheduledHandler handler;
    @MockBean
    private LocationRefDataService locationRefDataService;
    @MockBean
    private HearingFeesService feesService;

    @MockBean
    private Time time;

    @BeforeEach
    public void prepareTest() {
        given(time.now()).willReturn(LocalDateTime.now());
        given(feesService.getFeeForHearingSmallClaims(any())).willReturn(Fee.builder().build());
        given(feesService.getFeeForHearingFastTrackClaims(any())).willReturn(Fee.builder().build());
    }

    @Test
    void shouldNullPreviousSubmittedEventSelections_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingNoticeList(HearingNoticeList.OTHER)
            .hearingNoticeListOther("hearing notice list other")
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingLocation(DynamicList.builder().listItems(List.of(
                DynamicListElement.builder().label("element 1").code("E0").build(),
                DynamicListElement.builder().label("element 2").code("E1").build())).build())
            .channel(HearingChannel.IN_PERSON)
            .hearingDate(LocalDate.now())
            .hearingTimeHourMinute("hearingTimeHourMinute")
            .hearingDuration(HearingDuration.DAY_1)
            .information("hearing info")
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        assertThat(response.getData().get("hearingNoticeList")).isNull();
        assertThat(response.getData().get("listingOrRelisting")).isNull();
        assertThat(response.getData().get("hearingLocation")).isNull();
        assertThat(response.getData().get("channel")).isNull();
        assertThat(response.getData().get("hearingDate")).isNull();
        assertThat(response.getData().get("hearingTimeHourMinute")).isNull();
        assertThat(response.getData().get("hearingDuration")).isNull();
        assertThat(response.getData().get("information")).isNull();
        assertThat(response.getData().get("hearingNoticeListOther")).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "locationName" })
    void shouldReturnLocationList_whenLocationsAreQueried(String pageId) {
        // Given
        List<LocationRefData> locations = new ArrayList<>();
        locations.add(LocationRefData.builder().siteName("Site Name").courtAddress("Address").postcode("28000")
                          .build());
        given(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).willReturn(locations);

        // When
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CallbackParams params = callbackParamsOf(caseData, MID, pageId);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getData())
            .extracting("hearingLocation")
            .extracting("list_items")
            .asList().first()//item 0
            .extracting("label")
            .isEqualTo("Site Name - Address - 28000");
    }

    @ParameterizedTest
    @ValueSource(strings = { "checkPastDate" })
    void shouldReturnError_whenDateFromDateEqualToPresentDateProvided(String pageId) {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .dateOfApplication(time.now().toLocalDate())
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, pageId);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors().get(0)).isEqualTo("The Date must be in the past");
    }

    @ParameterizedTest
    @ValueSource(strings = { "checkPastDate" })
    void shouldReturnOk_whenDateFromDateNotGreaterThanPresentDateProvided(String pageId) {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .dateOfApplication(time.now().toLocalDate().minusDays(1))
            .build();
        CallbackParams params = callbackParamsOf(caseData, MID, pageId);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = { "checkFutureDate" })
    void shouldReturnError_whenDateFromDateNotTwentyFourHoursAfterPresentDateProvided(String pageId) {
        // Given
        LocalDateTime localDateTime = time.now();
        String hhmm = prepareHHmmString(localDateTime);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingDate(LocalDate.from(localDateTime)).hearingTimeHourMinute(hhmm).build();

        CallbackParams params = callbackParamsOf(caseData, MID, pageId);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors().get(0)).isEqualTo("The Date & Time must be 24hs in advance from now");
    }

    @ParameterizedTest
    @ValueSource(strings = { "checkFutureDate" })
    void shouldReturnError_whenHearingTimeNotProvided(String pageId) {
        // Given
        LocalDateTime localDateTime = time.now();
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingDate(LocalDate.from(localDateTime)).hearingTimeHourMinute(null).build();

        CallbackParams params = callbackParamsOf(caseData, MID, pageId);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors().get(0)).isEqualTo("Time is required");
    }

    @ParameterizedTest
    @ValueSource(strings = { "checkFutureDate" })
    void shouldNotReturnError_whenDateFromDateIsTwentyFourHoursAfterOfPresentDateProvided(String pageId) {
        // Given
        LocalDateTime localDateTime = time.now().plusHours(24).plusMinutes(1);
        String hhmm = prepareHHmmString(localDateTime);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingDate(LocalDate.from(localDateTime)).hearingTimeHourMinute(hhmm).build();
        CallbackParams params = callbackParamsOf(caseData, MID, pageId);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        // listing/relisting,case state
        "LISTING,HEARING_READINESS",
        "RELISTING,PREPARE_FOR_HEARING_CONDUCT_HEARING"
    })
    void shouldSetHearingReadinessStateOnListing_whenAboutToSubmit(String listingType, String expectedStateStr) {
        // Given: a case either in listing or relisting
        ListingOrRelisting listingOrRelisting = ListingOrRelisting.valueOf(listingType);
        CaseState expectedState = CaseState.valueOf(expectedStateStr);  // converting the string would be redundant but ensures there are no typos

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .hearingDate(time.now().toLocalDate().plusWeeks(2))
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .listingOrRelisting(listingOrRelisting)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When: I call the handler
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then: I expect the resulting state to match the expectation for the listing or relisting
        assertThat(response.getState()).isEqualTo(expectedState.name());
    }

    @Test
    void shouldGetDueDateAndFeeSmallClaim_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(2))
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        Fee expectedFee = Fee.builder()
            .calculatedAmountInPence(new BigDecimal(34600)).code("FEE0225").version("7").build();
        given(feesService.getFeeForHearingSmallClaims(any())).willReturn(expectedFee);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingFee()).isEqualTo(
            expectedFee);
    }

    @Test
    void shouldSetHearingLocationListItemsNull_whenHearingLocationProvided() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingLocation(DynamicList.builder().listItems(List.of(
                DynamicListElement.builder().label("element 1").code("E0").build(),
                DynamicListElement.builder().label("element 2").code("E1").build())).build())
            .addRespondent2(NO)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(2))
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingLocation()).isNull();
    }

    @Test
    void shouldTriggerBusinessProcessHearingScheduledOnRelisting_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .listingOrRelisting(ListingOrRelisting.RELISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(2))
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo(HEARING_SCHEDULED.name());
    }

    @Test
    void shouldGetDueDateAndFeeFastAndClaimValueClaim_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        Fee expectedFee = Fee.builder()
            .calculatedAmountInPence(new BigDecimal(54500)).code("FEE0441").version("1").build();
        given(feesService.getFeeForHearingFastTrackClaims(any())).willReturn(expectedFee);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingFee()).isEqualTo(expectedFee);
    }

    @Test
    void shouldGetDueDateAndFeeFastAndNoClaimValueClaim_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .claimValue(null)
            .totalInterest(BigDecimal.TEN)
            .totalClaimAmount(new BigDecimal(1000))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        Fee expectedFee = Fee.builder()
            .calculatedAmountInPence(new BigDecimal(54500)).code("FEE0441").version("1").build();
        given(feesService.getFeeForHearingFastTrackClaims(any())).willReturn(expectedFee);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingFee()).isEqualTo(expectedFee);
    }

    @Test
    void shouldGetDueDateAndFeeFastClaim_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(null)
            .claimValue(null)
            .totalClaimAmount(new BigDecimal(123))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        Fee expectedFee = Fee.builder()
            .calculatedAmountInPence(new BigDecimal(2700)).code("FEE0221").version("7").build();
        given(feesService.getFeeForHearingSmallClaims(any())).willReturn(expectedFee);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingFee()).isEqualTo(expectedFee);
    }

    @Test
    void shouldGetDueDateAndFeeMultiClaim_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .totalClaimAmount(new BigDecimal(123000))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        Fee expectedFee = Fee.builder()
            .calculatedAmountInPence(new BigDecimal(117500)).code("FEE0440").version("2").build();
        given(feesService.getFeeForHearingMultiClaims(any())).willReturn(expectedFee);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingFee()).isEqualTo(expectedFee);
    }

    @Test
    void shouldReturnHearingNoticeCreated_WhenSubmitted() {
        // Given
        String header = "# Hearing notice created\n"
            + "# Your reference number\n" + "# 000HN001";

        String body = "%n%n You may need to complete other tasks for the hearing"
            + ", for example, book an interpreter.";

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingReferenceNumber("000HN001")
            .build();
        // When
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
        // Then
        assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                      .confirmationHeader(header)
                                                                      .confirmationBody(String.format(body))
                                                                      .build());
    }

    private String prepareHHmmString(LocalDateTime localDateTime) {
        String hours = "0" + (localDateTime.getHour());
        String minutes = "0" + localDateTime.getMinute();
        hours = hours.substring(hours.length() - 2);
        minutes = minutes.substring(minutes.length() - 2);
        return hours + minutes;
    }
}
