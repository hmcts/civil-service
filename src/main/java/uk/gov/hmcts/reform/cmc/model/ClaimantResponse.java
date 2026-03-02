package uk.gov.hmcts.reform.cmc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaimantResponse {

    private ClaimantResponseType type;
    private BigDecimal amountPaid;
    private String paymentReceived;
    private String settleForAmount;
    private CourtDetermination courtDetermination;
    private FormaliseOption formaliseOption;

    @JsonIgnore
    public boolean hasCourtDetermination() {
        return courtDetermination != null;
    }
}
