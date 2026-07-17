package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class DocumentsToBeConsidered {

    @CCD(
            label = "Are there any documents the claimants have that you want the court to consider?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    public YesOrNo hasDocumentsToBeConsidered;
    @CCD(label = "What are the documents the claimants have that you want the court to consider?", searchable = false)
    public String details;
}
