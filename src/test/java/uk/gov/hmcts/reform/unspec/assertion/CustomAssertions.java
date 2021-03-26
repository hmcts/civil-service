package uk.gov.hmcts.reform.unspec.assertion;

import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsCaseData;

import java.math.BigDecimal;
import java.util.List;

public class CustomAssertions {

    private CustomAssertions() {
        //utility class
    }

    public static RoboticsAddressAssert assertThat(RoboticsAddress roboticsAddress) {
        return new RoboticsAddressAssert(roboticsAddress);
    }

    public static RoboticsCaseDataAssert assertThat(RoboticsCaseData roboticsCaseData) {
        return new RoboticsCaseDataAssert(roboticsCaseData);
    }

    public static ContactInformationAssert assertThat(List<ContactInformation> contactInformation) {
        return new ContactInformationAssert(contactInformation);
    }

    public static MoneyAssert assertMoney(BigDecimal amount) {
        return new MoneyAssert(amount);
    }
}
