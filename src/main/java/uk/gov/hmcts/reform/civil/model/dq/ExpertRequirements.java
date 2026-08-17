package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ExpertRequirements {

    @CCD(ignore = true)
    private String expertName;
    @CCD(ignore = true)
    private String fieldofExpertise;
    @CCD(ignore = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal estimatedCost;

}
