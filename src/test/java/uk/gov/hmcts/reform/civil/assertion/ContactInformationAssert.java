package uk.gov.hmcts.reform.civil.assertion;

import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.prd.model.ContactInformation;

import java.util.List;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static uk.gov.hmcts.reform.civil.assertion.CustomAssertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.Address.fromContactInformation;

public class ContactInformationAssert extends CustomAssert<ContactInformationAssert, List<ContactInformation>> {

    private List<ContactInformation> contactInformation;

    public ContactInformationAssert(List<ContactInformation> contactInformation) {
        super("ContactInformationAssert", contactInformation, ContactInformationAssert.class);
        this.contactInformation = contactInformation;
    }

    public ContactInformationAssert isEqualTo(RoboticsAddress roboticsAddress) {
        if (isEmpty(contactInformation)) {
            assertThat(roboticsAddress).isNull();
            return this;
        }

        Address expected = fromContactInformation(contactInformation.get(0));
        assertThat(roboticsAddress).isEqualTo(expected);
        return this;
    }

}
