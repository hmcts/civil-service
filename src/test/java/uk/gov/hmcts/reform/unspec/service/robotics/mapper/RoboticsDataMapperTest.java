package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.unspec.assertion.CustomAssertions;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    EventHistoryMapper.class,
    RoboticsDataMapper.class,
    RoboticsAddressMapper.class
})
class RoboticsDataMapperTest {

    @Autowired
    RoboticsDataMapper mapper;

    @Test
    void shouldMapToRoboticsCaseData_whenHandOffPointIsUnrepresentedDefendant() {
        CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant().build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
    }

    @Test
    void shouldMapToRoboticsCaseData_whenDefendantIsNotRegistered() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePaymentSuccessful()
            .respondentSolicitor1OrganisationDetails(SolicitorOrganisationDetails.builder()
                                                         .organisationName("My Organisation")
                                                         .email("me@server.net")
                                                         .phoneNumber("0123456789")
                                                         .fax("9999999999")
                                                         .dx("Dx")
                                                         .address(Address.builder().build())
                                                         .build())
            .build();

        RoboticsCaseData roboticsCaseData = mapper.toRoboticsCaseData(caseData);

        CustomAssertions.assertThat(roboticsCaseData).isEqualTo(caseData);
    }

    @Test
    void shouldThrowNullPointerException_whenCaseDataIsNull() {
        assertThrows(NullPointerException.class, () ->
                         mapper.toRoboticsCaseData(null),
                     "caseData cannot be null"
        );
    }
}
