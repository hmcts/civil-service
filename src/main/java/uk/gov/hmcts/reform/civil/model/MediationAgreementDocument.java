package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class MediationAgreementDocument {

    @CCD(label = "Document name", searchable = false)
    private String name;
    @CCD(
            label = "Document type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "MediationDocumentType"
    )
    private DocumentType documentType;
    @CCD(label = "Document link", searchable = false)
    private Document document;
    @CCD(label = "Document Uploaded DateTime", showCondition = "document = \"DO_NOT_SHOW\"", searchable = false)
    private LocalDateTime documentUploadedDatetime = LocalDateTime.now(ZoneId.of("Europe/London"));

}
