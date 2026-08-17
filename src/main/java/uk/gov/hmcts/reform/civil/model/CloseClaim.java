package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.validation.groups.ClaimWithdrawalDateGroup;

import java.time.LocalDate;
import jakarta.validation.constraints.PastOrPresent;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CloseClaim {

    @CCD(label = "Date", searchable = false)
    @PastOrPresent(message = "The date must not be in the future", groups = ClaimWithdrawalDateGroup.class)
    private LocalDate date;
    @CCD(label = "Reason", searchable = false, typeOverride = FieldType.TextArea)
    private String reason;
}