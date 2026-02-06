package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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
