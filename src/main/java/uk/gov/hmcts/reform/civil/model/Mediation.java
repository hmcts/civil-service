package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;

import java.util.List;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mediation {

    @JsonUnwrapped
    private MediationSuccessful mediationSuccessful;
    private String unsuccessfulMediationReason;
    private List<MediationUnsuccessfulReason> mediationUnsuccessfulReasonsMultiSelect;
}
