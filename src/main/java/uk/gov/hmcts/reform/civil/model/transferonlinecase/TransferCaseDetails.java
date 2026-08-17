package uk.gov.hmcts.reform.civil.model.transferonlinecase;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferCaseDetails {

    @CCD(label = " ")
    private String reasonForTransferCaseTxt;
}
