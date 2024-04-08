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

    private static final String APPLICANT = "applicant";

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

