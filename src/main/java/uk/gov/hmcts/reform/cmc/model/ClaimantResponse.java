package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaimantResponse {

    private ClaimantResponseType type;
    private BigDecimal amountPaid;
    private String paymentReceived;
    private String settleForAmount;
    private CourtDetermination courtDetermination;

    @JsonIgnore
    public boolean hasCourtDetermination() {
        return courtDetermination != null;
    }
}
