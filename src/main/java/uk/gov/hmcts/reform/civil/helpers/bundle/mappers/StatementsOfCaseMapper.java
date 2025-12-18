package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.bundle.BundleDocumentsRetrieval;
import uk.gov.hmcts.reform.civil.helpers.bundle.ConversionToBundleRequestDocs;
import uk.gov.hmcts.reform.civil.helpers.bundle.PartyType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DocumentWithRegex;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REPLIES_TO_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REPLY;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REQUEST_FOR_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP1_REQUEST_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_PARTICULARS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REPLIES_TO_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REPLY;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REQUEST_FOR_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.APP2_REQUEST_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.CLAIMANT1_DETAILS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.CLAIMANT2_DETAILS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF1_DEFENSE_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF1_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF2_DEFENSE_DQ;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEF2_SCHEDULE_OF_LOSS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.PARTICULARS_OF_CLAIM;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.buildBundlingRequestDoc;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.generateDocName;

@Service
@RequiredArgsConstructor
public class StatementsOfCaseMapper implements ManageDocMapper {

    private final BundleDocumentsRetrieval bundleDocumentsRetrieval;
    private final ConversionToBundleRequestDocs conversionToBundleRequestDocs;
    private final SystemGeneratedDocMapper systemGeneratedDocMapper;

