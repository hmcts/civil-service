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
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
                .put(callbackKey(ABOUT_TO_START), this::validateRequestEligibilityAndGetPartyDetails)
                .put(callbackKey(ABOUT_TO_SUBMIT), this::saveRequestForReconsiderationReason)
                .put(callbackKey(SUBMITTED), this::buildConfirmation)
                .build();
    }

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

    private List<String> validateRequestEligibility(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.getTotalClaimAmount().compareTo(MAX_CLAIM_AMOUNT) > 0) {
            errors.add(ERROR_MESSAGE_SPEC_AMOUNT_GREATER_THAN_TEN_THOUSAND);
        } else if (caseData.getEaCourtLocation() == YES && caseData.getIsReferToJudgeClaim() == YesOrNo.YES) {
            errors.add(ERROR_MESSAGE_EVENT_NOT_ALLOWED);
        } else {
            validateDeadline(caseData, errors);
        }
        return errors;
    }

    private void validateDeadline(CaseData caseData, List<String> errors) {
        if (caseData.getRequestForReconsiderationDeadline() != null) {
            if (LocalDateTime.now().isAfter(caseData.getRequestForReconsiderationDeadline())) {
                errors.add(ERROR_MESSAGE_DEADLINE_EXPIRED);
            }
        } else {
            caseData.getSystemGeneratedCaseDocuments()
                .stream()
                .filter(doc -> doc.getValue().getDocumentType().equals(DocumentType.SDO_ORDER))
                .max(Comparator.comparing(doc -> doc.getValue().getCreatedDatetime()))
                .map(doc -> doc.getValue().getCreatedDatetime())
                .filter(createdDate -> LocalDateTime.now().isAfter(createdDate.plusDays(RECONSIDERATION_DEADLINE_DAYS)))
                .ifPresent(expired -> errors.add(ERROR_MESSAGE_DEADLINE_EXPIRED));
        }
    }

    private void setPartyDetails(CaseData caseData, CallbackParams callbackParams) {
        List<String> roles = getUserRole(callbackParams);
        if (isApplicantSolicitor(roles)) {
            caseData.setCasePartyRequestForReconsideration("Applicant");
        } else if (isRespondentSolicitorOne(roles)) {
            caseData.setCasePartyRequestForReconsideration("Respondent1");
        } else if (isRespondentSolicitorTwo(roles)) {
            caseData.setCasePartyRequestForReconsideration("Respondent2");
        }
    }

    private boolean applicant2Present(CaseData caseData) {
        return caseData.getAddApplicant2() != null && caseData.getAddApplicant2() == YES;
    }

    private boolean respondent2Present(CaseData caseData) {
        return caseData.getAddRespondent2() != null
                && caseData.getAddRespondent2() == YES;
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
                && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private CallbackResponse saveRequestForReconsiderationReason(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        List<String> roles = getUserRole(callbackParams);

        if (isApplicantSolicitor(roles)) {
            handleApplicantSolicitor(caseData, callbackParams);
        } else if (isRespondentSolicitorOne(roles)) {
            handleRespondentSolicitorOne(caseData, callbackParams);
        } else if (isRespondentSolicitorTwo(roles)) {
            handleRespondentSolicitorTwo(caseData);
        } else if (isLIPClaimant(roles)) {
            handleLIPClaimant(caseData, callbackParams);
        } else if (isLIPDefendant(roles)) {
            handleLIPDefendant(caseData, callbackParams);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private void handleApplicantSolicitor(CaseData caseData, CallbackParams callbackParams) {
        String partyName = APPLICANT_PREFIX + caseData.getApplicant1().getPartyName()
            + (applicant2Present(caseData) ? AND + caseData.getApplicant2().getPartyName() : "");

        ReasonForReconsideration reason = caseData.getReasonForReconsiderationApplicant();
        reason.setRequestor(partyName);
        if (StringUtils.isBlank(reason.getReasonForReconsiderationTxt())) {
            reason.setReasonForReconsiderationTxt(REASON_NOT_PROVIDED);
        }
        caseData.setReasonForReconsiderationApplicant(reason);

        if (caseData.isRespondent1LiP()) {
            caseData.setRequestForReconsiderationDocument(generateLiPDocument(caseData, callbackParams, true));
            caseData.setBusinessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_CLAIMANT));
            caseData.setOrderRequestedForReviewClaimant(YES);
        }
    }

    private void handleRespondentSolicitorOne(CaseData caseData, CallbackParams callbackParams) {
        String partyName = DEFENDANT_PREFIX + caseData.getRespondent1().getPartyName()
            + (respondent2Present(caseData) && respondent2HasSameLegalRep(caseData)
            ? AND + caseData.getRespondent2().getPartyName() : "");

        ReasonForReconsideration reason = caseData.getReasonForReconsiderationRespondent1();
        reason.setRequestor(partyName);
        if (StringUtils.isBlank(reason.getReasonForReconsiderationTxt())) {
            reason.setReasonForReconsiderationTxt(REASON_NOT_PROVIDED);
        }
        caseData.setReasonForReconsiderationRespondent1(reason);

        if (caseData.isApplicantLiP()) {
            caseData.setRequestForReconsiderationDocumentRes(generateLiPDocument(caseData, callbackParams, false));
            caseData.setBusinessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_DEFENDANT));
            caseData.setOrderRequestedForReviewDefendant(YES);
        }
    }

    private void handleRespondentSolicitorTwo(CaseData caseData) {
        String partyName = DEFENDANT_PREFIX + (respondent2Present(caseData) ? caseData.getRespondent2().getPartyName() : "");

        ReasonForReconsideration reason = caseData.getReasonForReconsiderationRespondent2();
        reason.setRequestor(partyName);
        if (StringUtils.isBlank(reason.getReasonForReconsiderationTxt())) {
            reason.setReasonForReconsiderationTxt(REASON_NOT_PROVIDED);
        }
        caseData.setReasonForReconsiderationRespondent2(reason);
    }

    private void handleLIPClaimant(CaseData caseData, CallbackParams callbackParams) {
        ReasonForReconsideration reason = Optional.ofNullable(caseData.getReasonForReconsiderationApplicant())
            .orElseGet(ReasonForReconsideration::new);

        reason.setRequestor(getPartyAsRequestor(APPLICANT_PREFIX, caseData.getApplicant1(), caseData.getApplicant2()));
        reason.setReasonForReconsiderationTxt(Optional.ofNullable(caseData.getCaseDataLiP())
                                                  .map(CaseDataLiP::getRequestForReviewCommentsClaimant)
                                                  .filter(StringUtils::isNotBlank)
                                                  .orElse(REASON_NOT_PROVIDED));

        caseData.setReasonForReconsiderationApplicant(reason);
        caseData.setRequestForReconsiderationDocument(generateLiPDocument(caseData, callbackParams, true));
        caseData.setOrderRequestedForReviewClaimant(YES);
        caseData.setBusinessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_CLAIMANT));
    }

    private void handleLIPDefendant(CaseData caseData, CallbackParams callbackParams) {
        ReasonForReconsideration reason = Optional.ofNullable(caseData.getReasonForReconsiderationRespondent1())
            .orElseGet(ReasonForReconsideration::new);

        reason.setRequestor(getPartyAsRequestor(DEFENDANT_PREFIX, caseData.getRespondent1(), null));
        reason.setReasonForReconsiderationTxt(Optional.ofNullable(caseData.getCaseDataLiP())
                                                  .map(CaseDataLiP::getRequestForReviewCommentsDefendant)
                                                  .filter(StringUtils::isNotBlank)
                                                  .orElse(REASON_NOT_PROVIDED));

        caseData.setReasonForReconsiderationRespondent1(reason);
        caseData.setRequestForReconsiderationDocumentRes(generateLiPDocument(caseData, callbackParams, false));
        caseData.setOrderRequestedForReviewDefendant(YES);
        caseData.setBusinessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_DEFENDANT));
    }

    private CaseDocument generateLiPDocument(CaseData caseData, CallbackParams callbackParams, boolean isClaimant) {
        return documentGenerator.generateLiPDocument(
            caseData,
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            isClaimant
        );
    }

    private String getPartyAsRequestor(String prefix, Party party1, Party party2) {
        StringBuilder partyName = new StringBuilder();
        partyName.append(prefix);
        partyName.append(party1.getPartyName());
        Optional.ofNullable(party2)
                .ifPresent(p -> partyName.append(AND).append(p.getPartyName()));
        return partyName.toString();
    }

    private List<String> getUserRole(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return coreCaseUserService.getUserCaseRoles(
                callbackParams.getCaseData().getCcdCaseReference().toString(),
                userInfo.getUid()
        );
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
                .confirmationHeader(CONFIRMATION_HEADER)
                .confirmationBody(CONFIRMATION_BODY)
                .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
