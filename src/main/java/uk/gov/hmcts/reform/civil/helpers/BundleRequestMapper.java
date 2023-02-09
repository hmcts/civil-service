package uk.gov.hmcts.reform.civil.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
            "ddMMyyyy") + "_";
        BundleCreateRequest bundleCreateRequest = BundleCreateRequest.builder()
            .caseDetails(BundlingCaseDetails.builder()
                             .caseData(mapCaseData(caseData,
                                                   bundleConfigFileName, id))
                             .filenamePrefix(fileNameIdentifier)

                             .build()
            )
            .caseTypeId(caseTypeId)
            .jurisdictionId(jurisdiction).build();
        try {
            log.info("bundle request : " + new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(bundleCreateRequest));
        } catch (Exception e) {
            log.error("error in bundle request object", e);
        }
        return bundleCreateRequest;
    }

    private BundlingCaseData mapCaseData(CaseData caseData, String bundleConfigFileName, Long id) {
        BundlingCaseData bundlingCaseData =
            BundlingCaseData.builder().id(caseData.getCcdCaseReference()).bundleConfiguration(
                bundleConfigFileName)
                .systemGeneratedCaseDocuments(mapSystemGeneratedcaseDocument(caseData.getSystemGeneratedCaseDocuments()))
                .servedDocumentFiles(mapServedDocuments(caseData.getServedDocumentFiles()))
                .documentWitnessStatement(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessStatement()))
                .documentWitnessStatementRes(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessStatementRes()))
                .documentWitnessStatementRes2(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessStatementRes2()))
                .documentWitnessSummary(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessSummary()))
                .documentWitnessSummaryRes(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessSummaryRes()))
                .documentWitnessSummaryRes2(mapUploadEvidenceWitnessDoc(caseData.getDocumentWitnessSummaryRes2()))
                .documentHearsayNotice(mapUploadEvidenceWitnessDoc(caseData.getDocumentHearsayNotice()))
                .documentHearsayNoticeRes(mapUploadEvidenceWitnessDoc(caseData.getDocumentHearsayNoticeRes()))
                .documentHearsayNoticeRes2(mapUploadEvidenceWitnessDoc(caseData.getDocumentHearsayNoticeRes2()))
                .documentReferredInStatement(mapUploadEvidenceOtherDoc(caseData.getDocumentReferredInStatement()))
                .documentReferredInStatementRes(mapUploadEvidenceOtherDoc(caseData.getDocumentReferredInStatementRes()))
                .documentReferredInStatementRes2(mapUploadEvidenceOtherDoc(caseData.getDocumentReferredInStatementRes2()))
                .documentExpertReport(mapUploadEvidenceExpertDoc(caseData.getDocumentExpertReport()))
                .documentExpertReportRes(mapUploadEvidenceExpertDoc(caseData.getDocumentExpertReportRes()))
                .documentExpertReportRes2(mapUploadEvidenceExpertDoc(caseData.getDocumentExpertReportRes2()))
                .documentJointStatement(mapUploadEvidenceExpertDoc(caseData.getDocumentJointStatement()))
                .documentJointStatementRes(mapUploadEvidenceExpertDoc(caseData.getDocumentJointStatementRes()))
                .documentJointStatementRes2(mapUploadEvidenceExpertDoc(caseData.getDocumentJointStatementRes2()))
                .documentQuestions(mapUploadEvidenceExpertDoc(caseData.getDocumentQuestions()))
                .documentQuestionsRes(mapUploadEvidenceExpertDoc(caseData.getDocumentQuestionsRes()))
                .documentQuestionsRes2(mapUploadEvidenceExpertDoc(caseData.getDocumentQuestionsRes2()))
                .documentAnswers(mapUploadEvidenceExpertDoc(caseData.getDocumentAnswers()))
                .documentAnswersRes(mapUploadEvidenceExpertDoc(caseData.getDocumentAnswersRes()))
                .documentAnswersRes2(mapUploadEvidenceExpertDoc(caseData.getDocumentAnswersRes2()))
                .documentDisclosureList(mapUploadEvidenceOtherDoc(caseData.getDocumentDisclosureList()))
                .documentForDisclosure(mapUploadEvidenceOtherDoc(caseData.getDocumentForDisclosure()))
                .documentForDisclosureRes(mapUploadEvidenceOtherDoc(caseData.getDocumentForDisclosureRes()))
                .documentDisclosureListRes(mapUploadEvidenceOtherDoc(caseData.getDocumentDisclosureListRes()))
                .documentForDisclosureRes2(mapUploadEvidenceOtherDoc(caseData.getDocumentForDisclosureRes2()))
                .documentDisclosureListRes2(mapUploadEvidenceOtherDoc(caseData.getDocumentDisclosureListRes2()))
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
                .defendantResponseDocuments(mapSystemGeneratedcaseDocument(caseData.getDefendantResponseDocuments()))
                .applicant1(caseData.getApplicant1())
                .respondent1(caseData.getRespondent1())
                .courtLocation(caseData.getHearingLocation().getValue().getLabel())
                .hearingDate(caseData.getHearingDate())
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
        if (!Optional.ofNullable(servedDocumentFiles).isEmpty()) {
            servedDocumentFiles.getParticularsOfClaimDocument().forEach(document -> {
                bundlingServedDocFiles.add(BundlingRequestDocument.builder()
                                               .documentFileName(document.getValue().getDocumentFileName())
                                               .documentLink(DocumentLink.builder()
                                                                 .documentUrl(document.getValue().getDocumentUrl())
                                                                 .documentBinaryUrl(document.getValue().getDocumentBinaryUrl())
                                                                 .documentFilename(document.getValue().getDocumentFileName()).build())
                                               .build());

            });
        }
        List<Element<BundlingRequestDocument>> particulars = ElementUtils.wrapElements(bundlingServedDocFiles);
        return  ServedDocument.builder().particularsOfClaimDocument(particulars).build();
    }

    private List<Element<BundlingRequestDocument>> mapSystemGeneratedcaseDocument(List<Element<CaseDocument>> systemGeneratedCaseDocuments) {
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
        return ElementUtils.wrapElements(bundlingSystemGeneratedCaseDocs);
    }

    private List<Element<BundlingRequestDocument>> mapUploadEvidenceWitnessDoc(List<Element<UploadEvidenceWitness>> uploadEvidenceWitness) {
        List<BundlingRequestDocument> bundlingWitnessDocs = new ArrayList<>();
        if (!Optional.ofNullable(uploadEvidenceWitness).isEmpty()) {
            uploadEvidenceWitness.forEach(witnessDocs -> {
                Document document = witnessDocs.getValue().getWitnessOptionDocument();
                bundlingWitnessDocs.add(BundlingRequestDocument.builder()
                                            .documentFileName(document.getDocumentFileName())
                                            .documentLink(DocumentLink.builder()
                                                              .documentUrl(document.getDocumentUrl())
                                                              .documentBinaryUrl(document.getDocumentBinaryUrl())
                                                              .documentFilename(document.getDocumentFileName()).build())
                    .build());

            });
        }
        return ElementUtils.wrapElements(bundlingWitnessDocs);
    }

    private List<Element<BundlingRequestDocument>> mapUploadEvidenceExpertDoc(List<Element<UploadEvidenceExpert>> uploadEvidenceExpert) {
        List<BundlingRequestDocument> bundlingExpertDocs = new ArrayList<>();
        if (!Optional.ofNullable(uploadEvidenceExpert).isEmpty()) {
            uploadEvidenceExpert.forEach(expertDocs -> {
                Document document = expertDocs.getValue().getExpertDocument();
                bundlingExpertDocs.add(BundlingRequestDocument.builder()
                                           .documentFileName(document.getDocumentFileName())
                                           .documentLink(DocumentLink.builder()
                                                             .documentUrl(document.getDocumentUrl())
                                                             .documentBinaryUrl(document.getDocumentBinaryUrl())
                                                             .documentFilename(document.getDocumentFileName()).build())
                                           .build());

            });
        }
        return ElementUtils.wrapElements(bundlingExpertDocs);
    }

    private List<Element<BundlingRequestDocument>> mapUploadEvidenceOtherDoc(List<Element<UploadEvidenceDocumentType>> otherDocsEvidenceUpload) {
        List<BundlingRequestDocument> bundlingExpertDocs = new ArrayList<>();
        if (!Optional.ofNullable(otherDocsEvidenceUpload).isEmpty()) {
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
        }
        return ElementUtils.wrapElements(bundlingExpertDocs);
    }
}
