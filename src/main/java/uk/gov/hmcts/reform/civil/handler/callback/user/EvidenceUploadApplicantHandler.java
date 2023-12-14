package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;

@Service
public class EvidenceUploadApplicantHandler extends EvidenceUploadHandlerBase {

    public EvidenceUploadApplicantHandler(UserService userService, CoreCaseUserService coreCaseUserService,
                                          CaseDetailsConverter caseDetailsConverter,
                                          CoreCaseDataService coreCaseDataService,
                                          ObjectMapper objectMapper, Time time) {
        super(userService, coreCaseUserService, caseDetailsConverter, coreCaseDataService,
                objectMapper, time, Collections.singletonList(EVIDENCE_UPLOAD_APPLICANT),
              "validateValuesApplicant", "createShowCondition");
    }

    @Override
    CallbackResponse createShowCondition(CaseData caseData, UserInfo userInfo) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        //For case which are 1v1, 2v1  we show respondent fields for documents to be uploaded,
        //if a case is 1v2 and different solicitors we want to show separate fields for each respondent solicitor i.e.
        //RESPONDENTSOLICITORTWO and RESPONDENTSOLICITORONE
        //if a case is 1v2 with same solicitor they will see respondent 2 fields as they have RESPONDENTSOLICITORTWO role
        //default flag for respondent 1 solicitor
        caseDataBuilder.caseTypeFlag("do_not_show");

        boolean multiParts = Objects.nonNull(caseData.getEvidenceUploadOptions())
                && !caseData.getEvidenceUploadOptions().getListItems().isEmpty();
        if (multiParts
                && caseData.getEvidenceUploadOptions()
                .getValue().getLabel().startsWith("Claimant 2 - ")) {
            caseDataBuilder.caseTypeFlag("ApplicantTwoFields");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(this.objectMapper))
                .build();
    }

    @Override
    CallbackResponse validateValues(CallbackParams callbackParams, CaseData caseData) {
        if (Objects.nonNull(caseData.getCaseTypeFlag())
                && caseData.getCaseTypeFlag().equals("ApplicantTwoFields")) {
            return validateValuesParty(caseData.getDocumentForDisclosureRes2(),
                    caseData.getDocumentWitnessStatementApp2(),
                    caseData.getDocumentWitnessSummaryApp2(),
                    caseData.getDocumentHearsayNoticeApp2(),
                    caseData.getDocumentReferredInStatementApp2(),
                    caseData.getDocumentExpertReportApp2(),
                    caseData.getDocumentJointStatementApp2(),
                    caseData.getDocumentQuestionsApp2(),
                    caseData.getDocumentAnswersApp2(),
                    caseData.getDocumentEvidenceForTrialApp2());
        } else {
            return validateValuesParty(caseData.getDocumentForDisclosure(),
                    caseData.getDocumentWitnessStatement(),
                    caseData.getDocumentWitnessSummary(),
                    caseData.getDocumentHearsayNotice(),
                    caseData.getDocumentReferredInStatement(),
                    caseData.getDocumentExpertReport(),
                    caseData.getDocumentJointStatement(),
                    caseData.getDocumentQuestions(),
                    caseData.getDocumentAnswers(),
                    caseData.getDocumentEvidenceForTrial());
        }
    }

    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDate(now);
    }

    void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData) {
        List<Element<UploadEvidenceDocumentType>> applicantEvidenceUploadedAfterBundle = new ArrayList<>();
        Optional<Bundle> bundleDetails =
            caseData.getCaseBundles().stream().map(IdValue::getValue)
                .max(Comparator.comparing(bundle -> bundle.getCreatedOn().orElse(null)));

        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentDisclosureList(), bundleDetails,
                         EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentDisclosureListApp2(), bundleDetails,
                EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentForDisclosure(), bundleDetails,
                         EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentForDisclosureApp2(), bundleDetails,
                EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentReferredInStatement(),
                         bundleDetails,
                         EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentReferredInStatementApp2(),
                bundleDetails,
                EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentCaseSummary(), bundleDetails,
                         EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentCaseSummaryApp2(), bundleDetails,
                EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentSkeletonArgument(),
                         bundleDetails,
                         EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentSkeletonArgumentApp2(),
                bundleDetails,
                EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentAuthorities(), bundleDetails,
                         EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentAuthoritiesApp2(), bundleDetails,
                EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentCosts(), bundleDetails,
                         EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentCostsApp2(), bundleDetails,
                EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentEvidenceForTrial(),
                         bundleDetails,
                         EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName()
        );
        addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentEvidenceForTrialApp2(),
                bundleDetails,
                EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName()
        );
        addWitnessDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentWitnessStatement(),
                          bundleDetails,
                          EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName()
        );
        addWitnessDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentWitnessStatementApp2(),
                bundleDetails,
                EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName()
        );
        addWitnessDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentWitnessSummary(), bundleDetails,
                          EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName()
        );
        addWitnessDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentWitnessSummaryApp2(), bundleDetails,
                EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName()
        );
        addWitnessDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentHearsayNotice(), bundleDetails,
                          EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName()
        );
        addWitnessDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentHearsayNoticeApp2(), bundleDetails,
                EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName()
        );
        addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentExpertReport(), bundleDetails,
                         EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName()
        );
        addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentExpertReportApp2(), bundleDetails,
                EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName()
        );
        addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentJointStatement(), bundleDetails,
                         EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName()
        );
        addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentJointStatementApp2(), bundleDetails,
                EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName()
        );
        addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentQuestions(), bundleDetails,
                         EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName()
        );
        addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentQuestionsApp2(), bundleDetails,
                EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName()
        );
        addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentAnswers(), bundleDetails,
                         EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName()
        );
        addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentAnswersApp2(), bundleDetails,
                EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName()
        );
        caseDataBuilder.applicantDocsUploadedAfterBundle(applicantEvidenceUploadedAfterBundle);
    }
}

