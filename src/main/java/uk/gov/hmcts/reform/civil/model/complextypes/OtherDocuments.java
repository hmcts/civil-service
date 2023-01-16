package uk.gov.hmcts.reform.civil.model.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.enums.DocTypeOtherDocumentsEnum;

import java.util.List;

@Data
@Builder
public class OtherDocuments {

    @JsonProperty("documentName")
    private final String documentName;
    @JsonProperty("notes")
    private final String notes;
    private final Document documentOther;
    private final DocTypeOtherDocumentsEnum documentTypeOther;
    private final List<RestrictToCafcassHmcts> restrictCheckboxOtherDocuments;
}
