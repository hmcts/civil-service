package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateBundleDoucment implements MappableObject {

    private String id;
    private String title;
    private String description;
    private String eligibleForStitching;
    private List<IdValue<BundleDocument>> documents;
    private Optional<String> stitchStatus;
    private Optional<Document> stitchedDocument;

    private YesOrNo hasCoversheets;
    private YesOrNo hasTableOfContents;
    private String filename;
    private LocalDate hearingDate;
    private LocalDate hearingDueDate;

    private CreateBundleDoucment() {
        // noop -- for deserializer
    }


    public CreateBundleDoucment(
        String id,
        String title,
        String description,
        String eligibleForStitching,
        List<IdValue<BundleDocument>> documents,
        String filename,
        LocalDate hearingDate,
        LocalDate hearingDueDate

    ) {
        this(
            id,
            title,
            description,
            eligibleForStitching,
            documents,
            Optional.empty(),
            Optional.empty(),
            YesOrNo.NO,
            YesOrNo.NO,
            filename,
            hearingDate,
            hearingDueDate
        );
    }

    public CreateBundleDoucment(
        String id,
        String title,
        String description,
        String eligibleForStitching,
        List<IdValue<BundleDocument>> documents,
        Optional<String> stitchStatus,
        Optional<Document> stitchedDocument,
        YesOrNo hasCoversheets,
        YesOrNo hasTableOfContents,
        String filename,
        LocalDate hearingDate,
        LocalDate hearingDueDate
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.eligibleForStitching = eligibleForStitching;
        this.documents = documents;
        this.stitchStatus = stitchStatus;
        this.stitchedDocument = stitchedDocument;
        this.hasCoversheets = hasCoversheets;
        this.hasTableOfContents = hasTableOfContents;
        this.filename = filename;
        this.hearingDate = hearingDate;
        this.hearingDueDate = hearingDueDate;
    }
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getEligibleForStitching() {
        return eligibleForStitching;
    }

    public List<IdValue<BundleDocument>> getDocuments() {
        return documents;
    }

    public Optional<String> getStitchStatus() {
        return checkIsOptional(stitchStatus);
    }

    public Optional<Document> getStitchedDocument() {
        return checkIsOptional(stitchedDocument);
    }

    public YesOrNo getHasCoversheets() {
        return hasCoversheets;
    }

    public YesOrNo getHasTableOfContents() {
        return hasTableOfContents;
    }

    public String getFilename() {
        return filename;
    }

    public LocalDate getHearingDate() {
        return hearingDate;
    }

    public LocalDate getHearingDueDate() {
        return hearingDueDate;
    }

    //It is possible for the Optional types to be instantiated as null e.g. through Jackson
    private <T> Optional<T> checkIsOptional(Optional<T> field) {
        if (null == field) { //NOSONAR
            return Optional.empty();
        }

        return field;
    }
}
