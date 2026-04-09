package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Expert {

    private String name;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private String fieldOfExpertise;
    private String whyRequired;
    private String formattedCost;
}
