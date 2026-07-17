package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ExpertReportLiP {

    @CCD(label = " ", searchable = false)
    private String expertName;
    @CCD(label = " ", searchable = false)
    private LocalDate reportDate;
}

