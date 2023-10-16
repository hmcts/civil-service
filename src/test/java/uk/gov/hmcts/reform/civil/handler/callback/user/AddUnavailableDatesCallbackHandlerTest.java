package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    AddUnavailableDatesCallbackHandler.class,
    JacksonAutoConfiguration.class,
    UnavailableDateValidator.class
})

class AddUnavailableDatesCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private Time time;

    @Autowired
    private AddUnavailableDatesCallbackHandler handler;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UnavailableDateValidator unavailableDateValidator;

    private final LocalDate issueDate = now();
    private static final String ADD_UNAVAILABLE_DATES_EVENT = "Unavailability Dates Event";
    private static final String DEFENDANT_RESPONSE_EVENT = "Defendant Response Event";
    private static final String CLAIMANT_INTENTION_EVENT = "Claimant Intention Event";
    private static final String DJ_EVENT = "Request DJ Event";

    private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
        return objectMapper.convertValue(response.getData(), CaseData.class);
    }

    @Nested
    class AboutToStartCallback {

        @Nested
        class LegalRepView {
            @BeforeEach
            void setup() {
                when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
                when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));
            }

            @Test
            void shouldSetHidePartyChoiceToYes() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(getCaseData(response).getUpdateDetailsForm().getHidePartyChoice()).isEqualTo(YES);
            }
        }

        @Nested
        class AdminView {
            @BeforeEach
            void setup() {
                when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
                when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("caseworker-civil-admin"));
            }

            @Test
            void shouldSetHidePartyChoiceToNo() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(getCaseData(response).getUpdateDetailsForm().getHidePartyChoice()).isEqualTo(NO);
            }

            @Test
            void shouldPrepopulateDynamicListWithOptions_whenInvoked_1v1() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().size()).isEqualTo(
                    2);
                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().get(0).getLabel()).isEqualTo(
                    "Claimant");
                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().get(1).getLabel()).isEqualTo(
                    "Defendant");
            }

            @Test
            void shouldPrepopulateDynamicListWithOptions_whenInvoked_1v2SameSolicitor() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .multiPartyClaimOneDefendantSolicitor()
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().size()).isEqualTo(
                    2);
                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().get(0).getLabel()).isEqualTo(
                    "Claimant");
                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().get(1).getLabel()).isEqualTo(
                    "Defendants");
            }

            @Test
            void shouldPrepopulateDynamicListWithOptions_whenInvoked_1v2DifferentSolicitor() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().size()).isEqualTo(
                    3);
                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().get(0).getLabel()).isEqualTo(
                    "Claimant");
                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().get(1).getLabel()).isEqualTo(
                    "Defendant 1");
                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().get(2).getLabel()).isEqualTo(
                    "Defendant 2");
            }

            @Test
            void shouldPrepopulateDynamicListWithOptions_whenInvoked_2v1() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateRespondentFullDefence()
                    .multiPartyClaimTwoApplicants()
                    .build();
                CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

                AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                    .handle(params);

                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().size()).isEqualTo(
                    2);
                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().get(0).getLabel()).isEqualTo(
                    "Claimants");
                assertThat(getCaseData(response).getUpdateDetailsForm().getPartyChosen().getListItems().get(1).getLabel()).isEqualTo(
                    "Defendant");
            }

        }
    }

    @Nested
    class AboutToSubmit {
        List<UnavailableDate> dates = Stream.of(
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.of(2020, 5, 2))
                .build(),
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.of(2020, 5, 2))
                .toDate(LocalDate.of(2020, 6, 2))
                .build()
        ).collect(Collectors.toList());

        List<UnavailableDate> expectedNewDatesFromUnavailableDatesEvent = Stream.of(
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.of(2020, 5, 2))
                .dateAdded(issueDate)
                .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                .build(),
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.of(2020, 5, 2))
                .toDate(LocalDate.of(2020, 6, 2))
                .dateAdded(issueDate)
                .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                .build()
        ).collect(Collectors.toList());

        @BeforeEach
        void setup() {
            when(time.now()).thenReturn(issueDate.atStartOfDay());
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Nested
        class LegalRepView {
            UpdateDetailsForm form = UpdateDetailsForm.builder()
                .additionalUnavailableDates(wrapElements(new ArrayList<>(dates)))
                .partyChosen(DynamicList.builder().listItems(List.of(DynamicListElement.builder().label("something").build())).build())
                .build();

            @Nested
            class OneVOne {
                @Test
                void shouldPopulateRespondentOneDates_whenRespondentSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_whenApplicantSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateRespondentOneDates_whenItAlreadyHaveExistingDates_ForDefendantResponse() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORONE"));
                    List<Element<UnavailableDate>> existingDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .build()
                    ).map(ElementUtils::element).collect(Collectors.toList());

                    List<UnavailableDate> expectedDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(DEFENDANT_RESPONSE_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2020, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2020, 5, 2))
                            .toDate(LocalDate.of(2020, 6, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build()
                    ).collect(Collectors.toList());

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .updateDetailsForm(form)
                        .respondent1(PartyBuilder.builder()
                                         .soleTrader().build().toBuilder()
                                         .partyID("res-1-party-id")
                                         .unavailableDates(new ArrayList<>(existingDates))
                                         .build())
                        .respondent1ResponseDate(issueDate.atStartOfDay())
                        .respondent1DQ(Respondent1DQ.builder()
                                           .respondent1DQHearing(Hearing.builder()
                                                                     .unavailableDatesRequired(YES)
                                                                     .build())
                                           .build())
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1UnavailableDatesForTab())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_whenItAlreadyHaveExistingDates_ForClaimantResponse() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));
                    List<Element<UnavailableDate>> existingDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .build()
                    ).map(ElementUtils::element).collect(Collectors.toList());

                    List<UnavailableDate> expectedDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(CLAIMANT_INTENTION_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2020, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2020, 5, 2))
                            .toDate(LocalDate.of(2020, 6, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build()
                    ).collect(Collectors.toList());

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .updateDetailsForm(form)
                        .applicant1(PartyBuilder.builder()
                                         .soleTrader().build().toBuilder()
                                         .partyID("someid")
                                         .unavailableDates(new ArrayList<>(existingDates))
                                         .build())
                        .applicant1ResponseDate(issueDate.atStartOfDay())
                        .applicant1DQ(Applicant1DQ.builder()
                                           .applicant1DQHearing(Hearing.builder()
                                                                     .unavailableDatesRequired(YES)
                                                                     .build())
                                           .build())
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_whenItAlreadyHaveExistingDates_ForDJ() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));
                    List<Element<UnavailableDate>> existingDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2023, 8, 20))
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2023, 8, 20))
                            .toDate(LocalDate.of(2023, 8, 22))
                            .build()
                    ).map(ElementUtils::element).collect(Collectors.toList());

                    List<UnavailableDate> expectedDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2023, 8, 20))
                            .dateAdded(issueDate)
                            .eventAdded(DJ_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2023, 8, 20))
                            .toDate(LocalDate.of(2023, 8, 22))
                            .dateAdded(issueDate)
                            .eventAdded(DJ_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2020, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2020, 5, 2))
                            .toDate(LocalDate.of(2020, 6, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build()
                    ).collect(Collectors.toList());

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimDetailsNotified()
                        .atStateClaimantRequestsDJWithUnavailableDates()
                        .applicant1(PartyBuilder.builder()
                                        .soleTrader().build().toBuilder()
                                        .partyID("someid")
                                        .unavailableDates(new ArrayList<>(existingDates))
                                        .build())
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_secondRoundOfAdditionOfDates() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));
                    List<Element<UnavailableDate>> existingTopLevelDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(CLAIMANT_INTENTION_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2020, 3, 4))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build()
                    ).map(ElementUtils::element).collect(Collectors.toList());

                    List<UnavailableDate> expectedDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(CLAIMANT_INTENTION_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2020, 3, 4))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2020, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2020, 5, 2))
                            .toDate(LocalDate.of(2020, 6, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build()
                    ).collect(Collectors.toList());

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .updateDetailsForm(form)
                        .applicant1(PartyBuilder.builder()
                                        .soleTrader().build().toBuilder()
                                        .partyID("someid")
                                        .unavailableDates(new ArrayList<>(existingTopLevelDates))
                                        .build())
                        .applicant1ResponseDate(issueDate.atStartOfDay())
                        .applicant1DQ(Applicant1DQ.builder()
                                          .applicant1DQHearing(Hearing.builder()
                                                                   .unavailableDatesRequired(YES)
                                                                   .build())
                                          .build())
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                }
            }

            @Nested
            class OneVTwoSameSolicitor {
                @Test
                void shouldPopulateRespondentOneAndTwoDates_whenRespondentSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimOneDefendantSolicitor()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent2UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateRespondentOneAndTwoDates_whenRespondentSolicitorTwoTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORTWO"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimOneDefendantSolicitor()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent2UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_whenApplicantSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimOneDefendantSolicitor()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEmpty();
                }
            }

            @Nested
            class OneVTwoDifferentSolicitor {

                @Test
                void shouldPopulateRespondentTwoDates_whenRespondentSolicitorTwoTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORTWO"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent2UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_whenApplicantSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateRespondentTwoDates_whenItAlreadyHaveExistingDates_ForDefendantResponse() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORTWO"));
                    List<Element<UnavailableDate>> existingDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .build()
                    ).map(ElementUtils::element).collect(Collectors.toList());

                    List<UnavailableDate> expectedDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(DEFENDANT_RESPONSE_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2020, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2020, 5, 2))
                            .toDate(LocalDate.of(2020, 6, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build()
                    ).collect(Collectors.toList());

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .updateDetailsForm(form)
                        .respondent2(PartyBuilder.builder()
                                         .soleTrader().build().toBuilder()
                                         .partyID("res-2-party-id")
                                         .unavailableDates(new ArrayList<>(existingDates))
                                         .build())
                        .respondent2ResponseDate(issueDate.atStartOfDay())
                        .respondent2DQ(Respondent2DQ.builder()
                                           .respondent2DQHearing(Hearing.builder()
                                                                     .unavailableDatesRequired(YES)
                                                                     .build())
                                           .build())
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getRespondent2UnavailableDatesForTab())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                }
            }

            @Nested
            class TwoVOne {
                @Test
                void shouldPopulateRespondentOneDates_whenRespondentSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .multiPartyClaimTwoApplicants()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                    assertThat(unwrapElements(getCaseData(response).getApplicant2().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneAndTwoDates_whenApplicantSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .multiPartyClaimTwoApplicants()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant2().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant2UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneAndTwoDates_whenItAlreadyHaveExistingDates() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));
                    List<Element<UnavailableDate>> existingDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .build()
                    ).map(ElementUtils::element).collect(Collectors.toList());

                    List<UnavailableDate> expectedDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(CLAIMANT_INTENTION_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2020, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2020, 5, 2))
                            .toDate(LocalDate.of(2020, 6, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build()
                    ).collect(Collectors.toList());

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .updateDetailsForm(form)
                        .multiPartyClaimTwoApplicants()
                        .applicant1(PartyBuilder.builder()
                                         .soleTrader().build().toBuilder()
                                         .partyID("app-2-party-id")
                                         .unavailableDates(new ArrayList<>(existingDates))
                                         .build())
                        .applicant2(PartyBuilder.builder()
                                        .soleTrader().build().toBuilder()
                                        .partyID("app-2-party-id")
                                        .unavailableDates(new ArrayList<>(existingDates))
                                        .build())
                        .applicant1ResponseDate(issueDate.atStartOfDay())
                        .applicant1DQ(Applicant1DQ.builder()
                                          .applicant1DQHearing(Hearing.builder()
                                                                   .unavailableDatesRequired(YES)
                                                                   .build())
                                          .build())
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getApplicant2().getUnavailableDates())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getApplicant2UnavailableDatesForTab())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_whenItAlreadyHaveExistingDates_ForDJ() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));
                    List<Element<UnavailableDate>> existingDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2023, 8, 20))
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2023, 8, 20))
                            .toDate(LocalDate.of(2023, 8, 22))
                            .build()
                    ).map(ElementUtils::element).collect(Collectors.toList());

                    List<UnavailableDate> expectedDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2023, 8, 20))
                            .dateAdded(issueDate)
                            .eventAdded(DJ_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2023, 8, 20))
                            .toDate(LocalDate.of(2023, 8, 22))
                            .dateAdded(issueDate)
                            .eventAdded(DJ_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2020, 5, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build(),
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.DATE_RANGE)
                            .fromDate(LocalDate.of(2020, 5, 2))
                            .toDate(LocalDate.of(2020, 6, 2))
                            .dateAdded(issueDate)
                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT)
                            .build()
                    ).collect(Collectors.toList());

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimDetailsNotified()
                        .atStateClaimantRequestsDJWithUnavailableDates()
                        .multiPartyClaimTwoApplicants()
                        .applicant1(PartyBuilder.builder()
                                        .soleTrader().build().toBuilder()
                                        .partyID("someid")
                                        .unavailableDates(new ArrayList<>(existingDates))
                                        .build())
                        .applicant2(PartyBuilder.builder()
                                        .soleTrader().build().toBuilder()
                                        .partyID("app-2-party-id")
                                        .unavailableDates(new ArrayList<>(existingDates))
                                        .build())
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getApplicant2().getUnavailableDates())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getApplicant2UnavailableDatesForTab())).isEqualTo(expectedDates);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                }
            }
        }

        @Nested
        class AdminView {
            @Nested
            class OneVOne {
                @Test
                void shouldPopulateRespondentOneDates_whenDefendantChoiceIsSelected() {
                    UpdateDetailsForm form = UpdateDetailsForm.builder()
                        .additionalUnavailableDates(wrapElements(new ArrayList<>(dates)))
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Defendant").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendant").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_whenClaimantChoiceIsSelected() {
                    UpdateDetailsForm form = UpdateDetailsForm.builder()
                        .additionalUnavailableDates(wrapElements(new ArrayList<>(dates)))
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Claimant").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendant").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                }
            }

            @Nested
            class OneVTwoSameSolicitor {
                @Test
                void shouldPopulateRespondentOneAndTwoDates_whenDefendantsChoiceIsSelected() {
                    UpdateDetailsForm form = UpdateDetailsForm.builder()
                        .additionalUnavailableDates(wrapElements(new ArrayList<>(dates)))
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Defendants").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendants").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .multiPartyClaimOneDefendantSolicitor()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_whenClaimantChoiceIsSelected() {
                    UpdateDetailsForm form = UpdateDetailsForm.builder()
                        .additionalUnavailableDates(wrapElements(new ArrayList<>(dates)))
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Claimant").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendants").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .multiPartyClaimOneDefendantSolicitor()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEmpty();
                }
            }

            @Nested
            class OneVTwoDifferentSolicitor {
                @Test
                void shouldPopulateRespondentOneDates_whenDefendant1ChoiceIsSelected() {
                    UpdateDetailsForm form = UpdateDetailsForm.builder()
                        .additionalUnavailableDates(wrapElements(new ArrayList<>(dates)))
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Defendant 1").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendant 1").build(),
                                             DynamicListElement.builder().label("Defendant 2").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEmpty();
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateRespondentTwoDates_whenDefendant2ChoiceIsSelected() {
                    UpdateDetailsForm form = UpdateDetailsForm.builder()
                        .additionalUnavailableDates(wrapElements(new ArrayList<>(dates)))
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Defendant 2").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendant 1").build(),
                                             DynamicListElement.builder().label("Defendant 2").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent2UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_whenClaimantChoiceIsSelected() {
                    UpdateDetailsForm form = UpdateDetailsForm.builder()
                        .additionalUnavailableDates(wrapElements(new ArrayList<>(dates)))
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Claimant").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendant 1").build(),
                                             DynamicListElement.builder().label("Defendant 2").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                    assertThat(unwrapElements(getCaseData(response).getRespondent2().getUnavailableDates())).isEmpty();
                }
            }

            @Nested
            class TwoVOne {
                @Test
                void shouldPopulateRespondentOneDates_whenDefendantChoiceIsSelected() {
                    UpdateDetailsForm form = UpdateDetailsForm.builder()
                        .additionalUnavailableDates(wrapElements(new ArrayList<>(dates)))
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Defendant").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimants").build(),
                                             DynamicListElement.builder().label("Defendant").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .multiPartyClaimTwoApplicants()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEmpty();
                    assertThat(unwrapElements(getCaseData(response).getApplicant2().getUnavailableDates())).isEmpty();
                }

                @Test
                void shouldPopulateApplicantOneDates_whenClaimantsChoiceIsSelected() {
                    UpdateDetailsForm form = UpdateDetailsForm.builder()
                        .additionalUnavailableDates(wrapElements(new ArrayList<>(dates)))
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Claimants").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimants").build(),
                                             DynamicListElement.builder().label("Defendant").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateClaimantFullDefence()
                        .multiPartyClaimTwoApplicants()
                        .updateDetailsForm(form)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(unwrapElements(getCaseData(response).getApplicant1().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant2().getUnavailableDates())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant1UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getApplicant2UnavailableDatesForTab())).isEqualTo(expectedNewDatesFromUnavailableDatesEvent);
                    assertThat(unwrapElements(getCaseData(response).getRespondent1().getUnavailableDates())).isEmpty();
                }
            }

        }
    }
}
