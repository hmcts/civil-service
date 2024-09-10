package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentAddress;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(
    classes = {
        RoboticsAddressMapper.class,
        AddressLinesMapper.class
    })
class JudgmentAddressMapperTest {

    @InjectMocks
    private JudgmentAddressMapper judgmentAddressMapper;

    @Mock
    private RoboticsAddressMapper addressMapper;

    @Test
    void testJudgmentAddressIsReturned() {

        JudgmentAddress address = judgmentAddressMapper.getJudgmentAddress(Address.builder()
                                                                               .addressLine1("address line 1")
                                                                               .addressLine2("address line 2")
                                                                               .addressLine3("address line 3")
                                                                               .postCode("postcode")
                                                                               .build());

        assertNotNull(address);
        assertEquals("address line 1", address.getDefendantAddressLine1());
    }

}
