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

import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.buildBundlingRequestDoc;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.generateDocName;

@Service
@RequiredArgsConstructor
public class StatementsOfCaseMapper {

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
            if (Objects.nonNull(medicalReport)) {
                medicalReport.forEach(mr -> bundlingRequestDocuments.add(
                    buildBundlingRequestDoc(
                        bundleDocumentsRetrieval.getParticularsOfClaimName(caseData, BundleFileNameList.MEDICAL_REPORT),
                        mr.getValue().getDocument(), ""
                    )));
            }

            List<Element<DocumentWithRegex>> scheduleOfLoss = caseData.getServedDocumentFiles().getScheduleOfLoss();
            if (Objects.nonNull(scheduleOfLoss)) {
                scheduleOfLoss.forEach(poc -> bundlingRequestDocuments.add(
                    buildBundlingRequestDoc(
                        bundleDocumentsRetrieval.getParticularsOfClaimName(caseData, BundleFileNameList.SCHEDULE_OF_LOSS),
                        poc.getValue().getDocument(), ""
                    )));
            }

            List<Element<DocumentWithRegex>> cos = caseData.getServedDocumentFiles().getCertificateOfSuitability();
            if (Objects.nonNull(cos)) {
                cos.forEach(poc -> bundlingRequestDocuments.add(
                    buildBundlingRequestDoc(
                        bundleDocumentsRetrieval.getParticularsOfClaimName(caseData, BundleFileNameList.CERTIFICATE_OF_SUITABILITY),
                        poc.getValue().getDocument(), ""
                    )));
            }

            List<Element<DocumentWithRegex>> other = caseData.getServedDocumentFiles().getOther();
            if (Objects.nonNull(other)) {
                other.forEach(poc -> bundlingRequestDocuments.add(
                    buildBundlingRequestDoc(
                        bundleDocumentsRetrieval.getParticularsOfClaimName(caseData, BundleFileNameList.OTHER),
                        poc.getValue().getDocument(), ""
                    )));
            }

            List<Element<ManageDocument>> manageDocuments = caseData.getManageDocumentsList();
            if (!manageDocuments.isEmpty()) {
                manageDocuments.forEach(md -> {
                    if (DocCategory.PARTICULARS_OF_CLAIM.getValue()
                        .equals(md.getValue().getDocumentLink().getCategoryID())) {
                        bundlingRequestDocuments.add(
                            buildBundlingRequestDoc(
                                bundleDocumentsRetrieval.getParticularsOfClaimName(
                                    caseData,
                                    BundleFileNameList.PARTICULARS_OF_CLAIM
                                ),
                                md.getValue().getDocumentLink(),
                                md.getValue().getDocumentType().name()
                            )
                        );
                    }
                });
            }
        }
        return bundlingRequestDocuments;
    }
}
