package uk.gov.hmcts.reform.civil.helpers.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
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
import java.util.Objects;
import java.util.function.Function;

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
        removePartyEntries(witnessStatmentsMap, party);
        boolean amendBundleEnabled = featureToggleService.isAmendBundleEnabled();
        witnessStatmentsMap.forEach((witnessName, witnessEvidence) ->
            addOtherWitnessEvidenceDocuments(
                bundlingRequestDocuments,
                witnessEvidence,
                filterDocumentsForBundle(
                    witnessEvidence,
                    UploadEvidenceWitness::getWitnessOptionDocument,
                    amendBundleEnabled
                ),
                displayName,
                documentType
            )
        );
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
            witnessEvidence = filterDocumentsForBundle(witnessEvidence, UploadEvidenceWitness::getWitnessOptionDocument);
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
            evidenceUploadDocList = filterDocumentsForBundle(evidenceUploadDocList, UploadEvidenceDocumentType::getDocumentUpload);
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
            evidenceUploadExpert = filterDocumentsForBundle(evidenceUploadExpert, UploadEvidenceExpert::getExpertDocument);
            bundleRequestDocsOrganizer.sortExpertListByDate(evidenceUploadExpert);
            evidenceUploadExpert.forEach(expertElement -> {
                String expertise = getExpertise(documentType, expertElement.getValue());
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
        Objects.requireNonNull(fileNamePrefix);
        if (DOC_FILE_NAME.equals(fileNamePrefix)) {
            return getDocumentFileNameWithoutExtension(uploadEvidence.getValue().getDocumentUpload());
        }
        if (DOC_FILE_NAME_WITH_DATE.equals(fileNamePrefix)) {
            return generateDocName(
                getDocumentFileNameWithoutExtension(uploadEvidence.getValue().getDocumentUpload()) + " %s",
                null,
                null,
                getDateForOriginalFileName(documentType, uploadEvidence.getValue())
            );
        }
        String partyName = getPartyName(fileNamePrefix, party);
        if (documentType.equals(EvidenceUploadType.DOCUMENTS_REFERRED.name())) {
            return getEvidenceUploadTypeWithNameFileName(fileNamePrefix, uploadEvidence.getValue());
        }
        return generateDocName(
            fileNamePrefix,
            partyName,
            null,
            getDateForGeneratedFileName(documentType, uploadEvidence.getValue())
        );
    }

    private String getEvidenceUploadTypeWithNameFileName(String body, UploadEvidenceDocumentType uploadEvidence) {
        return String.format(body, uploadEvidence.getTypeOfDocument(),
            uploadEvidence.getWitnessOptionName(),
            uploadEvidence.getDocumentIssuedDate()
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.UK))
        );
    }

    private void removePartyEntries(Map<String, List<Element<UploadEvidenceWitness>>> witnessStatementsMap, Party party) {
        if (party == null) {
            return;
        }
        if (party.getPartyName() != null) {
            witnessStatementsMap.remove(party.getPartyName().trim().toLowerCase());
        }
        if (party.isIndividual() && party.getIndividualFirstName() != null) {
            witnessStatementsMap.remove(party.getIndividualFirstName().trim().toLowerCase());
        }
    }

    private void addOtherWitnessEvidenceDocuments(List<BundlingRequestDocument> bundlingRequestDocuments,
                                                  List<Element<UploadEvidenceWitness>> originalWitnessEvidence,
                                                  List<Element<UploadEvidenceWitness>> witnessEvidence,
                                                  String displayName,
                                                  String documentType) {
        for (Element<UploadEvidenceWitness> uploadEvidenceWitnessElement : witnessEvidence) {
            UploadEvidenceWitness uploadEvidenceWitness = uploadEvidenceWitnessElement.getValue();
            String docName = generateDocName(
                    displayName,
                    uploadEvidenceWitness.getWitnessOptionName(),
                    String.valueOf(originalWitnessEvidence.indexOf(uploadEvidenceWitnessElement) + 1),
                    uploadEvidenceWitness.getWitnessOptionUploadDate()
            );
            bundlingRequestDocuments.add(buildBundlingRequestDoc(
                    docName,
                    uploadEvidenceWitness.getWitnessOptionDocument(),
                    documentType
            ));
        }
    }

    private <T> List<Element<T>> filterDocumentsForBundle(List<Element<T>> documents, Function<T, Document> documentExtractor) {
        return filterDocumentsForBundle(documents, documentExtractor, featureToggleService.isAmendBundleEnabled());
    }

    private <T> List<Element<T>> filterDocumentsForBundle(List<Element<T>> documents,
                                                          Function<T, Document> documentExtractor,
                                                          boolean amendBundleEnabled) {
        if (!amendBundleEnabled) {
            return documents;
        }
        return new ArrayList<>(documents.stream()
            .filter(element -> isBundledDocument(documentExtractor.apply(element.getValue())))
            .toList());
    }

    private boolean isBundledDocument(Document document) {
        return document.getCategoryID() != null && !UNBUNDLED_FOLDER.equals(document.getCategoryID());
    }

    private String getExpertise(String documentType, UploadEvidenceExpert uploadEvidenceExpert) {
        if (documentType.equals(EvidenceUploadType.EXPERT_REPORT.name())) {
            return uploadEvidenceExpert.getExpertOptionExpertise();
        }
        if (documentType.equals(EvidenceUploadType.JOINT_STATEMENT.name())) {
            return uploadEvidenceExpert.getExpertOptionExpertises();
        }
        return null;
    }

    private String getDocumentFileNameWithoutExtension(Document document) {
        String documentFileName = document.getDocumentFileName();
        return documentFileName.substring(0, documentFileName.lastIndexOf("."));
    }

    private String getPartyName(String fileNamePrefix, PartyType party) {
        String partyName = party.getDisplayName();
        if (fileNamePrefix.equals(BundleFileNameList.SCHEDULE_OF_LOSS_FILE_DISPLAY_NAME.getDisplayName())
            && (party.equals(PartyType.DEFENDANT1) || party.equals(PartyType.DEFENDANT2))) {
            return partyName.concat(" counter");
        }
        return partyName;
    }

    private java.time.LocalDate getDateForOriginalFileName(String documentType, UploadEvidenceDocumentType uploadEvidence) {
        return documentType.equals(EvidenceUploadType.COSTS.name())
            ? uploadEvidence.getCreatedDatetime().toLocalDate()
            : uploadEvidence.getDocumentIssuedDate();
    }

    private java.time.LocalDate getDateForGeneratedFileName(String documentType, UploadEvidenceDocumentType uploadEvidence) {
        return documentType.equals(EvidenceUploadType.CASE_SUMMARY.name())
            || documentType.equals(EvidenceUploadType.SKELETON_ARGUMENT.name())
            ? uploadEvidence.getCreatedDatetime().toLocalDate()
            : uploadEvidence.getDocumentIssuedDate();
    }
}
