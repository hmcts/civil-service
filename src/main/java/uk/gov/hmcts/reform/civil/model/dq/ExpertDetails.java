package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
public class ExpertDetails {

    private String partyID;
    private String expertName;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private String whyRequired;
    private String fieldofExpertise;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal estimatedCost;
}
