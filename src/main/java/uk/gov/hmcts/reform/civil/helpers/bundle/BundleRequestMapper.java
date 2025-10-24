package uk.gov.hmcts.reform.civil.helpers.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.enums.caseprogression.TypeOfDocDocumentaryEvidenceOfTrial;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DocumentWithRegex;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getEvidenceUploadDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleFileNameHelper.getWitnessDocsByPartyAndDocType;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.buildBundlingRequestDoc;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.generateDocName;

@Slf4j
@Service
@RequiredArgsConstructor
public class BundleRequestMapper {

    private static final String DOC_FILE_NAME = "DOC_FILE_NAME";
    private static final String DOC_FILE_NAME_WITH_DATE = "DOC_FILE_NAME %s";

    private final BundleDocumentsRetrieval bundleDocumentsRetrieval;
    private final ConversionToBundleRequestDocs conversionToBundleRequestDocs;
    private final FeatureToggleService featureToggleService;
    private final BundleRequestDocsOrganizer bundleRequestDocsOrganizer;

    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData,
                                                                String bundleConfigFileName, String jurisdiction,
                                                                String caseTypeId) {
        log.info("Mapping case data to BundleCreateRequest for case ID: {}", caseData.getCcdCaseReference());
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
        }
        return bundlingRequestDocuments;
    }

    private List<Element<BundlingRequestDocument>> mapJointStatementOfExperts(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Arrays.stream(PartyType.values()).toList().forEach(partyType -> {
            Set<String> allJointExpertsNames = bundleDocumentsRetrieval.getAllExpertsNames(
                partyType,
                EvidenceUploadType.JOINT_STATEMENT,
                caseData
            );
            bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllExpertReports(
                partyType,
                EvidenceUploadType.JOINT_STATEMENT,
                caseData,
                BundleFileNameList.JOINT_STATEMENTS_OF_EXPERTS,
                allJointExpertsNames
            ));
            bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllOtherPartyQuestions(partyType, caseData,
                                                                                               allJointExpertsNames
            ));
            bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllExpertReports(
                partyType,
                EvidenceUploadType.ANSWERS_FOR_EXPERTS,
                caseData,
                BundleFileNameList.REPLIES_FROM,
                allJointExpertsNames
            ));
        });

        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private String generateFileName(CaseData caseData) {
        log.debug("Generating file name for case ID: {}", caseData.getCcdCaseReference());
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
                EvidenceUploadType.COSTS,
                caseData
            ),
            DOC_FILE_NAME_WITH_DATE,
            EvidenceUploadType.COSTS.name(),
            partyType
        ));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapDisclosedDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            getEvidenceUploadDocsByPartyAndDocType(partyType,
                                                   EvidenceUploadType.DOCUMENTS_FOR_DISCLOSURE, caseData
            ),
            DOC_FILE_NAME,
            EvidenceUploadType.DOCUMENTS_FOR_DISCLOSURE.name(),
            partyType
        ));
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialList =
            getEvidenceUploadDocsByPartyAndDocType(partyType, EvidenceUploadType.DOCUMENTARY, caseData);

        if (documentEvidenceForTrialList != null) {
            bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
                    documentEvidenceForTrialList,
                    TypeOfDocDocumentaryEvidenceOfTrial.getAllDocsDisplayNames(),
                    true
                ),
                DOC_FILE_NAME,
                EvidenceUploadType.DOCUMENTARY.name(),
                partyType
            ));
        }
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapExpertEvidenceDocs(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Set<String> allExpertsNames = bundleDocumentsRetrieval.getAllExpertsNames(
            partyType,
            EvidenceUploadType.EXPERT_REPORT,
            caseData
        );
        Set<String> allJointExpertsNames = bundleDocumentsRetrieval.getAllExpertsNames(
            partyType,
            EvidenceUploadType.JOINT_STATEMENT,
            caseData
        );
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllExpertReports(
            partyType,
            EvidenceUploadType.EXPERT_REPORT,
            caseData,
            BundleFileNameList.EXPERT_EVIDENCE,
            allExpertsNames
        ));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllOtherPartyQuestions(partyType,
                                                                                           caseData, allExpertsNames
        ));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllExpertReports(
            partyType,
            EvidenceUploadType.ANSWERS_FOR_EXPERTS,
            caseData,
            BundleFileNameList.REPLIES_FROM,
            allExpertsNames
        ));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllRemainingExpertQuestions(
            partyType,
            EvidenceUploadType.QUESTIONS_FOR_EXPERTS,
            caseData
        ));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getAllRemainingExpertReports(
            partyType,
            EvidenceUploadType.ANSWERS_FOR_EXPERTS,
            caseData,
            BundleFileNameList.REPLIES_FROM,
            allExpertsNames,
            allJointExpertsNames
        ));
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

    private List<Element<BundlingRequestDocument>> mapWitnessStatements(CaseData caseData, PartyType partyType) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Party party = bundleDocumentsRetrieval.getPartyByPartyType(partyType, caseData);
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap =
            bundleRequestDocsOrganizer.groupWitnessStatementsByName(getWitnessDocsByPartyAndDocType(
                partyType,
                EvidenceUploadType.WITNESS_STATEMENT,
                caseData
            ));
        List<Element<UploadEvidenceWitness>> witnessStatementSelf = bundleDocumentsRetrieval.getSelfStatement(
            witnessStatmentsMap,
            party
        );
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            witnessStatementSelf,
            BundleFileNameList.WITNESS_STATEMENT_DISPLAY_NAME.getDisplayName(),
            EvidenceUploadType.WITNESS_STATEMENT.name(),
            partyType,
            true
        ));
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertOtherWitnessEvidenceToBundleRequestDocs(
            witnessStatmentsMap,
            BundleFileNameList.WITNESS_STATEMENT_OTHER_DISPLAY_NAME.getDisplayName(),
            EvidenceUploadType.WITNESS_STATEMENT.name(),
            party
        ));
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            getWitnessDocsByPartyAndDocType(partyType, EvidenceUploadType.WITNESS_SUMMARY, caseData),
            BundleFileNameList.WITNESS_SUMMARY.getDisplayName(),
            EvidenceUploadType.WITNESS_SUMMARY.name(),
            partyType,
            false
        ));
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
            getEvidenceUploadDocsByPartyAndDocType(
                partyType,
                EvidenceUploadType.DOCUMENTS_REFERRED,
                caseData
            ),
            BundleFileNameList.DOC_REFERRED_TO.getDisplayName(),
            EvidenceUploadType.DOCUMENTS_REFERRED.name(),
            partyType
        ));
        bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertWitnessEvidenceToBundleRequestDocs(
            getWitnessDocsByPartyAndDocType(
                partyType, EvidenceUploadType.NOTICE_OF_INTENTION, caseData),
            BundleFileNameList.HEARSAY_NOTICE.getDisplayName(),
            EvidenceUploadType.NOTICE_OF_INTENTION.name(),
            partyType,
            false
        ));
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial = getEvidenceUploadDocsByPartyAndDocType(
            partyType,
            EvidenceUploadType.DOCUMENTARY,
            caseData
        );
        if (documentEvidenceForTrial != null) {
            bundlingRequestDocuments.addAll(conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
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
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(caseData,
                                                          DocCategory.APP1_DQ.getValue(), PartyType.CLAIMANT1));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(caseData,
                                                          DocCategory.DQ_APP1.getValue(), PartyType.CLAIMANT1));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(caseData,
                                                          DocCategory.DEF1_DEFENSE_DQ.getValue(), PartyType.DEFENDANT1));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(caseData,
                                                          DocCategory.DQ_DEF1.getValue(), PartyType.DEFENDANT1));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(caseData,
                                                          DocCategory.DEF2_DEFENSE_DQ.getValue(), PartyType.DEFENDANT2));
        bundlingRequestDocuments.addAll(bundleDocumentsRetrieval.getDqByCategoryId(caseData,
                                                          DocCategory.DQ_DEF2.getValue(), PartyType.DEFENDANT2));

        bundlingRequestDocuments.addAll(getDqWithNoCategoryId(caseData));
        return ElementUtils.wrapElements(bundlingRequestDocuments.stream()
            .distinct()
            .collect(Collectors.toList()));
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
        List<Element<CaseDocument>> sortedDefendantDefenceAndClaimantReply =
            bundleDocumentsRetrieval.getSortedDefendantDefenceAndClaimantReply(
                collectDefenceAndReplyDocuments(caseData)
            );
        sortedDefendantDefenceAndClaimantReply.forEach(caseDocumentElement -> {
            CaseDocument caseDocument = caseDocumentElement.getValue();
            StatementOfCaseDocumentType statementDocType = resolveStatementDocumentType(caseDocument);
            if (statementDocType == StatementOfCaseDocumentType.NONE) {
                return;
            }
            String docType = statementDocType == StatementOfCaseDocumentType.DEFENCE
                ? BundleFileNameList.DEFENCE.getDisplayName() : BundleFileNameList.CL_REPLY.getDisplayName();
            String createdBy = caseDocument.getCreatedBy() == null ? "" : caseDocument.getCreatedBy();
            String party;
            if (createdBy.equalsIgnoreCase("Defendant")) {
                party = PartyType.DEFENDANT1.getDisplayName();
            } else if (createdBy.equalsIgnoreCase("Defendant 2")) {
                party = PartyType.DEFENDANT2.getDisplayName();
            } else if (statementDocType == StatementOfCaseDocumentType.DEFENCE) {
                party = PartyType.DEFENDANT1.getDisplayName();
            } else {
                party = "";
            }
            String docName = generateDocName(docType, party, null,
                                             caseDocument.getCreatedDatetime().toLocalDate()
            );
            bundlingRequestDocuments.add(buildBundlingRequestDoc(
                docName,
                caseDocument.getDocumentLink(),
                docType
            ));
        });
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

    private List<Element<CaseDocument>> collectDefenceAndReplyDocuments(CaseData caseData) {
        Map<String, Element<CaseDocument>> uniqueDocuments = new LinkedHashMap<>();

        Stream.of(
                caseData.getDefendantResponseDocuments(),
                caseData.getClaimantResponseDocuments(),
                caseData.getDuplicateClaimantDefendantResponseDocs(),
                caseData.getSystemGeneratedCaseDocuments(),
                caseData.getDuplicateSystemGeneratedCaseDocs()
            )
            .filter(Objects::nonNull)
            .forEach(source -> source.stream()
                .forEach(element -> addDefenceDocument(uniqueDocuments, element)));

        Stream.of(
                caseData.getRespondent1GeneratedResponseDocument(),
                caseData.getRespondent2GeneratedResponseDocument(),
                caseData.getRespondent1ClaimResponseDocumentSpec(),
                caseData.getRespondent2ClaimResponseDocumentSpec()
            )
            .filter(Objects::nonNull)
            .forEach(caseDocument -> addDefenceDocument(uniqueDocuments, ElementUtils.<CaseDocument>element(caseDocument)));

        return new ArrayList<>(uniqueDocuments.values());
    }

    private void addDefenceDocument(Map<String, Element<CaseDocument>> uniqueDocuments,
                                    Element<CaseDocument> element) {
        if (element == null || element.getValue() == null) {
            return;
        }
        CaseDocument caseDocument = element.getValue();
        StatementOfCaseDocumentType statementDocType = resolveStatementDocumentType(caseDocument);
        if (statementDocType == StatementOfCaseDocumentType.NONE) {
            return;
        }
        Document documentLink = caseDocument.getDocumentLink();
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(statementDocType.name());
        keyBuilder.append(":");
        if (documentLink != null && documentLink.getDocumentUrl() != null) {
            keyBuilder.append(documentLink.getDocumentUrl());
        } else if (element.getId() != null) {
            keyBuilder.append(element.getId());
        } else if (caseDocument.getDocumentName() != null) {
            keyBuilder.append(caseDocument.getDocumentName());
        }
        uniqueDocuments.putIfAbsent(keyBuilder.toString(), element);
    }

    private StatementOfCaseDocumentType resolveStatementDocumentType(CaseDocument caseDocument) {
        if (caseDocument.getDocumentType() == DocumentType.DEFENDANT_DEFENCE) {
            return StatementOfCaseDocumentType.DEFENCE;
        }
        if (caseDocument.getDocumentType() == DocumentType.CLAIMANT_DEFENCE) {
            return StatementOfCaseDocumentType.CLAIMANT_REPLY;
        }
        if (caseDocument.getDocumentType() == DocumentType.SEALED_CLAIM && isMisCategorisedDefence(caseDocument)) {
            return StatementOfCaseDocumentType.DEFENCE;
        }
        return StatementOfCaseDocumentType.NONE;
    }

    private boolean isMisCategorisedDefence(CaseDocument caseDocument) {
        Document documentLink = caseDocument.getDocumentLink();
        String categoryId = documentLink != null ? documentLink.getCategoryID() : null;
        if (categoryId != null && isDefenceCategory(categoryId)) {
            return true;
        }
        String fileName = documentLink != null ? documentLink.getDocumentFileName() : caseDocument.getDocumentName();
        if (fileName != null && fileName.toLowerCase().contains("response")) {
            return true;
        }
        return false;
    }

    private boolean isDefenceCategory(String categoryId) {
        return DocCategory.DEF1_DEFENSE_DQ.getValue().equals(categoryId)
            || DocCategory.DQ_DEF1.getValue().equals(categoryId)
            || DocCategory.DEF2_DEFENSE_DQ.getValue().equals(categoryId)
            || DocCategory.DQ_DEF2.getValue().equals(categoryId);
    }

    private enum StatementOfCaseDocumentType {
        DEFENCE,
        CLAIMANT_REPLY,
        NONE
    }

    private List<Element<BundlingRequestDocument>> mapTrialDocuments(CaseData caseData) {
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        Arrays.stream(PartyType.values()).toList().forEach(partyType ->
                                                               bundlingRequestDocuments.addAll(
                                                                   conversionToBundleRequestDocs.covertEvidenceUploadTypeToBundleRequestDocs(
                                                                       getEvidenceUploadDocsByPartyAndDocType(
                                                                           partyType,
                                                                           EvidenceUploadType.CASE_SUMMARY,
                                                                           caseData
                                                                       ),
                                                                       BundleFileNameList.CASE_SUMMARY_FILE_DISPLAY_NAME.getDisplayName(),
                                                                       EvidenceUploadType.CASE_SUMMARY.name(),
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
                                                                       bundleDocumentsRetrieval.getDocumentaryEvidenceByType(
                                                                           getEvidenceUploadDocsByPartyAndDocType(
                                                                               partyType,
                                                                               EvidenceUploadType.DOCUMENTARY,
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
                                                                           EvidenceUploadType.SKELETON_ARGUMENT,
                                                                           caseData
                                                                       ),
                                                                       BundleFileNameList.SKELETON_ARGUMENT.getDisplayName(),
                                                                       EvidenceUploadType.SKELETON_ARGUMENT.name(),
                                                                       partyType
                                                                   ))
        );
        return ElementUtils.wrapElements(bundlingRequestDocuments);
    }

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
}
