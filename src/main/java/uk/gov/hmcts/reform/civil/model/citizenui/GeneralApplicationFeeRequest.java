package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneralApplicationFeeRequest {

    private List<GeneralApplicationTypes> applicationTypes;
    private Boolean withConsent;
    private Boolean withNotice;
    private LocalDate hearingDate;
}
