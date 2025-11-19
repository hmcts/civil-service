package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
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
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    static final String claimant = "Claimant";
    static final String defendant = "Defendant";
    static final String invalidParticipants = "Invalid participants";

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
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );

        // Todo: Sumit:: UpdateDetailsForm builder
        if (isRespondentSolicitorOne(roles) || isRespondentSolicitorTwo(roles) || isApplicantSolicitorOne(roles)) {
            caseData.setUpdateDetailsForm(UpdateDetailsForm.builder()
                                            .hidePartyChoice(YES)
                                            .build());

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
        }

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        List<String> dynamicListOptions = new ArrayList<>();

        switch (multiPartyScenario) {
            case ONE_V_ONE: {
                dynamicListOptions.add(claimant);
                dynamicListOptions.add(defendant);
                break;
            }
            case ONE_V_TWO_ONE_LEGAL_REP: {
                dynamicListOptions.add(claimant);
                dynamicListOptions.add("Defendants");
                break;
            }
            case ONE_V_TWO_TWO_LEGAL_REP: {
                dynamicListOptions.add(claimant);
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
                throw new CallbackException(invalidParticipants);
            }
        }

        caseData.setUpdateDetailsForm(UpdateDetailsForm.builder()
                                        .hidePartyChoice(YesOrNo.NO)
                                        .partyChosen(DynamicList.fromList(dynamicListOptions))
                                        .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
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
        // Todo: Sumit update DateUtils class
        CaseData caseData = copyDatesIntoListingTabFields(callbackParams.getCaseData());

        if (caseData.getUpdateDetailsForm() != null
            && caseData.getUpdateDetailsForm().getPartyChosen() != null
            && caseData.getUpdateDetailsForm().getPartyChosen().getValue() != null
        ) {
            // Admin Screens
            addDatesAdminScenario(caseData.getUpdateDetailsForm().getPartyChosen().getValue().getLabel(), caseData);

        } else {
            // Legal Rep Screens
            addDatesLegalRepScenario(callbackParams, caseData);
        }

        // clear form
        caseData.setUpdateDetailsForm(null);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(objectMapper)).build();
    }

    private void addDatesAdminScenario(String partyChosen, CaseData caseData) {
        switch (partyChosen) {
            case ("Claimants"): {
                addDateToApplicant2(caseData);
            }
            // FALL-THROUGH
            case (claimant): {
                addDateToApplicant1(caseData);
                break;
            }
            case ("Defendants"): {
                addDateToRespondent2(caseData);
            }
            // FALL-THROUGH
            case (defendant), ("Defendant 1"): {
                addDateToRespondent1(caseData);
                break;
            }
            case ("Defendant 2") : {
                addDateToRespondent2(caseData);
                break;
            }
            default: {
                throw new CallbackException(invalidParticipants);
            }
        }
    }

    private void addDatesLegalRepScenario(CallbackParams callbackParams, CaseData caseData) {
        MultiPartyScenario multiPartyScenario  = getMultiPartyScenario(caseData);
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );

        switch (multiPartyScenario) {
            case ONE_V_ONE: {
                if (isRespondentSolicitorOne(roles)) {
                    addDateToRespondent1(caseData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData);
                }
                break;
            }
            case ONE_V_TWO_ONE_LEGAL_REP: {
                if (isRespondentSolicitorOne(roles) || isRespondentSolicitorTwo(roles)) {
                    addDateToRespondent1(caseData);
                    addDateToRespondent2(caseData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData);
                }
                break;
            }
            case ONE_V_TWO_TWO_LEGAL_REP: {
                if (isRespondentSolicitorOne(roles)) {
                    addDateToRespondent1(caseData);
                } else if (isRespondentSolicitorTwo(roles)) {
                    addDateToRespondent2(caseData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData);
                }
                break;
            }
            case TWO_V_ONE: {
                if (isRespondentSolicitorOne(roles)) {
                    addDateToRespondent1(caseData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData);
                    addDateToApplicant2(caseData);
                }
                break;
            }
            default:
                throw new CallbackException(invalidParticipants);
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

    private void addDateToApplicant1(CaseData caseData) {
        Optional.ofNullable(caseData.getApplicant1()).ifPresent(applicant1 -> {
            List<Element<UnavailableDate>> accumulatedDates =
                addDatesToExistingDates(caseData, applicant1.getUnavailableDates());

            applicant1.setUnavailableDates(accumulatedDates);      // replace builder usage
            caseData.setApplicant1(applicant1);                    // preserve behaviour
            caseData.setApplicant1UnavailableDatesForTab(accumulatedDates);
        });
    }

    private void addDateToApplicant2(CaseData caseData) {
        Optional.ofNullable(caseData.getApplicant2()).ifPresent(applicant2 -> {
            List<Element<UnavailableDate>> accumulatedDates =
                addDatesToExistingDates(caseData, applicant2.getUnavailableDates());

            applicant2.setUnavailableDates(accumulatedDates);      // replaced builder
            caseData.setApplicant2(applicant2);                    // maintain same behaviour
            caseData.setApplicant2UnavailableDatesForTab(accumulatedDates);
        });
    }

    private void addDateToRespondent1(CaseData caseData) {
        Optional.ofNullable(caseData.getRespondent1()).ifPresent(respondent1 -> {
            List<Element<UnavailableDate>> accumulatedDates =
                addDatesToExistingDates(caseData, respondent1.getUnavailableDates());

            respondent1.setUnavailableDates(accumulatedDates);      // replace builder
            caseData.setRespondent1(respondent1);                   // maintain original behaviour
            caseData.setRespondent1UnavailableDatesForTab(accumulatedDates);
        });
    }

    private void addDateToRespondent2(CaseData caseData) {
        Optional.ofNullable(caseData.getRespondent2()).ifPresent(respondent2 -> {
            List<Element<UnavailableDate>> accumulatedDates =
                addDatesToExistingDates(caseData, respondent2.getUnavailableDates());

            respondent2.setUnavailableDates(accumulatedDates);
            caseData.setRespondent2(respondent2);
            caseData.setRespondent2UnavailableDatesForTab(accumulatedDates);
        });
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        String body = "<br /><h3 class=\"govuk-heading-\">What happens next</h3>"
            + " %n%n Any dates marked as being unavailable for a hearing are now displayed in the Listing notes tab.";

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Availability updated")
            .confirmationBody(format(body))
            .build();
    }
}
