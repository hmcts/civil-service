package uk.gov.hmcts.reform.civil.model.documentremoval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocumentToKeep;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentToKeep {

    @CCD(label = "Document ID", searchable = false)
    private String documentId;
    @CCD(label = "Document", searchable = false, typeOverride = FieldType.Document)
    private CaseDocumentToKeep caseDocumentToKeep;
    @CCD(label = "Uploaded Date", searchable = false)
    private LocalDateTime uploadedDate;
    @CCD(label = "Is Document System Generated?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo systemGenerated;

    //Override equals and hashcode to ignore uploadedDate and systemGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DocumentToKeep)) {
            return false;
        }
        DocumentToKeep that = (DocumentToKeep) o;
        if (!documentId.equals(that.documentId)) {
            return false;
        }
        return caseDocumentToKeep.equals(that.caseDocumentToKeep);
    }

    @Override
    public int hashCode() {
        int result = documentId.hashCode();
        result = 31 * result + caseDocumentToKeep.hashCode();
        return result;
    }
}
