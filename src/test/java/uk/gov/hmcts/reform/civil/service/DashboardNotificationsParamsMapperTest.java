package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class DashboardNotificationsParamsMapperTest {

    private DashboardNotificationsParamsMapper mapper;

    private CaseData caseData;

    @BeforeEach
    void setup() {
        mapper = new DashboardNotificationsParamsMapper();
        caseData = CaseDataBuilder.builder().atStateTrialReadyCheck().build();
    }

    public CaseDocument generateOrder(DocumentType documentType) {
        return CaseDocument.builder()
            .createdBy("Test")
            .documentName("document test name")
            .documentSize(0L)
            .documentType(documentType)
            .createdDatetime(LocalDateTime.of(2024, Month.APRIL, 04, 14, 14))
            .documentLink(Document.builder()
                              .documentUrl("fake-url")
                              .documentFileName("file-name.pdf")
                              .documentBinaryUrl("binary-url")
                              .build())
            .build();
    }

    @Test
    public void shouldMapAllParameters_WhenIsRequested() {

        LocalDate date = LocalDate.of(2024, Month.FEBRUARY, 22);

        LocalDateTime applicant1ResponseDeadline = LocalDateTime.of(2024, 3, 21, 16, 0);
        caseData = caseData.toBuilder()
            .hwfFeeType(FeeType.CLAIMISSUED)
            .totalClaimAmount(BigDecimal.valueOf(124.67))
            .respondToAdmittedClaimOwingAmountPounds(BigDecimal.valueOf(100))
            .respondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec(date))
            .respondent1RespondToSettlementAgreementDeadline(LocalDateTime.now())
            .applicant1AcceptFullAdmitPaymentPlanSpec(YES)
            .caseDataLiP(CaseDataLiP.builder().applicant1ClaimSettledDate(LocalDate.now()).build())
            .applicant1ResponseDeadline(applicant1ResponseDeadline)
            .hearingDate(LocalDate.of(2024, 4, 1))
            .hearingDueDate(LocalDate.of(2024, 4, 1))
            .hearingFee(new Fee(new BigDecimal(10000), "Test", "Test"))
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .hearingLocationCourtName("County Court")
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimFee").isEqualTo("£1");

        assertThat(result).extracting("ccdCaseReference").isEqualTo(1594901956117591L);

        assertThat(result).extracting("defaultRespondTime").isEqualTo("4pm");

        assertThat(result).extracting("defendantAdmittedAmount").isEqualTo("£100");

        assertThat(result).extracting("respondent1AdmittedAmountPaymentDeadlineEn")
            .isEqualTo(DateUtils.formatDate(date));

        assertThat(result).extracting("respondent1AdmittedAmountPaymentDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(date));

        assertThat(result).extracting("applicant1ResponseDeadlineEn")
            .isEqualTo(DateUtils.formatDate(applicant1ResponseDeadline.toLocalDate()));
        assertThat(result).extracting("applicant1ResponseDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(applicant1ResponseDeadline.toLocalDate()));
        assertThat(result).extracting("respondent1ResponseDeadlineEn")
            .isEqualTo(DateUtils.formatDate(LocalDate.now().plusDays(14L)));
        assertThat(result).extracting("respondent1ResponseDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(LocalDate.now().plusDays(14L)));
        assertThat(result).extracting("respondent1PartyName")
            .isEqualTo(caseData.getRespondent1().getPartyName());

        assertThat(result).extracting("typeOfFee").isEqualTo("claim");
        assertThat(result).extracting("respondent1SettlementAgreementDeadlineEn")
            .isEqualTo(DateUtils.formatDate(LocalDateTime.now()));

        assertThat(result).extracting("respondent1SettlementAgreementDeadlineCy")
            .isEqualTo(DateUtils.formatDateInWelsh(LocalDate.now()));

        assertThat(result).extracting("claimantSettlementAgreement").isEqualTo("accepted");
        assertThat(result).extracting("applicant1ClaimSettledDateEn")
            .isEqualTo(DateUtils.formatDate(LocalDateTime.now()));

        assertThat(result).extracting("applicant1ClaimSettledDateCy")
            .isEqualTo(DateUtils.formatDateInWelsh(LocalDate.now()));

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
        assertThat(result).extracting("hearingDueDateEn")
            .isEqualTo("1 April 2024");
        assertThat(result).extracting("hearingDueDateCy")
            .isEqualTo("1 Ebrill 2024");
        assertThat(result).extracting("hearingFee")
            .isEqualTo("£100");
    }

    @Test
    public void shouldMapParameters_WhenResponseDeadlineAndClaimFeeIsNull() {

        caseData = caseData.toBuilder()
            .respondent1ResponseDeadline(null)
            .respondToAdmittedClaimOwingAmountPounds(null)
            .respondToClaimAdmitPartLRspec(null)
            .respondent1ResponseDeadline(null)
            .claimFee(null)
            .respondent1RespondToSettlementAgreementDeadline(null)
            .applicant1ResponseDeadline(null)
            .build();

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

        caseData = caseData.toBuilder().respondent1ResponseDeadline(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .respondToClaim(RespondToClaim.builder()
                                .howMuchWasPaid(new BigDecimal("100050"))
                                .whenWasThisAmountPaid(LocalDate.parse("2023-03-29"))
                                .build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimSettledAmount").isEqualTo("£1000.50");
        assertThat(result).extracting("claimSettledDateEn").isEqualTo("29 March 2023");
        assertThat(result).extracting("claimSettledDateCy").isEqualTo("29 Mawrth 2023");
    }

    @Test
    public void shouldMapCaseSettleAmountAndCaseSettledDateWhenResponseTypeIsPartAdmit() {

        caseData = caseData.toBuilder().respondent1ResponseDeadline(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondToAdmittedClaim(RespondToClaim.builder()
                                        .howMuchWasPaid(new BigDecimal("100055"))
                                        .whenWasThisAmountPaid(LocalDate.parse("2023-03-29"))
                                        .build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimSettledAmount").isEqualTo("£1000.55");
        assertThat(result).extracting("claimSettledDateEn").isEqualTo("29 March 2023");
        assertThat(result).extracting("claimSettledDateCy").isEqualTo("29 Mawrth 2023");
    }

    @Test
    public void shouldMapParameters_whenHwFPartRemissionGranted() {
        caseData = caseData.toBuilder().hwfFeeType(FeeType.CLAIMISSUED)
            .claimIssuedHwfDetails(HelpWithFeesDetails.builder().remissionAmount(BigDecimal.valueOf(2500))
                                       .outstandingFeeInPounds(BigDecimal.valueOf(100)).build()).build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("claimIssueRemissionAmount").isEqualTo("£25");
        assertThat(result).extracting("claimIssueOutStandingAmount").isEqualTo("£100");
    }

    @Test
    public void shouldMapParameters_whenClaimantSubmitSettlmentEvent() {
        caseData = caseData.toBuilder().hwfFeeType(FeeType.CLAIMISSUED)
            .caseDataLiP(CaseDataLiP.builder().applicant1ClaimSettledDate(LocalDate.of(2024, 3, 19)).build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicant1ClaimSettledDateEn").isEqualTo("19 March 2024");
        assertThat(result).extracting("applicant1ClaimSettledDateCy").isEqualTo("19 Mawrth 2024");
    }

    @Test
    public void shouldMapParameters_whenRepaymentPlanIsSet() {
        LocalDate date = LocalDate.of(2024, Month.FEBRUARY, 22);

        caseData = caseData.toBuilder().respondent1RepaymentPlan(
            RepaymentPlanLRspec.builder()
                .firstRepaymentDate(date)
                .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                .paymentAmount(new BigDecimal(1000))
                .build()).build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("instalmentAmount").isEqualTo("£10");
        assertThat(result).extracting("instalmentTimePeriodEn").isEqualTo("week");
        assertThat(result).extracting("instalmentTimePeriodCy").isEqualTo("week");
        assertThat(result).extracting("instalmentStartDateEn").isEqualTo(DateUtils.formatDate(date));
        assertThat(result).extracting("instalmentStartDateCy").isEqualTo(DateUtils.formatDateInWelsh(date));
    }

    @Test
    public void shouldMapParameters_whenHearingDueDate() {
        LocalDate date = LocalDate.of(2024, Month.MARCH, 22);
        caseData = caseData.toBuilder().hearingDueDate(date)
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("hearingDueDateEn").isEqualTo("22 March 2024");
        assertThat(result).extracting("hearingDueDateCy").isEqualTo("22 Mawrth 2024");
    }

    @Test
    public void shouldMapParameters_whenStatesPaidInFull() {
        caseData = caseData.toBuilder()
            .respondent1ResponseDeadline(null)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .applicant1ResponseDeadline(LocalDate.parse("2020-03-29").atStartOfDay())
            .respondToClaim(RespondToClaim.builder()
                                .build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicant1ResponseDeadlineEn").isEqualTo("29 March 2020");
        assertThat(result).extracting("applicant1ResponseDeadlineCy")
            .isEqualTo("29 Mawrth 2020");
    }

    @Test
    public void shouldMapParameters_whenHearingFeeHwFPartRemissionGranted() {
        caseData = caseData.toBuilder().hwfFeeType(FeeType.HEARING)
            .hearingHwfDetails(HelpWithFeesDetails.builder().remissionAmount(BigDecimal.valueOf(2500))
                                       .outstandingFeeInPounds(BigDecimal.valueOf(100)).build()).build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("hearingFeeRemissionAmount").isEqualTo("£25");
        assertThat(result).extracting("hearingFeeOutStandingAmount").isEqualTo("£100");
    }

    @Test
    void shouldMapOrderParameters_whenEventIsFinalOrder() {
        List<Element<CaseDocument>> finalCaseDocuments = new ArrayList<>();
        finalCaseDocuments.add(element(generateOrder(JUDGE_FINAL_ORDER)));
        caseData = caseData.toBuilder().finalOrderDocumentCollection(finalCaseDocuments).build();

        Map<String, Object> resultClaimant =
            mapper.getMapWithDocumentInfo(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT);
        Map<String, Object> resultDefendant =
            mapper.getMapWithDocumentInfo(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT);

        assertThat(resultClaimant).extracting("orderDocument").isEqualTo("binary-url");
        assertThat(resultDefendant).extracting("orderDocument").isEqualTo("binary-url");
    }

    @Test
    void shouldMapOrderParameters_whenEventIsSdoDj() {
        List<Element<CaseDocument>> sdoDjCaseDocuments = new ArrayList<>();
        sdoDjCaseDocuments.add(element(generateOrder(SDO_ORDER)));
        caseData = caseData.toBuilder().orderSDODocumentDJCollection(sdoDjCaseDocuments).build();

        Map<String, Object> resultClaimant =
            mapper.getMapWithDocumentInfo(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT);
        Map<String, Object> resultDefendant =
            mapper.getMapWithDocumentInfo(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT);

        assertThat(resultClaimant).extracting("orderDocument").isEqualTo("binary-url");
        assertThat(resultDefendant).extracting("orderDocument").isEqualTo("binary-url");
    }

    @Test
    void shouldMapOrderParameters_whenEventIsSdo() {
        List<Element<CaseDocument>> systemGeneratedDocuments = new ArrayList<>();
        systemGeneratedDocuments.add(element(generateOrder(SDO_ORDER)));
        caseData = caseData.toBuilder().systemGeneratedCaseDocuments(systemGeneratedDocuments).build();

        Map<String, Object> resultClaimant =
            mapper.getMapWithDocumentInfo(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT);
        Map<String, Object> resultDefendant =
            mapper.getMapWithDocumentInfo(caseData, CaseEvent.CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT);

        assertThat(resultClaimant).extracting("orderDocument").isEqualTo("binary-url");
        assertThat(resultDefendant).extracting("orderDocument").isEqualTo("binary-url");
    }

    @Test
    void shouldThrowException_whenEventIsIncorrect() {
        List<Element<CaseDocument>> systemGeneratedDocuments = new ArrayList<>();
        systemGeneratedDocuments.add(element(generateOrder(SDO_ORDER)));
        caseData = caseData.toBuilder().systemGeneratedCaseDocuments(systemGeneratedDocuments).build();
        CaseEvent event = CaseEvent.CREATE_DASHBOARD_NOTIFICATION_HEARING_SCHEDULED_CLAIMANT;

        assertThatThrownBy(() -> mapper.getMapWithDocumentInfo(caseData, event))
            .isInstanceOf(IllegalArgumentException.class)
            .hasNoCause()
            .hasMessage("Invalid caseEvent in " + event);
    }
}

