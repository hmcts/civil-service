package uk.gov.hmcts.reform.civil.helpers.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
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

import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getWitnessDocsByPartyAndDocType;

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
                .claimant1TrialDocuments(mapTrialDocuments(caseData, PartyType.CLAIMANT1))
                .defendant1TrialDocuments(mapTrialDocuments(caseData, PartyType.DEFENDANT1))
                .defendant2TrialDocuments(mapTrialDocuments(caseData, PartyType.DEFENDANT2))
                .statementsOfCaseDocuments(mapStatmentOfcaseDocs(caseData))
                .ordersDocuments(mapOrdersDocument(caseData))
                .claimant1WitnessStatements(mapWitnessStatements(caseData, PartyType.CLAIMANT1))
                .claimant2WitnessStatements(mapWitnessStatements(caseData, PartyType.CLAIMANT2))
                .defendant1WitnessStatements(mapWitnessStatements(caseData, PartyType.DEFENDANT1))
                .defendant2WitnessStatements(mapWitnessStatements(caseData, PartyType.DEFENDANT2))
                .claimant1ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.CLAIMANT1))
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

    private List<Element<BundlingRequestDocument>> mapExpertEvidenceDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        switch (partyType) {
            case CLAIMANT1 -> {
                if (caseData.getDocumentExpertReport() != null) {
                    Map<String, List<Element<UploadEvidenceExpert>>> expertReportMap =
                        groupExpertStatementsByName(caseData.getDocumentExpertReport());
                    expertReportMap.forEach((expertName, expertEvidence) -> {
                        bundlingRequestDocuments.addAll(covertExpertEvidenceTypeToBundleRequestDocs(expertEvidence,
                                                                                                    BundleFileNameList.EXPERT_EVIDENCE.getDisplayName(),
                                                                                                    EvidenceUploadFiles.EXPERT_REPORT.name(),
                                                                                                    PartyType.CLAIMANT1
                        ));

                    });
                }
                if (caseData.getDocumentQuestions() != null) {
                    Map<String, List<Element<UploadEvidenceExpert>>> questionsExpertMap =
                        groupExpertStatementsByName(caseData.getDocumentQuestions());
                    questionsExpertMap.forEach((expertName, expertEvidence) -> {
                        bundlingRequestDocuments.addAll(covertExpertEvidenceTypeToBundleRequestDocs(expertEvidence,
                                                                                                    BundleFileNameList.QUESTIONS_TO.getDisplayName(),
                                                                                                    EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS.name(),
                                                                                                    PartyType.CLAIMANT1
                        ));

                    });
                }
                if (caseData.getDocumentAnswers() != null) {
                    Map<String, List<Element<UploadEvidenceExpert>>> repliesExpertMap =
                        groupExpertStatementsByName(caseData.getDocumentAnswers());
                    repliesExpertMap.forEach((expertName, expertEvidence) -> {
                        bundlingRequestDocuments.addAll(covertExpertEvidenceTypeToBundleRequestDocs(expertEvidence,
                                                                                                    BundleFileNameList.REPLIES_FROM.getDisplayName(),
                                                                                                    EvidenceUploadFiles.ANSWERS_FOR_EXPERTS.name(),
                                                                                                    PartyType.CLAIMANT1
                        ));

                    });
                }
            }
            default -> {
                return null;
            }
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private Map<String, List<Element<UploadEvidenceExpert>>> groupExpertStatementsByName(
        List<Element<UploadEvidenceExpert>> documentExpertReport) {
        Map<String, List<Element<UploadEvidenceExpert>>> expertStatementMap = new HashMap<String,
            List<Element<UploadEvidenceExpert>>>();
        if (expertStatementMap != null) {
            expertStatementMap = documentExpertReport.stream().collect(Collectors
                                                                        .groupingBy(uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement
                                                                            .getValue().getExpertOptionName().trim().toLowerCase()));
        }
        return expertStatementMap;
    }

    private List<Element<BundlingRequestDocument>> mapWitnessStatements(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        switch (partyType) {
            case CLAIMANT1 -> {
                Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
                    groupWitnessStatementsByName(caseData.getDocumentWitnessStatement());
                List<Element<UploadEvidenceWitness>> witnessStatementSelf = getSelfStatement(witnessStatmentsMap,
                                                                                              caseData.getApplicant1());
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(witnessStatementSelf,
                                                                                         BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_STATEMENT.name(), PartyType.CLAIMANT1));
                bundlingRequestDocuments.addAll(covertOtherWitnessEvidenceToBundleRequestDocs(witnessStatmentsMap,
                                                                                              BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
                                                                                              EvidenceUploadFiles.WITNESS_STATEMENT.name(),
                                                                                              PartyType.CLAIMANT1,
                                                                                              caseData.getApplicant1()));
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(caseData.getDocumentWitnessSummary(),
                                                                                         BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_SUMMARY.name(), PartyType.CLAIMANT1));
            }
            case CLAIMANT2 -> {
                Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
                    groupWitnessStatementsByName(caseData.getDocumentWitnessStatement());
                List<Element<UploadEvidenceWitness>> witnessStatementSelf = getSelfStatement(witnessStatmentsMap,
                                                                                             caseData.getApplicant2());
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(witnessStatementSelf,
                                                                                         BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_STATEMENT.name(), PartyType.CLAIMANT2));
                bundlingRequestDocuments.addAll(covertOtherWitnessEvidenceToBundleRequestDocs(witnessStatmentsMap,
                                                                                              BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
                                                                                              EvidenceUploadFiles.WITNESS_STATEMENT.name(),
                                                                                              PartyType.CLAIMANT2, caseData.getApplicant2()));
            }
            case DEFENDANT1 -> {
                Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
                    groupWitnessStatementsByName(caseData.getDocumentWitnessStatementRes());
                List<Element<UploadEvidenceWitness>> witnessStatementSelf = getSelfStatement(witnessStatmentsMap,
                                                                                             caseData.getRespondent1());
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(witnessStatementSelf,
                                                                                         BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_STATEMENT.name(), PartyType.DEFENDANT1));
                bundlingRequestDocuments.addAll(covertOtherWitnessEvidenceToBundleRequestDocs(witnessStatmentsMap,
                                                                                              BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
                                                                                              EvidenceUploadFiles.WITNESS_STATEMENT.name(),
                                                                                              PartyType.DEFENDANT1, caseData.getRespondent1()));
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(caseData.getDocumentWitnessSummaryRes(),
                                                                                         BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_SUMMARY.name(), PartyType.DEFENDANT1));
            }
            case DEFENDANT2 -> {
                Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
                    groupWitnessStatementsByName(caseData.getDocumentWitnessStatementRes2());

                List<Element<UploadEvidenceWitness>> witnessStatementSelf = getSelfStatement(witnessStatmentsMap,
                                                                                             caseData.getRespondent2());
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(witnessStatementSelf,
                                                                                         BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_STATEMENT.name(), PartyType.DEFENDANT2));
                bundlingRequestDocuments.addAll(covertOtherWitnessEvidenceToBundleRequestDocs(witnessStatmentsMap,
                                                                                              BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
                                                                                              EvidenceUploadFiles.WITNESS_STATEMENT.name(),
                                                                                              PartyType.DEFENDANT2, caseData.getRespondent2()));
                bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(caseData.getDocumentWitnessSummaryRes2(),
                                                                                         BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
                                                                                         EvidenceUploadFiles.WITNESS_SUMMARY.name(), PartyType.DEFENDANT2));
            }
            default -> {
                break;
            }
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<BundlingRequestDocument> covertOtherWitnessEvidenceToBundleRequestDocs(
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap, String displayName, String documentType,
        PartyType partyType, Party party) {
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

    private List<Element<BundlingRequestDocument>> mapTrialDocuments(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(getAllTrialDocsByPartyType(getWitnessDocsByPartyAndDocType(partyType,
                                                                                                       EvidenceUploadFiles.WITNESS_SUMMARY, caseData),
                                            getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                                   EvidenceUploadFiles.DOCUMENTARY,
                                                                                   caseData), partyType));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<BundlingRequestDocument> getAllTrialDocsByPartyType(List<Element<UploadEvidenceWitness>> documentWitnessSummary,
                                                                     List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial, PartyType party) {
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
                                                                                   String documentType,
                                                                                   PartyType party) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (witnessEvidence != null) {
            if (documentType.equals(EvidenceUploadFiles.WITNESS_STATEMENT.name())) {
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
                                                 documentType.equals(EvidenceUploadFiles.WITNESS_STATEMENT.name())
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
                                                                                      PartyType party) {
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

    private List<BundlingRequestDocument> covertExpertEvidenceTypeToBundleRequestDocs(List<Element<UploadEvidenceExpert>> evidenceUploadDocList,
                                                                                      String fileNamePrefix, String documentType,
                                                                                      PartyType party) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        if (evidenceUploadDocList != null) {
            evidenceUploadDocList.sort(Comparator.comparing(
                uploadEvidenceDocumentTypeElement -> uploadEvidenceDocumentTypeElement.getValue().getExpertOptionUploadDate(),
                Comparator.reverseOrder()
            ));
            evidenceUploadDocList.forEach(uploadEvidenceDocumentTypeElement -> {
                String docName = generateDocName(fileNamePrefix, uploadEvidenceDocumentTypeElement.getValue().getExpertOptionName(),
                                                 uploadEvidenceDocumentTypeElement.getValue().getExpertOptionUploadDate()
                );
                bundlingRequestDocuments.add(BundlingRequestDocument.builder()
                                                 .documentFileName(docName)
                                                 .documentType(documentType)
                                                 .documentLink(DocumentLink.builder()
                                                                   .documentUrl(uploadEvidenceDocumentTypeElement.getValue().getExpertDocument().getDocumentUrl())
                                                                   .documentBinaryUrl(uploadEvidenceDocumentTypeElement.getValue().getExpertDocument().getDocumentBinaryUrl())
                                                                   .documentFilename(uploadEvidenceDocumentTypeElement.getValue()
                                                                                         .getExpertDocument().getDocumentFileName()).build())
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

    protected enum PartyType {
        CLAIMANT1("CL 1"),
        CLAIMANT2("CL 2"),
        DEFENDANT1("DF 1"),
        DEFENDANT2("DF 2");
        String displayName;

        public String getDisplayName() {
            return displayName;
        }

        PartyType(String displayName) {
            this.displayName = displayName;
        }
    }
}
