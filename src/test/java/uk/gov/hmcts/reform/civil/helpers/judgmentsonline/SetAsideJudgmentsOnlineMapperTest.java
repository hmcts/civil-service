package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.robotics.RoboticsAddress;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetAsideJudgmentsOnlineMapperTest {

    @InjectMocks
    private SetAsideJudgmentOnlineMapper judgmentOnlineMapper;
    @InjectMocks
    private RecordJudgmentOnlineMapper recordJudgmentMapper;
    @MockBean
    private Time time;
    @Mock
    private InterestCalculator interestCalculator;
    @InjectMocks
    private DefaultJudgmentOnlineMapper defaultJudgmentOnlineMapper;
    @Mock
    private RoboticsAddressMapper addressMapper;

    @BeforeEach
    void setUp() {
        when(addressMapper.toRoboticsAddress(any())).thenReturn(RoboticsAddress.builder().build());
    }

    @Test
    void testIfActiveJudgmentIsHistoricSetAsideApplication() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        //SET ASIDE
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
        caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
        judgmentOnlineMapper.moveToHistoricJudgment(caseData);

        assertNull(caseData.getActiveJudgment());
        assertNotNull(caseData.getHistoricJudgment());
        JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
        assertEquals(JudgmentState.SET_ASIDE, historicJudgment.getState());
        assertEquals(LocalDate.of(2022, 12, 12), historicJudgment.getSetAsideDate());
    }

    @Test
    void testIfActiveJudgmentIsHistoricSetAsideDefence() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        //SET ASIDE
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
        caseData.setJoSetAsideDefenceReceivedDate(LocalDate.of(2022, 12, 12));
        judgmentOnlineMapper.moveToHistoricJudgment(caseData);

        assertNull(caseData.getActiveJudgment());
        assertNotNull(caseData.getHistoricJudgment());
        JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
        assertEquals(JudgmentState.SET_ASIDE, historicJudgment.getState());
        assertEquals(LocalDate.of(2022, 12, 12), historicJudgment.getSetAsideDate());
    }

    @Test
    void testIfActiveJudgmentIsHistoricSetAsideError() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        //SET ASIDE
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
        judgmentOnlineMapper.moveToHistoricJudgment(caseData);

        assertNull(caseData.getActiveJudgment());
        assertNotNull(caseData.getHistoricJudgment());
        JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
        assertEquals(JudgmentState.SET_ASIDE_ERROR, historicJudgment.getState());
        assertEquals(LocalDate.now(), historicJudgment.getSetAsideDate());
    }

    @Test
    void testIfDefaultActiveJudgmentIsHistoricSetAsideError() {

        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.ZERO);

        CaseData caseData = CaseDataBuilder.builder().getDefaultJudgment1v1Case();
        caseData.setActiveJudgment(defaultJudgmentOnlineMapper.addUpdateActiveJudgment(caseData));

        //SET ASIDE
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
        judgmentOnlineMapper.moveToHistoricJudgment(caseData);

        assertNull(caseData.getActiveJudgment());
        assertNotNull(caseData.getHistoricJudgment());
        JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
        assertEquals(JudgmentState.SET_ASIDE_ERROR, historicJudgment.getState());
        assertEquals(LocalDate.now(), historicJudgment.getSetAsideDate());
    }

    @ParameterizedTest
    @CsvSource({"R", "M", "S"})
    void testIfRTLIsUpdatedCorrectly(String rtlState) {
        //Given
        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.ZERO);
        CaseData caseData = CaseDataBuilder.builder().getDefaultJudgment1v1Case();
        caseData.setActiveJudgment(defaultJudgmentOnlineMapper.addUpdateActiveJudgment(caseData));
        caseData.getActiveJudgment().setRtlState(rtlState);
        //When
        SetAsideJudgmentOnlineMapper setAsideJudgmentOnlineMapper = new SetAsideJudgmentOnlineMapper();
        caseData.setActiveJudgment(setAsideJudgmentOnlineMapper.addUpdateActiveJudgment(caseData));
        //Then
        if (rtlState.equals("S")) {
            assertEquals(caseData.getActiveJudgment().getRtlState(), JudgmentRTLStatus.SATISFIED.getRtlState());
        } else {
            assertEquals(caseData.getActiveJudgment().getRtlState(), JudgmentRTLStatus.CANCELLED.getRtlState());
        }
    }

    @Test
    void testIfSetAsideApplicationDateUpdatedCorrectly() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        CaseData updatedCaseData = caseData.toBuilder()
            .activeJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData))
            .joSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER)
            .joSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION)
            .joSetAsideOrderDate(LocalDate.of(2024, 11, 12))
            .joSetAsideApplicationDate(LocalDate.of(2024, 11, 12))
            .build();

        //SET ASIDE
        judgmentOnlineMapper.moveToHistoricJudgment(updatedCaseData);

        assertNull(updatedCaseData.getActiveJudgment());
        assertNotNull(updatedCaseData.getHistoricJudgment());
        JudgmentDetails historicJudgment = updatedCaseData.getHistoricJudgment().get(0).getValue();
        assertEquals(JudgmentState.SET_ASIDE, historicJudgment.getState());
        assertEquals(LocalDate.of(2024, 11, 12), historicJudgment.getSetAsideApplicationDate());
    }
}
