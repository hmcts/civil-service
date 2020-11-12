package uk.gov.hmcts.reform.unspec.service.robotics.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.unspec.assertion.CustomAssertions;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.robotics.RoboticsCaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RoboticsDataMapperTest {

    RoboticsDataMapper mapper = new RoboticsDataMapper(new RoboticsAddressMapper());

    @Test
    void shouldMapToRoboticsCaseData_whenCaseDataIsProvided() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();

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
