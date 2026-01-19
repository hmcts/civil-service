package uk.gov.hmcts.reform.civil.service;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.MarkPaidConsentList;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardMapperTestConfiguration;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState.ISSUED;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_BY_DATE;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection.PAY_IN_INSTALMENTS;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_DAY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SpringBootTest(classes = {DashboardMapperTestConfiguration.class})
public class DashboardNotificationsParamsMapperTest {

    private CaseData caseData;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private ClaimantResponseUtils claimantResponseUtils;

    @Autowired
    private DashboardNotificationsParamsMapper mapper;

    @BeforeEach
    void setup() {
        caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
    }

    public CaseDocument generateOrder(DocumentType documentType) {
        Document document = new Document();
        document.setDocumentUrl("fake-url");
        document.setDocumentFileName("file-name.pdf");
        document.setDocumentBinaryUrl("binary-url");

        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setCreatedBy("Test");
        caseDocument.setDocumentName("document test name");
        caseDocument.setDocumentSize(0L);
        caseDocument.setDocumentType(documentType);
        caseDocument.setCreatedDatetime(LocalDateTime.of(2024, Month.APRIL, 4, 14, 14));
        caseDocument.setDocumentLink(document);
        return caseDocument;
    }

    @Test
    public void shouldMapAllParameters_WhenIsRequested() {
        when(claimantResponseUtils.getDefendantAdmittedAmount(any(), anyBoolean())).thenReturn(BigDecimal.valueOf(100));

        caseData.setHwfFeeType(FeeType.CLAIMISSUED);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(124.67));
        caseData.setRespondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(100));

        LocalDate date = LocalDate.of(2024, Month.FEBRUARY, 22);
        caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec(date));
        caseData.setRespondent1RespondToSettlementAgreementDeadline(LocalDateTime.now());
        caseData.setApplicant1AcceptFullAdmitPaymentPlanSpec(YES);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setApplicant1ClaimSettledDate(LocalDate.now());
        caseData.setCaseDataLiP(caseDataLiP);
        LocalDateTime applicant1ResponseDeadline = LocalDateTime.of(2024, 3, 21, 16, 0);
        caseData.setApplicant1ResponseDeadline(applicant1ResponseDeadline);
        caseData.setHearingDate(LocalDate.of(2024, 4, 1));
        caseData.setHearingDueDate(LocalDate.of(2024, 4, 1));
        caseData.setHearingFee(new Fee(new BigDecimal(10000), "Test", "Test"));
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setLabel("County Court");
        DynamicList hearingLocation = new DynamicList();
        hearingLocation.setValue(dynamicListElement);
        caseData.setHearingLocation(hearingLocation);
        caseData.setHearingLocationCourtName("County Court");
        caseData.setApplicant1Represented(NO);
        caseData.setRequestForReconsiderationDeadline(LocalDateTime.of(2024, 4, 1, 10, 20));

        LocalDateTime now = LocalDateTime.now();
        List<IdValue<Bundle>> bundles = List.of(
            new IdValue<>("1", new Bundle().setCreatedOn(Optional.of(now.minusDays(1)))),
            new IdValue<>("2", new Bundle().setCreatedOn(Optional.of(now))),
            new IdValue<>("3", new Bundle().setCreatedOn(Optional.of(now.minusDays(2))))
        );
        caseData.setCaseBundles(bundles);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("djDefendantNotificationMessage").isEqualTo(
            "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to set aside (remove) or vary the judgment</a>");

        assertThat(result).extracting("djClaimantNotificationMessage")
            .isEqualTo("<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to vary the judgment</a>");

        assertThat(result).extracting("claimFee").isEqualTo("£1");

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).extracting("defendantAdmittedAmount").isEqualTo("£100");

        assertThat(result).extracting("respondent1AdmittedAmountPaymentDeadline")
            .isEqualTo(date.atTime(END_OF_DAY));
        assertThat(result).extracting("respondent1AdmittedAmountPaymentDeadlineEn")
            .isEqualTo(DateUtils.formatDate(date));

        assertThat(result).extracting("respondent1AdmittedAmountPaymentDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(date, false));

        assertThat(result).extracting("applicant1ResponseDeadline")
            .isEqualTo(applicant1ResponseDeadline);
        assertThat(result).extracting("applicant1ResponseDeadlineEn")
            .isEqualTo(DateUtils.formatDate(applicant1ResponseDeadline.toLocalDate()));
        assertThat(result).extracting("applicant1ResponseDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(applicant1ResponseDeadline.toLocalDate(), false));
        assertThat(result).extracting("respondent1ResponseDeadline")
            .isEqualTo(RESPONSE_DEADLINE);
        assertThat(result).extracting("respondent1ResponseDeadlineEn")
            .isEqualTo(DateUtils.formatDate(LocalDate.now().plusDays(14L)));
        assertThat(result).extracting("respondent1ResponseDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(LocalDate.now().plusDays(14L), false));
        assertThat(result).extracting("respondent1PartyName")
            .isEqualTo(caseData.getRespondent1().getPartyName());

        assertThat(result).extracting("typeOfFee").isEqualTo("claim");
        assertThat(result).extracting("respondent1SettlementAgreementDeadline")
            .isEqualTo(LocalDate.now().atTime(END_OF_DAY));
        assertThat(result).extracting("respondent1SettlementAgreementDeadlineEn")
            .isEqualTo(DateUtils.formatDate(LocalDateTime.now()));

        assertThat(result).extracting("respondent1SettlementAgreementDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(LocalDate.now(), false));

        assertThat(result).extracting("claimantSettlementAgreementEn").isEqualTo("accepted");
        assertThat(result).extracting("claimantSettlementAgreementCy").isEqualTo("derbyn");
        assertThat(result).extracting("applicant1ClaimSettledObjectionsDeadline")
            .isEqualTo(LocalDate.now().plusDays(19).atTime(END_OF_DAY));
        assertThat(result).extracting("applicant1ClaimSettledDateEn")
            .isEqualTo(DateUtils.formatDate(LocalDateTime.now()));

        assertThat(result).extracting("applicant1ClaimSettledDateCy")
            .isEqualTo(DateUtils.formatDateInWelsh(LocalDate.now(), false));

        assertThat(result).extracting("applicant1ResponseDeadlineEn")
            .isEqualTo("21 March 2024");
        assertThat(result).extracting("applicant1ResponseDeadlineEn")
            .isEqualTo("21 March 2024");

        assertThat(result).extracting("hearingDateEn")
            .isEqualTo("1 April 2024");
        assertThat(result).extracting("hearingDateCy")
            .isEqualTo("1 Ebrill 2024");
        assertThat(result).extracting("hearingCourtEn")
            .isEqualTo("County Court");
        assertThat(result).extracting("hearingCourtCy")
            .isEqualTo("County Court");
        assertThat(result).extracting("hearingDueDate")
            .isEqualTo(LocalDate.of(2024, 4, 1).atTime(END_OF_DAY));
        assertThat(result).extracting("hearingDueDateEn")
            .isEqualTo("1 April 2024");
        assertThat(result).extracting("hearingDueDateCy")
            .isEqualTo("1 Ebrill 2024");
        assertThat(result).extracting("requestForReconsiderationDeadline")
            .isEqualTo(LocalDateTime.of(2024, 4, 1, 10, 20));
        assertThat(result).extracting("requestForReconsiderationDeadlineEn")
            .isEqualTo("1 April 2024");
        assertThat(result).extracting("requestForReconsiderationDeadlineCy")
            .isEqualTo("1 Ebrill 2024");
        assertThat(result).extracting("hearingFee")
            .isEqualTo("£100");
        assertThat(result).extracting("trialArrangementDeadline")
            .isEqualTo(LocalDate.of(2024, 3, 4).atTime(END_OF_DAY));
        assertThat(result).extracting("trialArrangementDeadlineEn")
            .isEqualTo("4 March 2024");
        assertThat(result).extracting("trialArrangementDeadlineCy")
            .isEqualTo("4 Mawrth 2024");
        assertThat(result).extracting("bundleRestitchedDateEn")
            .isEqualTo(DateUtils.formatDate(LocalDate.now()));
        assertThat(result).extracting("bundleRestitchedDateCy")
            .isEqualTo(DateUtils.formatDateInWelsh(LocalDate.now(), false));

    }

    @ParameterizedTest
    @EnumSource(PaymentFrequency.class)
    void shouldMapParameters_WhenRecordJudgmentDeterminationOfMeans(PaymentFrequency paymentFrequency) {

        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.All_FINAL_ORDERS_ISSUED);
        caseData.setJoJudgmentRecordReason(JudgmentRecordedReason.DETERMINATION_OF_MEANS);
        JudgmentInstalmentDetails instalmentDetails = new JudgmentInstalmentDetails();
        instalmentDetails.setStartDate(LocalDate.of(2022, 12, 12));
        instalmentDetails.setAmount("120");
        instalmentDetails.setPaymentFrequency(paymentFrequency);
        caseData.setJoInstalmentDetails(instalmentDetails);
        caseData.setJoAmountOrdered("1200");
        caseData.setJoAmountCostOrdered("1100");
        JudgmentPaymentPlan paymentPlan = new JudgmentPaymentPlan();
        paymentPlan.setType(PaymentPlanSelection.PAY_IN_INSTALMENTS);
        caseData.setJoPaymentPlan(paymentPlan);
        caseData.setJoOrderMadeDate(LocalDate.of(2022, 12, 12));
        caseData.setJoIsRegisteredWithRTL(YES);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        if (paymentFrequency.equals(PaymentFrequency.WEEKLY)) {
            assertThat(result).extracting("paymentFrequencyMessage").isEqualTo("You must pay the claim amount of £23.00" +
                " in weekly instalments of £1.20." +
                " The first payment is due on 12 December 2022");
            assertThat(result).extracting("paymentFrequencyMessageCy").isEqualTo("Rhaid i chi dalu swm yr hawliad," +
                " sef £23.00 mewn rhandaliadau wythnosol o £1.20." +
                " Bydd y taliad cyntaf yn ddyledus ar 12 Rhagfyr 2022");
        } else if (paymentFrequency.equals(PaymentFrequency.EVERY_TWO_WEEKS)) {
            assertThat(result).extracting("paymentFrequencyMessage").isEqualTo("You must pay the claim amount of £23.00" +
                " in biweekly instalments of £1.20." +
                " The first payment is due on 12 December 2022");
            assertThat(result).extracting("paymentFrequencyMessageCy").isEqualTo("Rhaid i chi dalu swm yr hawliad," +
                " sef £23.00 mewn rhandaliadau bob pythefnos o £1.20." +
                " Bydd y taliad cyntaf yn ddyledus ar 12 Rhagfyr 2022");
        } else {
            assertThat(result).extracting("paymentFrequencyMessage").isEqualTo("You must pay the claim amount of £23.00" +
                " in monthly instalments of £1.20." +
                " The first payment is due on 12 December 2022");
            assertThat(result).extracting("paymentFrequencyMessageCy").isEqualTo("Rhaid i chi dalu swm yr hawliad, " +
                "sef £23.00 mewn rhandaliadau misol o £1.20." +
                " Bydd y taliad cyntaf yn ddyledus ar 12 Rhagfyr 2022");
        }
    }

    @Test
    public void shouldMapParameters_WhenGeneralApplicationsIsEnabled() {

        // caseData already initialized in setup

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("djDefendantNotificationMessage").isEqualTo(
            "<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to set aside (remove) or vary the judgment</a>");

        assertThat(result).extracting("djClaimantNotificationMessage")
            .isEqualTo("<a href=\"{GENERAL_APPLICATIONS_INITIATION_PAGE_URL}\" class=\"govuk-link\">make an application to vary the judgment</a>");
    }

    @ParameterizedTest
    @EnumSource(PaymentFrequency.class)
    public void shouldMapParameters_WhenJudgementByAdmissionInstalmentsIsIssued(PaymentFrequency paymentFrequency) {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.All_FINAL_ORDERS_ISSUED);
        JudgmentInstalmentDetails instalmentDetails2 = new JudgmentInstalmentDetails();
        instalmentDetails2.setAmount("20001");
        instalmentDetails2.setPaymentFrequency(paymentFrequency);
        instalmentDetails2.setStartDate(LocalDate.of(2050, Month.AUGUST, 8));
        JudgmentPaymentPlan paymentPlan2 = new JudgmentPaymentPlan();
        paymentPlan2.setType(PAY_IN_INSTALMENTS);
        JudgmentDetails activeJudgment = new JudgmentDetails();
        activeJudgment.setState(ISSUED);
        activeJudgment.setPaymentPlan(paymentPlan2);
        activeJudgment.setOrderedAmount("150001");
        activeJudgment.setTotalAmount("150001");
        activeJudgment.setInstalmentDetails(instalmentDetails2);
        caseData.setActiveJudgment(activeJudgment);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("ccjDefendantAdmittedAmount").isEqualTo(BigDecimal.valueOf(1500.01));

        if (paymentFrequency.equals(PaymentFrequency.WEEKLY)) {
            assertThat(result).extracting("ccjPaymentMessageEn")
                .isEqualTo("in weekly instalments of £200.01. The first payment is due on 8 August 2050");
            assertThat(result).extracting("ccjPaymentMessageCy")
                .isEqualTo("mewn rhandaliadau wythnosol o £200.01. Bydd y taliad cyntaf yn ddyledus ar 8 Awst 2050");
        } else if (paymentFrequency.equals(PaymentFrequency.EVERY_TWO_WEEKS)) {
            assertThat(result).extracting("ccjPaymentMessageEn")
                .isEqualTo("in biweekly instalments of £200.01. The first payment is due on 8 August 2050");
            assertThat(result).extracting("ccjPaymentMessageCy")
                .isEqualTo("mewn rhandaliadau bob pythefnos o £200.01. Bydd y taliad cyntaf yn ddyledus ar 8 Awst 2050");
        } else {
            assertThat(result).extracting("ccjPaymentMessageEn")
                .isEqualTo("in monthly instalments of £200.01. The first payment is due on 8 August 2050");
            assertThat(result).extracting("ccjPaymentMessageCy")
                .isEqualTo("mewn rhandaliadau misol o £200.01. Bydd y taliad cyntaf yn ddyledus ar 8 Awst 2050");
        }
    }

    @Test
    public void shouldMapParameters_WhenJudgementByAdmissionIssuedImmediately() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.All_FINAL_ORDERS_ISSUED);
        JudgmentPaymentPlan paymentPlan3 = new JudgmentPaymentPlan();
        paymentPlan3.setType(PAY_IMMEDIATELY);
        JudgmentDetails activeJudgment3 = new JudgmentDetails();
        activeJudgment3.setState(ISSUED);
        activeJudgment3.setPaymentPlan(paymentPlan3);
        activeJudgment3.setOrderedAmount("150001");
        activeJudgment3.setTotalAmount("150001");
        caseData.setActiveJudgment(activeJudgment3);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("ccjDefendantAdmittedAmount").isEqualTo(BigDecimal.valueOf(1500.01));
        assertThat(result).extracting("ccjPaymentMessageEn").isEqualTo("immediately");
        assertThat(result).extracting("ccjPaymentMessageCy").isEqualTo("ar unwaith");
    }

    @Test
    public void shouldMapParameters_WhenJudgementByAdmissionIssuedPayByDate() {
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);

        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1ResponseDeadline(LocalDate.of(2020, Month.JANUARY, 18).atStartOfDay());
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setCcdState(CaseState.All_FINAL_ORDERS_ISSUED);
        JudgmentPaymentPlan paymentPlan4 = new JudgmentPaymentPlan();
        paymentPlan4.setType(PAY_BY_DATE);
        paymentPlan4.setPaymentDeadlineDate(LocalDate.of(2050, Month.AUGUST, 19));
        JudgmentDetails activeJudgment4 = new JudgmentDetails();
        activeJudgment4.setState(ISSUED);
        activeJudgment4.setPaymentPlan(paymentPlan4);
        activeJudgment4.setOrderedAmount("150001");
        activeJudgment4.setTotalAmount("150001");
        caseData.setActiveJudgment(activeJudgment4);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("ccjDefendantAdmittedAmount").isEqualTo(BigDecimal.valueOf(1500.01));
        assertThat(result).extracting("ccjPaymentMessageEn").isEqualTo("by 19 August 2050");
        assertThat(result).extracting("ccjPaymentMessageCy").isEqualTo("erbyn 19 Awst 2050");
    }

    @Test
    public void shouldMapParameters_WhenResponseDeadlineAndClaimFeeIsNull() {

        caseData.setRespondent1ResponseDeadline(null);
        caseData.setRespondToAdmittedClaimOwingAmountPounds(null);
        caseData.setRespondToClaimAdmitPartLRspec(null);
        caseData.setClaimFee(null);
        caseData.setRespondent1RespondToSettlementAgreementDeadline(null);
        caseData.setApplicant1ResponseDeadline(null);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).doesNotContainEntry("respondent1ResponseDeadlineEn", null);
        assertThat(result).doesNotContainEntry("respondent1ResponseDeadlineCy", null);

        assertThat(result).doesNotContainEntry("defendantAdmittedAmount", null);

        assertThat(result).doesNotContainEntry("defendantAdmittedAmount", null);

        assertThat(result).doesNotContainEntry("respondent1AdmittedAmountPaymentDeadlineEn", null);
        assertThat(result).doesNotContainEntry("respondent1AdmittedAmountPaymentDeadlineCy", null);

        assertThat(result).doesNotContainEntry("respondent1SettlementAgreementDeadlineEn", null);
        assertThat(result).doesNotContainEntry("respondent1SettlementAgreementDeadlineCy", null);

        assertThat(result).doesNotContainEntry("claimFee", null);

        assertThat(result).doesNotContainEntry("applicant1ResponseDeadlineEn", null);
        assertThat(result).doesNotContainEntry("applicant1ResponseDeadlineEn", null);

    }

    @Test
    public void shouldMapCaseSettleAmountAndCaseSettledDateWheResponseTypeIsFullDefence() {

        caseData.setRespondent1ResponseDeadline(null);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        RespondToClaim respondToClaim = new RespondToClaim();
        respondToClaim.setHowMuchWasPaid(new BigDecimal("100050"));
        respondToClaim.setWhenWasThisAmountPaid(LocalDate.parse("2023-03-29"));
        caseData.setRespondToClaim(respondToClaim);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimSettledAmount").isEqualTo("£1000.50");
        assertThat(result).extracting("claimSettledObjectionsDeadline").isEqualTo(LocalDate.parse("2023-04-17")
            .atTime(END_OF_DAY));
        assertThat(result).extracting("claimSettledDateEn").isEqualTo("29 March 2023");
        assertThat(result).extracting("claimSettledDateCy").isEqualTo("29 Mawrth 2023");
    }

    @Test
    public void shouldMapCaseSettleAmountAndCaseSettledDateWhenResponseTypeIsPartAdmit() {

        caseData.setRespondent1ResponseDeadline(null);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        RespondToClaim respondToAdmittedClaim = new RespondToClaim();
        respondToAdmittedClaim.setHowMuchWasPaid(new BigDecimal("100055"));
        respondToAdmittedClaim.setWhenWasThisAmountPaid(LocalDate.parse("2023-03-29"));
        caseData.setRespondToAdmittedClaim(respondToAdmittedClaim);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimSettledAmount").isEqualTo("£1000.55");
        assertThat(result).extracting("claimSettledDateEn").isEqualTo("29 March 2023");
        assertThat(result).extracting("claimSettledDateCy").isEqualTo("29 Mawrth 2023");
    }

    @Test
    public void shouldMapParameters_whenHwFPartRemissionGranted() {
        caseData.setHwfFeeType(FeeType.CLAIMISSUED);
        HelpWithFeesDetails hwfDetails = new HelpWithFeesDetails();
        hwfDetails.setRemissionAmount(BigDecimal.valueOf(2500));
        hwfDetails.setOutstandingFeeInPounds(BigDecimal.valueOf(100));
        caseData.setClaimIssuedHwfDetails(hwfDetails);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimIssueRemissionAmount").isEqualTo("£25");
        assertThat(result).extracting("claimIssueOutStandingAmount").isEqualTo("£100");
    }

    @Test
    public void shouldMapParameters_whenClaimantSubmitSettlmentEvent() {
        caseData.setHwfFeeType(FeeType.CLAIMISSUED);
        CaseDataLiP caseDataLiP2 = new CaseDataLiP();
        caseDataLiP2.setApplicant1ClaimSettledDate(LocalDate.of(2024, 3, 19));
        caseData.setCaseDataLiP(caseDataLiP2);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicant1ClaimSettledDateEn").isEqualTo("19 March 2024");
        assertThat(result).extracting("applicant1ClaimSettledDateCy").isEqualTo("19 Mawrth 2024");
    }

    @Test
    public void shouldMapParameters_whenRepaymentPlanIsSet() {
        LocalDate date = LocalDate.of(2024, Month.FEBRUARY, 22);

        RepaymentPlanLRspec repaymentPlan = new RepaymentPlanLRspec();
        repaymentPlan.setFirstRepaymentDate(date);
        repaymentPlan.setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK);
        repaymentPlan.setPaymentAmount(new BigDecimal(1000));
        caseData.setRespondent1RepaymentPlan(repaymentPlan);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("instalmentAmount").isEqualTo("£10");
        assertThat(result).extracting("paymentFrequency").isEqualTo("every week");
        assertThat(result).extracting("paymentFrequencyWelsh").isEqualTo("bob wythnos");
        assertThat(result).extracting("instalmentStartDateEn").isEqualTo(DateUtils.formatDate(date));
        assertThat(result).extracting("instalmentStartDateCy").isEqualTo(DateUtils.formatDateInWelsh(date, false));
    }

    @Test
    public void shouldMapParameters_whenHearingDueDate() {
        LocalDate date = LocalDate.of(2024, Month.MARCH, 22);
        caseData.setHearingDueDate(date);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("hearingDueDateEn").isEqualTo("22 March 2024");
        assertThat(result).extracting("hearingDueDateCy").isEqualTo("22 Mawrth 2024");
    }

    @Test
    public void shouldMapParameters_whenStatesPaidInFull() {
        caseData.setRespondent1ResponseDeadline(null);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1ResponseDeadline(LocalDate.parse("2020-03-29").atStartOfDay());
        caseData.setRespondToClaim(new RespondToClaim());

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicant1ResponseDeadlineEn").isEqualTo("29 March 2020");
        assertThat(result).extracting("applicant1ResponseDeadlineCy")
            .isEqualTo("29 Mawrth 2020");
    }

    @Test
    public void shouldMapParameters_whenHearingFeeHwFPartRemissionGranted() {
        caseData.setHwfFeeType(FeeType.HEARING);
        HelpWithFeesDetails hearingHwfDetails = new HelpWithFeesDetails();
        hearingHwfDetails.setRemissionAmount(BigDecimal.valueOf(2500));
        hearingHwfDetails.setOutstandingFeeInPounds(BigDecimal.valueOf(100));
        caseData.setHearingHwfDetails(hearingHwfDetails);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("hearingFeeRemissionAmount").isEqualTo("£25");
        assertThat(result).extracting("hearingFeeOutStandingAmount").isEqualTo("£100");
    }

    @ParameterizedTest
    @EnumSource(value = CaseEvent.class, names = {
        "CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT",
        "CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT",
        "UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT",
        "UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT"
    })
    void shouldMapOrderParameters(CaseEvent caseEvent) {
        List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
        finalCaseDocuments.add(element(generateOrder(JUDGE_FINAL_ORDER)));
        caseData.setFinalOrderDocumentCollection(finalCaseDocuments);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData, caseEvent);

        assertThat(result).extracting("orderDocument").isEqualTo("binary-url");
    }

    @ParameterizedTest
    @EnumSource(value = CaseEvent.class, names = {
        "CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT",
        "CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT",
        "UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT",
        "UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT"
    })
    void shouldNotThrowExceptionWhenNoFinalOrders(CaseEvent caseEvent) {
        caseData.setFinalOrderDocumentCollection(new ArrayList<>());

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData, caseEvent);

        assertThat(result).doesNotContainKey("orderDocument");
    }

    @ParameterizedTest
    @EnumSource(value = CaseEvent.class, names = {
        "CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT",
        "CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT",
        "UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT",
        "UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT"
    })
    void shouldNotThrowExceptionWhenFinalOrdersNull(CaseEvent caseEvent) {
        caseData.setFinalOrderDocumentCollection(null);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData, caseEvent);

        assertThat(result).doesNotContainKey("orderDocument");
    }

    @Test
    void shouldMapOrderParameters_whenEventIsSdoDj() {
        List<Element<CaseDocument>> sdoDjCaseDocuments = new ArrayList<>();
        sdoDjCaseDocuments.add(element(generateOrder(SDO_ORDER)));
        caseData.setOrderSDODocumentDJCollection(sdoDjCaseDocuments);

        Map<String, Object> resultClaimant =
            mapper.mapCaseDataToParams(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT);
        Map<String, Object> resultDefendant =
            mapper.mapCaseDataToParams(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT);

        assertThat(resultClaimant).extracting("orderDocument").isEqualTo("binary-url");
        assertThat(resultDefendant).extracting("orderDocument").isEqualTo("binary-url");
    }

    @Test
    void shouldMapOrderParameters_whenEventIsSdo() {
        List<Element<CaseDocument>> systemGeneratedDocuments = new ArrayList<>();
        systemGeneratedDocuments.add(element(generateOrder(SDO_ORDER)));
        List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
        preTranslationDocuments.add(element(generateOrder(SDO_ORDER)));
        caseData.setSystemGeneratedCaseDocuments(systemGeneratedDocuments);
        caseData.setPreTranslationDocuments(preTranslationDocuments);

        Map<String, Object> resultClaimant =
            mapper.mapCaseDataToParams(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT);
        Map<String, Object> resultDefendant =
            mapper.mapCaseDataToParams(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT);

        assertThat(resultClaimant).extracting("orderDocument").isEqualTo("binary-url");
        assertThat(resultDefendant).extracting("orderDocument").isEqualTo("binary-url");
        assertThat(resultClaimant).extracting("hiddenOrderDocument").isEqualTo("binary-url");
        assertThat(resultDefendant).extracting("hiddenOrderDocument").isEqualTo("binary-url");
    }

    @Test
    void shouldReturnNull_whenEventIsIncorrect() {
        List<Element<CaseDocument>> systemGeneratedDocuments = new ArrayList<>();
        systemGeneratedDocuments.add(element(generateOrder(SDO_ORDER)));
        caseData.setSystemGeneratedCaseDocuments(systemGeneratedDocuments);

        Map<String, Object> result =
            mapper.mapCaseDataToParams(caseData, CaseEvent.ADD_CASE_NOTE);
        assertThat(result).doesNotContainEntry("orderDocument", null);
    }

    @Test
    void shouldReturnNull_whenEventIsNull() {
        List<Element<CaseDocument>> systemGeneratedDocuments = new ArrayList<>();
        systemGeneratedDocuments.add(element(generateOrder(SDO_ORDER)));
        caseData.setSystemGeneratedCaseDocuments(systemGeneratedDocuments);

        Map<String, Object> result =
            mapper.mapCaseDataToParams(caseData, null);
        assertThat(result).doesNotContainEntry("orderDocument", null);
    }

    @Test
    void shouldReturnEmptyMap_whenCaseDataIsNull() {
        Map<String, Object> result = mapper.mapCaseDataToParams(null);
        assertThat(result).isEmpty();

        Map<String, Object> resultWithEvent = mapper.mapCaseDataToParams(null, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT);
        assertThat(resultWithEvent).isEmpty();
    }

    @Test
    void shouldNotThrowException_whenDjSdoCollectionIsEmpty() {
        caseData.setOrderSDODocumentDJCollection(new ArrayList<>());
        Map<String, Object> result = mapper.mapCaseDataToParams(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT);
        assertThat(result).doesNotContainKey("orderDocument");
    }

    @Test
    void shouldNotThrowException_whenDjSdoCollectionIsNull() {
        caseData.setOrderSDODocumentDJCollection(null);
        Map<String, Object> result = mapper.mapCaseDataToParams(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT);
        assertThat(result).doesNotContainKey("orderDocument");
    }

    @Test
    void shouldNotThrowException_whenSdoDocumentIsNull() {
        caseData.setSdoOrderDocument(null);
        Map<String, Object> result = mapper.mapCaseDataToParams(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT);
        assertThat(result).doesNotContainKey("orderDocument");
    }

    @Test
    public void shouldMapParameters_whenHearingFast() {
        caseData.setRespondent1ResponseDeadline(null);
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        caseData.setApplicant1ResponseDeadline(LocalDate.parse("2020-03-29").atStartOfDay());
        caseData.setRespondToClaim(new RespondToClaim());
        caseData.setDrawDirectionsOrderRequired(YES);
        caseData.setDrawDirectionsOrderSmallClaims(YesOrNo.NO);
        caseData.setOrderType(OrderType.DECIDE_DAMAGES);
        FastTrackDisclosureOfDocuments fastTrackDisclosure = new FastTrackDisclosureOfDocuments();
        LocalDate date = LocalDate.now();
        fastTrackDisclosure.setDate3(date);
        caseData.setFastTrackDisclosureOfDocuments(fastTrackDisclosure);

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("sdoDocumentUploadRequestedDateEn")
            .isEqualTo(DateUtils.formatDate(date));
        assertThat(result).extracting("sdoDocumentUploadRequestedDateCy")
            .isEqualTo(DateUtils.formatDateInWelsh(date, false));
    }

    @Test
    public void shouldContainDefaultNotificationsDeadline() {
        LocalDate today = LocalDate.now();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("priorityNotificationDeadline")
            .asInstanceOf(InstanceOfAssertFactories.LOCAL_DATE_TIME).isBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    @Test
    void shouldMapParameters_whenCertOfSc() {
        LocalDate fullPaymentDate = LocalDate.now();
        CaseData currentCaseData = CaseDataBuilder.builder().atCaseProgressionCheck().build();
        currentCaseData.setApplicant1Represented(YesOrNo.NO);
        CertOfSC certOfSC = new CertOfSC();
        certOfSC.setDefendantFinalPaymentDate(fullPaymentDate);
        currentCaseData.setCertOfSC(certOfSC);

        Map<String, Object> result =
            mapper.mapCaseDataToParams(currentCaseData, null);
        assertThat(result).extracting("coscFullPaymentDateEn")
            .isEqualTo(DateUtils.formatDate(fullPaymentDate));
        assertThat(result).extracting("coscFullPaymentDateCy")
            .isEqualTo(DateUtils.formatDateInWelsh(fullPaymentDate, false));
        assertThat(result).extracting("coscNotificationDateEn")
            .isEqualTo(DateUtils.formatDate(fullPaymentDate));
        assertThat(result).extracting("coscNotificationDateCy")
            .isEqualTo(DateUtils.formatDateInWelsh(fullPaymentDate, false));
    }

    @Test
    void shouldMapParameters_whenClaimantMarkedPaidInFull() {
        caseData.setLegacyCaseReference("reference");
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent1Represented(YesOrNo.NO);
        caseData.setMarkPaidConsent(MarkPaidConsentList.YES);

        LocalDate markedPaidInFullDate = LocalDate.now();
        Map<String, Object> result = mapper.mapCaseDataToParams(caseData, null);
        assertThat(result).extracting("settleClaimPaidInFullDateEn").isEqualTo(DateUtils.formatDate(markedPaidInFullDate));
        assertThat(result).extracting("settleClaimPaidInFullDateCy").isEqualTo(DateUtils.formatDateInWelsh(
            markedPaidInFullDate, false));
    }
}

