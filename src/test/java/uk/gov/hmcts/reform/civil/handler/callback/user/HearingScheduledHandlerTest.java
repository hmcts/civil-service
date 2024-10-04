package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.HEARING_SCHEDULED;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class HearingScheduledHandlerTest extends BaseCallbackHandlerTest {

    private ObjectMapper mapper;

    private HearingScheduledHandler handler;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    @Mock
    private HearingFeesService feesService;

    @Mock
    private Time time;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        handler = new HearingScheduledHandler(locationRefDataService, mapper, time, feesService, featureToggleService);
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
        given(locationRefDataService.getHearingCourtLocations(any())).willReturn(locations);

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
        given(time.now()).willReturn(LocalDateTime.now());

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
        given(time.now()).willReturn(LocalDateTime.now());

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
        given(time.now()).willReturn(LocalDateTime.now());

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
        given(time.now()).willReturn(LocalDateTime.now());

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
        given(time.now()).willReturn(LocalDateTime.now());

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
        "LISTING,SMALL_CLAIMS,HEARING_READINESS",
        "RELISTING,SMALL_CLAIMS,PREPARE_FOR_HEARING_CONDUCT_HEARING",
        "LISTING,OTHER,PREPARE_FOR_HEARING_CONDUCT_HEARING",
        "RELISTING,OTHER,PREPARE_FOR_HEARING_CONDUCT_HEARING"
    })
    void shouldSetHearingReadinessStateOnListing_whenAboutToSubmitNonMinti(String listingType, String hearingNoticeType, String expectedStateStr) {
        given(time.now()).willReturn(LocalDateTime.now());
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);

        // Given: a case either in listing or relisting
        ListingOrRelisting listingOrRelisting = ListingOrRelisting.valueOf(listingType);
        HearingNoticeList hearingNoticeList = HearingNoticeList.valueOf(hearingNoticeType);
        CaseState expectedState = CaseState.valueOf(expectedStateStr);  // converting the string would be redundant but ensures there are no typos

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .hearingDate(time.now().toLocalDate().plusWeeks(2))
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .hearingNoticeList(hearingNoticeList)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .listingOrRelisting(listingOrRelisting)
            .ccdState(CaseState.CASE_PROGRESSION)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When: I call the handler
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then: I expect the resulting state to match the expectation for the listing or relisting
        assertThat(response.getState()).isEqualTo(expectedState.name());
    }

    @ParameterizedTest
    @CsvSource({
        // listing/relisting,caseType, currentState, expectState
        "LISTING,SMALL_CLAIMS,HEARING_READINESS,HEARING_READINESS",
        "LISTING,SMALL_CLAIMS,PREPARE_FOR_HEARING_CONDUCT_HEARING,PREPARE_FOR_HEARING_CONDUCT_HEARING",
        "LISTING,SMALL_CLAIMS,DECISION_OUTCOME,DECISION_OUTCOME",
        "LISTING,SMALL_CLAIMS,CASE_PROGRESSION,HEARING_READINESS",
        "LISTING,OTHER,CASE_PROGRESSION,HEARING_READINESS",
        "RELISTING,SMALL_CLAIMS,CASE_PROGRESSION,HEARING_READINESS",
        "RELISTING,OTHER,CASE_PROGRESSION,HEARING_READINESS"
    })
    void shouldSetHearingReadinessStateOnListing_whenAboutToSubmitMinti(String listingType, String hearingNoticeType, String currentState, String expectedStateStr) {
        given(time.now()).willReturn(LocalDateTime.now());
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        // Given: a case either in listing or relisting
        ListingOrRelisting listingOrRelisting = ListingOrRelisting.valueOf(listingType);
        HearingNoticeList hearingNoticeList = HearingNoticeList.valueOf(hearingNoticeType);
        CaseState expectedState = CaseState.valueOf(expectedStateStr);  // converting the string would be redundant but ensures there are no typos

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .hearingDate(time.now().toLocalDate().plusWeeks(2))
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .hearingNoticeList(hearingNoticeList)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .listingOrRelisting(listingOrRelisting)
            .ccdState(CaseState.valueOf(currentState))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When: I call the handler
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then: I expect the resulting state to match the expectation for the listing or relisting
        assertThat(response.getState()).isEqualTo(expectedState.name());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGetDueDateAndFeeSmallClaim_whenAboutToSubmit(boolean toggle) {
        given(time.now()).willReturn(LocalDateTime.now());
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(toggle);

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(2))
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .ccdState(CaseState.CASE_PROGRESSION)
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
        given(time.now()).willReturn(LocalDateTime.now());

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingLocation(DynamicList.builder().listItems(List.of(
                DynamicListElement.builder().label("element 1").code("E0").build(),
                DynamicListElement.builder().label("element 2").code("E1").build())).build())
            .addRespondent2(NO)
            .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS)
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
        assertThat(updatedData.getHearingLocation()).isInstanceOf(DynamicList.class);
        DynamicList hearingLocation = updatedData.getHearingLocation();
        assertThat(hearingLocation.getValue()).isNull();
        assertThat(hearingLocation.getListItems()).isNull();
    }

    @Test
    void shouldTriggerBusinessProcessHearingScheduledOnRelisting_whenAboutToSubmit() {
        given(time.now()).willReturn(LocalDateTime.now());

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS)
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGetDueDateAndFeeFastAndClaimValueClaim_whenAboutToSubmit(boolean toggle) {
        given(time.now()).willReturn(LocalDateTime.now());
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(toggle);

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .addRespondent2(NO)
            .hearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(null)
            .responseClaimTrack("FAST_CLAIM")
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .ccdState(CaseState.CASE_PROGRESSION)
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGetDueDateAndFeeFastAndNoClaimValueClaim_whenAboutToSubmit(boolean toggle) {
        given(time.now()).willReturn(LocalDateTime.now());
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(toggle);

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .caseAccessCategory(SPEC_CLAIM)
            .addRespondent2(NO)
            .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(null)
            .responseClaimTrack("SMALL_CLAIM")
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .claimValue(null)
            .totalInterest(BigDecimal.TEN)
            .totalClaimAmount(new BigDecimal(1000))
            .ccdState(CaseState.CASE_PROGRESSION)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        Fee expectedFee = Fee.builder()
            .calculatedAmountInPence(new BigDecimal(54500)).code("FEE0441").version("1").build();
        given(feesService.getFeeForHearingSmallClaims(any())).willReturn(expectedFee);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingFee()).isEqualTo(expectedFee);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGetDueDateAndFeeFastClaim_whenAboutToSubmit(boolean toggle) {
        given(time.now()).willReturn(LocalDateTime.now());
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(toggle);

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .claimValue(null)
            .totalClaimAmount(new BigDecimal(123))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .ccdState(CaseState.CASE_PROGRESSION)
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGetDueDateAndFeeMultiClaim_whenAboutToSubmit(boolean toggle) {
        given(time.now()).willReturn(LocalDateTime.now());
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(toggle);

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .hearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .totalClaimAmount(new BigDecimal(123000))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .ccdState(CaseState.CASE_PROGRESSION)
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

    @ParameterizedTest
    @EnumSource(
        value = CaseState.class,
        names = {"HEARING_READINESS", "PREPARE_FOR_HEARING_CONDUCT_HEARING", "DECISION_OUTCOME", "All_FINAL_ORDERS_ISSUED"})
    void shouldNotOverwriteCaseState_listingNonOther_whenAboutToSubmit(CaseState caseState) {
        given(time.now()).willReturn(LocalDateTime.now());

        Fee fee = Fee.builder().code("code").calculatedAmountInPence(BigDecimal.valueOf(100)).version("999").build();
        LocalDate hearingDueDate = LocalDate.of(2030, 1, 1);

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .hearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .totalClaimAmount(new BigDecimal(123000))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .ccdState(caseState)
            .hearingDueDate(hearingDueDate)
            .hearingFee(fee)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getState()).isEqualTo(caseState.toString());
    }

    @ParameterizedTest
    @EnumSource(
        value = CaseState.class,
        names = {"HEARING_READINESS", "PREPARE_FOR_HEARING_CONDUCT_HEARING", "DECISION_OUTCOME", "All_FINAL_ORDERS_ISSUED"})
    void shouldNotOverwriteCaseState_reListing_whenAboutToSubmit(CaseState caseState) {
        given(time.now()).willReturn(LocalDateTime.now());

        Fee fee = Fee.builder().code("code").calculatedAmountInPence(BigDecimal.valueOf(100)).version("999").build();
        LocalDate hearingDueDate = LocalDate.of(2030, 1, 1);

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .hearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL)
            .listingOrRelisting(ListingOrRelisting.RELISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .totalClaimAmount(new BigDecimal(123000))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .ccdState(caseState)
            .hearingDueDate(hearingDueDate)
            .hearingFee(fee)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getState()).isEqualTo(caseState.toString());
    }

    @ParameterizedTest
    @EnumSource(
        value = CaseState.class,
        names = {"HEARING_READINESS", "PREPARE_FOR_HEARING_CONDUCT_HEARING", "DECISION_OUTCOME", "All_FINAL_ORDERS_ISSUED"})
    void shouldNotOverwriteCaseState_listingOther_whenAboutToSubmit(CaseState caseState) {
        given(time.now()).willReturn(LocalDateTime.now());

        Fee fee = Fee.builder().code("code").calculatedAmountInPence(BigDecimal.valueOf(100)).version("999").build();
        LocalDate hearingDueDate = LocalDate.of(2030, 1, 1);

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .hearingNoticeList(HearingNoticeList.OTHER)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .totalClaimAmount(new BigDecimal(123000))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .ccdState(caseState)
            .hearingDueDate(hearingDueDate)
            .hearingFee(fee)
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        assertThat(response.getState()).isEqualTo(caseState.toString());
    }

    @Test
    void shouldReturnHearingNoticeCreated_WhenSubmitted() {
        // Given
        String header = """
            # Hearing notice created
            # Your reference number
            # 000HN001""";

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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotGetDueDateAndFeeCalculationAndIsOther_whenAboutToSubmit(boolean toggle) {
        given(time.now()).willReturn(LocalDateTime.now());
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(toggle);

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingNoticeList(HearingNoticeList.OTHER)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .totalClaimAmount(new BigDecimal(12300))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingFee()).isNull();
        assertThat(updatedData.getHearingDueDate()).isNull();
    }

    @Test
    void shouldNotGetHearingFee_shouldRecalculateHearingDueDate_whenAboutToSubmitRelisting() {
        given(time.now()).willReturn(LocalDateTime.now());
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS)
            .listingOrRelisting(ListingOrRelisting.RELISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.SMALL_CLAIM)
            .totalClaimAmount(new BigDecimal(12300))
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingDueDate()).isNotNull();
    }

    @Test
    void shouldTriggerBusinessProcessHearingScheduledOtherAndRelisting_whenAboutToSubmit() {
        given(time.now()).willReturn(LocalDateTime.now());

        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .hearingNoticeList(HearingNoticeList.OTHER)
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

    private String prepareHHmmString(LocalDateTime localDateTime) {
        String hours = "0" + (localDateTime.getHour());
        String minutes = "0" + localDateTime.getMinute();
        hours = hours.substring(hours.length() - 2);
        minutes = minutes.substring(minutes.length() - 2);
        return hours + minutes;
    }
}
