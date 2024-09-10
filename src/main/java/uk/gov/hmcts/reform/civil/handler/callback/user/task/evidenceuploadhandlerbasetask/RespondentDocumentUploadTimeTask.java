package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceuploadhandlerbasetask;

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
public class RespondentDocumentUploadTimeTask extends  DocumentUploadTimeTask {

    private static final String RESPONDENT = "respondent";

    public RespondentDocumentUploadTimeTask(Time time, FeatureToggleService featureToggleService, ObjectMapper objectMapper, CaseDetailsConverter caseDetailsConverter, CoreCaseDataService coreCaseDataService) {
        super(time, featureToggleService, objectMapper, caseDetailsConverter, coreCaseDataService);
    }

    @Override
    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDateRes(now);
    }

    @Override
    void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData) {

        addUploadDocList(caseData.getDocumentDisclosureListRes(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName(), RESPONDENT);

        addUploadDocList(caseData.getDocumentDisclosureListRes2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentForDisclosureRes(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentForDisclosureRes2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentReferredInStatementRes(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentReferredInStatementRes2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentCaseSummaryRes(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentCaseSummaryRes2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentSkeletonArgumentRes(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentSkeletonArgumentRes2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentAuthoritiesRes(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentAuthoritiesRes2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentCostsRes(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentCostsRes2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentEvidenceForTrialRes(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentEvidenceForTrialRes2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentWitnessStatementRes(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentWitnessStatementRes2(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentWitnessSummaryRes(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentWitnessSummaryRes2(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentHearsayNoticeRes(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentHearsayNoticeRes2(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentExpertReportRes(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentExpertReportRes2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentJointStatementRes(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentJointStatementRes2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentQuestionsRes(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentQuestionsRes2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentAnswersRes(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName(), RESPONDENT
        );

        addUploadDocList(caseData.getDocumentAnswersRes2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName(), RESPONDENT
        );
    }
}
