package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingData {

    @JsonProperty("applicantCaseName")
    private final String applicantCaseName;

    @JsonProperty("caseNumber")
    private final String caseNumber;

    @JsonProperty("orders")
    private final List<Element<BundlingRequestDocument>> orders;

    @JsonProperty("citizenUploadedDocuments")
    private List<Element<BundlingRequestDocument>> citizenUploadedDocuments;

    @JsonProperty("applications")
    private List<Element<BundlingRequestDocument>> applications;

    @JsonProperty("otherDocumentsUploadedByCourtAdmin")
    private List<Element<BundlingRequestDocument>> otherDocuments;


}
