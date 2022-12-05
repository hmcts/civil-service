package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Component
@AllArgsConstructor
public class RoboticsDataMapperFactory {

    private final RoboticsDataMapperForLip roboticsDataMapperForLip;
    private final RoboticsDataMapperForSpec roboticsDataMapperForSpec;

    public RoboticsCaseDataMapper getRoboticsDataMapper(CaseData caseData) {
        if(caseData.isApplicantNotRepresented()){
            return roboticsDataMapperForLip;
        }
        return roboticsDataMapperForSpec;
    }
}
