package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_RESPONDENT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

@Service
public class EvidenceUploadRespondentHandler extends EvidenceUploadHandlerBase {

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    public EvidenceUploadRespondentHandler(UserService userService, CoreCaseUserService coreCaseUserService, ObjectMapper objectMapper, Time time) {
        super(userService, coreCaseUserService, objectMapper, time, Collections.singletonList(EVIDENCE_UPLOAD_RESPONDENT),
              "validateValuesRespondent", "createShowCondition");
        this.userService = userService;
        this.coreCaseUserService = coreCaseUserService;
    }

    @Override
    CallbackResponse validateValues(CallbackParams callbackParams, CaseData caseData) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        if (coreCaseUserService.userHasCaseRole(caseData
                                                    .getCcdCaseReference()
                                                    .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
            return validateValuesParty(caseData.getDocumentForDisclosureRes2(),
                                       caseData.getDocumentWitnessStatementRes2(),
                                       caseData.getDocumentHearsayNoticeRes2(),
                                       caseData.getDocumentReferredInStatementRes2(),
                                       caseData.getDocumentExpertReportRes2(),
                                       caseData.getDocumentJointStatementRes2(),
                                       caseData.getDocumentQuestionsRes2(),
                                       caseData.getDocumentAnswersRes2(),
                                       caseData.getDocumentEvidenceForTrialRes2());
        } else {
            return validateValuesParty(caseData.getDocumentForDisclosureRes(),
                                       caseData.getDocumentWitnessStatementRes(),
                                       caseData.getDocumentHearsayNoticeRes(),
                                       caseData.getDocumentReferredInStatementRes(),
                                       caseData.getDocumentExpertReportRes(),
                                       caseData.getDocumentJointStatementRes(),
                                       caseData.getDocumentQuestionsRes(),
                                       caseData.getDocumentAnswersRes(),
                                       caseData.getDocumentEvidenceForTrialRes());
        }

    }

    @Override
    CallbackResponse createShowCondition(CaseData caseData) {

        return showCondition(caseData, caseData.getWitnessSelectionEvidenceRes(),
                             caseData.getWitnessSelectionEvidenceSmallClaimRes(),
                             caseData.getWitnessSelectionEvidenceRes(),
                             caseData.getWitnessSelectionEvidenceSmallClaimRes(),
                             caseData.getWitnessSelectionEvidenceRes(),
                             caseData.getWitnessSelectionEvidenceSmallClaimRes(),
                             caseData.getExpertSelectionEvidenceRes(),
                             caseData.getExpertSelectionEvidenceSmallClaimRes(),
                             caseData.getExpertSelectionEvidenceRes(),
                             caseData.getExpertSelectionEvidenceSmallClaimRes(),
                             caseData.getTrialSelectionEvidenceRes(),
                             caseData.getTrialSelectionEvidenceSmallClaimRes(),
                             caseData.getTrialSelectionEvidenceRes(),
                             caseData.getTrialSelectionEvidenceSmallClaimRes());
    }

    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDateRes(now);
    }
}

