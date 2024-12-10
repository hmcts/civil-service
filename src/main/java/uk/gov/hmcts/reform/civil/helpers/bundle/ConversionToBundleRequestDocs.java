package uk.gov.hmcts.reform.civil.helpers.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.BundleFileNameList;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadType;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.buildBundlingRequestDoc;
import static uk.gov.hmcts.reform.civil.helpers.bundle.BundleUtils.generateDocName;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConversionToBundleRequestDocs {

    private static final String DOC_FILE_NAME = "DOC_FILE_NAME";
    private static final String DOC_FILE_NAME_WITH_DATE = "DOC_FILE_NAME %s";
    private static final String DATE_FORMAT = "dd/MM/yyyy";

    private final FeatureToggleService featureToggleService;
    private final BundleRequestDocsOrganizer bundleRequestDocsOrganizer;
    private static final String UNBUNDLED_FOLDER = "UnbundledFolder";

    public List<BundlingRequestDocument> covertOtherWitnessEvidenceToBundleRequestDocs(
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap, String displayName, String documentType,
        Party party) {
        log.debug("Converting other witness evidence to bundle request docs for party {}", party);
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (party != null) {
            if (party.getPartyName() != null) {
                witnessStatmentsMap.remove(party.getPartyName().trim().toLowerCase());
            }
            if (party.isIndividual() && party.getIndividualFirstName() != null) {
                witnessStatmentsMap.remove(party.getIndividualFirstName().trim().toLowerCase());
            }
        }
        if (featureToggleService.isAmendBundleEnabled()) {
            witnessStatmentsMap.forEach((witnessName, witnessEvidence) ->
                                            witnessEvidence.stream().filter(caseDocumentElement -> caseDocumentElement.getValue().getWitnessOptionDocument().getCategoryID() != null
                                                && !caseDocumentElement.getValue().getWitnessOptionDocument().getCategoryID().equals(
                                                UNBUNDLED_FOLDER)).toList().forEach(uploadEvidenceWitnessElement -> {
                                                    String docName = generateDocName(
                                                        displayName,
                                                        uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                                                        String.valueOf(witnessEvidence.indexOf(uploadEvidenceWitnessElement) + 1),
                                                        uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate()
                                                    );
                                                    bundlingRequestDocuments.add(buildBundlingRequestDoc(
                                                        docName,
                                                        uploadEvidenceWitnessElement.getValue().getWitnessOptionDocument(),
                                                        documentType
                                                    ));
                                                })
            );
        } else {
            witnessStatmentsMap.forEach((witnessName, witnessEvidence) ->
                                            witnessEvidence.forEach(uploadEvidenceWitnessElement -> {
                                                String docName = generateDocName(
                                                    displayName,
                                                    uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                                                    String.valueOf(witnessEvidence.indexOf(uploadEvidenceWitnessElement) + 1),
                                                    uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate()
                                                );
                                                bundlingRequestDocuments.add(buildBundlingRequestDoc(
                                                    docName,
                                                    uploadEvidenceWitnessElement.getValue().getWitnessOptionDocument(),
                                                    documentType
                                                ));
                                            })
            );
        }
        return bundlingRequestDocuments;
    }

    public List<BundlingRequestDocument> covertWitnessEvidenceToBundleRequestDocs(List<Element<UploadEvidenceWitness>> witnessEvidence,
                                                                                  String fileNamePrefix,
                                                                                  String documentType,
                                                                                  PartyType party,
                                                                                  boolean isWitnessSelf) {
        log.debug("Converting witness evidence to bundle request docs for file name prefix: {} and party: {}", fileNamePrefix, party);
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (witnessEvidence != null) {
            if (featureToggleService.isAmendBundleEnabled()) {
                witnessEvidence = new ArrayList<>(witnessEvidence.stream()
                                                      .filter(caseDocumentElement -> caseDocumentElement.getValue().getWitnessOptionDocument().getCategoryID() != null
                                                          && !caseDocumentElement.getValue().getWitnessOptionDocument().getCategoryID().equals(
                                                          UNBUNDLED_FOLDER)).toList());
            }
            bundleRequestDocsOrganizer.sortWitnessListByDate(
                witnessEvidence,
                !documentType.equals(EvidenceUploadType.WITNESS_STATEMENT.name())
            );
            witnessEvidence.forEach(uploadEvidenceWitnessElement -> {
                String docName = generateDocName(
                    fileNamePrefix,
                    isWitnessSelf ? party.getDisplayName() :
                        uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                    null,
                    uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate()
                );
                bundlingRequestDocuments.add(buildBundlingRequestDoc(
                    docName,
                    uploadEvidenceWitnessElement.getValue().getWitnessOptionDocument(),
                    documentType
                ));
            });
        }
        return bundlingRequestDocuments;
    }

    public List<BundlingRequestDocument> covertEvidenceUploadTypeToBundleRequestDocs(List<Element<UploadEvidenceDocumentType>> evidenceUploadDocList,
                                                                                     String fileNamePrefix, String documentType,
                                                                                     PartyType party) {
        log.debug("Converting evidence upload type to bundle request docs for file name prefix: {} and party: {}", fileNamePrefix, party);
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (evidenceUploadDocList != null) {
            if (featureToggleService.isAmendBundleEnabled()) {
                evidenceUploadDocList = new ArrayList<>(evidenceUploadDocList.stream()
                    .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentUpload().getCategoryID() != null
                        && !caseDocumentElement.getValue().getDocumentUpload().getCategoryID().equals(
                        UNBUNDLED_FOLDER)).toList());
            }
            bundleRequestDocsOrganizer.sortEvidenceUploadByDate(
                evidenceUploadDocList,
                documentType.equals(EvidenceUploadType.CASE_SUMMARY.name())
                    || documentType.equals(EvidenceUploadType.SKELETON_ARGUMENT.name())
                    || documentType.equals(EvidenceUploadType.COSTS.name())
            );

            evidenceUploadDocList.forEach(uploadEvidenceDocumentTypeElement -> {
                String docName = getFileNameBaseOnType(
                    fileNamePrefix,
                    uploadEvidenceDocumentTypeElement,
                    documentType,
                    party
                );
                bundlingRequestDocuments.add(buildBundlingRequestDoc(
                    docName,
                    uploadEvidenceDocumentTypeElement.getValue().getDocumentUpload(),
                    documentType
                ));
            });
        }
        return bundlingRequestDocuments;
    }

    public List<BundlingRequestDocument> covertExpertEvidenceTypeToBundleRequestDocs(List<Element<UploadEvidenceExpert>> evidenceUploadExpert,
                                                                                     String fileNamePrefix, String documentType) {

        log.debug("Converting expert evidence type to bundle request docs for file name prefix: {}", fileNamePrefix);
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        if (evidenceUploadExpert != null) {
            if (featureToggleService.isAmendBundleEnabled()) {
                evidenceUploadExpert = new ArrayList<>(evidenceUploadExpert.stream()
                    .filter(caseDocumentElement -> caseDocumentElement.getValue().getExpertDocument().getCategoryID() != null
                        && !caseDocumentElement.getValue().getExpertDocument().getCategoryID().equals(
                        UNBUNDLED_FOLDER)).toList());
            }
            bundleRequestDocsOrganizer.sortExpertListByDate(evidenceUploadExpert);
            evidenceUploadExpert.forEach(expertElement -> {
                String expertise = null;
                if (documentType.equals(EvidenceUploadType.EXPERT_REPORT.name())) {
                    expertise = expertElement.getValue().getExpertOptionExpertise();
                } else if (documentType.equals(EvidenceUploadType.JOINT_STATEMENT.name())) {
                    expertise = expertElement.getValue().getExpertOptionExpertises();
                }
                String docName = generateDocName(
                    fileNamePrefix,
                    expertElement.getValue().getExpertOptionName(),
                    expertise,
                    expertElement.getValue().getExpertOptionUploadDate()

                );
                bundlingRequestDocuments.add(buildBundlingRequestDoc(
                    docName,
                    expertElement.getValue().getExpertDocument(),
                    documentType
                ));
            });
        }
        return bundlingRequestDocuments;
    }

    private String getFileNameBaseOnType(String fileNamePrefix, Element<UploadEvidenceDocumentType> uploadEvidence,
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
                documentType.equals(EvidenceUploadType.COSTS.name())
                    ? uploadEvidence.getValue().getCreatedDatetime().toLocalDate() :
                    uploadEvidence.getValue().getDocumentIssuedDate()
            );
        } else {
            String partyName = party.getDisplayName();
            if (fileNamePrefix.equals(BundleFileNameList.SCHEDULE_OF_LOSS_FILE_DISPLAY_NAME.getDisplayName())
                && (party.equals(PartyType.DEFENDANT1) || party.equals(PartyType.DEFENDANT2))) {
                partyName = partyName.concat(" counter");
            }
            if (documentType.equals(EvidenceUploadType.DOCUMENTS_REFERRED.name())) {
                return getEvidenceUploadTypeWithNameFileName(fileNamePrefix, uploadEvidence.getValue());
            } else {
                return generateDocName(fileNamePrefix, partyName, null,
                    documentType.equals(EvidenceUploadType.CASE_SUMMARY.name()) || documentType.equals(
                        EvidenceUploadType.SKELETON_ARGUMENT.name())
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
}
