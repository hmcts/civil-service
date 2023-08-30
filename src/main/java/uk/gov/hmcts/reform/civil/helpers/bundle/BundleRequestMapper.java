package uk.gov.hmcts.reform.civil.helpers.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DocumentWithRegex;
import uk.gov.hmcts.reform.civil.model.Party;

import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.bundle.DocumentLink;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getExpertDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getWitnessDocsByPartyAndDocType;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleRequestMapper {

    private static final String DOC_FILE_NAME = "DOC_FILE_NAME";

    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData,
                                                                String bundleConfigFileName, String jurisdiction,
                                                                String caseTypeId, Long id) {
        String fileNameIdentifier = generateFileName(caseData);
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

    private String generateFileName(CaseData caseData) {
        String applicantName = caseData.getApplicant1().isIndividual()
            ? caseData.getApplicant1().getIndividualLastName() : caseData.getApplicant1().getPartyName();
        String respondentName = caseData.getApplicant1().isIndividual()
            ? caseData.getRespondent1().getIndividualLastName() : caseData.getRespondent1().getPartyName();
        return applicantName + "v" + respondentName +
            "- Bundle" + DateFormatHelper.formatLocalDate(caseData.getHearingDate(), "ddMMyyyy");
    }

    private BundlingCaseData mapCaseData(CaseData caseData, String bundleConfigFileName) {
        BundlingCaseData bundlingCaseData =
            BundlingCaseData.builder().id(caseData.getCcdCaseReference()).bundleConfiguration(
                    bundleConfigFileName)
                    .trialDocuments(mapTrialDocuments(caseData))
                    .statementsOfCaseDocuments(mapStatmentOfcaseDocs(caseData))
                    .ordersDocuments(mapOrdersDocument(caseData))
                    .claimant1WitnessStatements(mapWitnessStatements(caseData, PartyType.CLAIMANT1))
                    .claimant2WitnessStatements(mapWitnessStatements(caseData, PartyType.CLAIMANT2))
                    .defendant1WitnessStatements(mapWitnessStatements(caseData, PartyType.DEFENDANT1))
                    .defendant2WitnessStatements(mapWitnessStatements(caseData, PartyType.DEFENDANT2))
                    .claimant1ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.CLAIMANT1))
                    .claimant2ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.CLAIMANT2))
                    .defendant1ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.DEFENDANT1))
                    .defendant2ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.DEFENDANT2))
                    .claimant1DisclosedDocuments(mapDisclosedDocs(caseData, PartyType.CLAIMANT1))
                    .claimant2DisclosedDocuments(mapDisclosedDocs(caseData, PartyType.CLAIMANT2))
                    .defendant1DisclosedDocuments(mapDisclosedDocs(caseData, PartyType.DEFENDANT1))
                    .defendant2DisclosedDocuments(mapDisclosedDocs(caseData, PartyType.DEFENDANT2))
                    .claimant1CostsBudgets(mapCostBudgetDocs(caseData, PartyType.CLAIMANT1))
                    .claimant2CostsBudgets(mapCostBudgetDocs(caseData, PartyType.CLAIMANT2))
                    .defendant1CostsBudgets(mapCostBudgetDocs(caseData, PartyType.DEFENDANT1))
                    .defendant2CostsBudgets(mapCostBudgetDocs(caseData, PartyType.DEFENDANT2))
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

    private List<Element<BundlingRequestDocument>> mapCostBudgetDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                                                                           EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE, caseData),
                                                                                    BundleFileNameList.CL1_COSTS_BUDGET.getDisplayName(),
                                                                                    EvidenceUploadFiles.COSTS.name(),
                                                                                    partyType));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapDisclosedDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                                   EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE, caseData),
                                                                                    DOC_FILE_NAME,
                                                                                 EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.name(),
                                                                                    partyType));
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialList =
            getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadFiles.DOCUMENTARY, caseData);

        if (documentEvidenceForTrialList != null) {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getDocumentaryEvidenceByType(documentEvidenceForTrialList,
                                             TypeOfDocDocumentaryEvidenceOfTrial.getAllDocsDisplayNames(), true),
                BundleFileNameList.NOTICE_TO_ADMIT_FACTS.getDisplayName(),
                TypeOfDocDocumentaryEvidenceOfTrial.NOTICE_TO_ADMIT_FACTS.name(),
                partyType
            ));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapExpertEvidenceDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        bundlingRequestDocuments.addAll(getAllExpertReports(partyType, EvidenceUploadFiles.EXPERT_REPORT, caseData,
                                                            BundleFileNameList.EXPERT_EVIDENCE));
        bundlingRequestDocuments.addAll(getAllExpertReports(partyType, EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS, caseData,
                                                            BundleFileNameList.QUESTIONS_TO));
        bundlingRequestDocuments.addAll(getAllExpertReports(partyType, EvidenceUploadFiles.ANSWERS_FOR_EXPERTS, caseData,
                                                            BundleFileNameList.REPLIES_FROM));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<BundlingRequestDocument> getAllExpertReports(PartyType partyType, EvidenceUploadFiles evidenceUploadFiles,
                                                           CaseData caseData, BundleFileNameList bundleFileNameList) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Map<String, List<Element<UploadEvidenceExpert>>> expertReportMap =
            groupExpertStatementsByName(getExpertDocsByPartyAndDocType(partyType, evidenceUploadFiles, caseData));
        if (expertReportMap != null) {
            expertReportMap.forEach((expertName, expertEvidence) -> {
                bundlingRequestDocuments.addAll(covertExpertEvidenceTypeToBundleRequestDocs(
                    expertEvidence,
                    bundleFileNameList.getDisplayName(),
                    evidenceUploadFiles.name()
                ));
            });
        }
        return bundlingRequestDocuments;
    }

    private Map<String, List<Element<UploadEvidenceExpert>>> groupExpertStatementsByName(
        List<Element<UploadEvidenceExpert>> documentExpertReport) {
        Map<String, List<Element<UploadEvidenceExpert>>> expertStatementMap = new HashMap<String,
            List<Element<UploadEvidenceExpert>>>();
        if (documentExpertReport != null) {
            expertStatementMap = documentExpertReport.stream().collect(Collectors
                                                                        .groupingBy(uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement
                                                                            .getValue().getExpertOptionName().trim().toLowerCase()));
        }
        return expertStatementMap;
    }

    private List<Element<BundlingRequestDocument>> mapWitnessStatements(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        Party party = getPartyByPartyType(partyType, caseData);
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
            groupWitnessStatementsByName(getWitnessDocsByPartyAndDocType(partyType,
                                                                         EvidenceUploadFiles.WITNESS_STATEMENT, caseData));
        List<Element<UploadEvidenceWitness>> witnessStatementSelf = getSelfStatement(witnessStatmentsMap,
                                                                                     party);
        bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(witnessStatementSelf,
                                                                                 BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
                                                                                 EvidenceUploadFiles.WITNESS_STATEMENT.name(), partyType));
        bundlingRequestDocuments.addAll(covertOtherWitnessEvidenceToBundleRequestDocs(witnessStatmentsMap,
                                                                                      BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
                                                                                      EvidenceUploadFiles.WITNESS_STATEMENT.name(),
                                                                                      partyType,
                                                                                      party));
        bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(getWitnessDocsByPartyAndDocType(partyType,
                                                                                                                EvidenceUploadFiles.WITNESS_SUMMARY, caseData),
                                                                                BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
                                                                                EvidenceUploadFiles.WITNESS_SUMMARY.name(), partyType));
        bundlingRequestDocuments.addAll(covertWitnessEvidenceToBundleRequestDocs(getWitnessDocsByPartyAndDocType(partyType,
                                                                                                                 EvidenceUploadFiles.NOTICE_OF_INTENTION, caseData),
                                                                                 BundleFileNameList.HEARSAY_NOTICE.getDisplayName(),
                                                                                 EvidenceUploadFiles.NOTICE_OF_INTENTION.name(),
                                                                                 partyType));
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial = getEvidenceUploadDocsByPartyAndDocType(partyType,
            EvidenceUploadFiles.DOCUMENTARY, caseData);
        if (documentEvidenceForTrial != null) {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getDocumentaryEvidenceByType(documentEvidenceForTrial,
                                             TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.getDisplayNames(), false),
                BundleFileNameList.NOTICE_TO_ADMIT_FACTS.getDisplayName(),
                TypeOfDocDocumentaryEvidenceOfTrial.NOTICE_TO_ADMIT_FACTS.name(),
                partyType
            ));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private Party getPartyByPartyType(PartyType partyType, CaseData caseData) {
        switch (partyType) {
            case CLAIMANT1 : return caseData.getApplicant1() != null ? caseData.getApplicant1() : null;
            case CLAIMANT2: return caseData.getApplicant2() != null ? caseData.getApplicant2() : null;
            case DEFENDANT1: return caseData.getRespondent1() != null ? caseData.getRespondent1() : null;
            case DEFENDANT2: return caseData.getRespondent2() != null ? caseData.getRespondent2() : null;
            default: return null;
        }
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

    private List<Element<UploadEvidenceWitness>>  sortWitnessListByDate(List<Element<UploadEvidenceWitness>> witnessEvidence,
                                       boolean sortByCreatedDate) {
        if (sortByCreatedDate) {
            witnessEvidence.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getCreatedDatetime(),
                Comparator.reverseOrder()
            ));
        } else {
            witnessEvidence.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate(),
                Comparator.reverseOrder()
            ));
        }
        return witnessEvidence;
    }

    private List<Element<UploadEvidenceDocumentType>>  sortEvidenceUploadByDate(List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocType,
                                                                        boolean sortByCreatedDate) {
        if (sortByCreatedDate) {
            uploadEvidenceDocType.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getCreatedDatetime(),
                Comparator.reverseOrder()
            ));
        } else {
            uploadEvidenceDocType.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getDocumentIssuedDate(),
                Comparator.reverseOrder()
            ));
        }
        return uploadEvidenceDocType;
    }

    private List<Element<UploadEvidenceExpert>>  sortExpertListByDate(List<Element<UploadEvidenceExpert>> expertEvidence,
                                                                        boolean sortByCreatedDate) {
        if (sortByCreatedDate) {
            expertEvidence.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getCreatedDatetime(),
                Comparator.reverseOrder()
            ));
        } else {
            expertEvidence.sort(Comparator.comparing(
                uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement.getValue().getExpertOptionUploadDate(),
                Comparator.reverseOrder()
            ));
        }
        return expertEvidence;
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
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments().stream()
                                                                           .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                                                                           .equals(DocumentType.DEFAULT_JUDGMENT_SDO_ORDER)).collect(
                                                                           Collectors.toList()),
                                                                       BundleFileNameList.DIRECTIONS_ORDER.getDisplayName()));
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments().stream()
                                                                           .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                                                                           .equals(DocumentType.SDO_ORDER)).collect(
                                                                           Collectors.toList()),
                                                                       BundleFileNameList.DIRECTIONS_ORDER.getDisplayName()));
        if (caseData.getGeneralOrderDocument() != null) {
            bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(
                caseData.getGeneralOrderDocument(),
                BundleFileNameList.ORDER.getDisplayName()
            ));
        }
        if (caseData.getDismissalOrderDocument() != null) {
            bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(
                caseData.getDismissalOrderDocument(),
                BundleFileNameList.ORDER.getDisplayName()
            ));
        }
        if (caseData.getDirectionOrderDocument() != null) {
            bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(
                caseData.getDirectionOrderDocument(),
                BundleFileNameList.ORDER.getDisplayName()
            ));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapStatmentOfcaseDocs(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        log.info("System generated docs : " + caseData.getSystemGeneratedCaseDocuments());
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments().stream()
                                                                           .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                                                                           .equals(DocumentType.SEALED_CLAIM)).collect(
                                                                           Collectors.toList()),
                                                                       BundleFileNameList.CLAIM_FORM.getDisplayName()));
        if (caseData.getServedDocumentFiles() != null && caseData.getServedDocumentFiles().getScheduleOfLoss() != null) {
            bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments().stream()
                                                                               .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                                                                                   .equals(DocumentType.DIRECTIONS_QUESTIONNAIRE)).collect(
                                                                                   Collectors.toList()),
                                                                           BundleFileNameList.DIRECTIONS_QUESTIONNAIRE.getDisplayName()));
        }
        List<Element<CaseDocument>> clAndDfDocList = caseData.getDefendantResponseDocuments();
        clAndDfDocList.addAll(caseData.getClaimantResponseDocuments());
        List<Element<CaseDocument>> sortedDefendantDefenceAndClaimantReply =
            getSortedDefendantDefenceAndClaimantReply(clAndDfDocList);
        sortedDefendantDefenceAndClaimantReply.forEach(caseDocumentElement -> {
            String docType = caseDocumentElement.getValue().getDocumentType().equals(DocumentType.DEFENDANT_DEFENCE)
                ? BundleFileNameList.DEFENCE.getDisplayName() : BundleFileNameList.CL_REPLY.getDisplayName();
            String party = caseDocumentElement.getValue().getCreatedBy().equalsIgnoreCase("Defendant")
                ? PartyType.DEFENDANT1.getDisplayName() :
                    caseDocumentElement.getValue().getCreatedBy().equalsIgnoreCase("Defendant 2")
                        ? PartyType.DEFENDANT2.getDisplayName() : "";
            String docName = String.format(generateDocName(docType, party,
                                                           caseDocumentElement.getValue().getCreatedDatetime().toLocalDate()),
                                           caseDocumentElement.getValue().getCreatedDatetime());
            bundlingRequestDocuments.add(BundlingRequestDocument.builder()
                                                    .documentFileName(docName)
                                                    .documentLink(DocumentLink.builder()
                                                                      .documentUrl(caseDocumentElement.getValue().getDocumentLink().getDocumentUrl())
                                                                      .documentBinaryUrl(caseDocumentElement.getValue().getDocumentLink().getDocumentBinaryUrl())
                                                                      .documentFilename(caseDocumentElement.getValue().getDocumentLink().getDocumentFileName()).build())
                                                    .build());
        });
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(caseData.getSystemGeneratedCaseDocuments().stream()
                                                                           .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                                                                               .equals(DocumentType.DIRECTIONS_QUESTIONNAIRE)).collect(
                                                                               Collectors.toList()),
                                                                       BundleFileNameList.DIRECTIONS_QUESTIONNAIRE.getDisplayName()));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<BundlingRequestDocument> mapServedDocumentFiles(List<Element<DocumentWithRegex>> scheduleOfLoss, String displayName) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (scheduleOfLoss != null) {
            scheduleOfLoss.forEach(caseDocumentElement -> {
                bundlingRequestDocuments.add(BundlingRequestDocument.builder()
                                                        .documentFileName(caseDocumentElement.getValue().getDocument().getDocumentFileName())
                                                        .documentLink(DocumentLink.builder()
                                                                          .documentUrl(caseDocumentElement.getValue().getDocument().getDocumentUrl())
                                                                          .documentBinaryUrl(caseDocumentElement.getValue().getDocument().getDocumentBinaryUrl())
                                                                          .documentFilename(caseDocumentElement.getValue().getDocument().getDocumentFileName()).build())
                                                        .build());
            });
        }

        return bundlingRequestDocuments;
    }

    private List<Element<CaseDocument>> getSortedDefendantDefenceAndClaimantReply(List<Element<CaseDocument>> systemGeneratedCaseDocuments) {
        List<Element<CaseDocument>> sortedDefendantDefenceAndClaimantReply = new ArrayList<>();
        List<Element<CaseDocument>> dfDefence =
            systemGeneratedCaseDocuments.stream().filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                .equals(DocumentType.DEFENDANT_DEFENCE)).collect(Collectors.toList());

        List<Element<CaseDocument>> clDefence =
            systemGeneratedCaseDocuments.stream().filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                .equals(DocumentType.CLAIMANT_DEFENCE)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dfDefence) || CollectionUtils.isEmpty(clDefence)) {
            sortedDefendantDefenceAndClaimantReply = CollectionUtils.isEmpty(dfDefence) ? clDefence : dfDefence;
            sortedDefendantDefenceAndClaimantReply.sort(Comparator.comparing(caseDocumentElement -> caseDocumentElement.getValue().getCreatedDatetime()));
            return sortedDefendantDefenceAndClaimantReply;
        }
        dfDefence.sort(Comparator.comparing(caseDocumentElement -> caseDocumentElement.getValue().getCreatedDatetime()));
        clDefence.sort(Comparator.comparing(caseDocumentElement -> caseDocumentElement.getValue().getCreatedDatetime()));
        sortedDefendantDefenceAndClaimantReply = IntStream.range(0, Math.min(dfDefence.size(), clDefence.size()))
            .mapToObj(index -> Arrays.asList(dfDefence.get(index), clDefence.get(index)))
            .flatMap(List::stream)
            .collect(Collectors.toList());

        int remainingIndex = Math.min(dfDefence.size(), clDefence.size());
        List<Element<CaseDocument>> remainingList = (dfDefence.size() > clDefence.size()) ? dfDefence : clDefence;

        sortedDefendantDefenceAndClaimantReply.addAll(remainingList.subList(remainingIndex, remainingList.size()));
        return sortedDefendantDefenceAndClaimantReply;
    }

    private List<Element<BundlingRequestDocument>> mapTrialDocuments(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                                                                                     EvidenceUploadFiles.CASE_SUMMARY, caseData),
                                                                                     BundleFileNameList.CASE_SUMMARY_FILE_DISPLAY_NAME.getDisplayName(),
                                                                                     EvidenceUploadFiles.CASE_SUMMARY.name(),
                                                                                        partyType));
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getDocumentaryEvidenceByType(getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadFiles.DOCUMENTARY, caseData),
                                             TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.getDisplayNames(), false),
                BundleFileNameList.CHRONOLOGY_FILE_DISPLAY_NAME.getDisplayName(), TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.name(), partyType
            ));
            bundlingRequestDocuments.addAll(covertEvidenceUploadTypeToBundleRequestDocs(
                getDocumentaryEvidenceByType(getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadFiles.DOCUMENTARY, caseData),
                                             TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.getDisplayNames(), false),
                BundleFileNameList.TRIAL_TIMETABLE_FILE_DISPLAY_NAME.getDisplayName(), TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.name(), partyType
            ));
        });
        log.info("Trial docs list : " + bundlingRequestDocuments.size());
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<UploadEvidenceDocumentType>> getDocumentaryEvidenceByType(
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial, List<String> displayNames, boolean doesNotMatchType) {
        if (documentEvidenceForTrial != null) {
            return
                filterDocumentaryEvidenceForTrialDocs(
                    documentEvidenceForTrial,
                    displayNames, doesNotMatchType
                );
        } else {
            return null;
        }
    }

    public List<Element<UploadEvidenceDocumentType>> filterDocumentaryEvidenceForTrialDocs(
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial, List<String> displayNames,
        boolean doesNotMatchType) {
        sortEvidenceUploadByDate(documentEvidenceForTrial, false);
        return documentEvidenceForTrial.stream().filter(uploadEvidenceDocumentTypeElement -> matchType(
            uploadEvidenceDocumentTypeElement.getValue().getTypeOfDocument(),
            displayNames, doesNotMatchType
        )).collect(Collectors.toList());
    }

    private boolean matchType(String typeOfDocument, List<String> displayNames, boolean doesNotMatchType) {
        if (doesNotMatchType) {
            return displayNames.stream().noneMatch(s -> s.equalsIgnoreCase(typeOfDocument.trim()));
        } else {
            return displayNames.stream().anyMatch(s -> s.equalsIgnoreCase(typeOfDocument.trim()));
        }
    }

    private List<BundlingRequestDocument> covertWitnessEvidenceToBundleRequestDocs(List<Element<UploadEvidenceWitness>> witnessEvidence,
                                                                                   String fileNamePrefix,
                                                                                   String documentType,
                                                                                   PartyType party) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (witnessEvidence != null) {
            if (documentType.equals(EvidenceUploadFiles.WITNESS_STATEMENT.name())) {
                sortWitnessListByDate(witnessEvidence, false);
            } else {
                sortWitnessListByDate(witnessEvidence, true);
            }
            witnessEvidence.forEach(uploadEvidenceWitnessElement -> {
                String docName = generateDocName(fileNamePrefix, uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                                                 documentType.equals(EvidenceUploadFiles.WITNESS_STATEMENT.name())
                                                     ? uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate() : uploadEvidenceWitnessElement
                                                     .getValue().getCreatedDatetime().toLocalDate());
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

    private String generateDocName(String fileName, String strParam, LocalDate date) {
        if (StringUtils.isBlank(strParam)) {
            return String.format(fileName, DateFormatHelper.formatLocalDate(date, "dd/MM/yyyy"));
        }
        return String.format(fileName, strParam, DateFormatHelper.formatLocalDate(date, "dd/MM/yyyy"));
    }

    private List<BundlingRequestDocument> covertEvidenceUploadTypeToBundleRequestDocs(List<Element<UploadEvidenceDocumentType>> evidenceUploadDocList,
                                                                                      String fileNamePrefix, String documentType,
                                                                                      PartyType party) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        if (evidenceUploadDocList != null) {
            sortEvidenceUploadByDate(evidenceUploadDocList, false);
            evidenceUploadDocList.forEach(uploadEvidenceDocumentTypeElement -> {
                String docName = fileNamePrefix.equals(DOC_FILE_NAME)
                    ?
                    generateDocName(uploadEvidenceDocumentTypeElement.getValue().getDocumentUpload().getDocumentFileName() + "%s",
                                      null, uploadEvidenceDocumentTypeElement.getValue().getDocumentIssuedDate())
                    : generateDocName(fileNamePrefix, party.getDisplayName(),
                                                documentType.equals(EvidenceUploadFiles.CASE_SUMMARY.name())
                                      ? uploadEvidenceDocumentTypeElement.getValue().getCreatedDatetime().toLocalDate() :
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

    private List<BundlingRequestDocument> covertExpertEvidenceTypeToBundleRequestDocs(List<Element<UploadEvidenceExpert>> evidenceUploadExpert,
                                                                                      String fileNamePrefix, String documentType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        if (evidenceUploadExpert != null) {
            sortExpertListByDate(evidenceUploadExpert, false);
            evidenceUploadExpert.forEach(uploadEvidenceDocumentTypeElement -> {
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

    private List<BundlingRequestDocument> mapSystemGeneratedCaseDocument(List<Element<CaseDocument>> systemGeneratedCaseDocuments, String displayName) {
        List<BundlingRequestDocument> bundlingSystemGeneratedCaseDocs = new ArrayList<>();
        if (systemGeneratedCaseDocuments != null) {
            systemGeneratedCaseDocuments.forEach(caseDocumentElement -> {
                bundlingSystemGeneratedCaseDocs.add(BundlingRequestDocument.builder()
                                                        .documentFileName(generateDocName(displayName, null,
                                                                                          caseDocumentElement.getValue().getCreatedDatetime().toLocalDate()))
                                                        .documentLink(DocumentLink.builder()
                                                                          .documentUrl(caseDocumentElement.getValue().getDocumentLink().getDocumentUrl())
                                                                          .documentBinaryUrl(caseDocumentElement.getValue().getDocumentLink().getDocumentBinaryUrl())
                                                                          .documentFilename(caseDocumentElement.getValue().getDocumentLink().getDocumentFileName()).build())
                                                        .build());
            });
        }
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
