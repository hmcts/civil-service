package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
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
                             caseData.getTrialSelectionEvidenceSmallClaimRes(),
                             caseData.getTrialSelectionEvidenceRes(),
                             caseData.getTrialSelectionEvidenceSmallClaimRes());
    }

    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDateRes(now);
    }

    void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData) {
        List<Element<UploadEvidenceDocumentType>> respondentEvidenceUploadedAfterBundle = new ArrayList<>();
        Optional<Bundle> bundleDetails =
            caseData.getCaseBundles().stream().map(IdValue::getValue).filter(bundle -> bundle.getCreatedOn().isPresent())
                .max(Comparator.comparing(bundle -> bundle.getCreatedOn().orElse(null)));

        if (bundleDetails.isEmpty() || bundleDetails.get().getCreatedOn().isEmpty()) {
            //If no bundle is created then return
        } else {
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentDisclosureListRes(), bundleDetails,
                             EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName()
            );
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentDisclosureListRes2(), bundleDetails,
                             EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName()
            );
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentForDisclosureRes(),
                             bundleDetails,
                             EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentForDisclosureRes2(),
                             bundleDetails,
                             EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentReferredInStatementRes(),
                             bundleDetails,
                             EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentReferredInStatementRes2(),
                             bundleDetails,
                             EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentCaseSummaryRes(), bundleDetails,
                             EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentCaseSummaryRes2(),
                             bundleDetails,
                             EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentSkeletonArgumentRes(),
                             bundleDetails,
                             EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentSkeletonArgumentRes2(),
                             bundleDetails,
                             EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentAuthoritiesRes(),
                             bundleDetails,
                             EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentAuthoritiesRes2(),
                             bundleDetails,
                             EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentCostsRes(), bundleDetails,
                             EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentCostsRes2(), bundleDetails,
                             EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentEvidenceForTrialRes(),
                             bundleDetails,
                             EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName());
            addUploadDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentEvidenceForTrialRes2(),
                             bundleDetails,
                             EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName());
            addWitnessDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentWitnessStatementRes(),
                              bundleDetails,
                              EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName());
            addWitnessDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentWitnessStatementRes2(),
                              bundleDetails,
                              EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName());
            addWitnessDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentWitnessSummaryRes(),
                              bundleDetails,
                              EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName());
            addWitnessDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentWitnessSummaryRes2(),
                              bundleDetails,
                              EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName());
            addWitnessDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentHearsayNoticeRes(),
                              bundleDetails,
                              EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName());
            addWitnessDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentHearsayNoticeRes2(),
                              bundleDetails,
                              EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName());
            addExpertDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentExpertReportRes(), bundleDetails,
                             EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName());
            addExpertDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentExpertReportRes2(), bundleDetails,
                             EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName());
            addExpertDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentJointStatementRes(), bundleDetails,
                             EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName());
            addExpertDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentJointStatementRes2(), bundleDetails,
                             EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName());
            addExpertDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentQuestionsRes(), bundleDetails,
                             EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName());
            addExpertDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentQuestionsRes2(), bundleDetails,
                             EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName());
            addExpertDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentAnswersRes(), bundleDetails,
                             EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName());
            addExpertDocList(respondentEvidenceUploadedAfterBundle, caseData.getDocumentAnswersRes2(), bundleDetails,
                             EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName());
            caseDataBuilder.respondentDocsUploadedAfterBundle(respondentEvidenceUploadedAfterBundle);
        }
    }
}

