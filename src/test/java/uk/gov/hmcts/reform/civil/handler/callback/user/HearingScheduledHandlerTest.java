package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
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
import uk.gov.hmcts.reform.civil.enums.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.repositories.HearingReferenceNumberRepository;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    HearingScheduledHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseDetailsConverter.class
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

    @MockBean
    private Time time;


    @BeforeEach
    public void prepareTest() {
        given(time.now()).willReturn(LocalDateTime.now());

        Set<LocalDate> publicHolidays = new HashSet<>();
        publicHolidays.add(time.now().toLocalDate().plusDays(3));
        given(publicHolidaysCollection.getPublicHolidays()).willReturn(publicHolidays);

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

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingFee()).isEqualTo(
            Fee.builder().calculatedAmountInPence(new BigDecimal(54500)).build());
    }

    @Test
    void shouldGetDueDateAndFeeMultiClaim_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingFee()).isEqualTo(
            Fee.builder().calculatedAmountInPence(new BigDecimal(117500)).build());
    }

    @Test
    void shouldGetDueDateAndFeeFastClaim_whenAboutToSubmit() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .addRespondent2(NO)
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .hearingDate(time.now().toLocalDate().plusWeeks(5))
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        // When
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        // Then
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
        assertThat(updatedData.getHearingFee()).isEqualTo(
            Fee.builder().calculatedAmountInPence(new BigDecimal(2700)).build());
    }

    @Test
    void shouldReturnHearingNoticeCreated_WhenSubmitted() {
        // Given
        given(hearingReferenceNumberRepository.getHearingReferenceNumber()).willReturn("000HN001");

        String header = "# Hearing notice created\n"
            + "# Your reference number\n" + "# 000HN001";

        String body = "%n%n You may need to complete other tasks for the hearing"
            + ", for example, book an interpreter.";

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .build();

        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        // When
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
