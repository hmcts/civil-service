package uk.gov.hmcts.reform.civil.model.documents;

import lombok.Data;

@Data
public class PDF {

    private final String fileBaseName;
    private final byte[] bytes;
    private final DocumentType documentType;
}
