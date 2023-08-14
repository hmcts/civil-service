package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.AdditionalDates;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_UNAVAILABLE_DATES;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@Service
@RequiredArgsConstructor
public class AddUnavailableDatesCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(ADD_UNAVAILABLE_DATES);

    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final UnavailableDateValidator unavailableDateValidator;
    private final ObjectMapper objectMapper;

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
            callbackKey(SUBMITTED), this::emptyCallbackResponse
        );
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
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

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.addUnavailableDatesScreens(AdditionalDates.builder()
                                                       .partyChosen(DynamicList.fromList(dynamicListOptions))
                                                       .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<String> errors = unavailableDateValidator.validateAdditionalUnavailableDates(
            caseData.getAddUnavailableDatesScreens().getAdditionalUnavailableDates()
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        if (caseData.getAddUnavailableDatesScreens() != null
            && caseData.getAddUnavailableDatesScreens().getPartyChosen() != null
            && caseData.getAddUnavailableDatesScreens().getPartyChosen().getValue() != null
        ) {
            // Admin Screens
            addDatesAdminScenario(caseData.getAddUnavailableDatesScreens().getPartyChosen().getValue().getLabel(),
                                  caseData, updatedData);

        } else {
            // Legal Rep Screens
            addDatesLegalRepScenario(callbackParams, updatedData);
        }

        //clear form
        updatedData.addUnavailableDatesScreens(null);

        return AboutToStartOrSubmitCallbackResponse.builder().data(updatedData.build().toMap(objectMapper)).build();
    }

    private void addDatesAdminScenario(String partyChosen, CaseData caseData, CaseData.CaseDataBuilder updatedData) {
        switch (partyChosen) {
            case ("Claimants"): {
                // FALL-THROUGH
                addDateToApplicant2(caseData, updatedData);
            }
            case ("Claimant"): {
                addDateToApplicant1(caseData, updatedData);
                break;
            }
            case ("Defendants"): {
                // FALL-THROUGH
                addDateToDefendant2(caseData, updatedData);
            }
            case ("Defendant"):
            case ("Defendant 1"): {
                addDateToDefendant1(caseData, updatedData);
                break;
            }
            case ("Defendant 2") : {
                addDateToDefendant2(caseData, updatedData);
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
                    addDateToDefendant1(caseData, updatedData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData, updatedData);
                }
                break;
            }
            case ONE_V_TWO_ONE_LEGAL_REP: {
                if (isRespondentSolicitorOne(roles) || isRespondentSolicitorTwo(roles)) {
                    addDateToDefendant1(caseData, updatedData);
                    addDateToDefendant2(caseData, updatedData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData, updatedData);
                }
                break;
            }
            case ONE_V_TWO_TWO_LEGAL_REP: {
                if (isRespondentSolicitorOne(roles)) {
                    addDateToDefendant1(caseData, updatedData);
                } else if (isRespondentSolicitorTwo(roles)) {
                    addDateToDefendant2(caseData, updatedData);
                } else {
                    // Claimant Solicitor
                    addDateToApplicant1(caseData, updatedData);
                }
                break;
            }
            case TWO_V_ONE: {
                if (isRespondentSolicitorOne(roles)) {
                    addDateToDefendant1(caseData, updatedData);
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

    private boolean isRespondentSolicitorOne(List<String> roles) {
        return hasCaseRole(roles, RESPONDENTSOLICITORONE);
    }

    private boolean isRespondentSolicitorTwo(List<String> roles) {
        return hasCaseRole(roles, RESPONDENTSOLICITORTWO);
    }

    private boolean hasCaseRole(List<String> roles, CaseRole role) {
        return roles.stream().anyMatch(role.getFormattedName()::contains);
    }

    private List<Element<UnavailableDate>> addDateToExistingDate(CaseData caseData, List<Element<UnavailableDate>> partyDates) {
        List<Element<UnavailableDate>> updatedUnavailableDates = ofNullable(partyDates).orElse(newArrayList());
        updatedUnavailableDates.addAll(caseData.getAddUnavailableDatesScreens().getAdditionalUnavailableDates());
        return updatedUnavailableDates;
    }

    private void addDateToApplicant1(CaseData caseData, CaseData.CaseDataBuilder updatedData) {
        updatedData.applicant1(caseData.getApplicant1().toBuilder()
                                   .unavailableDates(
                                       addDateToExistingDate(caseData, caseData.getApplicant1().getUnavailableDates())
                                   ).build());
        //need to add the tab one too
    }

    private void addDateToApplicant2(CaseData caseData, CaseData.CaseDataBuilder updatedData) {
        updatedData.applicant2(caseData.getApplicant2().toBuilder()
                                   .unavailableDates(
                                       addDateToExistingDate(caseData, caseData.getApplicant2().getUnavailableDates())
                                   ).build());
        //need to add the tab one too
    }

    private void addDateToDefendant1(CaseData caseData, CaseData.CaseDataBuilder updatedData) {
        updatedData.respondent1(caseData.getRespondent1().toBuilder()
                                   .unavailableDates(
                                       addDateToExistingDate(caseData, caseData.getRespondent1().getUnavailableDates())
                                   ).build());
        //need to add the tab one too
    }

    private void addDateToDefendant2(CaseData caseData, CaseData.CaseDataBuilder updatedData) {
        updatedData.respondent2(caseData.getRespondent2().toBuilder()
                                    .unavailableDates(
                                        addDateToExistingDate(caseData, caseData.getRespondent2().getUnavailableDates())
                                    ).build());
        //need to add the tab one too
    }
}
