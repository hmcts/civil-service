package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;

@ExtendWith(MockitoExtension.class)
public class GaDashboardNotificationsParamsMapperTest {

    public static final String CUSTOMER_REFERENCE = "12345";
    private GeneralApplicationCaseData caseData;
    @InjectMocks
    private GaDashboardNotificationsParamsMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new GaDashboardNotificationsParamsMapper();
        caseData = GeneralApplicationCaseDataBuilder.builder().build();
    }

    @Test
    void shouldMapAllParametersWhenIsRequested() {
        LocalDateTime deadline = LocalDateTime.of(2024, 3, 21, 16, 0);
        LocalDate requestMoreInfoDate = LocalDate.of(2024, 9, 4);
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .generalAppNotificationDeadlineDate(deadline)
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .generalAppPBADetails(
                GAPbaDetails.builder()
                    .fee(
                        Fee.builder()
                            .code("FE203")
                            .calculatedAmountInPence(BigDecimal.valueOf(27500))
                            .version("1")
                            .build())
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .generalAppSuperClaimType("SPEC_CLAIM")
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder().requestMoreInfoOption(
                REQUEST_MORE_INFORMATION).judgeRequestMoreInfoByDate(requestMoreInfoDate).build()).build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicationFee").isEqualTo("£275.00");
        assertThat(result).extracting("generalAppNotificationDeadlineDate").isEqualTo(deadline);
        assertThat(result).extracting("generalAppNotificationDeadlineDateEn").isEqualTo("21 March 2024");
        assertThat(result).extracting("generalAppNotificationDeadlineDateCy").isEqualTo("21 Mawrth 2024");
        assertThat(result).extracting("judgeRequestMoreInfoByDate").isEqualTo(requestMoreInfoDate.atTime(
            END_OF_BUSINESS_DAY));
        assertThat(result).extracting("judgeRequestMoreInfoByDateEn").isEqualTo("4 September 2024");
        assertThat(result).extracting("judgeRequestMoreInfoByDateCy").isEqualTo("4 Medi 2024");
    }

    @Test
    void shouldMapWrittenRepSequentialDeadlinesClaimantIsApplicantWhenIsRequested() {
        LocalDate claimantDate = LocalDate.of(2024, 3, 1);
        LocalDate defendantDate = LocalDate.of(2024, 3, 2);
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .parentClaimantIsApplicant(YesOrNo.YES)
            .judicialDecisionMakeAnOrderForWrittenRepresentations(
                GAJudicialWrittenRepresentations.builder()
                    .writtenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
                    .sequentialApplicantMustRespondWithin(claimantDate)
                    .writtenSequentailRepresentationsBy(defendantDate).build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("writtenRepApplicantDeadline").isEqualTo(claimantDate.atTime(END_OF_BUSINESS_DAY));
        assertThat(result).extracting("writtenRepApplicantDeadlineDateEn").isEqualTo("1 March 2024");
        assertThat(result).extracting("writtenRepApplicantDeadlineDateCy").isEqualTo("1 Mawrth 2024");
        assertThat(result).extracting("writtenRepRespondentDeadline").isEqualTo(defendantDate.atTime(END_OF_BUSINESS_DAY));
        assertThat(result).extracting("writtenRepRespondentDeadlineDateEn").isEqualTo("2 March 2024");
        assertThat(result).extracting("writtenRepRespondentDeadlineDateCy").isEqualTo("2 Mawrth 2024");
    }

    @Test
    void shouldMapWrittenRepSequentialDeadlinesDefendantIsApplicantWhenIsRequested() {
        LocalDate claimantDate = LocalDate.of(2024, 3, 1);
        LocalDate defendantDate = LocalDate.of(2024, 3, 2);
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .parentClaimantIsApplicant(YesOrNo.NO)
            .judicialDecisionMakeAnOrderForWrittenRepresentations(
                GAJudicialWrittenRepresentations.builder()
                    .writtenOption(GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
                    .sequentialApplicantMustRespondWithin(claimantDate)
                    .writtenSequentailRepresentationsBy(defendantDate).build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("writtenRepApplicantDeadlineDateEn").isEqualTo("2 March 2024");
        assertThat(result).extracting("writtenRepApplicantDeadlineDateCy").isEqualTo("2 Mawrth 2024");
        assertThat(result).extracting("writtenRepRespondentDeadlineDateEn").isEqualTo("1 March 2024");
        assertThat(result).extracting("writtenRepRespondentDeadlineDateCy").isEqualTo("1 Mawrth 2024");
    }

    @Test
    void shouldMapWrittenRepConcurrentDeadlinesWhenIsRequested() {
        LocalDate date = LocalDate.of(2024, 3, 1);
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionMakeAnOrderForWrittenRepresentations(
                GAJudicialWrittenRepresentations.builder()
                    .writtenOption(GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS)
                    .writtenConcurrentRepresentationsBy(date).build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("writtenRepApplicantDeadlineDateEn").isEqualTo("1 March 2024");
        assertThat(result).extracting("writtenRepApplicantDeadlineDateCy").isEqualTo("1 Mawrth 2024");
        assertThat(result).extracting("writtenRepRespondentDeadlineDateEn").isEqualTo("1 March 2024");
        assertThat(result).extracting("writtenRepRespondentDeadlineDateCy").isEqualTo("1 Mawrth 2024");
    }

    @Test
    void shouldMapParametersWhenHwfApplicationFeeIsRequested() {
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdState(AWAITING_APPLICATION_PAYMENT)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .generalAppSuperClaimType("SPEC_CLAIM")
            .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).helpWithFeesReferenceNumber(
                "HWF-A1B-23C").build())
            .hwfFeeType(FeeType.APPLICATION)
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicationFeeTypeEn").isEqualTo("application");
        assertThat(result).extracting("applicationFeeTypeCy").isEqualTo("gwneud cais");
    }

    @Test
    void shouldMapParametersWhenHwfApplicationFeeIsRequestedAndMoreInfoRequired() {
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdState(AWAITING_APPLICATION_PAYMENT)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .generalAppSuperClaimType("SPEC_CLAIM")
            .hwfFeeType(FeeType.APPLICATION)
            .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).helpWithFeesReferenceNumber(
                "HWF-A1B-23C").build())
            .gaHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(CaseEvent.MORE_INFORMATION_HWF_GA)
                              .remissionAmount(BigDecimal.valueOf(7500))
                              .outstandingFee(new BigDecimal("200.00")).build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicationFeeTypeEn").isEqualTo("application");
        assertThat(result).extracting("applicationFeeTypeCy").isEqualTo("gwneud cais");
    }

    @Test
    void shouldMapParametersWhenHwfAdditionalApplicationFeeIsRequestedAndMoreInfoRequired() {
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdState(APPLICATION_ADD_PAYMENT)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .generalAppSuperClaimType("SPEC_CLAIM")
            .hwfFeeType(FeeType.ADDITIONAL)
            .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).helpWithFeesReferenceNumber(
                "HWF-A1B-23C").build())
            .additionalHwfDetails(HelpWithFeesDetails.builder()
                                      .hwfCaseEvent(CaseEvent.MORE_INFORMATION_HWF_GA)
                                      .remissionAmount(BigDecimal.valueOf(7500))
                                      .outstandingFee(new BigDecimal("200.00")).build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicationFeeTypeEn").isEqualTo("additional application");
        assertThat(result).extracting("applicationFeeTypeCy").isEqualTo("ychwanegol i wneud cais");
    }

    @Test
    void shouldMapParametersWhenHwfApplicationFeeIsRequestedAndIsPartAdmitted_PartRemission() {
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdState(AWAITING_APPLICATION_PAYMENT)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .generalAppSuperClaimType("SPEC_CLAIM")
            .hwfFeeType(FeeType.APPLICATION)
            .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).helpWithFeesReferenceNumber(
                "HWF-A1B-23C").build())
            .gaHwfDetails(HelpWithFeesDetails.builder().hwfCaseEvent(CaseEvent.PARTIAL_REMISSION_HWF_GA)
                              .remissionAmount(BigDecimal.valueOf(7500))
                              .outstandingFee(new BigDecimal("200.00")).build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicationFeeTypeEn").isEqualTo("application");
        assertThat(result).extracting("applicationFeeTypeCy").isEqualTo("gwneud cais");
        assertThat(result).extracting("remissionAmount").isEqualTo("£75.00");
        assertThat(result).extracting("outstandingFeeInPounds").isEqualTo("£2.00");
    }

    @Test
    void shouldMapParametersWhenHwfAdditionalApplicationFeeIsRequestedAndIsPartAdmitted_PartRemission() {
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdState(APPLICATION_ADD_PAYMENT)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .generalAppSuperClaimType("SPEC_CLAIM")
            .hwfFeeType(FeeType.ADDITIONAL)
            .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).helpWithFeesReferenceNumber(
                "HWF-A1B-23C").build())
            .additionalHwfDetails(HelpWithFeesDetails.builder()
                                      .hwfCaseEvent(CaseEvent.PARTIAL_REMISSION_HWF_GA).remissionAmount(BigDecimal.valueOf(
                    7500))
                                      .outstandingFee(new BigDecimal("200.00")).build())
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicationFeeTypeEn").isEqualTo("additional application");
        assertThat(result).extracting("applicationFeeTypeCy").isEqualTo("ychwanegol i wneud cais");
        assertThat(result).extracting("remissionAmount").isEqualTo("£75.00");
        assertThat(result).extracting("outstandingFeeInPounds").isEqualTo("£2.00");
    }

    @Test
    void shouldMapParametersWhenHwfApplicationFeeIsRequestedAndCaseStateIsAwaitingApplicationPayment() {
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .generalAppSuperClaimType("SPEC_CLAIM")
            .generalAppType(GAApplicationType.builder().types(List.of(GeneralApplicationTypes.VARY_ORDER))
                                .build())
            .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).helpWithFeesReferenceNumber(
                "HWF-A1B-23C").build())
            .ccdState(CaseState.AWAITING_APPLICATION_PAYMENT)
            .hwfFeeType(FeeType.APPLICATION)
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicationFeeTypeEn").isEqualTo("application");
        assertThat(result).extracting("applicationFeeTypeCy").isEqualTo("gwneud cais");
    }

    @Test
    void shouldMapParametersWhenHwfAdditionalApplicationFeeIsRequested() {
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdState(APPLICATION_ADD_PAYMENT)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).helpWithFeesReferenceNumber(
                "HWF-A1B-23C").build())
            .generalAppSuperClaimType("SPEC_CLAIM")
            .hwfFeeType(FeeType.ADDITIONAL)
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicationFeeTypeEn").isEqualTo("additional application");
        assertThat(result).extracting("applicationFeeTypeCy").isEqualTo("ychwanegol i wneud cais");
    }

    @Test
    void shouldMapParametersWhenHwfAdditionalApplicationFeeIsRequestedAndCaseStateIsApplicationAddPayment() {
        caseData = GeneralApplicationCaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .generalAppSuperClaimType("SPEC_CLAIM")
            .generalAppType(GAApplicationType.builder().types(List.of(GeneralApplicationTypes.VARY_ORDER))
                                .build())
            .generalAppHelpWithFees(HelpWithFees.builder().helpWithFee(YesOrNo.YES).helpWithFeesReferenceNumber(
                "HWF-A1B-23C").build())
            .ccdState(APPLICATION_ADD_PAYMENT)
            .hwfFeeType(FeeType.ADDITIONAL)
            .build();

        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);

        assertThat(result).extracting("applicationFeeTypeEn").isEqualTo("additional application");
        assertThat(result).extracting("applicationFeeTypeCy").isEqualTo("ychwanegol i wneud cais");
    }

    @Test
    void shouldNotMapDataWhenNotPresent() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildMakePaymentsCaseData();
        caseData = caseData.toBuilder().generalAppPBADetails(null).build();
        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);
        assertFalse(result.containsKey("applicationFee"));
        assertFalse(result.containsKey("judgeRequestMoreInfoByDateEn"));
        assertFalse(result.containsKey("applicationFee"));
    }

    @Test
    void shouldMapAllParametersWhenIsRequestedForHearingScheduled() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildCaseWorkerHearingScheduledInfo();
        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);
        assertThat(result).extracting("hearingNoticeApplicationDateEn").isEqualTo("4 September 2024");
        assertThat(result).extracting("hearingNoticeApplicationDateCy").isEqualTo("4 Medi 2024");
    }

    @Test
    void shouldNotMapCaseworkerHearingDateInfoDateNotPresent() {

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().buildMakePaymentsCaseData();
        caseData = caseData.toBuilder().generalAppPBADetails(null).build();
        Map<String, Object> result = mapper.mapCaseDataToParams(caseData);
        assertFalse(result.containsKey("hearingNoticeApplicationDateEn"));
    }
}
