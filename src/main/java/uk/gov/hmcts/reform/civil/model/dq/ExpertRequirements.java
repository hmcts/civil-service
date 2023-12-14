package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpertRequirements {

    private String expertName;
    private String fieldofExpertise;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal estimatedCost;

}
