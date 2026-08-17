package uk.gov.hmcts.reform.civil.model.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.model.BundleDocument;
import uk.gov.hmcts.reform.civil.model.BundleFolder;
import uk.gov.hmcts.reform.civil.model.PaginationStyle;
import uk.gov.hmcts.reform.civil.model.PageNumberFormat;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Bundle", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BundleDetails {

    @CCD(label = "Bundle ID", showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"", searchable = false)
    private String id;
    @CCD(label = "Bundles", searchable = false)
    private String title;
    @CCD(
            label = "Description",
            showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String description;
    @CCD(label = "Stitch status", showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"", searchable = false)
    private String stitchStatus;
    @CCD(label = "Document URL", searchable = false)
    private Document stitchedDocument;
    @CCD(label = "Error from Stiching service", searchable = false)
    private String stitchingFailureMessage;
    @CCD(label = "Name", searchable = false)
    private String fileName;

    @CCD(label = "Created On", searchable = false)
    private LocalDateTime createdOn;
    @CCD(label = "Hearing Date", searchable = false)
    private LocalDate bundleHearingDate;

    @JsonCreator
    public BundleDetails(@JsonProperty("id") String id,
                         @JsonProperty("title") String title,
                         @JsonProperty("description") String description,
                         @JsonProperty("stitchStatus") String stitchStatus,
                         @JsonProperty("stitchedDocument") Document stitchedDocument,
                         @JsonProperty("stitchingFailureMessage") String stitchingFailureMessage,
                         @JsonProperty("fileName") String fileName,
                         @JsonProperty("createdOn") LocalDateTime createdOn,
                         @JsonProperty("bundleHearingDate") LocalDate bundleHearingDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.stitchStatus = stitchStatus;
        this.stitchedDocument = stitchedDocument;
        this.stitchingFailureMessage = stitchingFailureMessage;
        this.fileName = fileName;
        this.createdOn = createdOn;
        this.bundleHearingDate = bundleHearingDate;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Bundle document", showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<BundleDocument>> documents;
  @CCD(label = "Bundle folder", showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<BundleFolder>> folders;
  @CCD(
          label = "Is this the bundle you want to amend?",
          showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo eligibleForStitching;
  @CCD(
          label = "Is this the bundle you want to clone?",
          showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo eligibleForCloning;
  @CCD(
          label = "Should this bundle have coversheets separating each document?",
          showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasCoversheets;
  @CCD(
          label = "Should this bundle have a title page with a table of contents?",
          showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasTableOfContents;
  @CCD(
          label = "Should this bundle’s folders have a coversheet?",
          showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hasFolderCoversheets;
  @CCD(
          label = "Pagination Style",
          showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "paginationStyle"
  )
  private PaginationStyle paginationStyle;
  @CCD(label = "Cover page template", showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"", searchable = false)
  private String coverpageTemplate;
  @CCD(
          label = "Page Number Format",
          showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "pageNumberFormat"
  )
  private PageNumberFormat pageNumberFormat;
  @CCD(label = "File Name Identifier", showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"", searchable = false)
  private String fileNameIdentifier;
  @CCD(
          label = "Enable Email notification?",
          showCondition = "stitchStatus=\"DUMMY_VALUE_TO_HIDE_FIELD\"",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo enableEmailNotification;
  @CCD(label = "UploadedBy", searchable = false)
  private String uploadedBy;
  // ==== end synthesised definition-only fields ====
}
