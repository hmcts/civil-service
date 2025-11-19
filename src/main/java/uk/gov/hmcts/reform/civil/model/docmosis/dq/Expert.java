package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
