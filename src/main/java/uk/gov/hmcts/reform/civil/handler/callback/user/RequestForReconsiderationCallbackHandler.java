package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.LiPRequestReconsiderationGeneratorService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_CLAIMANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
public class RequestForReconsiderationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(REQUEST_FOR_RECONSIDERATION);
    private static final String AND = " and ";
    private static final String APPLICANT_PREFIX = "Applicant - ";
    private static final String DEFENDANT_PREFIX = "Defendant - ";
    private static final String REASON_NOT_PROVIDED = "Not provided";
    protected final ObjectMapper objectMapper;
    private static final String ERROR_MESSAGE_DEADLINE_EXPIRED
            = "You can no longer request a reconsideration because the deadline has expired";
    private static final String ERROR_MESSAGE_SPEC_AMOUNT_GREATER_THAN_TEN_THOUSAND = "You can only request a reconsideration for claims of £10,000 or less.";
    private static final BigDecimal MAX_CLAIM_AMOUNT = BigDecimal.valueOf(10000);
    private static final int RECONSIDERATION_DEADLINE_DAYS = 7;
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private static final String CONFIRMATION_HEADER = "# Your request has been submitted";
    private static final String CONFIRMATION_BODY = "### What happens next \n" +
            "You should receive an update on your request for determination after 10 days, please monitor" +
            " your notifications/dashboard for an update.";

    private final LiPRequestReconsiderationGeneratorService documentGenerator;
    private static final String ERROR_MESSAGE_EVENT_NOT_ALLOWED = "You can only request a reconsideration where a Legal Advisor has drawn the SDO.";

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
                .put(callbackKey(ABOUT_TO_START), this::validateRequestEligibilityAndGetPartyDetails)
                .put(callbackKey(ABOUT_TO_SUBMIT), this::saveRequestForReconsiderationReason)
                .put(callbackKey(SUBMITTED), this::buildConfirmation)
                .build();
    }

    /**
     * Validates if the request for reconsideration is allowed based on claim amount and state.
     * If valid, identifies which party is making the request and sets it in the case data.
     */
    private CallbackResponse validateRequestEligibilityAndGetPartyDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = validateRequestEligibility(caseData);

        if (errors.isEmpty()) {
            setPartyDetails(caseData, callbackParams);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper)).build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    /**
     * Saves the reason for reconsideration provided by the user.
     * Different handling logic is applied based on whether the user is a solicitor or a LiP (Litigant in Person).
     */
    private CallbackResponse saveRequestForReconsiderationReason(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        List<String> roles = getUserRole(callbackParams);

        if (isApplicantSolicitor(roles)) {
            handleSolicitorRequest(caseData, callbackParams, true);
        } else if (isRespondentSolicitorOne(roles)) {
            handleSolicitorRequest(caseData, callbackParams, false);
        } else if (isRespondentSolicitorTwo(roles)) {
            handleRespondentSolicitorTwo(caseData);
        } else if (isLIPClaimant(roles)) {
            handleLiPRequest(caseData, callbackParams, true);
        } else if (isLIPDefendant(roles)) {
            handleLiPRequest(caseData, callbackParams, false);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private List<String> validateRequestEligibility(CaseData caseData) {
        if (caseData.getTotalClaimAmount().compareTo(MAX_CLAIM_AMOUNT) > 0) {
            return List.of(ERROR_MESSAGE_SPEC_AMOUNT_GREATER_THAN_TEN_THOUSAND);
        }
        if (caseData.getEaCourtLocation() == YES && caseData.getIsReferToJudgeClaim() == YesOrNo.YES) {
            return List.of(ERROR_MESSAGE_EVENT_NOT_ALLOWED);
        }
        List<String> errors = new ArrayList<>();
        validateDeadline(caseData, errors);
        return errors;
    }

    /**
     * Validates the deadline for requesting reconsideration.
     * The deadline is either explicitly stored in 'requestForReconsiderationDeadline'
     * or calculated as 7 days (RECONSIDERATION_DEADLINE_DAYS) from the most recent SDO order creation date.
     */
    private void validateDeadline(CaseData caseData, List<String> errors) {
        LocalDateTime deadline = Optional.ofNullable(caseData.getRequestForReconsiderationDeadline())
            .orElseGet(() -> caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(doc -> doc.getValue().getDocumentType().equals(DocumentType.SDO_ORDER))
                .max(Comparator.comparing(doc -> doc.getValue().getCreatedDatetime()))
                .map(doc -> doc.getValue().getCreatedDatetime().plusDays(RECONSIDERATION_DEADLINE_DAYS))
                .orElse(null));

        if (deadline != null && LocalDateTime.now().isAfter(deadline)) {
            errors.add(ERROR_MESSAGE_DEADLINE_EXPIRED);
        }
    }

    private void setPartyDetails(CaseData caseData, CallbackParams callbackParams) {
        List<String> roles = getUserRole(callbackParams);

        Optional<String> party = Optional.empty();
        if (isApplicantSolicitor(roles)) {
            party = Optional.of("Applicant");
        } else if (isRespondentSolicitorOne(roles)) {
            party = Optional.of("Respondent1");
        } else if (isRespondentSolicitorTwo(roles)) {
            party = Optional.of("Respondent2");
        }

        party.ifPresent(caseData::setCasePartyRequestForReconsideration);
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
                .confirmationHeader(CONFIRMATION_HEADER)
                .confirmationBody(CONFIRMATION_BODY)
                .build();
    }

    /**
     * Generic handler for solicitor requests for reconsideration.
     * Sets the requestor's name and ensures a reason is provided.
     * If the opposing party is a LiP, it also triggers document generation and notification processes.
     */
    private void handleSolicitorRequest(CaseData caseData, CallbackParams callbackParams, boolean isApplicant) {
        String partyName;
        ReasonForReconsideration reason;
        boolean isOtherPartyLiP;

        if (isApplicant) {
            partyName = APPLICANT_PREFIX + caseData.getApplicant1().getPartyName()
                + (isApplicant2Present(caseData) ? AND + caseData.getApplicant2().getPartyName() : "");
            reason = caseData.getReasonForReconsiderationApplicant();
            isOtherPartyLiP = caseData.isRespondent1LiP();
        } else {
            partyName = DEFENDANT_PREFIX + caseData.getRespondent1().getPartyName()
                + (isRespondent2Present(caseData) && isRespondent2SameLegalRep(caseData)
                ? AND + caseData.getRespondent2().getPartyName() : "");
            reason = caseData.getReasonForReconsiderationRespondent1();
            isOtherPartyLiP = caseData.isApplicantLiP();
        }

        reason.setRequestor(partyName);
        if (StringUtils.isBlank(reason.getReasonForReconsiderationTxt())) {
            reason.setReasonForReconsiderationTxt(REASON_NOT_PROVIDED);
        }

        if (isApplicant) {
            caseData.setReasonForReconsiderationApplicant(reason);
            if (isOtherPartyLiP) {
                caseData.setRequestForReconsiderationDocument(generateLiPDocument(caseData, callbackParams, true));
                caseData.setBusinessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_CLAIMANT));
                caseData.setOrderRequestedForReviewClaimant(YES);
            }
        } else {
            caseData.setReasonForReconsiderationRespondent1(reason);
            if (isOtherPartyLiP) {
                caseData.setRequestForReconsiderationDocumentRes(generateLiPDocument(caseData, callbackParams, false));
                caseData.setBusinessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_DEFENDANT));
                caseData.setOrderRequestedForReviewDefendant(YES);
            }
        }
    }

    /**
     * Generic handler for LiP (Litigant in Person) requests for reconsideration.
     * Captures comments from LiP-specific fields, generates a reconsideration document,
     * and sets up the business process for notifications.
     */
    private void handleLiPRequest(CaseData caseData, CallbackParams callbackParams, boolean isClaimant) {
        ReasonForReconsideration reason = Optional.ofNullable(isClaimant
                                                                 ? caseData.getReasonForReconsiderationApplicant()
                                                                 : caseData.getReasonForReconsiderationRespondent1())
            .orElseGet(ReasonForReconsideration::new);

        String prefix = isClaimant ? APPLICANT_PREFIX : DEFENDANT_PREFIX;
        Party party1 = isClaimant ? caseData.getApplicant1() : caseData.getRespondent1();
        Party party2 = isClaimant ? caseData.getApplicant2() : null;

        reason.setRequestor(formatPartyName(prefix, party1, party2));
        reason.setReasonForReconsiderationTxt(Optional.ofNullable(caseData.getCaseDataLiP())
                                                  .map(isClaimant ? CaseDataLiP::getRequestForReviewCommentsClaimant : CaseDataLiP::getRequestForReviewCommentsDefendant)
                                                  .filter(StringUtils::isNotBlank)
                                                  .orElse(REASON_NOT_PROVIDED));

        if (isClaimant) {
            caseData.setReasonForReconsiderationApplicant(reason);
            caseData.setRequestForReconsiderationDocument(generateLiPDocument(caseData, callbackParams, true));
            caseData.setOrderRequestedForReviewClaimant(YES);
            caseData.setBusinessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_CLAIMANT));
        } else {
            caseData.setReasonForReconsiderationRespondent1(reason);
            caseData.setRequestForReconsiderationDocumentRes(generateLiPDocument(caseData, callbackParams, false));
            caseData.setOrderRequestedForReviewDefendant(YES);
            caseData.setBusinessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_DEFENDANT));
        }
    }

    private void handleRespondentSolicitorTwo(CaseData caseData) {
        String partyName = DEFENDANT_PREFIX + (isRespondent2Present(caseData) ? caseData.getRespondent2().getPartyName() : "");

        ReasonForReconsideration reason = caseData.getReasonForReconsiderationRespondent2();
        reason.setRequestor(partyName);
        if (StringUtils.isBlank(reason.getReasonForReconsiderationTxt())) {
            reason.setReasonForReconsiderationTxt(REASON_NOT_PROVIDED);
        }
        caseData.setReasonForReconsiderationRespondent2(reason);
    }

    private CaseDocument generateLiPDocument(CaseData caseData, CallbackParams callbackParams, boolean isClaimant) {
        return documentGenerator.generateLiPDocument(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            isClaimant
        );
    }

    private boolean isApplicant2Present(CaseData caseData) {
        return caseData.getAddApplicant2() != null && caseData.getAddApplicant2() == YES;
    }

    private boolean isRespondent2Present(CaseData caseData) {
        return caseData.getAddRespondent2() != null
                && caseData.getAddRespondent2() == YES;
    }

    private boolean isRespondent2SameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
                && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private String formatPartyName(String prefix, Party party1, Party party2) {
        return prefix + party1.getPartyName() + Optional.ofNullable(party2)
            .map(p -> AND + p.getPartyName())
            .orElse("");
    }

    private List<String> getUserRole(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return coreCaseUserService.getUserCaseRoles(
                callbackParams.getCaseData().getCcdCaseReference().toString(),
                userInfo.getUid()
        );
    }
}
