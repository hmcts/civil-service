package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();


        boolean isApplicant1 = userRoles.contains(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
        boolean isRespondent1 = userRoles.contains(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
        boolean isRespondent2 = userRoles.contains(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
        caseDataBuilder.isApplicant1(isApplicant1 ? YES : NO)
            .isRespondent1(isRespondent1 ? YES : NO)
            .isRespondent2(isRespondent2 ? YES : NO);

        // depending on flags, keep the current reference so we know if it was changed
        caseDataBuilder.solicitorReferencesCopy(
            SolicitorReferences.builder()
                .applicantSolicitor1Reference(getSolicitorReference(caseData.getApplicant1OrganisationPolicy()))
                .respondentSolicitor1Reference(getSolicitorReference(caseData.getRespondent1OrganisationPolicy()))
                .respondentSolicitor2Reference(getSolicitorReference(caseData.getRespondent2OrganisationPolicy()))
                .build()
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    @Nullable
    private static String getSolicitorReference(OrganisationPolicy policy) {
        return Optional.ofNullable(policy)
            .map(OrganisationPolicy::getOrgPolicyReference)
            .orElse(null);
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseBuilder = caseData.toBuilder();

        updateSolicitorReferences(caseData, caseBuilder);
        clearPartyFlags(caseBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseBuilder.build().toMap(objectMapper))
            .build();
    }

    /**
     * Solicitor references appear twice with two different names (solicitorReferences,
     * XXXOrganisationPolicy_OrgPolicyReference, respondentSolicitor2Reference...).
     * At least two of them (solicitorReferences and XXXOrganisationPolicy) appear in the same journey, with no
     * guarantee that their values are going to match. To lessen the side effects of this event, we check if
     * the field was actually changed before updating the other field(s).
     *
     * @param caseData    original case data
     * @param caseBuilder case data being updated
     */
    private static void updateSolicitorReferences(CaseData caseData, CaseData.CaseDataBuilder caseBuilder) {
        if (caseData.getIsApplicant1() == YES) {
            updateReference(
                Optional.ofNullable(caseData.getSolicitorReferencesCopy())
                    .map(SolicitorReferences::getApplicantSolicitor1Reference)
                    .orElse(null),
                caseData.getApplicant1OrganisationPolicy(),
                newReference -> {
                    SolicitorReferences references = Optional.ofNullable(caseData.getSolicitorReferences())
                        .orElse(SolicitorReferences.builder().build());
                    references.setApplicantSolicitor1Reference(newReference);
                    caseBuilder.solicitorReferences(references);
                }
            );
        } else if (caseData.getIsRespondent1() == YES) {
            updateReference(
                Optional.ofNullable(caseData.getSolicitorReferencesCopy())
                    .map(SolicitorReferences::getRespondentSolicitor1Reference)
                    .orElse(null),
                caseData.getRespondent1OrganisationPolicy(),
                newReference -> {
                    SolicitorReferences references = Optional.ofNullable(caseData.getSolicitorReferences())
                        .orElse(SolicitorReferences.builder().build());
                    references.setRespondentSolicitor1Reference(newReference);
                    caseBuilder.solicitorReferences(references);
                }
            );
        } else if (caseData.getIsRespondent2() == YES) {
            updateReference(
                Optional.ofNullable(caseData.getSolicitorReferencesCopy())
                    .map(SolicitorReferences::getRespondentSolicitor2Reference)
                    .orElse(null),
                caseData.getRespondent2OrganisationPolicy(),
                newReference -> {
                    SolicitorReferences references = Optional.ofNullable(caseData.getSolicitorReferences())
                        .orElse(SolicitorReferences.builder().build());
                    references.setRespondentSolicitor2Reference(newReference);
                    caseBuilder.solicitorReferences(references)
                        .respondentSolicitor2Reference(newReference);
                }
            );
        }
        caseBuilder.solicitorReferencesCopy(SolicitorReferences.builder().build());
    }

    private static void updateReference(String oldReference, OrganisationPolicy policy, Consumer<String> ifDifferent) {
        String newReference = Optional.ofNullable(policy)
            .map(OrganisationPolicy::getOrgPolicyReference)
            .orElse("");
        if (!StringUtils.equals(newReference, oldReference)) {
            ifDifferent.accept(newReference);
        }
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        String partyType = getUserRoles(callbackParams).stream()
            .anyMatch(r -> r.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                || r.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()))
            ? "defendant" : "claimant";

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(
                String.format("# You have updated a %s's legal representative's information", partyType))
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

