package uk.gov.hmcts.reform.civil.helpers.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getExpertDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.buildBundlingRequestDoc;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.generateDocName;

@Component
@RequiredArgsConstructor
@Slf4j
public class BundleDocumentsRetrieval {

    private final ConversionToBundleRequestDocs conversionToBundleRequestDocs;
    private final BundleRequestDocsOrganizer bundleRequestDocsOrganizer;

    public String getParticularsOfClaimName(CaseData caseData) {

        log.info("Getting details of claim for case ID: {}", caseData.getCcdCaseReference());
        LocalDate pocDate;
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            pocDate = caseData.getIssueDate();
        } else if (Objects.nonNull(caseData.getClaimDetailsNotificationDate())) {
            pocDate = caseData.getClaimDetailsNotificationDate().toLocalDate();
        } else {
            pocDate = caseData.getSubmittedDate().toLocalDate();
        }
        return generateDocName(BundleFileNameList.PARTICULARS_OF_CLAIM.getDisplayName(),
                               null, null, pocDate
        );
    }

    public List<BundlingRequestDocument> getAllRemainingExpertQuestions(PartyType partyType,
                                                                        EvidenceUploadType questionsForExperts, CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        List<Element<UploadEvidenceExpert>> listOfDocsOtherPartyQues = getExpertDocsByPartyAndDocType(partyType,
                                                                                                      questionsForExperts,
                                                                                                      caseData
        );
        Set<String> allExpertFromOtherParty1 = getAllExpertFromOtherParty(partyType, EvidenceUploadType.EXPERT_REPORT,
                                                                          caseData, true
        );
        Set<String> allExpertFromOtherParty2 = getAllExpertFromOtherParty(partyType, EvidenceUploadType.EXPERT_REPORT,
                                                                          caseData, false
        );
        Set<String> allJointExpertsFromOtherParty1 = getAllExpertFromOtherParty(partyType,
                                                                                EvidenceUploadType.JOINT_STATEMENT,
                                                                                caseData,
                                                                                true
        );
        Set<String> allJointExpertsFromOtherParty2 = getAllExpertFromOtherParty(partyType,
                                                                                EvidenceUploadType.JOINT_STATEMENT,
                                                                                caseData,
                                                                                false
        );
        Party otherParty1;
        Party otherParty2;
        if (partyType.equals(PartyType.CLAIMANT1) || partyType.equals(PartyType.CLAIMANT2)) {
            otherParty1 = getPartyByPartyType(PartyType.DEFENDANT1, caseData);
            otherParty2 = getPartyByPartyType(PartyType.DEFENDANT2, caseData);

        } else {
            otherParty1 = getPartyByPartyType(PartyType.CLAIMANT1, caseData);
            otherParty2 = getPartyByPartyType(PartyType.CLAIMANT2, caseData);
        }
        List<Element<UploadEvidenceExpert>> tempList = new ArrayList<>();
        listOfDocsOtherPartyQues.forEach(expertElement -> {
            if (!((matchParty(expertElement.getValue().getExpertOptionOtherParty(), otherParty1)
                && matchType(expertElement.getValue().getExpertOptionName(), allExpertFromOtherParty1, false))
                || (matchParty(expertElement.getValue().getExpertOptionOtherParty(), otherParty2)
                && matchType(expertElement.getValue().getExpertOptionName(), allExpertFromOtherParty2, false))
                || (matchParty(expertElement.getValue().getExpertOptionOtherParty(), otherParty1)
                && matchType(expertElement.getValue().getExpertOptionName(), allJointExpertsFromOtherParty1, false))
                || (matchParty(expertElement.getValue().getExpertOptionOtherParty(), otherParty2)
                && matchType(expertElement.getValue().getExpertOptionName(), allJointExpertsFromOtherParty2, false)))) {
                tempList.add(expertElement);
            }
        });
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertExpertEvidenceTypeToBundleRequestDocs(
            tempList, BundleFileNameList.QUESTIONS_TO.getDisplayName(),
            EvidenceUploadType.QUESTIONS_FOR_EXPERTS.name()
        ));

        return bundlingRequestDocuments;
    }

    public List<BundlingRequestDocument> getAllOtherPartyQuestions(PartyType partyType,
                                                                   CaseData caseData,
                                                                   Set<String> allExpertsNames) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        List<Element<UploadEvidenceExpert>> questionsFromOtherPartyDocs = getAllDocsFromOtherParty(partyType, caseData,
                                                                                                   EvidenceUploadType.QUESTIONS_FOR_EXPERTS
        );
        if (!questionsFromOtherPartyDocs.isEmpty()) {
            List<Element<UploadEvidenceExpert>> tempList = questionsFromOtherPartyDocs.stream().filter(expertElement -> matchType(
                    expertElement.getValue().getExpertOptionName(), allExpertsNames, false
                ))
                .filter(expertElement -> matchParty(
                    expertElement.getValue().getExpertOptionOtherParty(),
                    getPartyByPartyType(partyType, caseData)
                )).collect(Collectors.toList());
            if (!tempList.isEmpty()) {
                Map<String, List<Element<UploadEvidenceExpert>>> expertReportMap = bundleRequestDocsOrganizer.groupExpertStatementsByName(
                    tempList);
                expertReportMap.forEach((expertName, expertEvidenceList) -> bundlingRequestDocuments.addAll(
                    conversionToBundleRequestDocs.covertExpertEvidenceTypeToBundleRequestDocs(
                        expertEvidenceList,
                        BundleFileNameList.QUESTIONS_TO.getDisplayName(),
                        EvidenceUploadType.QUESTIONS_FOR_EXPERTS.name()
                    )));
            }
        }
        return bundlingRequestDocuments;
    }

    public List<BundlingRequestDocument> getAllRemainingExpertReports(PartyType partyType,
                                                                      EvidenceUploadType evidenceUploadFiles, CaseData caseData,
                                                                      BundleFileNameList bundleFileNameList, Set<String> allExpertsNames, Set<String> allJointExpertsNames) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Map<String, List<Element<UploadEvidenceExpert>>> expertReportMap =
            bundleRequestDocsOrganizer.groupExpertStatementsByName(getExpertDocsByPartyAndDocType(
                partyType,
                evidenceUploadFiles,
                caseData
            ));
        if (expertReportMap != null) {
            expertReportMap.forEach((expertName, expertEvidence) -> {
                if ((allExpertsNames != null && allExpertsNames.stream().anyMatch(s -> s.equalsIgnoreCase(expertName)))
                    || (allJointExpertsNames != null && allJointExpertsNames.stream().anyMatch(s -> s.equalsIgnoreCase(
                    expertName)))) {
                    return;
                }
                bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertExpertEvidenceTypeToBundleRequestDocs(
                    expertEvidence,
                    bundleFileNameList.getDisplayName(),
                    evidenceUploadFiles.name()
                ));
            });
        }
        return bundlingRequestDocuments;
    }

    public Set<String> getAllExpertsNames(PartyType partyType, EvidenceUploadType evidenceUploadFileType,
                                          CaseData caseData) {
        List<Element<UploadEvidenceExpert>> expertsList = getExpertDocsByPartyAndDocType(partyType,
                                                                                         evidenceUploadFileType,
                                                                                         caseData
        );
        if (expertsList != null) {
            return expertsList.stream().map(expertElement -> expertElement.getValue().getExpertOptionName().trim().toLowerCase())
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public List<BundlingRequestDocument> getAllExpertReports(PartyType partyType, EvidenceUploadType evidenceUploadFiles,
                                                             CaseData caseData, BundleFileNameList bundleFileNameList,
                                                             Set<String> allExpertsNames) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Map<String, List<Element<UploadEvidenceExpert>>> expertReportMap =
            bundleRequestDocsOrganizer.groupExpertStatementsByName(getExpertDocsByPartyAndDocType(
                partyType,
                evidenceUploadFiles,
                caseData
            ));
        if (expertReportMap != null) {
            expertReportMap.forEach((expertName, expertEvidence) -> {
                if (allExpertsNames != null && allExpertsNames.stream().anyMatch(s -> s.equalsIgnoreCase(expertName))) {
                    bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertExpertEvidenceTypeToBundleRequestDocs(
                        expertEvidence,
                        bundleFileNameList.getDisplayName(),
                        evidenceUploadFiles.name()
                    ));
                }
            });
        }
        return bundlingRequestDocuments;
    }

    public Party getPartyByPartyType(PartyType partyType, CaseData caseData) {
        return switch (partyType) {
            case CLAIMANT1 -> caseData.getApplicant1() != null ? caseData.getApplicant1() : null;
            case CLAIMANT2 -> caseData.getApplicant2() != null ? caseData.getApplicant2() : null;
            case DEFENDANT1 -> caseData.getRespondent1() != null ? caseData.getRespondent1() : null;
            case DEFENDANT2 -> caseData.getRespondent2() != null ? caseData.getRespondent2() : null;
        };
    }

    public List<Element<UploadEvidenceWitness>> getSelfStatement(Map<String,
        List<Element<UploadEvidenceWitness>>> witnessStatementsMap, Party party) {
        List<Element<UploadEvidenceWitness>> selfStatementList = new ArrayList<>();
        if (party != null && party.getPartyName() != null && witnessStatementsMap.get(party.getPartyName().trim().toLowerCase()) != null) {
            selfStatementList.addAll(witnessStatementsMap.get(party.getPartyName().trim().toLowerCase()));
        }
        if (party != null && party.isIndividual() && party.getIndividualFirstName() != null && witnessStatementsMap.get(
            party.getIndividualFirstName()
                .trim().toLowerCase()) != null) {
            selfStatementList.addAll(witnessStatementsMap.get(party.getIndividualFirstName().trim().toLowerCase()));
        }
        return selfStatementList;
    }

    public List<Element<UploadEvidenceDocumentType>> getDocumentaryEvidenceByType(
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial, List<String> displayNames, boolean doesNotMatchType) {
        if (documentEvidenceForTrial != null) {
            return
                bundleRequestDocsOrganizer.filterDocumentaryEvidenceForTrialDocs(
                    documentEvidenceForTrial,
                    displayNames,
                    doesNotMatchType
                );
        } else {
            return Collections.emptyList();
        }
    }

    public List<Element<CaseDocument>> getSortedDefendantDefenceAndClaimantReply(List<Element<CaseDocument>> systemGeneratedCaseDocuments) {
        List<Element<CaseDocument>> sortedDefendantDefenceAndClaimantReply;
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

    public List<BundlingRequestDocument> getDqByCategoryId(CaseData caseData, String category, PartyType partyType) {
        List<Element<CaseDocument>> docs = caseData.getSystemGeneratedCaseDocuments().stream()
            .filter(caseDocumentElement -> (caseDocumentElement.getValue().getDocumentType()
                .equals(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                && nonNull(caseDocumentElement.getValue().getDocumentLink().getCategoryID())
                && caseDocumentElement.getValue().getDocumentLink().getCategoryID().equals(category)))
            .sorted(Comparator.comparing(caseDocumentElement -> caseDocumentElement
                .getValue().getCreatedDatetime())).toList();
        return docs.stream().map(caseDocumentElement -> {
            String docName = generateDocName(BundleFileNameList.DIRECTIONS_QUESTIONNAIRE.getDisplayName(),
                                             partyType.getDisplayName(), null,
                                             caseDocumentElement.getValue().getCreatedDatetime().toLocalDate()
            );
            return buildBundlingRequestDoc(
                docName,
                caseDocumentElement.getValue().getDocumentLink(),
                DocumentType.DIRECTIONS_QUESTIONNAIRE.name()
            );
        }).toList();
    }

    private boolean matchType(String name, Collection<String> displayNames, boolean doesNotMatchType) {

        if (doesNotMatchType) {
            return displayNames.stream().noneMatch(s -> s.equalsIgnoreCase(name.trim()));
        } else {
            return displayNames.stream().anyMatch(s -> s.equalsIgnoreCase(name.trim()));
        }
    }

    private Set<String> getAllExpertFromOtherParty(PartyType partyType, EvidenceUploadType expertReport,
                                                   CaseData caseData, boolean isDefendant1) {
        if (partyType.equals(PartyType.CLAIMANT1) || partyType.equals(PartyType.CLAIMANT2)) {
            return getAllExpertsNames(isDefendant1 ? PartyType.DEFENDANT1 : PartyType.DEFENDANT2,
                                      expertReport, caseData
            );
        } else {
            return getAllExpertsNames(isDefendant1 ? PartyType.CLAIMANT1 : PartyType.CLAIMANT2,
                                      expertReport, caseData
            );
        }
    }

    private boolean matchParty(String expertOptionOtherParty, Party party) {
        if (party != null && party.getPartyName() != null && expertOptionOtherParty.equalsIgnoreCase(party.getPartyName())) {
            return true;
        }
        return party != null && party.isIndividual() && party.getIndividualFirstName() != null && expertOptionOtherParty.equalsIgnoreCase(
            party.getIndividualFirstName());
    }

    private List<Element<UploadEvidenceExpert>> getAllDocsFromOtherParty(PartyType partyType, CaseData caseData,
                                                                         EvidenceUploadType evidenceUploadFileType) {
        List<Element<UploadEvidenceExpert>> list = new ArrayList<>();
        if (partyType.equals(PartyType.CLAIMANT1) || partyType.equals(PartyType.CLAIMANT2)) {
            list.addAll(getExpertDocsByPartyAndDocType(PartyType.DEFENDANT1,
                                                       evidenceUploadFileType, caseData
            ));
            list.addAll(getExpertDocsByPartyAndDocType(PartyType.DEFENDANT2, evidenceUploadFileType, caseData));
        } else {
            list.addAll(getExpertDocsByPartyAndDocType(PartyType.CLAIMANT1, evidenceUploadFileType, caseData));
            list.addAll(getExpertDocsByPartyAndDocType(PartyType.CLAIMANT2, evidenceUploadFileType, caseData));
        }
        return list;
    }
}
