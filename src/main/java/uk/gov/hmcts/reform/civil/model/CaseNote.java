package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CaseNote {

    @CCD(label = "Created by", searchable = false)
    private String createdBy;
    @CCD(label = "Created on", searchable = false)
    private LocalDateTime createdOn;
    @CCD(label = "Note", searchable = false, typeOverride = FieldType.TextArea)
    private String note;
}
