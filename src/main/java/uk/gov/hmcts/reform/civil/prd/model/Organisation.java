package uk.gov.hmcts.reform.civil.prd.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Organisation {

    private String companyNumber;
    private String companyUrl;
    private List<ContactInformation> contactInformation;
    private String name;
    private String organisationIdentifier;
    private List<String> paymentAccount;
    private String sraId;
    private boolean sraRegulated;
    private String status;
    private SuperUser superUser;
}
