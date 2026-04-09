package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GeneralApplicationFeeRequest {

    private List<GeneralApplicationTypes> applicationTypes;
    private Boolean withConsent;
    private Boolean withNotice;
    private LocalDate hearingDate;
}
