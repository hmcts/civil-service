package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentAddress;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgmentAddressMapper {

    private final RoboticsAddressMapper addressMapper = new RoboticsAddressMapper(new AddressLinesMapper());

    public JudgmentAddress getJudgmentAddress(Address address) {

        RoboticsAddress roboticsAddress = addressMapper.toRoboticsAddress(address);
        return JudgmentAddress.builder()
            .defendantAddressLine1(roboticsAddress.getAddressLine1())
            .defendantAddressLine2(roboticsAddress.getAddressLine2())
            .defendantAddressLine3(roboticsAddress.getAddressLine3())
            .defendantAddressLine4(roboticsAddress.getAddressLine4())
            .defendantAddressLine5(roboticsAddress.getAddressLine5())
            .defendantPostCode(roboticsAddress.getPostCode()).build();
    }
}
