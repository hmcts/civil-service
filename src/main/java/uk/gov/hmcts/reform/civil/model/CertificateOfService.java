package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.CoSRecipientServeLocationOwnerType;
import uk.gov.hmcts.reform.civil.enums.CoSRecipientServeType;
import uk.gov.hmcts.reform.civil.enums.CosRecipientServeLocationType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CertificateOfService {

    @JsonProperty("cosDateOfServiceForDefendant")
    private LocalDate cosDateOfServiceForDefendant;
    @JsonProperty("cosDateDeemedServedForDefendant")
    private LocalDate cosDateDeemedServedForDefendant;
    @JsonProperty("cosServedDocumentFiles")
    private String cosServedDocumentFiles;
    @JsonProperty("cosEvidenceDocument")
    List<Element<Document>> cosEvidenceDocument;
    @JsonProperty("cosRecipient")
    private String cosRecipient;
    @JsonProperty("cosRecipientServeType")
    private CoSRecipientServeType cosRecipientServeType;
    @JsonProperty("cosRecipientServeLocation")
    private String cosRecipientServeLocation;
    @JsonProperty("cosRecipientServeLocationOwnerType")
    private CoSRecipientServeLocationOwnerType cosRecipientServeLocationOwnerType;
    @JsonProperty("cosRecipientServeLocationType")
    private CosRecipientServeLocationType cosRecipientServeLocationType;
    @JsonProperty("cosRecipientServeLocationTypeOther")
    private String cosRecipientServeLocationTypeOther;
    @JsonProperty("cosSender")
    private String cosSender;
    @JsonProperty("cosSenderFirm")
    private String cosSenderFirm;
    @JsonProperty("cosSenderStatementOfTruthLabel")
    private List<String> cosSenderStatementOfTruthLabel;
    @JsonProperty("cosDocSaved")
    private YesOrNo cosDocSaved;
}
