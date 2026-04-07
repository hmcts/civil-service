package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsUploadDoc {

    private String sdoUploadOfDocumentsTxt;
    private String uploadDocWarning;

    @Future(message = "The date entered must be in the future")
    private LocalDate deadlineDate;
}
