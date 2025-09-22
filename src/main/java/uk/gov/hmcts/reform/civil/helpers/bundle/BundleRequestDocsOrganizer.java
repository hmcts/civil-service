package uk.gov.hmcts.reform.civil.helpers.bundle;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class BundleRequestDocsOrganizer {

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

    public List<Element<UploadEvidenceDocumentType>> filterDocumentaryEvidenceForTrialDocs(
        List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial, List<String> displayNames,
        boolean doesNotMatchType) {
        sortEvidenceUploadByDate(documentEvidenceForTrial, false);
        return documentEvidenceForTrial.stream().filter(uploadEvidenceDocumentTypeElement -> matchType(
            uploadEvidenceDocumentTypeElement.getValue().getTypeOfDocument(),
            displayNames, doesNotMatchType
        )).collect(Collectors.toList());
    }

    private boolean matchType(String name, Collection<String> displayNames, boolean doesNotMatchType) {
        if (doesNotMatchType) {
            return displayNames.stream().noneMatch(s -> s.equalsIgnoreCase(name.trim()));
        } else {
            return displayNames.stream().anyMatch(s -> s.equalsIgnoreCase(name.trim()));
        }
    }
}
