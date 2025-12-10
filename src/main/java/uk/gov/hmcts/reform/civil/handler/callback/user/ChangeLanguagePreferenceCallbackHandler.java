package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;
import uk.gov.hmcts.reform.civil.model.welshenhancements.UserType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_LANGUAGE_PREFERENCE;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.UserType.CLAIMANT;
import static uk.gov.hmcts.reform.civil.model.welshenhancements.UserType.DEFENDANT;

@Service
@RequiredArgsConstructor
public class ChangeLanguagePreferenceCallbackHandler extends CallbackHandler {

    private static final String VALIDATE_LANGUAGE_PREFERENCE = "validate-lang-pref";

    private static final String SELECTED_PARTY_LIP_REQUIRED = "The selected party must be unrepresented.";
    private static final String DEFENDANT_RESPONSE_REQUIRED = "The defendant must have already responded in order to change their language preference.";

    private static final List<CaseEvent> EVENTS = List.of(CHANGE_LANGUAGE_PREFERENCE);

    private final ObjectMapper objectMapper;

    private final Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_START), this::nullFieldsForNewSubmission,
                                                             callbackKey(MID, VALIDATE_LANGUAGE_PREFERENCE), this::validateChangeLanguagePreference,
                                                             callbackKey(ABOUT_TO_SUBMIT), this::changeLanguagePreference,
                                                             callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse);

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse nullFieldsForNewSubmission(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        caseData.setChangeLanguagePreference(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateChangeLanguagePreference(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        CaseData caseData = callbackParams.getCaseData();
        UserType userType = Optional.ofNullable(caseData.getChangeLanguagePreference())
            .map(ChangeLanguagePreference::getUserType)
            .orElseThrow(() -> new IllegalArgumentException("User type not found"));
        if ((userType == CLAIMANT && !caseData.isApplicantLiP()) || (userType == DEFENDANT && !caseData.isRespondent1LiP())) {
            errors.add(SELECTED_PARTY_LIP_REQUIRED);
        } else if (userType == DEFENDANT && (caseData.getCaseDataLiP() == null || caseData.getCaseDataLiP().getRespondent1LiPResponse() == null)) {
            errors.add(DEFENDANT_RESPONSE_REQUIRED);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse changeLanguagePreference(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        PreferredLanguage preferredLanguage = Optional.ofNullable(caseData.getChangeLanguagePreference())
            .map(ChangeLanguagePreference::getPreferredLanguage)
            .orElseThrow(() -> new IllegalArgumentException("Preferred language not found"));
        String revisedBilingualPreference = switch (preferredLanguage) {
            case ENGLISH -> "ENGLISH";
            case WELSH -> "WELSH";
            case ENGLISH_AND_WELSH -> "BOTH";
        };
        UserType userType = Optional.ofNullable(caseData.getChangeLanguagePreference())
            .map(ChangeLanguagePreference::getUserType)
            .orElseThrow(() -> new IllegalArgumentException("User type not found"));
        switch (userType) {
            case CLAIMANT -> setClaimantBilingualLanguagePreference(caseData, preferredLanguage, revisedBilingualPreference);
            case DEFENDANT -> setRespondentResponseBilingualLanguagePreference(caseData, preferredLanguage, revisedBilingualPreference);
            default -> throw new IllegalArgumentException("Unexpected user type");
        }
        caseData.setBusinessProcess(BusinessProcess.ready(CHANGE_LANGUAGE_PREFERENCE));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void setClaimantBilingualLanguagePreference(CaseData caseData,
                                                        PreferredLanguage preferredLanguage,
                                                        String revisedBilingualPreference) {
        caseData.setClaimantBilingualLanguagePreference(revisedBilingualPreference);
        caseData.setClaimantLanguagePreferenceDisplay(preferredLanguage);
    }

    private void setRespondentResponseBilingualLanguagePreference(CaseData caseData,
                                                                  PreferredLanguage preferredLanguage,
                                                                  String revisedBilingualPreference) {
        CaseDataLiP caseDataLiP = caseData.getCaseDataLiP();
        RespondentLiPResponse respondent1LiPResponse = caseDataLiP.getRespondent1LiPResponse();
        respondent1LiPResponse.setRespondent1ResponseLanguage(revisedBilingualPreference);
        caseDataLiP.setRespondent1LiPResponse(respondent1LiPResponse);

        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setDefendantLanguagePreferenceDisplay(preferredLanguage);
    }
}
