package uk.gov.hmcts.reform.unspec.assertion;

import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsAddress;

import java.util.List;

import static io.jsonwebtoken.lang.Collections.isEmpty;
import static uk.gov.hmcts.reform.unspec.assertion.CustomAssertions.assertThat;
import static uk.gov.hmcts.reform.unspec.model.Address.fromContactInformation;

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
