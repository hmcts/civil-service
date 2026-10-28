package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationAbandonedReason;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationCancelledReason;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationNotAttendedReason;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationNotConductedReason;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationNotSettledReason;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulOutcome;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;

import java.util.List;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mediation {

    @JsonUnwrapped
    private MediationSuccessful mediationSuccessful;
    private String unsuccessfulMediationReason;
    private List<MediationUnsuccessfulReason> mediationUnsuccessfulReasonsMultiSelect;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MediationUnsuccessfulOutcome mediationUnsuccessfulOutcome;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MediationNotConductedReason mediationNotConductedReason;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MediationNotAttendedReason mediationNotAttendedReason;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MediationCancelledReason mediationCancelledReason;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MediationAbandonedReason mediationAbandonedReason;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MediationNotSettledReason mediationNotSettledReason;
}