    public List<Element<BundlingRequestDocument>> map(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        // Claim form
        bundlingRequestDocuments.addAll(systemGeneratedDocMapper.mapSystemGeneratedCaseDocument(
            caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(caseDocumentElement -> DocumentType.SEALED_CLAIM
                    .equals(caseDocumentElement.getValue().getDocumentType())
                    && null != caseDocumentElement.getValue().getDocumentLink().getCategoryID()
                    && "detailsOfClaim".equals(caseDocumentElement.getValue().getDocumentLink().getCategoryID()))
                .collect(Collectors.toCollection(ArrayList::new)),
            BundleFileNameList.CLAIM_FORM.getDisplayName()
        ));

        // Particulars of claim bundle
        bundlingRequestDocuments.addAll(mapParticularsOfClaimDocs(caseData));

        // Defendant defence and Claimant reply
        List<Element<CaseDocument>> clAndDfDocList = caseData.getDefendantResponseDocuments();
        clAndDfDocList.addAll(caseData.getClaimantResponseDocuments());
        caseData.getSystemGeneratedCaseDocuments().stream()
            .filter(caseDocumentElement ->
                        DocumentType.DEFENDANT_DEFENCE.equals(caseDocumentElement.getValue().getDocumentType())
                            || DocumentType.CLAIMANT_DEFENCE.equals(caseDocumentElement.getValue().getDocumentType())
            )
            .filter(caseDocumentElement -> !clAndDfDocList.contains(caseDocumentElement))
            .forEach(clAndDfDocList::add);
        List<Element<CaseDocument>> sortedDefendantDefenceAndClaimantReply =
            bundleDocumentsRetrieval.getSortedDefendantDefenceAndClaimantReply(clAndDfDocList);
        sortedDefendantDefenceAndClaimantReply.forEach(caseDocumentElement -> {
            String docType = DocumentType.DEFENDANT_DEFENCE.equals(caseDocumentElement.getValue().getDocumentType())
                ? BundleFileNameList.DEFENCE.getDisplayName() : BundleFileNameList.CL_REPLY.getDisplayName();
            String party;
            if ("Defendant".equalsIgnoreCase(caseDocumentElement.getValue().getCreatedBy())) {
                party = PartyType.DEFENDANT1.getDisplayName();
            } else if ("Defendant 2".equalsIgnoreCase(caseDocumentElement.getValue().getCreatedBy())) {
                party = PartyType.DEFENDANT2.getDisplayName();
            } else {
                party = "";
            }
            String docName = generateDocName(docType, party, null,
                caseDocumentElement.getValue().getCreatedDatetime().toLocalDate());
            bundlingRequestDocuments.add(buildBundlingRequestDoc(
                docName,
                caseDocumentElement.getValue().getDocumentLink(),
                docType
            ));
        });

        // Part 18 requests and Schedule of loss extracted from documentary evidence
        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
            bundlingRequestDocuments.addAll(
                conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                    bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
                        getEvidenceUploadDocsByPartyAndDocType(
                            partyType,
                            EvidenceUploadType.DOCUMENTARY,
                            caseData
                        ),
                        TypeOfDocDocumentaryEvidenceOfTrial.PART18.getDisplayNames(),
                        false
                    ),
                    BundleFileNameList.REPLY_TO_PART_18.getDisplayName(),
                    TypeOfDocDocumentaryEvidenceOfTrial.PART18.name(),
                    partyType
                ))
        );

        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
            bundlingRequestDocuments.addAll(
                conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                    bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
                        getEvidenceUploadDocsByPartyAndDocType(
                            partyType,
                            EvidenceUploadType.DOCUMENTARY,
                            caseData
                        ),
                        TypeOfDocDocumentaryEvidenceOfTrial.SCHEDULE_OF_LOSS.getDisplayNames(),
                        false
                    ),
                    BundleFileNameList.SCHEDULE_OF_LOSS_FILE_DISPLAY_NAME.getDisplayName(),
                    TypeOfDocDocumentaryEvidenceOfTrial.SCHEDULE_OF_LOSS.name(),
                    partyType
                ))
        );

        List<Element<ManageDocument>> manageDocuments = caseData.getManageDocumentsList();
        if (!manageDocuments.isEmpty()) {
            Arrays.stream(PartyType.values()).toList().forEach(partyType ->
                addManageDocuments(manageDocuments, partyType, bundlingRequestDocuments)
            );
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<BundlingRequestDocument> mapParticularsOfClaimDocs(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (Objects.nonNull(caseData.getServedDocumentFiles())) {
            List<Element<Document>> particularsOfClaimDocument = caseData.getServedDocumentFiles().getParticularsOfClaimDocument();
            if (Objects.nonNull((particularsOfClaimDocument))) {
                particularsOfClaimDocument.forEach(poc -> bundlingRequestDocuments.add(
                    buildBundlingRequestDoc(
                        bundleDocumentsRetrieval.getParticularsOfClaimName(caseData, BundleFileNameList.PARTICULARS_OF_CLAIM),
                        poc.getValue(), ""
                    )));
            }
            List<Element<DocumentWithRegex>> medicalReport = caseData.getServedDocumentFiles().getMedicalReport();
            addParticularsOfClaimDocuments(
                medicalReport,
                bundlingRequestDocuments,
                caseData,
                BundleFileNameList.MEDICAL_REPORT
            );

            List<Element<DocumentWithRegex>> scheduleOfLoss = caseData.getServedDocumentFiles().getScheduleOfLoss();
            addParticularsOfClaimDocuments(
                scheduleOfLoss,
                bundlingRequestDocuments,
                caseData,
                BundleFileNameList.SCHEDULE_OF_LOSS
            );

            List<Element<DocumentWithRegex>> cos = caseData.getServedDocumentFiles().getCertificateOfSuitability();
            addParticularsOfClaimDocuments(
                cos,
                bundlingRequestDocuments,
                caseData,
                BundleFileNameList.CERTIFICATE_OF_SUITABILITY
            );

            List<Element<DocumentWithRegex>> other = caseData.getServedDocumentFiles().getOther();
            addParticularsOfClaimDocuments(other, bundlingRequestDocuments, caseData, BundleFileNameList.OTHER);
        }
        return bundlingRequestDocuments;
    }

    private void addManageDocuments(List<Element<ManageDocument>> manageDocuments,
                                    PartyType partyType,
                                    List<BundlingRequestDocument> bundlingRequestDocuments) {
        List<DocCategory> documentCategories = switch (partyType) {
            case CLAIMANT1 -> List.of(
                APP1_DQ,
                APP1_REPLIES_TO_FURTHER_INFORMATION,
                APP1_REQUEST_FOR_FURTHER_INFORMATION,
                APP1_REQUEST_SCHEDULE_OF_LOSS,
                APP1_REPLY,
                CLAIMANT1_DETAILS_OF_CLAIM,
                PARTICULARS_OF_CLAIM
            );
            case CLAIMANT2 -> List.of(
                APP2_DQ,
                APP2_REPLIES_TO_FURTHER_INFORMATION,
                APP2_REQUEST_FOR_FURTHER_INFORMATION,
                APP2_REQUEST_SCHEDULE_OF_LOSS,
                APP2_REPLY,
                CLAIMANT2_DETAILS_OF_CLAIM,
                APP2_PARTICULARS_OF_CLAIM
            );
            case DEFENDANT1 -> List.of(
                DEF1_DEFENSE_DQ,
                DEF1_SCHEDULE_OF_LOSS
            );
            case DEFENDANT2 -> List.of(
                DEF2_DEFENSE_DQ,
                DEF2_SCHEDULE_OF_LOSS
            );
        };

        documentCategories.forEach(category ->
                                       manageDocuments.forEach(md -> addDocumentByCategoryId(
                                           md,
                                           bundlingRequestDocuments,
                                           category
                                       )));

    }

    private void addParticularsOfClaimDocuments(List<Element<DocumentWithRegex>> document,
                                                List<BundlingRequestDocument> bundlingRequestDocuments,
                                                CaseData caseData,
                                                BundleFileNameList bundleFileNameList) {
        if (Objects.nonNull(document)) {
            document.forEach(mr -> bundlingRequestDocuments.add(
                buildBundlingRequestDoc(
                    bundleDocumentsRetrieval.getParticularsOfClaimName(caseData, bundleFileNameList),
                    mr.getValue().getDocument(), ""
                )));
        }
    }
}
