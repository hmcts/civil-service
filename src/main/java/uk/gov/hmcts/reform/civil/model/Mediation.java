package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mediation {

    @JsonUnwrapped
    private MediationSuccessful mediationSuccessful;
    private String unsuccessfulMediationReason;
}
