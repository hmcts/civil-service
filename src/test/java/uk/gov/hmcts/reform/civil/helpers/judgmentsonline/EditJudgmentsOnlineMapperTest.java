package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class EditJudgmentsOnlineMapperTest {

    @Mock
    private FeatureToggleService featureToggleService;

    private InterestCalculator interestCalculator;
    private JudgementService judgementService;
    private RoboticsAddressMapper addressMapper;
    private EditJudgmentOnlineMapper editJudgmentOnlineMapper;
    private RecordJudgmentOnlineMapper recordJudgmentMapper;
    private JudgmentByAdmissionOnlineMapper judgmentByAdmissionMapper;
    private DefaultJudgmentOnlineMapper defaultJudgmentMapper;

    @Mock
    private Time time;

    @BeforeEach
    public void setUp() {
        interestCalculator = new InterestCalculator();
        judgementService = new JudgementService(featureToggleService, interestCalculator);
        addressMapper = new RoboticsAddressMapper(new AddressLinesMapper());
        editJudgmentOnlineMapper = new EditJudgmentOnlineMapper(time);
        recordJudgmentMapper = new RecordJudgmentOnlineMapper(time, addressMapper);
        judgmentByAdmissionMapper = new JudgmentByAdmissionOnlineMapper(time, addressMapper, judgementService, interestCalculator);
        defaultJudgmentMapper = new DefaultJudgmentOnlineMapper(time, interestCalculator, addressMapper);
    }

    @Test
    void testIfActiveJudgmentIsnullIfnotSet() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNull(activeJudgment);
    }

    @Test
    void testIfActiveJudgmentIsUpdated() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("1200", activeJudgment.getOrderedAmount());
        assertEquals("1100", activeJudgment.getCosts());
        assertEquals("2300", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(JudgmentRTLStatus.MODIFIED_EXISTING.getRtlState(), activeJudgment.getRtlState());
        assertEquals(LocalDate.of(2022, 12, 12), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_FOLLOWING_HEARING, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(caseData.getJoPaymentPlan(), activeJudgment.getPaymentPlan());
        assertEquals("Mr. Sole Trader", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Dob());
        assertNotNull(activeJudgment.getDefendant1Address());

        assertEquals("Mr. Sole Trader", caseData.getJoDefendantName1());
        assertEquals(PaymentPlanSelection.PAY_IMMEDIATELY, caseData.getJoPaymentPlanSelected());
    }

    @Test
    void testIfActiveJudgmentIsUpdated_scenario2() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentImmediately();
        caseData.setJoAmountCostOrdered(null);
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("1200", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("1200", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(JudgmentRTLStatus.MODIFIED_EXISTING.getRtlState(), activeJudgment.getRtlState());
        assertEquals(LocalDate.of(2022, 12, 12), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_FOLLOWING_HEARING, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(caseData.getJoPaymentPlan(), activeJudgment.getPaymentPlan());
        assertEquals("Mr. Sole Trader", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Dob());
        assertNotNull(activeJudgment.getDefendant1Address());
    }

    @Test
    void testIfActiveJudgmentIsUpdated_PayInstallments() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setJoAmountCostOrdered(null);
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("1200", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("1200", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.of(2022, 12, 12), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_FOLLOWING_HEARING, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(caseData.getJoPaymentPlan(), activeJudgment.getPaymentPlan());
        assertEquals("Mr. John Rambo", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Dob());
        assertNotNull(activeJudgment.getDefendant1Address());

        assertEquals("Mr. John Rambo", caseData.getJoDefendantName1());
        assertEquals(PaymentPlanSelection.PAY_IN_INSTALMENTS, caseData.getJoPaymentPlanSelected());
        assertEquals("120", caseData.getJoRepaymentAmount());
        assertNotNull(caseData.getJoRepaymentStartDate());
        assertEquals(PaymentFrequency.MONTHLY, caseData.getJoRepaymentFrequency());
    }

    @Test
    void testIfActiveJudgmentIsUpdated_PayByDate() {

        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();
        caseData.setJoAmountCostOrdered(null);
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("1200", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("1200", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.of(2022, 12, 12), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_FOLLOWING_HEARING, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals(caseData.getJoPaymentPlan(), activeJudgment.getPaymentPlan());
        assertEquals("The Organisation", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Address());

        assertEquals("The Organisation", caseData.getJoDefendantName1());
        assertEquals(PaymentPlanSelection.PAY_BY_DATE, caseData.getJoPaymentPlanSelected());
    }

    @Test
    void testIfDefaultActiveJudgmentIsUpdated_scenario2() {

        CaseData caseData = CaseDataBuilder.builder().getDefaultJudgment1v1Case();
        caseData.setJoAmountCostOrdered(null);
        caseData.setActiveJudgment(defaultJudgmentMapper.addUpdateActiveJudgment(caseData));

        caseData.setJoOrderMadeDate(caseData.getActiveJudgment().getIssueDate());
        caseData.setJoPaymentPlan(caseData.getActiveJudgment().getPaymentPlan());
        caseData.setJoInstalmentDetails(caseData.getActiveJudgment().getInstalmentDetails());
        caseData.setJoJudgmentRecordReason(null);
        caseData.setJoAmountOrdered(caseData.getActiveJudgment().getOrderedAmount());
        caseData.setJoAmountCostOrdered(caseData.getActiveJudgment().getCosts());
        caseData.setJoIsRegisteredWithRTL(caseData.getActiveJudgment().getIsRegisterWithRTL());
        caseData.setJoIssuedDate(caseData.getActiveJudgment().getIssueDate());

        JudgmentDetails activeJudgment = editJudgmentOnlineMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.MODIFIED, activeJudgment.getState());
        assertEquals("100990", activeJudgment.getOrderedAmount());
        assertEquals("0", activeJudgment.getCosts());
        assertEquals("100990", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(JudgmentRTLStatus.MODIFIED_EXISTING.getRtlState(), activeJudgment.getRtlState());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.DEFAULT_JUDGMENT, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertEquals("Mr. Sole Trader", activeJudgment.getDefendant1Name());
        assertNotNull(activeJudgment.getDefendant1Dob());
        assertNotNull(activeJudgment.getDefendant1Address());
    }

    @Test
    void testIfJudgmentByAdmission_scenario3() {

        CCJPaymentDetails ccjPaymentDetails = new CCJPaymentDetails()
            .setCcjJudgmentAmountClaimAmount(BigDecimal.valueOf(140))
            .setCcjPaymentPaidSomeOption(YesOrNo.YES)
            .setCcjJudgmentFixedCostAmount(BigDecimal.valueOf(10))
            .setCcjJudgmentTotalStillOwed(BigDecimal.valueOf(150));

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YES)
            .specRespondent1Represented(YES)
            .applicant1Represented(YES)
            .defendantDetailsSpec(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("John Doe")
                    .build())
                .build())
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                .setWhenWillThisAmountBePaid(LocalDate.now().plusDays(5)))
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .ccjPaymentDetails(ccjPaymentDetails)
            .respondent1(PartyBuilder.builder().organisation().build())
            .build();
        JudgmentDetails activeJudgment = judgmentByAdmissionMapper.addUpdateActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.ISSUED, activeJudgment.getState());
        assertEquals("14000", activeJudgment.getOrderedAmount());
        assertEquals("1000", activeJudgment.getCosts());
        assertEquals("15000", activeJudgment.getTotalAmount());
        assertEquals(YesOrNo.YES, activeJudgment.getIsRegisterWithRTL());
        assertEquals(LocalDate.now(), activeJudgment.getIssueDate());
        assertEquals("0123", activeJudgment.getCourtLocation());
        assertEquals(JudgmentType.JUDGMENT_BY_ADMISSION, activeJudgment.getType());
        assertEquals(YesOrNo.YES, activeJudgment.getIsJointJudgment());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertNotNull(activeJudgment.getDefendant1Address());
    }
}
