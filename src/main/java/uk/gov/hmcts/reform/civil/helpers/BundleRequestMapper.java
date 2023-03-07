package uk.gov.hmcts.reform.civil.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;

import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.bundle.DocumentLink;
import uk.gov.hmcts.reform.civil.model.bundle.ServedDocument;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleRequestMapper {

    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData,
                                                                String bundleConfigFileName, String jurisdiction,
                                                                String caseTypeId, Long id) {
        String fileNameIdentifier =
            caseData.getCcdCaseReference() + "_" + DateFormatHelper.formatLocalDate(caseData.getHearingDate(),
                                                                                    "ddMMyyyy");
        BundleCreateRequest bundleCreateRequest = BundleCreateRequest.builder()
            .caseDetails(BundlingCaseDetails.builder()
                             .caseData(mapCaseData(caseData,
                                                   bundleConfigFileName))
                             .filenamePrefix(fileNameIdentifier)

                             .build()
            )
            .caseTypeId(caseTypeId)
            .jurisdictionId(jurisdiction).build();
        return bundleCreateRequest;
    }

    private BundlingCaseData mapCaseData(CaseData caseData, String bundleConfigFileName) {
        BundlingCaseData bundlingCaseData =
            BundlingCaseData.builder().id(caseData.getCcdCaseReference()).bundleConfiguration(
                    bundleConfigFileName)
                .systemGeneratedCaseDocuments(mapSystemGeneratedcaseDocument(caseData.getSystemGeneratedCaseDocuments(), caseData.getOrderSDODocumentDJ()))
                .servedDocumentFiles(mapServedDocuments(caseData.getServedDocumentFiles()))
                .documentWitnessStatement(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessStatement(),
                                                                      EvidenceUploadFiles.WITNESS_STATEMENT.getDisplayName()))
                .documentWitnessStatementRes(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessStatementRes(),
                                                                         EvidenceUploadFiles.WITNESS_STATEMENT.getDisplayName()))
                .documentWitnessStatementRes2(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessStatementRes2(),
                                                                          EvidenceUploadFiles.WITNESS_STATEMENT.getDisplayName()))
                .documentWitnessSummary(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessSummary(),
                                                                    EvidenceUploadFiles.WITNESS_SUMMARY.getDisplayName()))
                .documentWitnessSummaryRes(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessSummaryRes(),
                                                                       EvidenceUploadFiles.WITNESS_SUMMARY.getDisplayName()))
                .documentWitnessSummaryRes2(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessSummaryRes2(),
                                                                        EvidenceUploadFiles.WITNESS_SUMMARY.getDisplayName()))
                .documentHearsayNotice(mapUploadEvidenceWitnessDoc(caseData.getDocumentHearsayNotice(),
                                                                   EvidenceUploadFiles.NOTICE_OF_INTENTION.getDisplayName()))
                .documentHearsayNoticeRes(mapUploadEvidenceWitnessDoc(caseData.getDocumentHearsayNoticeRes(),
                                                                      EvidenceUploadFiles.NOTICE_OF_INTENTION.getDisplayName()))
                .documentHearsayNoticeRes2(mapUploadEvidenceWitnessDoc(caseData.getDocumentHearsayNoticeRes2(),
                                                                       EvidenceUploadFiles.NOTICE_OF_INTENTION.getDisplayName()))
                .documentReferredInStatement(mapUploadEvidenceOtherDoc(caseData.getDocumentReferredInStatement()))
                .documentReferredInStatementRes(mapUploadEvidenceOtherDoc(caseData.getDocumentReferredInStatementRes()))
                .documentReferredInStatementRes2(mapUploadEvidenceOtherDoc(caseData.getDocumentReferredInStatementRes2()))
                .documentExpertReport(mapUploadEvidenceExpertDoc(caseData.getDocumentExpertReport(),
                                                                 EvidenceUploadFiles.EXPERT_REPORT.getDisplayName()))
                .documentExpertReportRes(mapUploadEvidenceExpertDoc(caseData.getDocumentExpertReportRes(),
                                                                    EvidenceUploadFiles.EXPERT_REPORT.getDisplayName()))
                .documentExpertReportRes2(mapUploadEvidenceExpertDoc(caseData.getDocumentExpertReportRes2(),
                                                                     EvidenceUploadFiles.EXPERT_REPORT.getDisplayName()))
                .documentJointStatement(mapUploadEvidenceExpertDoc(caseData.getDocumentJointStatement(),
                                                                   EvidenceUploadFiles.JOINT_STATEMENT.getDisplayName()))
                .documentJointStatementRes(mapUploadEvidenceExpertDoc(caseData.getDocumentJointStatementRes(),
                                                                      EvidenceUploadFiles.JOINT_STATEMENT.getDisplayName()))
                .documentJointStatementRes2(mapUploadEvidenceExpertDoc(caseData.getDocumentJointStatementRes2(),
                                                                       EvidenceUploadFiles.JOINT_STATEMENT.getDisplayName()))
                .documentQuestions(mapUploadEvidenceExpertDoc(caseData.getDocumentQuestions(),
                                                              EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDisplayName()))
                .documentQuestionsRes(mapUploadEvidenceExpertDoc(caseData.getDocumentQuestionsRes(),
                                                                 EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDisplayName()))
                .documentQuestionsRes2(mapUploadEvidenceExpertDoc(caseData.getDocumentQuestionsRes2(),
                                                                  EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.getDisplayName()))
                .documentAnswers(mapUploadEvidenceExpertDoc(caseData.getDocumentAnswers(),
                                                            EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDisplayName()))
                .documentAnswersRes(mapUploadEvidenceExpertDoc(caseData.getDocumentAnswersRes(),
                                                               EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDisplayName()))
                .documentAnswersRes2(mapUploadEvidenceExpertDoc(caseData.getDocumentAnswersRes2(),
                                                                EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.getDisplayName()))
                .documentDisclosureList(mapUploadEvidenceOtherDoc(caseData.getDocumentDisclosureList()))
                .documentDisclosureListRes(mapUploadEvidenceOtherDoc(caseData.getDocumentDisclosureListRes()))
                .documentDisclosureListRes2(mapUploadEvidenceOtherDoc(caseData.getDocumentDisclosureListRes2()))
                .documentForDisclosure(mapUploadEvidenceOtherDoc(caseData.getDocumentForDisclosure()))
                .documentForDisclosureRes(mapUploadEvidenceOtherDoc(caseData.getDocumentForDisclosureRes()))
                .documentForDisclosureRes2(mapUploadEvidenceOtherDoc(caseData.getDocumentForDisclosureRes2()))
                .documentCaseSummary(mapUploadEvidenceOtherDoc(caseData.getDocumentCaseSummary()))
                .documentCaseSummaryRes(mapUploadEvidenceOtherDoc(caseData.getDocumentCaseSummaryRes()))
                .documentCaseSummaryRes2(mapUploadEvidenceOtherDoc(caseData.getDocumentCaseSummaryRes2()))
                .documentSkeletonArgument(mapUploadEvidenceOtherDoc(caseData.getDocumentSkeletonArgument()))
                .documentSkeletonArgumentRes(mapUploadEvidenceOtherDoc(caseData.getDocumentSkeletonArgumentRes()))
                .documentSkeletonArgumentRes2(mapUploadEvidenceOtherDoc(caseData.getDocumentSkeletonArgumentRes2()))
                .generalOrderDocument(new ArrayList<>())
                .documentAuthorities(mapUploadEvidenceOtherDoc(caseData.getDocumentAuthorities()))
                .documentAuthoritiesRes(mapUploadEvidenceOtherDoc(caseData.getDocumentAuthoritiesRes()))
                .documentAuthoritiesRes2(mapUploadEvidenceOtherDoc(caseData.getDocumentAuthoritiesRes2()))
                .documentEvidenceForTrial(mapUploadEvidenceOtherDoc(caseData.getDocumentEvidenceForTrial()))
                .documentEvidenceForTrialRes(mapUploadEvidenceOtherDoc(caseData.getDocumentEvidenceForTrialRes()))
                .documentEvidenceForTrialRes2(mapUploadEvidenceOtherDoc(caseData.getDocumentEvidenceForTrialRes2()))
                .defendantResponseDocuments(mapSystemGeneratedcaseDocument(caseData.getDefendantResponseDocuments(),
                                                                           null))
                .applicant1(caseData.getApplicant1())
                .respondent1(caseData.getRespondent1())
                .courtLocation(caseData.getHearingLocation().getValue().getLabel())
                .hearingDate(null != caseData.getHearingDate()
                                 ? DateFormatHelper.formatLocalDate(caseData.getHearingDate(), "dd-MM-yyyy") : null)
                .ccdCaseReference(caseData.getCcdCaseReference())
                .build();
        bundlingCaseData = mapRespondent2Applicant2Details(bundlingCaseData, caseData);
        return  bundlingCaseData;
    }

    private BundlingCaseData mapRespondent2Applicant2Details(BundlingCaseData bundlingCaseData, CaseData caseData) {
        if (null != caseData.getAddApplicant2() && caseData.getAddApplicant2().equals(YesOrNo.YES)) {
            bundlingCaseData.toBuilder().hasApplicant2(true);
        }
        if (null != caseData.getAddRespondent2() && caseData.getAddRespondent2().equals(YesOrNo.YES)) {
            bundlingCaseData.toBuilder().hasRespondant2(true);
        }
        if (null != caseData.getApplicant2()) {
            bundlingCaseData.toBuilder().applicant2(caseData.getApplicant2());
        }
        if (null != caseData.getRespondent2()) {
            bundlingCaseData.toBuilder().respondent2(caseData.getRespondent2());
        }
        return bundlingCaseData;
    }

    private ServedDocument mapServedDocuments(ServedDocumentFiles servedDocumentFiles) {
        List<BundlingRequestDocument> bundlingServedDocFiles = new ArrayList<>();
        if (null == servedDocumentFiles || null == servedDocumentFiles.getParticularsOfClaimDocument()) {
            return null;
        }
        servedDocumentFiles.getParticularsOfClaimDocument().forEach(document -> {
            bundlingServedDocFiles.add(BundlingRequestDocument.builder()
                                               .documentFileName(document.getValue().getDocumentFileName())
                                               .documentLink(DocumentLink.builder()
                                                                 .documentUrl(document.getValue().getDocumentUrl())
                                                                 .documentBinaryUrl(document.getValue().getDocumentBinaryUrl())
                                                                 .documentFilename(document.getValue().getDocumentFileName()).build())
                                               .build());

        });
        List<Element<BundlingRequestDocument>> particulars = ElementUtils.wrapElements(bundlingServedDocFiles);
        return  ServedDocument.builder().particularsOfClaimDocument(particulars).build();
    }

    private List<Element<BundlingRequestDocument>> mapSystemGeneratedcaseDocument(List<Element<CaseDocument>> systemGeneratedCaseDocuments, Document orderSDODocumentDJ) {
        List<BundlingRequestDocument> bundlingSystemGeneratedCaseDocs = new ArrayList<>();
        if (!Optional.ofNullable(systemGeneratedCaseDocuments).isEmpty()) {
            systemGeneratedCaseDocuments.forEach(sysGeneratedCaseDocuments -> {
                CaseDocument uploadedDocuments = sysGeneratedCaseDocuments.getValue();
                Document document = uploadedDocuments.getDocumentLink();
                bundlingSystemGeneratedCaseDocs.add(BundlingRequestDocument.builder()
                                                        .documentFileName(uploadedDocuments.getDocumentName())
                                                        .documentLink(DocumentLink.builder()
                                                                          .documentUrl(document.getDocumentUrl())
                                                                          .documentBinaryUrl(document.getDocumentBinaryUrl())
                                                                          .documentFilename(document.getDocumentFileName()).build())
                                                        .documentType(uploadedDocuments.getDocumentType().name())
                                                        .build()
                );

            });
        }
        if (null != orderSDODocumentDJ) {
            bundlingSystemGeneratedCaseDocs.add(BundlingRequestDocument.builder()
                                                    .documentFileName(orderSDODocumentDJ.getDocumentFileName())
                                                    .documentLink(DocumentLink.builder()
                                                                      .documentUrl(orderSDODocumentDJ.getDocumentUrl())
                                                                      .documentBinaryUrl(orderSDODocumentDJ.getDocumentBinaryUrl())
                                                                      .documentFilename(orderSDODocumentDJ.getDocumentFileName()).build())
                                                    .documentType(DocumentType.SDO_ORDER.name()).build());
        }
        return ElementUtils.wrapElements(bundlingSystemGeneratedCaseDocs);
    }

    private List<Element<BundlingRequestDocument>> mapUploadEvidenceWitnessDoc(List<Element<UploadEvidenceWitness>> uploadEvidenceWitness, String displayName) {
        List<BundlingRequestDocument> bundlingWitnessDocs = new ArrayList<>();
        if (null == uploadEvidenceWitness || uploadEvidenceWitness.isEmpty()) {
            return ElementUtils.wrapElements(bundlingWitnessDocs);
        }
        uploadEvidenceWitness.forEach(witnessDocs -> {
            StringBuilder fileNameBuilder = new StringBuilder();
            fileNameBuilder.append(displayName);
            if (Optional.ofNullable(witnessDocs.getValue().getWitnessOptionName()).isPresent()) {
                fileNameBuilder.append("_" + witnessDocs.getValue().getWitnessOptionName());
            }
            if (Optional.ofNullable(witnessDocs.getValue().getWitnessOptionUploadDate()).isPresent()) {
                fileNameBuilder.append("_" + DateFormatHelper.formatLocalDate(
                    witnessDocs.getValue()
                        .getWitnessOptionUploadDate(),
                    "ddMMyyyy"
                ));
            }
            Document document = witnessDocs.getValue().getWitnessOptionDocument();
            bundlingWitnessDocs.add(BundlingRequestDocument.builder()
                                        .documentFileName(fileNameBuilder.toString())
                                        .documentLink(DocumentLink.builder()
                                                          .documentUrl(document.getDocumentUrl())
                                                          .documentBinaryUrl(document.getDocumentBinaryUrl())
                                                          .documentFilename(document.getDocumentFileName()).build())
                                        .build());

        });
        return ElementUtils.wrapElements(bundlingWitnessDocs);
    }

    private List<Element<BundlingRequestDocument>> mapUploadEvidenceExpertDoc(List<Element<UploadEvidenceExpert>> uploadEvidenceExpert, String displayName) {
        List<BundlingRequestDocument> bundlingExpertDocs = new ArrayList<>();
        if (null == uploadEvidenceExpert || uploadEvidenceExpert.isEmpty()) {
            return ElementUtils.wrapElements(bundlingExpertDocs);
        }

        uploadEvidenceExpert.forEach(expertDocs -> {
            StringBuilder fileNameBuilder = new StringBuilder();
            fileNameBuilder.append(displayName);
            if (Optional.ofNullable(expertDocs.getValue().getExpertOptionName()).isPresent()) {
                fileNameBuilder.append("_" + expertDocs.getValue().getExpertOptionName());
            }
            if (Optional.ofNullable(expertDocs.getValue().getExpertOptionUploadDate()).isPresent()) {
                fileNameBuilder.append("_" + DateFormatHelper.formatLocalDate(
                    expertDocs.getValue()
                        .getExpertOptionUploadDate(),
                    "ddMMyyyy"
                ));
            }
            Document document = expertDocs.getValue().getExpertDocument();
            bundlingExpertDocs.add(BundlingRequestDocument.builder()
                                       .documentFileName(fileNameBuilder.toString())
                                       .documentLink(DocumentLink.builder()
                                                         .documentUrl(document.getDocumentUrl())
                                                         .documentBinaryUrl(document.getDocumentBinaryUrl())
                                                         .documentFilename(document.getDocumentFileName()).build())
                                       .build());

        });

        return ElementUtils.wrapElements(bundlingExpertDocs);
    }

    private List<Element<BundlingRequestDocument>> mapUploadEvidenceOtherDoc(List<Element<UploadEvidenceDocumentType>> otherDocsEvidenceUpload) {
        List<BundlingRequestDocument> bundlingExpertDocs = new ArrayList<>();
        if (null == otherDocsEvidenceUpload || otherDocsEvidenceUpload.isEmpty()) {
            return ElementUtils.wrapElements(bundlingExpertDocs);
        }

        otherDocsEvidenceUpload.forEach(otherDocs -> {
            Document document = otherDocs.getValue().getDocumentUpload();
            bundlingExpertDocs.add(BundlingRequestDocument.builder()
                                       .documentFileName(document.getDocumentFileName())
                                       .documentLink(DocumentLink.builder()
                                                         .documentUrl(document.getDocumentUrl())
                                                         .documentBinaryUrl(document.getDocumentBinaryUrl())
                                                         .documentFilename(document.getDocumentFileName()).build())
                                       .build());

        });

        return ElementUtils.wrapElements(bundlingExpertDocs);
    }
}
