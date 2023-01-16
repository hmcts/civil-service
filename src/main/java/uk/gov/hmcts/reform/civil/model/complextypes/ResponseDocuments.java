package uk.gov.hmcts.reform.civil.model.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.time.LocalDate;

@Data
@Builder
public class ResponseDocuments {

    private final String partyName;
    private final String createdBy;
    private final LocalDate dateCreated;
    private final Document citizenDocument;
}
