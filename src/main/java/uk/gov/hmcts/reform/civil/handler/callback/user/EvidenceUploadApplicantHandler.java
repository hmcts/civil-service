package uk.gov.hmcts.reform.civil.handler.callback.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD_APPLICANT;

@Service
public class EvidenceUploadApplicantHandler extends EvidenceUploadHandlerBase {

    public EvidenceUploadApplicantHandler(UserService userService, CoreCaseUserService coreCaseUserService, ObjectMapper objectMapper, Time time) {
        super(userService, coreCaseUserService, objectMapper, time, Collections.singletonList(EVIDENCE_UPLOAD_APPLICANT),
              "validateValuesApplicant", null);
    }

    @Override
    CallbackResponse createShowCondition(CaseData caseData) {
        return null;
    }

    @Override
    CallbackResponse validateValues(CallbackParams callbackParams, CaseData caseData) {
        return validateValuesParty(caseData.getDocumentForDisclosure(),
                                   caseData.getDocumentWitnessStatement(),
                                   caseData.getDocumentHearsayNotice(),
                                   caseData.getDocumentReferredInStatement(),
                                   caseData.getDocumentExpertReport(),
                                   caseData.getDocumentJointStatement(),
                                   caseData.getDocumentQuestions(),
                                   caseData.getDocumentAnswers(),
                                   caseData.getDocumentEvidenceForTrial());
    }

    void applyDocumentUploadDate(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, LocalDateTime now) {
        caseDataBuilder.caseDocumentUploadDate(now);
    }

    void updateDocumentListUploadedAfterBundle(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData) {
        List<Element<UploadEvidenceDocumentType>> applicantEvidenceUploadedAfterBundle = new ArrayList<>();
        Optional<Bundle> bundleDetails =
            caseData.getCaseBundles().stream().map(IdValue::getValue)
                .max(Comparator.comparing(bundle -> bundle.getCreatedOn().orElse(null)));
        if (bundleDetails.isPresent() && nonNull(bundleDetails.get().getCreatedOn())) {
            addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentDisclosureList(), bundleDetails,
                             EvidenceUploadFiles.DISCLOSURE_LIST.getDocumentTypeDisplayName()
            );
            addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentForDisclosure(), bundleDetails,
                             EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.getDocumentTypeDisplayName());
            addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentReferredInStatement(),
                             bundleDetails,
                             EvidenceUploadFiles.DOCUMENTS_REFERRED.getDocumentTypeDisplayName());
            addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentCaseSummary(), bundleDetails,
                             EvidenceUploadFiles.CASE_SUMMARY.getDocumentTypeDisplayName());
            addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentSkeletonArgument(),
                             bundleDetails,
                             EvidenceUploadFiles.SKELETON_ARGUMENT.getDocumentTypeDisplayName());
            addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentAuthorities(), bundleDetails,
                             EvidenceUploadFiles.AUTHORITIES.getDocumentTypeDisplayName());
            addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentCosts(), bundleDetails,
                             EvidenceUploadFiles.COSTS.getDocumentTypeDisplayName());
            addUploadDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentEvidenceForTrial(),
                             bundleDetails,
                             EvidenceUploadFiles.DOCUMENTARY.getDocumentTypeDisplayName());
            addWitnessDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentWitnessStatement(),
                              bundleDetails,
                              EvidenceUploadFiles.WITNESS_STATEMENT.getDocumentTypeDisplayName());
            addWitnessDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentWitnessSummary(), bundleDetails,
                              EvidenceUploadFiles.WITNESS_SUMMARY.getDocumentTypeDisplayName());
            addWitnessDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentHearsayNotice(), bundleDetails,
                              EvidenceUploadFiles.NOTICE_OF_INTENTION.getDocumentTypeDisplayName());
            addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentExpertReport(), bundleDetails,
                             EvidenceUploadFiles.EXPERT_REPORT.getDocumentTypeDisplayName());
            addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentJointStatement(), bundleDetails,
                             EvidenceUploadFiles.JOINT_STATEMENT.getDocumentTypeDisplayName());
            addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentQuestions(), bundleDetails,
                             EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDocumentTypeDisplayName());
            addExpertDocList(applicantEvidenceUploadedAfterBundle, caseData.getDocumentAnswers(), bundleDetails,
                             EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDocumentTypeDisplayName());
            caseDataBuilder.applicantDocsUploadedAfterBundle(applicantEvidenceUploadedAfterBundle);
        }
    }
}

