package uk.gov.hmcts.reform.civil.model.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import java.time.LocalDate;

@Data
@Builder
public class UploadedDocuments {

    private final String parentDocumentType;
    private final String documentType;
    private final String partyName;
    private final String isApplicant;
    private final String uploadedBy;
    private final LocalDate dateCreated;
    private final DocumentDetails documentDetails;
    private final Document citizenDocument;
    private final YesOrNo documentRequestedByCourt;
}

