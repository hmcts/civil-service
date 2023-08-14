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
import uk.gov.hmcts.reform.civil.model.AdditionalDates;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    AddUnavailableDatesCallbackHandler.class,
    JacksonAutoConfiguration.class,
    UnavailableDateValidator.class
})

class AddUnavailableDatesCallbackHandlerTest extends BaseCallbackHandlerTest {

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

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmit {
        List<Element<UnavailableDate>> dates = Stream.of(
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                .date(LocalDate.of(2020, 5, 2))
                .build(),
            UnavailableDate.builder()
                .unavailableDateType(UnavailableDateType.DATE_RANGE)
                .fromDate(LocalDate.of(2020, 5, 2))
                .toDate(LocalDate.of(2020, 6, 2))
                .build()
        ).map(ElementUtils::element).collect(Collectors.toList());

        @BeforeEach
        void setup() {
            when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        }

        @Nested
        class LegalRepView {
            AdditionalDates additionalDates = AdditionalDates.builder()
                .additionalUnavailableDates(dates)
                .partyChosen(DynamicList.builder().listItems(List.of(DynamicListElement.builder().label("something").build())).build())
                .build();

            @Nested
            class OneVOne {
                @Test
                void shouldPopulateRespondentOneDates_whenRespondentSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateApplicantOneDates_whenApplicantSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateRespondentOneDates_whenItAlreadyHaveExistingDates() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORONE"));
                    List<Element<UnavailableDate>> existingDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .build()
                    ).map(ElementUtils::element).collect(Collectors.toList());

                    List<Element<UnavailableDate>> expectedDates = new ArrayList<>();
                    expectedDates.addAll(existingDates);
                    expectedDates.addAll(dates);


                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .addUnavailableDatesScreens(additionalDates)
                        .respondent1(PartyBuilder.builder()
                                         .soleTrader().build().toBuilder()
                                         .partyID("res-1-party-id")
                                         .unavailableDates(existingDates)
                                         .build())
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(expectedDates);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateApplicantOneDates_whenItAlreadyHaveExistingDates() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));
                    List<Element<UnavailableDate>> existingDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .build()
                    ).map(ElementUtils::element).collect(Collectors.toList());

                    List<Element<UnavailableDate>> expectedDates = new ArrayList<>();
                    expectedDates.addAll(existingDates);
                    expectedDates.addAll(dates);


                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .addUnavailableDatesScreens(additionalDates)
                        .applicant1(PartyBuilder.builder()
                                         .soleTrader().build().toBuilder()
                                         .partyID("someid")
                                         .unavailableDates(existingDates)
                                         .build())
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(expectedDates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
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
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateRespondentOneAndTwoDates_whenRespondentSolicitorTwoTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORTWO"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimOneDefendantSolicitor()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateApplicantOneDates_whenApplicantSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimOneDefendantSolicitor()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(null);
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
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateApplicantOneDates_whenApplicantSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateRespondentTwoDates_whenItAlreadyHaveExistingDates() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORTWO"));
                    List<Element<UnavailableDate>> existingDates = Stream.of(
                        UnavailableDate.builder()
                            .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                            .date(LocalDate.of(2022, 5, 2))
                            .build()
                    ).map(ElementUtils::element).collect(Collectors.toList());

                    List<Element<UnavailableDate>> expectedDates = new ArrayList<>();
                    expectedDates.addAll(existingDates);
                    expectedDates.addAll(dates);


                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .addUnavailableDatesScreens(additionalDates)
                        .respondent2(PartyBuilder.builder()
                                         .soleTrader().build().toBuilder()
                                         .partyID("res-2-party-id")
                                         .unavailableDates(existingDates)
                                         .build())
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(expectedDates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                }
            }

            @Nested
            class TwoVOne {
                @Test
                void shouldPopulateRespondentOneDates_whenRespondentSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("RESPONDENTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .multiPartyClaimTwoApplicants()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                    assertThat(getCaseData(response).getApplicant2().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateApplicantOneAndTwoDates_whenApplicantSolicitorOneTriggersEvent() {
                    when(coreCaseUserService.getUserCaseRoles(anyString(), anyString())).thenReturn(List.of("APPLICANTSOLICITORONE"));

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .multiPartyClaimTwoApplicants()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant2().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
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

                    List<Element<UnavailableDate>> expectedDates = new ArrayList<>();
                    expectedDates.addAll(existingDates);
                    expectedDates.addAll(dates);


                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .addUnavailableDatesScreens(additionalDates)
                        .applicant1(PartyBuilder.builder()
                                         .soleTrader().build().toBuilder()
                                         .partyID("app-2-party-id")
                                         .unavailableDates(existingDates)
                                         .build())
                        .applicant2(PartyBuilder.builder()
                                        .soleTrader().build().toBuilder()
                                        .partyID("app-2-party-id")
                                        .unavailableDates(existingDates)
                                        .build())
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(expectedDates);
                    assertThat(getCaseData(response).getApplicant2().getUnavailableDates()).isEqualTo(expectedDates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                }
            }
        }

        @Nested
        class AdminView {
            @Nested
            class OneVOne {
                @Test
                void shouldPopulateRespondentOneDates_whenDefendantChoiceIsSelected() {
                    AdditionalDates additionalDates = AdditionalDates.builder()
                        .additionalUnavailableDates(dates)
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Defendant").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendant").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateApplicantOneDates_whenClaimantChoiceIsSelected() {
                    AdditionalDates additionalDates = AdditionalDates.builder()
                        .additionalUnavailableDates(dates)
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Claimant").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendant").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                }
            }

            @Nested
            class OneVTwoSameSolicitor {
                @Test
                void shouldPopulateRespondentOneAndTwoDates_whenDefendantsChoiceIsSelected() {
                    AdditionalDates additionalDates = AdditionalDates.builder()
                        .additionalUnavailableDates(dates)
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Defendants").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendants").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimOneDefendantSolicitor()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateApplicantOneDates_whenClaimantChoiceIsSelected() {
                    AdditionalDates additionalDates = AdditionalDates.builder()
                        .additionalUnavailableDates(dates)
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Claimant").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendants").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimOneDefendantSolicitor()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(null);
                }
            }

            @Nested
            class OneVTwoDifferentSolicitor {
                @Test
                void shouldPopulateRespondentOneDates_whenDefendant1ChoiceIsSelected() {
                    AdditionalDates additionalDates = AdditionalDates.builder()
                        .additionalUnavailableDates(dates)
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Defendant 1").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendant 1").build(),
                                             DynamicListElement.builder().label("Defendant 2").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(null);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateRespondentTwoDates_whenDefendant2ChoiceIsSelected() {
                    AdditionalDates additionalDates = AdditionalDates.builder()
                        .additionalUnavailableDates(dates)
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Defendant 2").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendant 1").build(),
                                             DynamicListElement.builder().label("Defendant 2").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateApplicantOneDates_whenClaimantChoiceIsSelected() {
                    AdditionalDates additionalDates = AdditionalDates.builder()
                        .additionalUnavailableDates(dates)
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Claimant").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimant").build(),
                                             DynamicListElement.builder().label("Defendant 1").build(),
                                             DynamicListElement.builder().label("Defendant 2").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .multiPartyClaimTwoDefendantSolicitors()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    //Checking for def1 and applicant1 only because it's a 1v1 case
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                    assertThat(getCaseData(response).getRespondent2().getUnavailableDates()).isEqualTo(null);
                }
            }

            @Nested
            class TwoVOne {
                @Test
                void shouldPopulateRespondentOneDates_whenDefendantChoiceIsSelected() {
                    AdditionalDates additionalDates = AdditionalDates.builder()
                        .additionalUnavailableDates(dates)
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Defendant").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimants").build(),
                                             DynamicListElement.builder().label("Defendant").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .multiPartyClaimTwoApplicants()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(null);
                    assertThat(getCaseData(response).getApplicant2().getUnavailableDates()).isEqualTo(null);
                }

                @Test
                void shouldPopulateApplicantOneDates_whenClaimantsChoiceIsSelected() {
                    AdditionalDates additionalDates = AdditionalDates.builder()
                        .additionalUnavailableDates(dates)
                        .partyChosen(DynamicList.builder()
                                         .value(DynamicListElement.builder().label("Claimants").build())
                                         .listItems(List.of(
                                             DynamicListElement.builder().label("Claimants").build(),
                                             DynamicListElement.builder().label("Defendant").build()
                                         )).build())
                        .build();

                    CaseData caseData = CaseDataBuilder.builder()
                        .atStateRespondentFullDefence()
                        .multiPartyClaimTwoApplicants()
                        .addUnavailableDatesScreens(additionalDates)
                        .build();
                    CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

                    AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                        .handle(params);

                    assertThat(getCaseData(response).getApplicant1().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getApplicant2().getUnavailableDates()).isEqualTo(dates);
                    assertThat(getCaseData(response).getRespondent1().getUnavailableDates()).isEqualTo(null);
                }
            }

        }

        private CaseData getCaseData(AboutToStartOrSubmitCallbackResponse response) {
            return objectMapper.convertValue(response.getData(), CaseData.class);
        }

    }
}
