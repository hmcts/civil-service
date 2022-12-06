package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Component
@AllArgsConstructor
@Slf4j
public class RoboticsDataMapperFactory {

    private final RoboticsDataMapperForLip roboticsDataMapperForLip;
    private final RoboticsDataMapperForSpec roboticsDataMapperForSpec;

    public RoboticsCaseDataMapper getRoboticsDataMapper(CaseData caseData) {
        if(caseData.isApplicantNotRepresented()){
            log.info("returning lip");
            return roboticsDataMapperForLip;
        }
        log.info("returning spec");
        return roboticsDataMapperForSpec;
    }
}
