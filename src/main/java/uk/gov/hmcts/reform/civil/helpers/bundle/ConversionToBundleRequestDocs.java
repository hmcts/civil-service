package uk.gov.hmcts.reform.civil.helpers.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadFiles;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConversionToBundleRequestDocs {

    private final FeatureToggleService featureToggleService;
    private final BundleRequestMapper bundleRequestMapper;
    private static final String UNBUNDLED_FOLDER = "UnbundledFolder";

    public List<BundlingRequestDocument> covertOtherWitnessEvidenceToBundleRequestDocs(
        Map<String, List<Element<UploadEvidenceWitness>>> witnessStatmentsMap, String displayName, String documentType,
        Party party) {
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
                                                    String docName = bundleRequestMapper.generateDocName(
                                                        displayName,
                                                        uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                                                        String.valueOf(witnessEvidence.indexOf(uploadEvidenceWitnessElement) + 1),
                                                        uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate()
                                                    );
                                                    bundlingRequestDocuments.add(bundleRequestMapper.buildBundlingRequestDoc(
                                                        docName,
                                                        uploadEvidenceWitnessElement.getValue().getWitnessOptionDocument(),
                                                        documentType
                                                    ));
                                                })
            );
        } else {
            witnessStatmentsMap.forEach((witnessName, witnessEvidence) ->
                                            witnessEvidence.forEach(uploadEvidenceWitnessElement -> {
                                                String docName = bundleRequestMapper.generateDocName(
                                                    displayName,
                                                    uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                                                    String.valueOf(witnessEvidence.indexOf(uploadEvidenceWitnessElement) + 1),
                                                    uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate()
                                                );
                                                bundlingRequestDocuments.add(bundleRequestMapper.buildBundlingRequestDoc(
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
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (witnessEvidence != null) {
            if (featureToggleService.isAmendBundleEnabled()) {
                witnessEvidence = new ArrayList<>(witnessEvidence.stream()
                                                      .filter(caseDocumentElement -> caseDocumentElement.getValue().getWitnessOptionDocument().getCategoryID() != null
                                                          && !caseDocumentElement.getValue().getWitnessOptionDocument().getCategoryID().equals(
                                                          UNBUNDLED_FOLDER)).toList());
            }
            bundleRequestMapper.sortWitnessListByDate(
                witnessEvidence,
                !documentType.equals(EvidenceUploadFiles.WITNESS_STATEMENT.name())
            );
            witnessEvidence.forEach(uploadEvidenceWitnessElement -> {
                String docName = bundleRequestMapper.generateDocName(
                    fileNamePrefix,
                    isWitnessSelf ? party.getDisplayName() :
                        uploadEvidenceWitnessElement.getValue().getWitnessOptionName(),
                    null,
                    uploadEvidenceWitnessElement.getValue().getWitnessOptionUploadDate()
                );
                bundlingRequestDocuments.add(bundleRequestMapper.buildBundlingRequestDoc(
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
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();
        if (evidenceUploadDocList != null) {
            if (featureToggleService.isAmendBundleEnabled()) {
                evidenceUploadDocList = new ArrayList<>(evidenceUploadDocList.stream()
                                                            .filter(caseDocumentElement -> caseDocumentElement.getValue().getDocumentUpload().getCategoryID() != null
                                                                && !caseDocumentElement.getValue().getDocumentUpload().getCategoryID().equals(
                                                                UNBUNDLED_FOLDER)).toList());
            }
            bundleRequestMapper.sortEvidenceUploadByDate(
                evidenceUploadDocList,
                documentType.equals(EvidenceUploadFiles.CASE_SUMMARY.name())
                    || documentType.equals(EvidenceUploadFiles.SKELETON_ARGUMENT.name())
                    || documentType.equals(EvidenceUploadFiles.COSTS.name())
            );

            evidenceUploadDocList.forEach(uploadEvidenceDocumentTypeElement -> {
                String docName = bundleRequestMapper.getFileNameBaseOnType(
                    fileNamePrefix,
                    uploadEvidenceDocumentTypeElement,
                    documentType,
                    party
                );
                bundlingRequestDocuments.add(bundleRequestMapper.buildBundlingRequestDoc(
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
        List<BundlingRequestDocument> bundlingRequestDocuments = new ArrayList<>();

        if (evidenceUploadExpert != null) {
            if (featureToggleService.isAmendBundleEnabled()) {
                evidenceUploadExpert = new ArrayList<>(evidenceUploadExpert.stream()
                                                           .filter(caseDocumentElement -> caseDocumentElement.getValue().getExpertDocument().getCategoryID() != null
                                                               && !caseDocumentElement.getValue().getExpertDocument().getCategoryID().equals(
                                                               UNBUNDLED_FOLDER)).toList());
            }
            bundleRequestMapper.sortExpertListByDate(evidenceUploadExpert);
            evidenceUploadExpert.forEach(expertElement -> {
                String expertise = null;
                if (documentType.equals(EvidenceUploadFiles.EXPERT_REPORT.name())) {
                    expertise = expertElement.getValue().getExpertOptionExpertise();
                } else if (documentType.equals(EvidenceUploadFiles.JOINT_STATEMENT.name())) {
                    expertise = expertElement.getValue().getExpertOptionExpertises();
                }
                String docName = bundleRequestMapper.generateDocName(
                    fileNamePrefix,
                    expertElement.getValue().getExpertOptionName(),
                    expertise,
                    expertElement.getValue().getExpertOptionUploadDate()

                );
                bundlingRequestDocuments.add(bundleRequestMapper.buildBundlingRequestDoc(
                    docName,
                    expertElement.getValue().getExpertDocument(),
                    documentType
                ));
            });
        }
        return bundlingRequestDocuments;
    }
}
