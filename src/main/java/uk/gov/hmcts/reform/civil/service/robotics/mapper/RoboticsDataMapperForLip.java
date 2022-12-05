package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsCaseDataSpec;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class RoboticsDataMapperForLip extends RoboticsCaseDataMapper{

    private final EventHistoryMapper eventHistoryMapper;

    @Override
    public RoboticsCaseDataSpec toRoboticsCaseData(CaseData caseData) {
        requireNonNull(caseData);
        RoboticsCaseDataSpec.RoboticsCaseDataSpecBuilder builder = RoboticsCaseDataSpec.builder()
            .header(buildCaseHeader(caseData))
            .claimDetails(buildClaimDetails(caseData))
            .events(eventHistoryMapper.buildEvents(caseData));
        return builder.build();
    }
}
