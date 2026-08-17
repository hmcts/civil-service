package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsImpNotes {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String text;
    @CCD(label = " ", searchable = false)
    private LocalDate date;
}
