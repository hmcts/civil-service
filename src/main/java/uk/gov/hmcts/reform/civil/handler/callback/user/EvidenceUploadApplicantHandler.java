package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
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

        addUploadDocList(caseData.getDocumentDisclosureList(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName(), "applicant");

        addUploadDocList(caseData.getDocumentDisclosureListApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentForDisclosure(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentForDisclosureApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentReferredInStatement(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentReferredInStatementApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentCaseSummary(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentCaseSummaryApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentSkeletonArgument(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentSkeletonArgumentApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentAuthorities(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentAuthoritiesApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentCosts(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentCostsApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentEvidenceForTrial(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentEvidenceForTrialApp2(), document -> document.getValue().getDocumentUpload(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentWitnessStatement(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentWitnessStatementApp2(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentWitnessSummary(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentWitnessSummaryApp2(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentHearsayNotice(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentHearsayNoticeApp2(), document -> document.getValue().getWitnessOptionDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentExpertReport(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentExpertReportApp2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentJointStatement(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentJointStatementApp2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentQuestions(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentQuestionsApp2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentAnswers(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName(), "applicant"
        );

        addUploadDocList(caseData.getDocumentAnswersApp2(), document -> document.getValue().getExpertDocument(),
                         documentDateTime -> documentDateTime.getValue().getCreatedDatetime(), caseDataBuilder, caseData,
                         EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName(), "applicant"
        );
    }
}

