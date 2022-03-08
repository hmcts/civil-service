package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class ChangeSolicitorEmailCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CHANGE_SOLICITOR_EMAIL);

    private final ValidateEmailService validateEmailService;

    private final UserService userService;

    private final CoreCaseUserService coreCaseUserService;

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::aboutToStart,
            callbackKey(MID, "validate-applicant1-solicitor-email"), this::validateApplicant1SolicitorEmail,
            callbackKey(MID, "validate-respondent1-solicitor-email"), this::validateRespondent1SolicitorEmail,
            callbackKey(MID, "validate-respondent2-solicitor-email"), this::validateRespondent2SolicitorEmail,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        List<String> userRoles = getUserRoles(callbackParams);
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        buildPartyFlags(userRoles, caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseBuilder = caseData.toBuilder();

        clearPartyFlags(caseBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        String partyType = getUserRoles(callbackParams).stream()
            .anyMatch(r -> r.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                || r.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()))
            ? "defendant" : "claimant";

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(
                String.format("# You have updated a %s's legal representative's email address", partyType))
            .confirmationBody("<br />")
            .build();
    }

    private CallbackResponse validateApplicant1SolicitorEmail(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validateEmailService.validate(
                callbackParams.getCaseData().getApplicantSolicitor1UserDetails().getEmail())).build();
    }

    private CallbackResponse validateRespondent1SolicitorEmail(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validateEmailService.validate(
                callbackParams.getCaseData().getRespondentSolicitor1EmailAddress())).build();
    }

    private CallbackResponse validateRespondent2SolicitorEmail(CallbackParams callbackParams) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validateEmailService.validate(
                callbackParams.getCaseData().getRespondentSolicitor2EmailAddress())).build();
    }

    private void buildPartyFlags(List<String> userRoles, CaseData.CaseDataBuilder caseDataBuilder) {
        caseDataBuilder.isApplicant1(userRoles.contains(CaseRole.APPLICANTSOLICITORONE.getFormattedName()) ? YES : NO)
            .isRespondent1(userRoles.contains(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()) ? YES : NO)
            .isRespondent2(userRoles.contains(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName()) ? YES : NO);
    }

    private void clearPartyFlags(CaseData.CaseDataBuilder caseDataBuilder) {
        caseDataBuilder.isApplicant1(null)
            .isRespondent1(null)
            .isRespondent2(null);
    }

    private List<String> getUserRoles(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        return coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );
    }
}

