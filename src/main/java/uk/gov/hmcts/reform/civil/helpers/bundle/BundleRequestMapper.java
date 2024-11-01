package uk.gov.hmcts.reform.civil.helpers.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getWitnessDocsByPartyAndDocType;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleRequestMapper {

    private static final String DOC_FILE_NAME = "DOC_FILE_NAME";
    private static final String DOC_FILE_NAME_WITH_DATE = "DOC_FILE_NAME %s";
    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private final DocumentsRetrievalUtils documentsRetrievalUtils;
    private final ConversionToBundleRequestDocs conversionToBundleRequestDocs;
    private final FeatureToggleService featureToggleService;

    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData,
                                                                String bundleConfigFileName, String jurisdiction,
                                                                String caseTypeId) {
        String fileNameIdentifier = generateFileName(caseData);
        return BundleCreateRequest.builder()
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
    }

    private BundlingCaseData mapCaseData(CaseData caseData, String bundleConfigFileName) {

        BundlingCaseData bundlingCaseData =
            BundlingCaseData.builder().id(caseData.getCcdCaseReference()).bundleConfiguration(
                    bundleConfigFileName)
                .trialDocuments(mapTrialDocuments(caseData))
                .statementsOfCaseDocuments(mapStatementOfcaseDocs(caseData))
                .directionsQuestionnaires(mapDq(caseData))
                .ordersDocuments(mapOrdersDocument(caseData))
                .claimant1WitnessStatements(mapWitnessStatements(caseData, PartyType.CLAIMANT1))
                .claimant2WitnessStatements(mapWitnessStatements(caseData, PartyType.CLAIMANT2))
                .defendant1WitnessStatements(mapWitnessStatements(caseData, PartyType.DEFENDANT1))
                .defendant2WitnessStatements(mapWitnessStatements(caseData, PartyType.DEFENDANT2))
                .claimant1ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.CLAIMANT1))
                .claimant2ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.CLAIMANT2))
                .defendant1ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.DEFENDANT1))
                .defendant2ExpertEvidence(mapExpertEvidenceDocs(caseData, PartyType.DEFENDANT2))
                .jointStatementOfExperts(mapJointStatementOfExperts(caseData))
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
                .hearingDate(null != caseData.getHearingDate()
                                 ? DateFormatHelper.formatLocalDate(caseData.getHearingDate(), "dd-MM-yyyy") : null)
                .ccdCaseReference(caseData.getCcdCaseReference())
                .build();
        return mapRespondent2Applicant2Details(bundlingCaseData, caseData);
    }

    private List<BundlingRequestDocument> mapParticularsOfClaimDocs(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (Objects.nonNull(caseData.getServedDocumentFiles())
            && Objects.nonNull((caseData.getServedDocumentFiles().getParticularsOfClaimDocument()))) {
            caseData.getServedDocumentFiles()
                .getParticularsOfClaimDocument()
                .forEach(poc -> bundlingRequestDocuments.add(
                    buildBundlingRequestDoc(documentsRetrievalUtils.getParticularsOfClaimName(caseData),
                                            poc.getValue(), ""
                    )));
        }
        return bundlingRequestDocuments;
    }

    private List<Element<BundlingRequestDocument>> mapJointStatementOfExperts(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            Set<String> allJointExpertsNames = documentsRetrievalUtils.getAllExpertsNames(
                partyType,
                EvidenceUploadFiles.JOINT_STATEMENT,
                caseData
            );
            bundlingRequestDocuments.addAll(documentsRetrievalUtils.getAllExpertReports(
                partyType,
                EvidenceUploadFiles.JOINT_STATEMENT,
                caseData,
                BundleFileNameList.JOINT_STATEMENTS_OF_EXPERTS,
                allJointExpertsNames
            ));
            bundlingRequestDocuments.addAll(documentsRetrievalUtils.getAllOtherPartyQuestions(partyType, caseData,
                                                                                              allJointExpertsNames
            ));
            bundlingRequestDocuments.addAll(documentsRetrievalUtils.getAllExpertReports(
                partyType,
                EvidenceUploadFiles.ANSWERS_FOR_EXPERTS,
                caseData,
                BundleFileNameList.REPLIES_FROM,
                allJointExpertsNames
            ));
        });

        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private String generateFileName(CaseData caseData) {
        String applicantName = caseData.getApplicant1().isIndividual()
            ? caseData.getApplicant1().getIndividualLastName() : caseData.getApplicant1().getPartyName();
        String respondentName = caseData.getRespondent1().isIndividual()
            ? caseData.getRespondent1().getIndividualLastName() : caseData.getRespondent1().getPartyName();
        return applicantName + " v " + respondentName +
            "-" + DateFormatHelper.formatLocalDate(caseData.getHearingDate(), "ddMMyyyy");
    }

    private List<Element<BundlingRequestDocument>> mapCostBudgetDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            getEvidenceUploadDocsByPartyAndDocType(
                partyType,
                EvidenceUploadFiles.COSTS,
                caseData
            ),
            DOC_FILE_NAME_WITH_DATE,
            EvidenceUploadFiles.COSTS.name(),
            partyType
        ));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapDisclosedDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                   EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE, caseData
            ),
            DOC_FILE_NAME,
            EvidenceUploadFiles.DOCUMENTS_FOR_DISCLOSURE.name(),
            partyType
        ));
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialList =
            getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadFiles.DOCUMENTARY, caseData);

        if (documentEvidenceForTrialList != null) {
            bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                documentsRetrievalUtils.getDocumentaryEvidenceByType(
                    documentEvidenceForTrialList,
                    TypeOfDocDocumentaryEvidenceOfTrial.getAllDocsDisplayNames(),
                    true
                ),
                DOC_FILE_NAME,
                EvidenceUploadFiles.DOCUMENTARY.name(),
                partyType
            ));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapExpertEvidenceDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Set<String> allExpertsNames = documentsRetrievalUtils.getAllExpertsNames(
            partyType,
            EvidenceUploadFiles.EXPERT_REPORT,
            caseData
        );
        Set<String> allJointExpertsNames = documentsRetrievalUtils.getAllExpertsNames(
            partyType,
            EvidenceUploadFiles.JOINT_STATEMENT,
            caseData
        );
        bundlingRequestDocuments.addAll(documentsRetrievalUtils.getAllExpertReports(
            partyType,
            EvidenceUploadFiles.EXPERT_REPORT,
            caseData,
            BundleFileNameList.EXPERT_EVIDENCE,
            allExpertsNames
        ));
        bundlingRequestDocuments.addAll(documentsRetrievalUtils.getAllOtherPartyQuestions(partyType,
                                                                                          caseData, allExpertsNames
        ));
        bundlingRequestDocuments.addAll(documentsRetrievalUtils.getAllExpertReports(
            partyType,
            EvidenceUploadFiles.ANSWERS_FOR_EXPERTS,
            caseData,
            BundleFileNameList.REPLIES_FROM,
            allExpertsNames
        ));
        bundlingRequestDocuments.addAll(documentsRetrievalUtils.getAllRemainingExpertQuestions(
            partyType,
            EvidenceUploadFiles.QUESTIONS_FOR_EXPERTS,
            caseData
        ));
        bundlingRequestDocuments.addAll(documentsRetrievalUtils.getAllRemainingExpertReports(
            partyType,
            EvidenceUploadFiles.ANSWERS_FOR_EXPERTS,
            caseData,
            BundleFileNameList.REPLIES_FROM,
            allExpertsNames,
            allJointExpertsNames
        ));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private boolean matchParty(String expertOptionOtherParty, Party party) {
        if (party != null && party.getPartyName() != null && expertOptionOtherParty.equalsIgnoreCase(party.getPartyName())) {
            return true;
        }
        return party != null && party.isIndividual() && party.getIndividualFirstName() != null && expertOptionOtherParty.equalsIgnoreCase(
            party.getIndividualFirstName());
    }

    public Map<String, List<Element<UploadEvidenceExpert>>> groupExpertStatementsByName(
        List<Element<UploadEvidenceExpert>> documentExpertReport) {
        Map<String, List<Element<UploadEvidenceExpert>>> expertStatementMap = new TreeMap<>();
        if (documentExpertReport != null) {
            expertStatementMap = documentExpertReport.stream().collect(Collectors
                                                                           .groupingBy(uploadEvidenceExpertElement -> uploadEvidenceExpertElement
                                                                               .getValue().getExpertOptionName().trim().toLowerCase()));
        }
        return expertStatementMap;
    }

    private List<Element<BundlingRequestDocument>> mapWitnessStatements(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Party party = documentsRetrievalUtils.getPartyByPartyType(partyType, caseData);
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
            groupWitnessStatementsByName(getWitnessDocsByPartyAndDocType(
                partyType,
                EvidenceUploadFiles.WITNESS_STATEMENT,
                caseData
            ));
        List<Element<UploadEvidenceWitness>> witnessStatementSelf = documentsRetrievalUtils.getSelfStatement(
            witnessStatmentsMap,
            party
        );
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            witnessStatementSelf,
            BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
            EvidenceUploadFiles.WITNESS_STATEMENT.name(),
            partyType,
            true
        ));
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertOtherWitnessEvidenceToBundleRequestDocs(
            witnessStatmentsMap,
            BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
            EvidenceUploadFiles.WITNESS_STATEMENT.name(),
            party
        ));
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            getWitnessDocsByPartyAndDocType(partyType, EvidenceUploadFiles.WITNESS_SUMMARY, caseData),
            BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
            EvidenceUploadFiles.WITNESS_SUMMARY.name(),
            partyType,
            false
        ));
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            getEvidenceUploadDocsByPartyAndDocType(
                partyType,
                EvidenceUploadFiles.DOCUMENTS_REFERRED,
                caseData
            ),
            BundleFileNameList.DOC_REFERRED_TO.getDisplayName(),
            EvidenceUploadFiles.DOCUMENTS_REFERRED.name(),
            partyType
        ));
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            getWitnessDocsByPartyAndDocType(
                partyType, EvidenceUploadFiles.NOTICE_OF_INTENTION, caseData),
            BundleFileNameList.HEARSAY_NOTICE.getDisplayName(),
            EvidenceUploadFiles.NOTICE_OF_INTENTION.name(),
            partyType,
            false
        ));
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial = getEvidenceUploadDocsByPartyAndDocType(
            partyType,
            EvidenceUploadFiles.DOCUMENTARY,
            caseData
        );
        if (documentEvidenceForTrial != null) {
            bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                documentsRetrievalUtils.getDocumentaryEvidenceByType(
                    documentEvidenceForTrial,
                    TypeOfDocDocumentaryEvidenceOfTrial.NOTICE_TO_ADMIT_FACTS.getDisplayNames(),
                    false
                ),
                BundleFileNameList.NOTICE_TO_ADMIT_FACTS.getDisplayName(),
                TypeOfDocDocumentaryEvidenceOfTrial.NOTICE_TO_ADMIT_FACTS.name(),
                partyType
            ));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    public void sortWitnessListByDate(List<Element<UploadEvidenceWitness>> witnessEvidence,
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
    }

    public void sortEvidenceUploadByDate(List<Element<UploadEvidenceDocumentType>> uploadEvidenceDocType,
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
    }

    public void sortExpertListByDate(List<Element<UploadEvidenceExpert>> expertEvidence) {
        expertEvidence.sort(Comparator.comparing(
            uploadEvidenceExpertElement -> uploadEvidenceExpertElement.getValue().getExpertOptionUploadDate(),
            Comparator.reverseOrder()
        ));
    }

    public Map<String, List<Element<UploadEvidenceWitness>>> groupWitnessStatementsByName(
        List<Element<UploadEvidenceWitness>> witnessStatement) {
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatementMap = new HashMap<>();
        if (witnessStatement != null) {
            witnessStatementMap = witnessStatement.stream().collect(Collectors.groupingBy(uploadEvidenceWitnessElement -> uploadEvidenceWitnessElement
                .getValue().getWitnessOptionName().trim().toLowerCase()));
        }
        return witnessStatementMap;
    }

    private List<Element<BundlingRequestDocument>> mapOrdersDocument(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(
            caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                    .equals(DocumentType.DEFAULT_JUDGMENT_SDO_ORDER)).toList(),
            BundleFileNameList.DIRECTIONS_ORDER.getDisplayName()
        ));
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(
            caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                    .equals(DocumentType.SDO_ORDER)).toList(),
            BundleFileNameList.DIRECTIONS_ORDER.getDisplayName()
        ));
        if (caseData.getGeneralOrderDocStaff() != null) {
            bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(
                caseData.getGeneralOrderDocStaff(),
                BundleFileNameList.ORDER.getDisplayName()
            ));
        }
        if (caseData.getDismissalOrderDocStaff() != null) {
            bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(
                caseData.getDismissalOrderDocStaff(),
                BundleFileNameList.ORDER.getDisplayName()
            ));
        }
        if (caseData.getDirectionOrderDocStaff() != null) {
            bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(
                caseData.getDirectionOrderDocStaff(),
                BundleFileNameList.ORDER.getDisplayName()
            ));
        }

        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapDq(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(documentsRetrievalUtils.getDqByCategoryId(
            caseData,
            DocCategory.APP1_DQ.getValue(),
            PartyType.CLAIMANT1
        ));
        if (featureToggleService.isCaseProgressionEnabled()) {
            bundlingRequestDocuments.addAll(documentsRetrievalUtils.getDqByCategoryId(
                caseData,
                DocCategory.DQ_APP1.getValue(),
                PartyType.CLAIMANT1
            ));
        }
        bundlingRequestDocuments.addAll(documentsRetrievalUtils.getDqByCategoryId(
            caseData,
            DocCategory.DEF1_DEFENSE_DQ.getValue(),
            PartyType.DEFENDANT1
        ));
        if (featureToggleService.isCaseProgressionEnabled()) {
            bundlingRequestDocuments.addAll(documentsRetrievalUtils.getDqByCategoryId(
                caseData,
                DocCategory.DQ_DEF1.getValue(),
                PartyType.DEFENDANT1
            ));
        }
        bundlingRequestDocuments.addAll(documentsRetrievalUtils.getDqByCategoryId(
            caseData,
            DocCategory.DEF2_DEFENSE_DQ.getValue(),
            PartyType.DEFENDANT2
        ));

        bundlingRequestDocuments.addAll(getDqWithNoCategoryId(caseData));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapStatementOfcaseDocs(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(
            caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                    .equals(DocumentType.SEALED_CLAIM)
                    && null != caseDocumentElement.getValue().getDocumentLink().getCategoryID()
                    && caseDocumentElement.getValue().getDocumentLink().getCategoryID().equals("detailsOfClaim"))
                .collect(Collectors.toList()),
            BundleFileNameList.CLAIM_FORM.getDisplayName()
        ));
        bundlingRequestDocuments.addAll(mapParticularsOfClaimDocs(caseData));
        List<Element<CaseDocument>> clAndDfDocList = caseData.getDefendantResponseDocuments();
        clAndDfDocList.addAll(caseData.getClaimantResponseDocuments());
        List<Element<CaseDocument>> sortedDefendantDefenceAndClaimantReply =
            documentsRetrievalUtils.getSortedDefendantDefenceAndClaimantReply(clAndDfDocList);
        sortedDefendantDefenceAndClaimantReply.forEach(caseDocumentElement -> {
            String docType = caseDocumentElement.getValue().getDocumentType().equals(DocumentType.DEFENDANT_DEFENCE)
                ? BundleFileNameList.DEFENCE.getDisplayName() : BundleFileNameList.CL_REPLY.getDisplayName();
            String party;
            if (caseDocumentElement.getValue().getCreatedBy().equalsIgnoreCase("Defendant")) {
                party = PartyType.DEFENDANT1.getDisplayName();
            } else if (caseDocumentElement.getValue().getCreatedBy().equalsIgnoreCase("Defendant 2")) {
                party = PartyType.DEFENDANT2.getDisplayName();
            } else {
                party = "";
            }
            String docName = generateDocName(docType, party, null,
                                             caseDocumentElement.getValue().getCreatedDatetime().toLocalDate()
            );
            bundlingRequestDocuments.add(buildBundlingRequestDoc(
                docName,
                caseDocumentElement.getValue().getDocumentLink(),
                docType
            ));
        });
        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
                                                               bundlingRequestDocuments.addAll(
                                                                   conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                                                                       documentsRetrievalUtils.getDocumentaryEvidenceByType(
                                                                           getEvidenceUploadDocsByPartyAndDocType(
                                                                               partyType,
                                                                               EvidenceUploadFiles.DOCUMENTARY,
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
                                                                       documentsRetrievalUtils.getDocumentaryEvidenceByType(
                                                                           getEvidenceUploadDocsByPartyAndDocType(
                                                                               partyType,
                                                                               EvidenceUploadFiles.DOCUMENTARY,
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

    private List<Element<BundlingRequestDocument>> mapTrialDocuments(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
                                                               bundlingRequestDocuments.addAll(
                                                                   conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                                                                       getEvidenceUploadDocsByPartyAndDocType(
                                                                           partyType,
                                                                           EvidenceUploadFiles.CASE_SUMMARY,
                                                                           caseData
                                                                       ),
                                                                       BundleFileNameList.CASE_SUMMARY_FILE_DISPLAY_NAME.getDisplayName(),
                                                                       EvidenceUploadFiles.CASE_SUMMARY.name(),
                                                                       partyType
                                                                   ))
        );
        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
                                                               bundlingRequestDocuments.addAll(
                                                                   conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                                                                       documentsRetrievalUtils.getDocumentaryEvidenceByType(
                                                                           getEvidenceUploadDocsByPartyAndDocType(
                                                                               partyType,
                                                                               EvidenceUploadFiles.DOCUMENTARY,
                                                                               caseData
                                                                           ),
                                                                           TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.getDisplayNames(),
                                                                           false
                                                                       ),
                                                                       BundleFileNameList.CHRONOLOGY_FILE_DISPLAY_NAME.getDisplayName(),
                                                                       TypeOfDocDocumentaryEvidenceOfTrial.CHRONOLOGY.name(),
                                                                       partyType
                                                                   ))
        );
        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
                                                               bundlingRequestDocuments.addAll(
                                                                   conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                                                                       documentsRetrievalUtils.getDocumentaryEvidenceByType(
                                                                           getEvidenceUploadDocsByPartyAndDocType(
                                                                               partyType,
                                                                               EvidenceUploadFiles.DOCUMENTARY,
                                                                               caseData
                                                                           ),
                                                                           TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.getDisplayNames(),
                                                                           false
                                                                       ),
                                                                       BundleFileNameList.TRIAL_TIMETABLE_FILE_DISPLAY_NAME.getDisplayName(),
                                                                       TypeOfDocDocumentaryEvidenceOfTrial.TIMETABLE.name(),
                                                                       partyType
                                                                   ))
        );
        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
                                                               bundlingRequestDocuments.addAll(
                                                                   conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                                                                       getEvidenceUploadDocsByPartyAndDocType(
                                                                           partyType,
                                                                           EvidenceUploadFiles.SKELETON_ARGUMENT,
                                                                           caseData
                                                                       ),
                                                                       BundleFileNameList.SKELETON_ARGUMENT.getDisplayName(),
                                                                       EvidenceUploadFiles.SKELETON_ARGUMENT.name(),
                                                                       partyType
                                                                   ))
        );
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    // public List<Element<UploadEvidenceDocumentType>> filterDocumentaryEvidenceForTrialDocs(
    //     List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial, List<String> displayNames,
    //     boolean doesNotMatchType) {
    //     sortEvidenceUploadByDate(documentEvidenceForTrial, false);
    //     return documentEvidenceForTrial.stream().filter(uploadEvidenceDocumentTypeElement -> matchType(
    //         uploadEvidenceDocumentTypeElement.getValue().getTypeOfDocument(),
    //         displayNames, doesNotMatchType
    //     )).collect(Collectors.toList());
    // }

    public List<BundlingRequestDocument> getDqWithNoCategoryId(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(mapSystemGeneratedCaseDocument(
            caseData.getSystemGeneratedCaseDocuments().stream()
                .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentType()
                    .equals(DocumentType.DIRECTIONS_QUESTIONNAIRE)
                    && caseDocumentElement.getValue().getDocumentLink().getCategoryID() == null)
                .collect(Collectors.toList()),
            BundleFileNameList.DIRECTIONS_QUESTIONNAIRE_NO_CATEGORY_ID.getDisplayName()
        ));
        return bundlingRequestDocuments;
    }

    private boolean matchType(String name, Collection<String> displayNames, boolean doesNotMatchType) {
        if (doesNotMatchType) {
            return displayNames.stream().noneMatch(s -> s.equalsIgnoreCase(name.trim()));
        } else {
            return displayNames.stream().anyMatch(s -> s.equalsIgnoreCase(name.trim()));
        }
    }

    public String generateDocName(String fileName, String strParam, String strParam2, LocalDate date) {
        if (StringUtils.isBlank(strParam)) {
            return String.format(fileName, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        } else if (StringUtils.isBlank(strParam2)) {
            return String.format(fileName, strParam, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        } else {
            return String.format(fileName, strParam, strParam2, DateFormatHelper.formatLocalDate(date, DATE_FORMAT));
        }
    }

    public String getFileNameBaseOnType(String fileNamePrefix, Element<UploadEvidenceDocumentType> uploadEvidence,
                                        String documentType, PartyType party) {
        if (fileNamePrefix.equals(DOC_FILE_NAME)) {
            return uploadEvidence.getValue().getDocumentUpload().getDocumentFileName()
                .substring(0, uploadEvidence.getValue().getDocumentUpload().getDocumentFileName().lastIndexOf("."));
        } else if (fileNamePrefix.equals(DOC_FILE_NAME_WITH_DATE)) {
            return generateDocName(uploadEvidence.getValue().getDocumentUpload().getDocumentFileName()
                                       .substring(
                                           0,
                                           uploadEvidence.getValue().getDocumentUpload().getDocumentFileName().lastIndexOf(
                                               ".")
                                       ) + " %s", null,
                                   null,
                                   documentType.equals(EvidenceUploadFiles.COSTS.name())
                                       ? uploadEvidence.getValue().getCreatedDatetime().toLocalDate() :
                                       uploadEvidence.getValue().getDocumentIssuedDate()
            );
        } else {
            String partyName = party.getDisplayName();
            if (fileNamePrefix.equals(BundleFileNameList.SCHEDULE_OF_LOSS_FILE_DISPLAY_NAME.getDisplayName())
                && (party.equals(PartyType.DEFENDANT1) || party.equals(PartyType.DEFENDANT2))) {
                partyName = partyName.concat(" counter");
            }
            if (documentType.equals(EvidenceUploadFiles.DOCUMENTS_REFERRED.name())) {
                return getEvidenceUploadTypeWithNameFileName(fileNamePrefix, uploadEvidence.getValue());
            } else {
                return generateDocName(fileNamePrefix, partyName, null,
                                       documentType.equals(EvidenceUploadFiles.CASE_SUMMARY.name()) || documentType.equals(
                                           EvidenceUploadFiles.SKELETON_ARGUMENT.name())
                                           ? uploadEvidence.getValue().getCreatedDatetime().toLocalDate() :
                                           uploadEvidence.getValue().getDocumentIssuedDate()
                );
            }
        }
    }

    private String getEvidenceUploadTypeWithNameFileName(String body, UploadEvidenceDocumentType uploadEvidence) {
        return String.format(body, uploadEvidence.getTypeOfDocument(),
                             uploadEvidence.getWitnessOptionName(),
                             uploadEvidence.getDocumentIssuedDate()
                                 .format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK))
        );
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

    public List<BundlingRequestDocument> mapSystemGeneratedCaseDocument(List<Element<CaseDocument>> systemGeneratedCaseDocuments, String displayName) {
        List<BundlingRequestDocument> bundlingSystemGeneratedCaseDocs = new ArrayList<>();
        if (systemGeneratedCaseDocuments != null) {
            systemGeneratedCaseDocuments.forEach(caseDocumentElement -> {
                String docName = generateDocName(displayName, null, null,
                                                 caseDocumentElement.getValue().getCreatedDatetime().toLocalDate()
                );
                bundlingSystemGeneratedCaseDocs.add(buildBundlingRequestDoc(
                    docName,
                    caseDocumentElement.getValue().getDocumentLink(),
                    caseDocumentElement.getValue().getDocumentType().name()
                ));
            });
        }
        return bundlingSystemGeneratedCaseDocs;
    }

    public BundlingRequestDocument buildBundlingRequestDoc(String docName, Document document, String docType) {
        return BundlingRequestDocument.builder()
            .documentFileName(docName)
            .documentType(docType)
            .documentLink(DocumentLink.builder()
                              .documentUrl(document.getDocumentUrl())
                              .documentBinaryUrl(document.getDocumentBinaryUrl())
                              .documentFilename(document.getDocumentFileName()).build())
            .build();
    }
}
