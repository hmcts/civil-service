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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.LiPRequestReconsiderationGeneratorService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.civil.model.common.Element;

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
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
public class RequestForReconsiderationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(REQUEST_FOR_RECONSIDERATION);
    private static final String AND = " and ";
    private static final String DEFENDANT = "Defendant - ";
    private static final String REASON_NOT_PROVIDED = "Not provided";
    protected final ObjectMapper objectMapper;
    private static final String ERROR_MESSAGE_DEADLINE_EXPIRED
        = "You can no longer request a reconsideration because the deadline has expired";
    private static final String ERROR_MESSAGE_SPEC_AMOUNT_GREATER_THAN_THOUSAND = "You can only request a reconsideration for claims of £1,000 or less.";
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private static final String CONFIRMATION_HEADER = "# Your request has been submitted";
    private static final String CONFIRMATION_BODY = "### What happens next \n" +
        "You should receive an update on your request for determination after 10 days, please monitor" +
        " your notifications/dashboard for an update.";

    private final LiPRequestReconsiderationGeneratorService documentGenerator;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::validateRequestEligibilityAndGetPartyDetails)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveRequestForReconsiderationReason)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse validateRequestEligibilityAndGetPartyDetails(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        if (callbackParams.getCaseData().getTotalClaimAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {
            errors.add(ERROR_MESSAGE_SPEC_AMOUNT_GREATER_THAN_THOUSAND);
        } else {
            Optional<Element<CaseDocument>> sdoDocLatest = callbackParams.getCaseData().getSystemGeneratedCaseDocuments()
                .stream().filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                    .equals(DocumentType.SDO_ORDER))
                .sorted(Comparator.comparing(
                    caseDocumentElement -> caseDocumentElement.getValue().getCreatedDatetime(),
                    Comparator.reverseOrder()
                )).findFirst();
            if (sdoDocLatest.isPresent()) {
                LocalDateTime sdoDocLatestDate = sdoDocLatest.get().getValue().getCreatedDatetime();
                if (LocalDateTime.now().isAfter(sdoDocLatestDate.plusDays(7))) {
                    errors.add(ERROR_MESSAGE_DEADLINE_EXPIRED);
                }
            }
        }
        if (errors.isEmpty()) {
            CaseData.CaseDataBuilder<?, ?> updatedData = getPartyDetails(callbackParams);
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
        } else {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
    }

    private CaseData.CaseDataBuilder<?, ?> getPartyDetails(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        List<String> roles = getUserRole(callbackParams);
        if (isApplicantSolicitor(roles)) {
            updatedData.casePartyRequestForReconsideration("Applicant");
        } else if (isRespondentSolicitorOne(roles)) {
            updatedData.casePartyRequestForReconsideration("Respondent1");
        } else if (isRespondentSolicitorTwo(roles)) {
            updatedData.casePartyRequestForReconsideration("Respondent2");
        }
        return updatedData;
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
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        List<String> roles = getUserRole(callbackParams);
        StringBuilder partyName = new StringBuilder();
        if (isApplicantSolicitor(roles)) {
            partyName.append("Applicant - ");
            partyName.append(caseData.getApplicant1().getPartyName());
            partyName.append(applicant2Present(caseData)
                                 ? AND + caseData.getApplicant2().getPartyName() : "");
            ReasonForReconsideration reasonForReconsideration = caseData.getReasonForReconsiderationApplicant();
            reasonForReconsideration.setRequestor(partyName.toString());
            if (StringUtils.isBlank(reasonForReconsideration.getReasonForReconsiderationTxt())) {
                reasonForReconsideration.setReasonForReconsiderationTxt(REASON_NOT_PROVIDED);
            }
            updatedData.reasonForReconsiderationApplicant(reasonForReconsideration);
            if (featureToggleService.isCaseProgressionEnabled() && caseData.isRespondent1LiP()) {
                updatedData.requestForReconsiderationDocument(documentGenerator.generateLiPDocument(
                    caseData,
                    callbackParams.getParams().get(BEARER_TOKEN).toString(),
                    true
                ));
                updatedData.businessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_CLAIMANT));
                updatedData.orderRequestedForReviewClaimant(YES);
            }
        } else if (isRespondentSolicitorOne(roles)) {
            partyName.append(DEFENDANT);
            partyName.append(caseData.getRespondent1().getPartyName());
            partyName.append(respondent2Present(caseData) && respondent2HasSameLegalRep(caseData)
                                 ? AND + caseData.getRespondent2().getPartyName() : "");

            ReasonForReconsideration reasonForReconsideration = caseData.getReasonForReconsiderationRespondent1();
            reasonForReconsideration.setRequestor(partyName.toString());
            if (StringUtils.isBlank(reasonForReconsideration.getReasonForReconsiderationTxt())) {
                reasonForReconsideration.setReasonForReconsiderationTxt(REASON_NOT_PROVIDED);
            }
            updatedData.reasonForReconsiderationRespondent1(reasonForReconsideration);
            if (featureToggleService.isCaseProgressionEnabled() && caseData.isApplicantLiP()) {
                updatedData.requestForReconsiderationDocumentRes(documentGenerator.generateLiPDocument(
                    caseData,
                    callbackParams.getParams().get(BEARER_TOKEN).toString(),
                    false
                ));
                updatedData.businessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_DEFENDANT));
                updatedData.orderRequestedForReviewDefendant(YES);
            }
        } else if (isRespondentSolicitorTwo(roles)) {
            partyName.append(DEFENDANT);
            partyName.append(respondent2Present(caseData) ? caseData.getRespondent2().getPartyName() : "");
            ReasonForReconsideration reasonForReconsideration = caseData.getReasonForReconsiderationRespondent2();
            reasonForReconsideration.setRequestor(partyName.toString());
            if (StringUtils.isBlank(reasonForReconsideration.getReasonForReconsiderationTxt())) {
                reasonForReconsideration.setReasonForReconsiderationTxt(REASON_NOT_PROVIDED);
            }
            updatedData.reasonForReconsiderationRespondent2(reasonForReconsideration);
        } else if (featureToggleService.isCaseProgressionEnabled() && isLIPClaimant(roles)) {
            ReasonForReconsideration reasonForReconsideration = Optional
                .ofNullable(caseData.getReasonForReconsiderationApplicant())
                .orElseGet(ReasonForReconsideration::new);
            reasonForReconsideration.setRequestor(getPartyAsRequestor(
                "Applicant - ",
                caseData.getApplicant1(),
                caseData.getApplicant2()
            ));
            updatedData.reasonForReconsiderationApplicant(reasonForReconsideration);
            reasonForReconsideration.setReasonForReconsiderationTxt(Optional.ofNullable(caseData.getCaseDataLiP())
                                                                        .map(CaseDataLiP::getRequestForReviewCommentsClaimant)
                                                                        .filter(StringUtils::isNotBlank)
                                                                        .orElse(REASON_NOT_PROVIDED));

            // visible if respondent is LiP but also because CUI confirmation page includes the link
            updatedData.requestForReconsiderationDocument(documentGenerator.generateLiPDocument(
                caseData,
                callbackParams.getParams().get(
                    BEARER_TOKEN).toString(),
                true
            ));

            updatedData.orderRequestedForReviewClaimant(YES);
            updatedData.businessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_CLAIMANT));
        } else if (featureToggleService.isCaseProgressionEnabled() && isLIPDefendant(roles)) {
            ReasonForReconsideration reasonForReconsideration = Optional
                .ofNullable(caseData.getReasonForReconsiderationRespondent1())
                .orElseGet(ReasonForReconsideration::new);
            reasonForReconsideration.setRequestor(getPartyAsRequestor(DEFENDANT,
                                                                      caseData.getRespondent1(), null
            ));
            updatedData.reasonForReconsiderationRespondent1(reasonForReconsideration);
            reasonForReconsideration.setReasonForReconsiderationTxt(
                Optional.ofNullable(caseData.getCaseDataLiP())
                    .map(CaseDataLiP::getRequestForReviewCommentsDefendant)
                    .filter(StringUtils::isNotBlank)
                    .orElse(REASON_NOT_PROVIDED)
            );

            // visible if applicant is LiP but also because CUI confirmation page includes the link
            updatedData.requestForReconsiderationDocumentRes(documentGenerator.generateLiPDocument(
                caseData,
                callbackParams.getParams().get(
                    BEARER_TOKEN).toString(),
                false
            ));

            updatedData.orderRequestedForReviewDefendant(YES);
            updatedData.businessProcess(BusinessProcess.ready(REQUEST_FOR_RECONSIDERATION_NOTIFICATION_CUI_DEFENDANT));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
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
