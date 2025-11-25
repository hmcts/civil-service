package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.HearingDates;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class DefaultJudgementHandlerTest extends BaseCallbackHandlerTest {

    private DefaultJudgementHandler handler;
    private ObjectMapper mapper;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        handler = new DefaultJudgementHandler(mapper, locationRefDataService, deadlinesCalculator);
    }

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldNotReturnError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isNotEmpty();
            Assertions.assertTrue(
                response.getErrors().stream()
                    .anyMatch(errorMessage ->
                                  errorMessage.contains(
                                      "The Claim is not eligible for Default Judgment until 5:00pm on ")
                    ));
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedOneDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1ResponseDeadline(
                    LocalDateTime.now().minusDays(15)).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);
            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_WhenAboutToStartIsInvokedWithTwoDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class MidEventShowCertifyConditionCallback {

        private static final String PAGE_ID = "showcertifystatement";

        @Test
        void shouldReturnBoth_whenHaveTwoDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement bothElement = new DynamicListElement(null, "Both");
            DynamicList defendantDetails = new DynamicList();
            defendantDetails.setValue(bothElement);
            caseData.setDefendantDetails(defendantDetails);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).containsEntry("bothDefendants", "Both");
        }

        @Test
        void shouldReturnOne_whenHaveOneDefendants() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            caseData.setRespondent2(PartyBuilder.builder().individual().build());
            caseData.setAddRespondent2(YES);
            caseData.setRespondent2SameLegalRepresentative(YES);
            caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
            DynamicListElement testUserElement = new DynamicListElement(null, "Test User");
            DynamicList defendantDetails = new DynamicList();
            defendantDetails.setValue(testUserElement);
            caseData.setDefendantDetails(defendantDetails);

            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getData()).containsEntry("bothDefendants", "One");
        }

        @Nested
        class MidEventCheckLocationsCallback {

            private static final String PAGE_ID = "checkPreferredLocations";

            @Test
            void shouldReturnLocationList_whenLocationsAreQueried() {
                List<LocationRefData> locations = new ArrayList<>();
                locations.add(LocationRefData.builder().courtName("Court Name").region("Region").build());
                when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getData().get("hearingSupportRequirementsDJ")).isNotNull();
            }
        }

        @Nested
        class MidEventShowCPRAcceptCallback {

            private static final String PAGE_ID = "acceptCPR";

            @Test
            void shouldReturnError_whenCPRisNotAccepted() {
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors()).isNotNull();
            }
        }

        @Nested
        class MidEventHearingSupportCallback {

            private static final String PAGE_ID = "HearingSupportRequirementsDJ";

            @Test
            void shouldReturnError_whenDateFromDateGreaterThanDateToProvided() {
                HearingDates hearingDates = new HearingDates();
                hearingDates.setHearingUnavailableFrom(
                    LocalDate.now().plusMonths(2));
                hearingDates.setHearingUnavailableUntil(
                    LocalDate.now().plusMonths(1));
                HearingSupportRequirementsDJ hearingSupportRequirementsDJ = new HearingSupportRequirementsDJ();
                hearingSupportRequirementsDJ.setHearingDates(wrapElements(hearingDates));

                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setRespondent2(PartyBuilder.builder().individual().build());
                caseData.setAddRespondent2(YES);
                caseData.setHearingSupportRequirementsDJ(hearingSupportRequirementsDJ);
                caseData.setRespondent2SameLegalRepresentative(YES);
                caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
                caseData.setHearingSupportRequirementsDJ(hearingSupportRequirementsDJ);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors().get(0)).isEqualTo("Unavailable From Date should be less than To Date");
            }

            @Test
            void shouldReturnError_whenDateFromDateGreaterThreeMonthsProvided() {
                HearingDates hearingDates = new HearingDates();
                hearingDates.setHearingUnavailableFrom(
                    LocalDate.now().plusMonths(2));
                hearingDates.setHearingUnavailableUntil(
                    LocalDate.now().plusMonths(4));
                HearingSupportRequirementsDJ hearingSupportRequirementsDJ = new HearingSupportRequirementsDJ();
                hearingSupportRequirementsDJ.setHearingDates(wrapElements(hearingDates));

                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setRespondent2(PartyBuilder.builder().individual().build());
                caseData.setAddRespondent2(YES);
                caseData.setRespondent2SameLegalRepresentative(YES);
                caseData.setHearingSupportRequirementsDJ(hearingSupportRequirementsDJ);
                caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors().get(0))
                    .isEqualTo("Unavailable Dates must be within the next 3 months.");
            }

            @Test
            void shouldReturnError_whenDateFromPastDatedProvided() {
                HearingDates hearingDates = new HearingDates();
                hearingDates.setHearingUnavailableFrom(
                    LocalDate.now().plusMonths(1));
                hearingDates.setHearingUnavailableUntil(
                    LocalDate.now().plusMonths(-4));
                HearingSupportRequirementsDJ hearingSupportRequirementsDJ = new HearingSupportRequirementsDJ();
                hearingSupportRequirementsDJ.setHearingDates(wrapElements(hearingDates));

                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setRespondent2(PartyBuilder.builder().individual().build());
                caseData.setAddRespondent2(YES);
                caseData.setHearingSupportRequirementsDJ(hearingSupportRequirementsDJ);
                caseData.setRespondent2SameLegalRepresentative(YES);
                caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors().get(0)).isEqualTo("Unavailable Date cannot be past date");
            }

            @Test
            void shouldNotReturnError_whenValidDateRangeProvided() {
                HearingDates hearingDates = new HearingDates();
                hearingDates.setHearingUnavailableFrom(
                    LocalDate.now().plusMonths(1));
                hearingDates.setHearingUnavailableUntil(
                    LocalDate.now().plusMonths(2));
                HearingSupportRequirementsDJ hearingSupportRequirementsDJ = new HearingSupportRequirementsDJ();
                hearingSupportRequirementsDJ.setHearingDates(wrapElements(hearingDates));
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setRespondent2(PartyBuilder.builder().individual().build());
                caseData.setAddRespondent2(YES);
                caseData.setHearingSupportRequirementsDJ(hearingSupportRequirementsDJ);
                caseData.setRespondent2SameLegalRepresentative(YES);
                caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors()).isEmpty();
            }

            @Test
            void shouldNotReturnError_whenNoDateRangeProvided() {
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setRespondent2(PartyBuilder.builder().individual().build());
                caseData.setAddRespondent2(YES);
                HearingSupportRequirementsDJ hearingSupportRequirementsDJ = new HearingSupportRequirementsDJ();
                hearingSupportRequirementsDJ.setHearingDates(null);
                caseData.setHearingSupportRequirementsDJ(hearingSupportRequirementsDJ);
                caseData.setRespondent2SameLegalRepresentative(YES);
                caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                assertThat(response.getErrors()).isNull();
            }

            @Test
            void shouldNotReturnError_whenLocationsProvided() {
                HearingDates hearingDates = new HearingDates();
                hearingDates.setHearingUnavailableFrom(
                    LocalDate.now().plusMonths(1));
                hearingDates.setHearingUnavailableUntil(
                    LocalDate.now().plusMonths(2));
                DynamicListElement loc1Element = new DynamicListElement(null, "Loc - 1 - 1");
                List<DynamicListElement> temporaryLocationList = List.of(loc1Element);
                DynamicList hearingTemporaryLocation = new DynamicList();
                hearingTemporaryLocation.setListItems(temporaryLocationList);
                hearingTemporaryLocation.setValue(loc1Element);
                HearingSupportRequirementsDJ hearingSupportRequirementsDJ = new HearingSupportRequirementsDJ();
                hearingSupportRequirementsDJ.setHearingDates(wrapElements(hearingDates));
                hearingSupportRequirementsDJ.setHearingTemporaryLocation(hearingTemporaryLocation);

                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setRespondent2(PartyBuilder.builder().individual().build());
                caseData.setAddRespondent2(YES);
                caseData.setRespondent2SameLegalRepresentative(YES);
                caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
                caseData.setHearingSupportRequirementsDJ(hearingSupportRequirementsDJ);
                List<LocationRefData> locations = new ArrayList<>();
                locations.add(LocationRefData.builder().siteName("Loc").courtAddress("1").postcode("1")
                                  .courtName("Court Name").region("Region").regionId("1").courtVenueId("000")
                                  .epimmsId("123456").build());
                when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
                CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

                assertThat(response.getErrors()).isEmpty();
                assertThat(updatedData.getCaseManagementLocation()).isNotNull();
                assertThat(updatedData.getLocationName()).isEqualTo("Loc");
            }
        }

        @Nested
        class AboutToSubmitCallback {

            @Test
            void shouldCallExternalTask_whenAboutToSubmit() {
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setAddRespondent2(NO);
                caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(updatedData.getBusinessProcess()).isNotNull();
                assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo("DEFAULT_JUDGEMENT");
                assertThat(updatedData.getSetRequestDJDamagesFlagForWA()).isEqualTo(YesOrNo.YES);
            }

            @Test
            void shouldCallExternalTaskAndDeleteLocationList_whenAboutToSubmit() {
                List<LocationRefData> locations = new ArrayList<>();
                locations.add(LocationRefData.builder().courtName("Court Name").regionId("2").epimmsId("123456").build());
                when(locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(
                    any(),
                    any()
                )).thenReturn(locations);
                DynamicListElement loc1Element = new DynamicListElement("loc1-123456", "loc1");
                DynamicList hearingTemporaryLocation = new DynamicList();
                hearingTemporaryLocation.setValue(loc1Element);
                hearingTemporaryLocation.setListItems(List.of(loc1Element));
                HearingSupportRequirementsDJ hearingSupportRequirementsDJ = new HearingSupportRequirementsDJ();
                hearingSupportRequirementsDJ.setHearingTemporaryLocation(hearingTemporaryLocation);
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setAddRespondent2(NO);
                caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));

                caseData.setHearingSupportRequirementsDJ(hearingSupportRequirementsDJ);
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
                CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);
                assertThat(updatedData.getCaseManagementLocation()).isNotNull();
                assertThat(updatedData.getCaseManagementLocation().getRegion()).isEqualTo("2");
                assertThat(updatedData.getCaseManagementLocation().getBaseLocation()).isEqualTo("123456");
                assertThat(updatedData.getBusinessProcess()).isNotNull();
                assertThat(updatedData.getBusinessProcess().getCamundaEvent()).isEqualTo("DEFAULT_JUDGEMENT");
                assertThat(updatedData.getSetRequestDJDamagesFlagForWA()).isEqualTo(YesOrNo.YES);
            }
        }

        @Nested
        class SubmittedCallback {

            @Test
            void shouldReturnJudgementGrantedResponse_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setLegacyCaseReference("111111");
                caseData.setApplicant1(PartyBuilder.builder().build());
                caseData.setRespondent1(PartyBuilder.builder().individual().build());

                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                String header = "# Judgment for damages to be decided "
                    + "Granted ";
                String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" target=\"_blank\">Download"
                    + "  interim judgment</a> "
                    + "%n%n Judgment has been entered and your case"
                    + " will be referred to a judge for directions.";
                assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                              .confirmationHeader(header)
                                                                              .confirmationBody(String.format(body))
                                                                              .build());
            }

            @Test
            void shouldReturnJudgementRequestedResponseOneDefendantSelected_whenInvokedAnd1v2() {
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setLegacyCaseReference("111111");
                caseData.setRespondent1(PartyBuilder.builder().build());
                caseData.setRespondent2(PartyBuilder.builder().build());
                caseData.setAddRespondent2(YesOrNo.YES);
                caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
                caseData.setApplicant1(PartyBuilder.builder().build());
                DynamicListElement testUserElement = new DynamicListElement(null, "Test User");
                DynamicList defendantDetails = new DynamicList();
                defendantDetails.setValue(testUserElement);
                caseData.setDefendantDetails(defendantDetails);
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
                String header = "# Judgment for damages to be decided "
                    + "requested ";
                String body = "Your request will be referred"
                    + " to a judge and we will contact you "
                    + "and tell you what happens next.";
                assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                              .confirmationHeader(header)
                                                                              .confirmationBody(body)
                                                                              .build());
            }

            @Test
            void shouldReturnJudgementGrantedResponseBothDefendantSelected_whenInvokedAnd1v2() {
                CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
                caseData.setLegacyCaseReference("111111");
                caseData.setRespondent1(PartyBuilder.builder().build());
                caseData.setRespondent2(PartyBuilder.builder().build());
                caseData.setAddRespondent2(YesOrNo.YES);
                caseData.setRespondent2SameLegalRepresentative(YesOrNo.YES);
                caseData.setApplicant1(PartyBuilder.builder().build());
                DynamicListElement bothElement = new DynamicListElement(null, "Both");
                DynamicList defendantDetails = new DynamicList();
                defendantDetails.setValue(bothElement);
                caseData.setDefendantDetails(defendantDetails);
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
                String header = "# Judgment for damages to be decided "
                    + "Granted ";
                String body = "<br /><a href=\"/cases/case-details/1594901956117591#Claim documents\" "
                    + "target=\"_blank\">Download  interim judgment</a> %n%n Judgment has been entered"
                    + " and your case will be referred to a judge for directions.";
                assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                              .confirmationHeader(header)
                                                                              .confirmationBody(String.format(body))
                                                                              .build());
            }
        }
    }

    @Test
    void shouldExtendDeadline() {
        when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(36, LocalDate.now()))
            .thenReturn(LocalDateTime.now().plusMonths(36));
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
        caseData.setAddRespondent2(NO);
        caseData.setRespondent1ResponseDeadline(LocalDateTime.now().minusDays(15));
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        Object deadlineValue = response.getData().get("claimDismissedDeadline");
        assertThat(deadlineValue).isNotNull();

        LocalDate expectedDate = LocalDate.now().plusMonths(36);
        LocalDate actualDate = LocalDateTime.parse(deadlineValue.toString()).toLocalDate();

        assertThat(actualDate).isEqualTo(expectedDate);
    }
}


