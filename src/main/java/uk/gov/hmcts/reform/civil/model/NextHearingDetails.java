package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextHearingDetails {

    @CCD(label = "ID")
    private String hearingID;
    @CCD(label = "Date")
    private LocalDateTime hearingDateTime;
}
