package uk.gov.hmcts.reform.civil.documentmanagement.model;

import lombok.Data;

@Data
public class UploadedDocument {

    private final String fileBaseName;
    private final byte[] bytes;
    private final DocumentType documentType;
}
