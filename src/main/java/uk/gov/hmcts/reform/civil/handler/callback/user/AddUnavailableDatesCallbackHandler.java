package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_UNAVAILABLE_DATES;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils.copyDatesIntoListingTabFields;

@Service
@RequiredArgsConstructor
public class AddUnavailableDatesCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(ADD_UNAVAILABLE_DATES);
    private static final String ADD_UNAVAILABLE_DATES_EVENT = "Unavailability Dates Event";

    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final UnavailableDateValidator unavailableDateValidator;
    private final ObjectMapper objectMapper;
    private final Time time;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::aboutToStart,
            callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );

        if (isRespondentSolicitorOne(roles) || isRespondentSolicitorTwo(roles) || isApplicantSolicitorOne(roles)) {
            caseDataBuilder.updateDetailsForm(UpdateDetailsForm.builder()
                                                           .hidePartyChoice(YES)
                                                           .build());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
        }

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        List<String> dynamicListOptions = new ArrayList<>();

        switch (multiPartyScenario) {
            case ONE_V_ONE: {
                dynamicListOptions.add("Claimant");
                dynamicListOptions.add("Defendant");
                break;
            }
            case ONE_V_TWO_ONE_LEGAL_REP: {
                dynamicListOptions.add("Claimant");
                dynamicListOptions.add("Defendants");
                break;
            }
            case ONE_V_TWO_TWO_LEGAL_REP: {
                dynamicListOptions.add("Claimant");
                dynamicListOptions.add("Defendant 1");
                dynamicListOptions.add("Defendant 2");
                break;
            }
            case TWO_V_ONE: {
                dynamicListOptions.add("Claimants");
                dynamicListOptions.add("Defendant");
                break;
            }
            default: {
                throw new CallbackException(String.format("Invalid participants"));
            }
        }

        caseDataBuilder.updateDetailsForm(UpdateDetailsForm.builder()
                                                       .hidePartyChoice(YesOrNo.NO)
                                                       .partyChosen(DynamicList.fromList(dynamicListOptions))
                                                       .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> errors = unavailableDateValidator.validateAdditionalUnavailableDates(
            caseData.getUpdateDetailsForm().getAdditionalUnavailableDates()
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        copyDatesIntoListingTabFields(caseData, updatedData);

        if (caseData.getUpdateDetailsForm() != null
            && caseData.getUpdateDetailsForm().getPartyChosen() != null
            && caseData.getUpdateDetailsForm().getPartyChosen().getValue() != null
        ) {
            // Admin Screens
            addDatesAdminScenario(caseData.getUpdateDetailsForm().getPartyChosen().getValue().getLabel(),
                                  caseData, updatedData);

        } else {
            // Legal Rep Screens
            addDatesLegalRepScenario(callbackParams, updatedData);
        }

        // clear form
        updatedData.updateDetailsForm(null);

        return AboutToStartOrSubmitCallbackResponse.builder().data(updatedData.build().toMap(objectMapper)).build();
    }

    private void addDatesAdminScenario(String partyChosen, CaseData caseData, CaseData.CaseDataBuilder updatedData) {
        switch (partyChosen) {
            case ("Claimants"): {
                addDateToApplicant2(caseData, updatedData);
            }
            // FALL-THROUGH
            case ("Claimant"): {
                addDateToApplicant1(caseData, updatedData);
                break;
            }
            case ("Defendants"): {
                addDateToRespondent2(caseData, updatedData);
            }
            // FALL-THROUGH
            case ("Defendant"):
            case ("Defendant 1"): {
                addDateToRespondent1(caseData, updatedData);
                break;
            }
            case ("Defendant 2") : {
                addDateToRespondent2(caseData, updatedData);
                break;
            }
            default: {
                throw new CallbackException(String.format("Invalid participants"));
            }
        }
    }

    private void addDatesLegalRepScenario(CallbackParams callbackParams, CaseData.CaseDataBuilder updatedData) {
        CaseData caseData = callbackParams.getCaseData();
        MultiPartyScenario multiPartyScenario  = getMultiPartyScenario(caseData);
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );

        switch (multiPartyScenario) {
            case ONE_V_ONE: {
                if (isRespondentSolicitorOne(roles)) {
                    addDateToRespondent1(caseData, updatedData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData, updatedData);
                }
                break;
            }
            case ONE_V_TWO_ONE_LEGAL_REP: {
                if (isRespondentSolicitorOne(roles) || isRespondentSolicitorTwo(roles)) {
                    addDateToRespondent1(caseData, updatedData);
                    addDateToRespondent2(caseData, updatedData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData, updatedData);
                }
                break;
            }
            case ONE_V_TWO_TWO_LEGAL_REP: {
                if (isRespondentSolicitorOne(roles)) {
                    addDateToRespondent1(caseData, updatedData);
                } else if (isRespondentSolicitorTwo(roles)) {
                    addDateToRespondent2(caseData, updatedData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData, updatedData);
                }
                break;
            }
            case TWO_V_ONE: {
                if (isRespondentSolicitorOne(roles)) {
                    addDateToRespondent1(caseData, updatedData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData, updatedData);
                    addDateToApplicant2(caseData, updatedData);
                }
                break;
            }
            default:
                throw new CallbackException(String.format("Invalid participants"));
        }
    }

    private boolean isApplicantSolicitorOne(List<String> roles) {
        return hasCaseRole(roles, APPLICANTSOLICITORONE);
    }

    private boolean isRespondentSolicitorOne(List<String> roles) {
        return hasCaseRole(roles, RESPONDENTSOLICITORONE);
    }

    private boolean isRespondentSolicitorTwo(List<String> roles) {
        return hasCaseRole(roles, RESPONDENTSOLICITORTWO);
    }

    private boolean hasCaseRole(List<String> roles, CaseRole role) {
        return roles.stream().anyMatch(role.getFormattedName()::contains);
    }

    private List<Element<UnavailableDate>> addDatesToExistingDates(CaseData caseData, List<Element<UnavailableDate>> partyDates) {
        List<Element<UnavailableDate>> existingUnavailableDates = ofNullable(partyDates).orElse(newArrayList());
        List<Element<UnavailableDate>> newUnavailableDates = caseData.getUpdateDetailsForm().getAdditionalUnavailableDates();
        List<Element<UnavailableDate>> updatedUnavailableDates = new ArrayList<>();

        if (!existingUnavailableDates.isEmpty()) {
            updatedUnavailableDates.addAll(existingUnavailableDates);
        }

        for (Element<UnavailableDate> newDate : newUnavailableDates) {
            updatedUnavailableDates.addAll(wrapElements(newDate.getValue().toBuilder()
                                                            .dateAdded(time.now().toLocalDate())
                                                            .eventAdded(ADD_UNAVAILABLE_DATES_EVENT).build()));
        }

        return updatedUnavailableDates;
    }

    private void addDateToApplicant1(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        List<Element<UnavailableDate>> accumulatedDates = addDatesToExistingDates(
            caseData,
            updatedData.build().getApplicant1().getUnavailableDates()
        );

        updatedData
            .applicant1(caseData.getApplicant1().toBuilder().unavailableDates(accumulatedDates).build())
            .applicant1UnavailableDatesForTab(accumulatedDates);
    }

    private void addDateToApplicant2(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        List<Element<UnavailableDate>> accumulatedDates = addDatesToExistingDates(
            caseData,
            updatedData.build().getApplicant2().getUnavailableDates()
        );

        updatedData
            .applicant2(caseData.getApplicant2().toBuilder().unavailableDates(accumulatedDates).build())
            .applicant2UnavailableDatesForTab(accumulatedDates);
    }

    private void addDateToRespondent1(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        List<Element<UnavailableDate>> accumulatedDates = addDatesToExistingDates(
            caseData,
            updatedData.build().getRespondent1().getUnavailableDates()
        );

        updatedData
            .respondent1(caseData.getRespondent1().toBuilder().unavailableDates(accumulatedDates).build())
            .respondent1UnavailableDatesForTab(accumulatedDates);
    }

    private void addDateToRespondent2(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        List<Element<UnavailableDate>> accumulatedDates = addDatesToExistingDates(
            caseData,
            updatedData.build().getRespondent2().getUnavailableDates()
        );

        updatedData
            .respondent2(caseData.getRespondent2().toBuilder().unavailableDates(accumulatedDates).build())
            .respondent2UnavailableDatesForTab(accumulatedDates);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        String body = "<br /><h3 class=\"govuk-heading-\">What happens next</h3>"
            + " %n%n Any dates marked as being unavailable for a hearing are now displayed in the Listing notes tab.";

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Availability updated"))
            .confirmationBody(format(body))
            .build();
    }
}
