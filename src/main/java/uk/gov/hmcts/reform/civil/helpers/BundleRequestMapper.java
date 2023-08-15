package uk.gov.hmcts.reform.civil.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
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
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleRequestMapper {

    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData,
                                                                String bundleConfigFileName, String jurisdiction,
                                                                String caseTypeId, Long id) {
        String fileNameIdentifier =
            caseData.getCcdCaseReference() + "_" + DateFormatHelper.formatLocalDate(
                caseData.getHearingDate(),
                "ddMMyyyy"
            );
        BundleCreateRequest bundleCreateRequest = BundleCreateRequest.builder()
            .caseDetails(BundlingCaseDetails.builder()
                             .caseData(mapCaseData(
                                 caseData,
                                 bundleConfigFileName
                             ))
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
                .claimant1TrialDocuments(mapTrialDocuments(caseData, PartType.CLAIMANT1))
                .defendant1TrialDocuments(mapTrialDocuments(caseData, PartType.DEFENDANT1))
                .defendant2TrialDocuments(mapTrialDocuments(caseData, PartType.DEFENDANT2))
                .statementsOfCaseDocuments(mapStatmentOfcaseDocs(caseData))
                .ordersDocuments(mapOrdersDocument(caseData))
                .claimant1WitnessStatements(mapWitnessStatements(caseData, PartType.CLAIMANT1))
                .claimant2WitnessStatements(mapWitnessStatements(caseData, PartType.CLAIMANT2))
                .defendant1WitnessStatements(mapWitnessStatements(caseData, PartType.DEFENDANT1))
                .defendant2WitnessStatements(mapWitnessStatements(caseData, PartType.DEFENDANT2))
                .applicant1(caseData.getApplicant1())
                .respondent1(caseData.getRespondent1())
                .courtLocation(caseData.getHearingLocation().getValue().getLabel())
                .hearingDate(null != caseData.getHearingDate()
                                 ? DateFormatHelper.formatLocalDate(caseData.getHearingDate(), "dd-MM-yyyy") : null)
                .ccdCaseReference(caseData.getCcdCaseReference())
                .build();
        bundlingCaseData = mapRespondent2Applicant2Details(bundlingCaseData, caseData);
        return bundlingCaseData;
    }

    private List<Element<BundlingRequestDocument>> mapWitnessStatements(CaseData caseData, PartType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        switch (partyType) {
            case CLAIMANT1 -> {
                Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
                    groupWitnessStatementsByName(caseData.getDocumentWitnessStatement());
                List<Element<UploadEvidenceWitness>> witnessStatementSelf = getSelfStatement(witnessStatmentsMap,
                                                                                              caseData.getApplicant1());
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(witnessStatementSelf,
                                                                                         BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_STATEMENT.name(), PartType.CLAIMANT1));
                bundlingRequestDocuments.addAll(covertOtherWitnessEvidenceToBundleRequestDocs(witnessStatmentsMap,
                                                                                              BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
                                                                                              EvidenceUploadFiles.WITNESS_STATEMENT.name(),
                                                                                              PartType.CLAIMANT1, caseData.getApplicant1()));
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(caseData.getDocumentWitnessSummary(),
                                                                                         BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_SUMMARY.name(), PartType.CLAIMANT1));
            }
            case CLAIMANT2 -> {
                Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
                    groupWitnessStatementsByName(caseData.getDocumentWitnessStatement());
                List<Element<UploadEvidenceWitness>> witnessStatementSelf = getSelfStatement(witnessStatmentsMap,
                                                                                             caseData.getApplicant2());
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(witnessStatementSelf,
                                                                                         BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_STATEMENT.name(), PartType.CLAIMANT2));
                bundlingRequestDocuments.addAll(covertOtherWitnessEvidenceToBundleRequestDocs(witnessStatmentsMap,
                                                                                              BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
                                                                                              EvidenceUploadFiles.WITNESS_STATEMENT.name(),
                                                                                              PartType.CLAIMANT2, caseData.getApplicant2()));
            }
            case DEFENDANT1 -> {
                Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
                    groupWitnessStatementsByName(caseData.getDocumentWitnessStatementRes());
                List<Element<UploadEvidenceWitness>> witnessStatementSelf = getSelfStatement(witnessStatmentsMap,
                                                                                             caseData.getRespondent1());
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(witnessStatementSelf,
                                                                                         BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_STATEMENT.name(), PartType.DEFENDANT1));
                bundlingRequestDocuments.addAll(covertOtherWitnessEvidenceToBundleRequestDocs(witnessStatmentsMap,
                                                                                              BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
                                                                                              EvidenceUploadFiles.WITNESS_STATEMENT.name(),
                                                                                              PartType.DEFENDANT1, caseData.getRespondent1()));
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(caseData.getDocumentWitnessSummaryRes(),
                                                                                         BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_SUMMARY.name(), PartType.DEFENDANT1));
            }
            case DEFENDANT2 -> {
                Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
                    groupWitnessStatementsByName(caseData.getDocumentWitnessStatementRes2());

                List<Element<UploadEvidenceWitness>> witnessStatementSelf = getSelfStatement(witnessStatmentsMap,
                                                                                             caseData.getRespondent2());
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(witnessStatementSelf,
                                                                                         BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_STATEMENT.name(), PartType.DEFENDANT2));
                bundlingRequestDocuments.addAll(covertOtherWitnessEvidenceToBundleRequestDocs(witnessStatmentsMap,
                                                                                              BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
                                                                                              EvidenceUploadFiles.WITNESS_STATEMENT.name(),
                                                                                              PartType.DEFENDANT2, caseData.getRespondent2()));
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(caseData.getDocumentWitnessSummaryRes2(),
                                                                                         BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_SUMMARY.name(), PartType.DEFENDANT2));
            }
            default -> {
                break;
            }
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<BundlingRequestDocument> covertOtherWitnessEvidenceToBundleRequestDocs(
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap, String displayName, String documentType,
        PartType partType, Party party) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (party != null) {
            if (party.getPartyName() != null) {
                witnessStatmentsMap.remove(party.getPartyName().trim().toLowerCase());
            }
            if (party.isIndividual() && party.getIndividualFirstName() != null) {
                witnessStatmentsMap.remove(party.getIndividualFirstName().trim().toLowerCase());
            }
        }
        witnessStatmentsMap.forEach((witnessName, witnessEvidence) -> {
            witnessEvidence.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate(),
                Comparator.reverseOrder()
            ));
            witnessEvidence.forEach(uploadEvidenceWitnessElement -> {
                String docName = generateWitnessDocName(displayName, uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                                                        witnessEvidence.indexOf(uploadEvidenceWitnessElement) + 1,
                                                 uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate());
                bundlingRequestDocuments.add(BundlingRequestDocument.builder()
                                                 .documentFileName(docName)
                                                 .documentType(documentType)
                                                 .documentLink(DocumentLink.builder().documentUrl(uploadEvidenceWitnessElement
                                                                                                      .getValue().getWitnessOptionDocument().getDocumentUrl())
                                                                   .documentBinaryUrl(uploadEvidenceWitnessElement.getValue().getWitnessOptionDocument().getDocumentBinaryUrl())
                                                                   .documentFilename(uploadEvidenceWitnessElement.getValue()
                                                                                         .getWitnessOptionDocument().getDocumentFileName()).build()).build());
            });
        });
        return bundlingRequestDocuments;
    }

    private String generateWitnessDocName(String displayName, String witnessOptionName, int index,
                                          LocalDate witnessOptionUploadDate) {
        StringBuilder displayFileName = new StringBuilder();
        displayFileName.append(witnessOptionName);
        displayFileName.append(" " + index + " ");
        displayFileName.append(witnessOptionUploadDate);
        return String.format(displayName, displayFileName);
    }

    private List<Element<UploadEvidenceWitness>> getSelfStatement(Map<String,
        List<Element<UploadEvidenceWitness>>> witnessStatmentsMap, Party party) {
        List<Element<UploadEvidenceWitness>> selfStatementList = new ArrayList<>();
        if (party != null && party.getPartyName() != null && witnessStatmentsMap.get(party.getPartyName().trim().toLowerCase()) != null) {
            selfStatementList.addAll(
                witnessStatmentsMap.get(party.getPartyName().trim().toLowerCase()));
        }
        if (party != null && party.isIndividual() && party.getIndividualFirstName() != null && witnessStatmentsMap.get(party.getIndividualFirstName()
                                                                                                                           .trim().toLowerCase()) != null) {
            selfStatementList.addAll(witnessStatmentsMap.get(party.getIndividualFirstName()
                                                                .trim().toLowerCase()));
        }
        return selfStatementList;
    }

    private Map<String, List<Element<UploadEvidenceWitness>>> groupWitnessStatementsByName(
        List<Element<UploadEvidenceWitness>> witnessStatement) {
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatementMap = new HashMap<String,
            List<Element<UploadEvidenceWitness>>>();
        if (witnessStatement != null) {
            witnessStatementMap = witnessStatement.stream().collect(Collectors
                                                      .groupingBy(uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement
                                                          .getValue().getWitnessOptionName().trim().toLowerCase()));
        }
        return witnessStatementMap;
    }

    private List<Element<BundlingRequestDocument>> mapOrdersDocument(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments(),
                                                                       DocumentType.DEFAULT_JUDGMENT_SDO_ORDER,
                                                                       BundleFileNameList.DIRECTIONS_ORDER.getDisplayName()));
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments(),
                                                                       DocumentType.SDO_ORDER,
                                                                       BundleFileNameList.DIRECTIONS_ORDER.getDisplayName()));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapStatmentOfcaseDocs(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments(),
                                                                       DocumentType.SEALED_CLAIM,
                                                                       BundleFileNameList.CLAIM_FORM.getDisplayName()));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapTrialDocuments(CaseData caseData, PartType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        switch (partyType) {
            case CLAIMANT1 -> {

                bundlingRequestDocuments.addAll(getAllTrialDocsByPartyType(caseData.getDocumentWitnessSummary() != null ? caseData.getDocumentWitnessSummary() : null,
                                                                           caseData.getDocumentEvidenceForTrial() != null ? caseData.getDocumentEvidenceForTrial() : null,
                                                                           PartType.CLAIMANT1));
                break;
            }
            case DEFENDANT1 -> {
                bundlingRequestDocuments.addAll(getAllTrialDocsByPartyType(caseData.getDocumentWitnessSummaryRes() != null ? caseData.getDocumentWitnessSummaryRes() : null,
                                                                           caseData.getDocumentEvidenceForTrialRes() != null ? caseData.getDocumentEvidenceForTrialRes() : null,
                                                                           PartType.DEFENDANT1));
                break;
            }
            case DEFENDANT2 -> {
                bundlingRequestDocuments.addAll(getAllTrialDocsByPartyType(caseData.getDocumentWitnessSummaryRes2() != null ? caseData.getDocumentWitnessSummaryRes2() : null,
                                                                           caseData.getDocumentEvidenceForTrialRes2() != null ? caseData.getDocumentEvidenceForTrialRes2() : null,
                                                                           PartType.DEFENDANT2));
                break;
            }
            default -> {
                break;
            }
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<BundlingRequestDocument> getAllTrialDocsByPartyType(List<Element<UploadEvidenceWitness>> documentWitnessSummary,
                                                                     List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial, PartType party) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(documentWitnessSummary, BundleFileNameList.CASE_SUMMARY_FILE_DISPLAY_NAME.getDisplayName(),
                                                                                 EvidenceUploadFiles.WITNESS_SUMMARY.name(), party));
        if (documentEvidenceForTrial != null) {
            List<Element<UploadEvidenceDocumentType>> chronologyDocs =
                filterDocumentaryEvidenceForTrialDocs(
                    documentEvidenceForTrial,
                    TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.getDisplayNames()
                );
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                chronologyDocs,
                BundleFileNameList.CHRONOLOGY_FILE_DISPLAY_NAME.getDisplayName(),
                TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.name(),
                party
            ));
            List<Element<UploadEvidenceDocumentType>> trialTimeTableDocs =
                filterDocumentaryEvidenceForTrialDocs(
                    documentEvidenceForTrial,
                    TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.getDisplayNames()
                );
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                trialTimeTableDocs,
                BundleFileNameList.TRIAL_TIMETABLE_FILE_DISPLAY_NAME.getDisplayName(),
                TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.name(),
                party
            ));
        }
        return bundlingRequestDocuments;
    }

    private List<Element<UploadEvidenceDocumentType>> filterDocumentaryEvidenceForTrialDocs(
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial, List<String> displayNames) {
        documentEvidenceForTrial.sort(Comparator.comparing(
            uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getDocumentIssuedDate(),
            Comparator.reverseOrder()
        ));
        return documentEvidenceForTrial.stream().filter(uploadEvidenceDocumentTypeElement -> matchType(
            uploadEvidenceDocumentTypeElement.getValue().getTypeOfDocument(),
            displayNames
        )).collect(Collectors.toList());
    }

    private boolean matchType(String typeOfDocument, List<String> displayNames) {
        return displayNames.stream().anyMatch(s -> s.equalsIgnoreCase(typeOfDocument.trim()));
    }

    private List<BundlingRequestDocument> covertWitnessEvidenceToBundleRequestDocs(List<Element<UploadEvidenceWitness>> witnessEvidence,
                                                                                   String fileNamePrefix,
                                                                                   String documentType, PartType party) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (witnessEvidence != null) {
            if (documentType.equals(EvidenceUploadFiles.WITNESS_STATEMENT)) {
                witnessEvidence.sort(Comparator.comparing(
                    uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate(),
                    Comparator.reverseOrder()
                ));
            } else {
                witnessEvidence.sort(Comparator.comparing(
                    uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getCreatedDatetime().toLocalDate(),
                    Comparator.reverseOrder()
                ));
            }
            witnessEvidence.forEach(uploadEvidenceWitnessElement -> {
                String docName = generateDocName(fileNamePrefix, uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                                                 documentType.equals(EvidenceUploadFiles.WITNESS_STATEMENT)
                                                     ?
                                                     uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate() : uploadEvidenceWitnessElement
                                                     .getValue().getCreatedDatetime().toLocalDate()
                );
                bundlingRequestDocuments.add(BundlingRequestDocument.builder()
                                                 .documentFileName(docName)
                                                 .documentType(documentType)
                                                 .documentLink(DocumentLink.builder()
                                                                   .documentUrl(uploadEvidenceWitnessElement.getValue().getWitnessOptionDocument().getDocumentUrl())
                                                                   .documentBinaryUrl(uploadEvidenceWitnessElement.getValue().getWitnessOptionDocument().getDocumentBinaryUrl())
                                                                   .documentFilename(uploadEvidenceWitnessElement.getValue()
                                                                                         .getWitnessOptionDocument().getDocumentFileName()).build())
                                                 .build());
            });
        }
        return bundlingRequestDocuments;
    }

    private String generateDocName(String fileNamePrefix, String displayName, LocalDate witnessOptionUploadDate) {
        return String.format(fileNamePrefix, displayName, witnessOptionUploadDate);

    }

    private List<BundlingRequestDocument> covertEvidenceUploadTypeToBundleRequestDocs(List<Element<UploadEvidenceDocumentType>> evidenceUploadDocList,
                                                                                      String fileNamePrefix, String documentType,
                                                                                      PartType party) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        if (evidenceUploadDocList != null) {
            evidenceUploadDocList.sort(Comparator.comparing(
                uploadEvidenceDocumentTypeElement -> uploadEvidenceDocumentTypeElement.getValue().getDocumentIssuedDate(),
                Comparator.reverseOrder()
            ));
            evidenceUploadDocList.forEach(uploadEvidenceDocumentTypeElement -> {
                String docName = generateDocName(fileNamePrefix, party.getDisplayName(),
                                                 uploadEvidenceDocumentTypeElement.getValue().getDocumentIssuedDate()
                );
                bundlingRequestDocuments.add(BundlingRequestDocument.builder()
                                                 .documentFileName(docName)
                                                 .documentType(documentType)
                                                 .documentLink(DocumentLink.builder()
                                                                   .documentUrl(uploadEvidenceDocumentTypeElement.getValue().getDocumentUpload().getDocumentUrl())
                                                                   .documentBinaryUrl(uploadEvidenceDocumentTypeElement.getValue().getDocumentUpload().getDocumentBinaryUrl())
                                                                   .documentFilename(uploadEvidenceDocumentTypeElement.getValue()
                                                                                         .getDocumentUpload().getDocumentFileName()).build())
                                                 .build());
            });
        }
        return bundlingRequestDocuments;
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
        if (Optional.ofNullable(servedDocumentFiles).isEmpty() || null == servedDocumentFiles.getParticularsOfClaimDocument()) {
            return ServedDocument.builder().particularsOfClaimDocument(ElementUtils.wrapElements(bundlingServedDocFiles)).build();
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
        return ServedDocument.builder().particularsOfClaimDocument(particulars).build();
    }

    private List<BundlingRequestDocument> mapSystemGeneratedCaseDocument(List<Element<CaseDocument>> systemGeneratedCaseDocuments, DocumentType documentType, String displayName) {
        List<BundlingRequestDocument> bundlingSystemGeneratedCaseDocs = new ArrayList<>();
        systemGeneratedCaseDocuments.stream().filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType().name()
                .equals(documentType.name())).forEach(caseDocumentElement -> {
                    String docName = String.format(displayName, caseDocumentElement.getValue().getCreatedDatetime());
                    bundlingSystemGeneratedCaseDocs.add(BundlingRequestDocument.builder()
                                               .documentFileName(docName)
                                               .documentLink(DocumentLink.builder()
                                                                 .documentUrl(caseDocumentElement.getValue().getDocumentLink().getDocumentUrl())
                                                                 .documentBinaryUrl(caseDocumentElement.getValue().getDocumentLink().getDocumentBinaryUrl())
                                                                 .documentFilename(caseDocumentElement.getValue().getDocumentLink().getDocumentFileName()).build())
                                               .build());
                });
        return bundlingSystemGeneratedCaseDocs;
    }

    private List<Element<BundlingRequestDocument>> mapUploadEvidenceWitnessDoc(List<Element<UploadEvidenceWitness>> uploadEvidenceWitness, String displayName) {
        List<BundlingRequestDocument> bundlingWitnessDocs = new ArrayList<>();
        if (null == uploadEvidenceWitness) {
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
        if (null == uploadEvidenceExpert) {
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
        if (null == otherDocsEvidenceUpload) {
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

    private enum PartType {
        CLAIMANT1("CL 1"),
        CLAIMANT2("CL 2"),
        DEFENDANT1("DF 1"),
        DEFENDANT2("DF 2");
        String displayName;

        public String getDisplayName() {
            return displayName;
        }

        PartType(String displayName) {
            this.displayName = displayName;
        }
    }
}
