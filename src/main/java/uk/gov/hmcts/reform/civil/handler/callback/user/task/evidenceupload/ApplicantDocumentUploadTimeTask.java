package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;

@Component
public class ApplicantDocumentUploadTimeTask extends DocumentUploadTimeTask{

    private static final String APPLICANT = "applicant";

    public ApplicantDocumentUploadTimeTask(Time time, FeatureToggleService featureToggleService, ObjectMapper objectMapper, CaseDetailsConverter caseDetailsConverter, CoreCaseDataService coreCaseDataService) {
        super(time, featureToggleService, objectMapper, caseDetailsConverter, coreCaseDataService);
    }

    @Override
    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDate(now);
    }

    @Override
    void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData) {

         addUploadDocList(caseData.getDocumentDisclosureList(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName(), APPLICANT);

        addUploadDocList(caseData.getDocumentDisclosureListApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentForDisclosure(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentForDisclosureApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentReferredInStatement(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentReferredInStatementApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentCaseSummary(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentCaseSummaryApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentSkeletonArgument(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentSkeletonArgumentApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentAuthorities(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentAuthoritiesApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentCosts(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentCostsApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentEvidenceForTrial(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentEvidenceForTrialApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentWitnessStatement(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentWitnessStatementApp2(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentWitnessSummary(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentWitnessSummaryApp2(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentHearsayNotice(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentHearsayNoticeApp2(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentExpertReport(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentExpertReportApp2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentJointStatement(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentJointStatementApp2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentQuestions(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentQuestionsApp2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentAnswers(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName(), APPLICANT
        );

        addUploadDocList(caseData.getDocumentAnswersApp2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName(), APPLICANT
        );
    }

}
