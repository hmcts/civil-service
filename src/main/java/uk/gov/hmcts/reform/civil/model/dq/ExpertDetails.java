package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class ExpertDetails {

    private final String partyID;
    private final String expertName;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String emailAddress;
    private final String whyRequired;
    private final String fieldofExpertise;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal estimatedCost;
}
