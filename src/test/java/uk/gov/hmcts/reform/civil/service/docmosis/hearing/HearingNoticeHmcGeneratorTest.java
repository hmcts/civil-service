package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.hearing.HearingNoticeHmc;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    HearingNoticeHmcGenerator.class,
    JacksonAutoConfiguration.class
})

class HearingNoticeHmcGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String EPIMS = "venue-id";
    private static final Long VERSION_NUMBER = 1L;

    private HearingGetResponse baseHearing;

    private static final String fileName_application = String.format(
        HEARING_NOTICE_HMC.getDocumentTitle(), REFERENCE_NUMBER);

    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName_application)
        .documentType(HEARING_FORM)
        .build();

    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private HearingNoticeHmcGenerator generator;
    @MockBean
    private LocationRefDataService locationRefDataService;
    @MockBean
    private HearingFeesService hearingFeesService;
    @MockBean
    private AssignCategoryId assignCategoryId;

    @BeforeEach
    void setupTest() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_NOTICE_HMC)))
            .thenReturn(new DocmosisDocument(HEARING_NOTICE_HMC.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        when(locationRefDataService
                 .getCourtLocationsForDefaultJudgments(BEARER_TOKEN)).thenReturn(List.of(LocationRefData.builder()
                                                                                             .epimmsId(EPIMS)
                                                                                             .siteName("SiteName")
                                                                                             .courtAddress(
                                                                                                 "CourtAddress")
                                                                                             .postcode("Postcode")
                                                                                             .build()));

        HearingDay hearingDay = HearingDay.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 01, 01, 12, 0, 0))
            .build();
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 02, 02, 0, 0, 0);
        baseHearing = HearingGetResponse.builder()
            .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                    List.of(
                        HearingDaySchedule.builder()
                            .hearingVenueId(EPIMS)
                            .hearingStartDateTime(hearingDay.getHearingStartDateTime())
                            .hearingEndDateTime(hearingDay.getHearingEndDateTime())
                            .build()))
                                 .receivedDateTime(hearingResponseDate)
                                 .build())
            .requestDetails(HearingRequestDetails.builder()
                                .versionNumber(VERSION_NUMBER)
                                .build())
            .build();
    }

    @Test
    void shouldGenerateHearingNoticeHmc_1v1_whenHearingFeeHasBeenPaid() {

        var hearing = baseHearing.toBuilder()
                    .hearingDetails(HearingDetails.builder()
                                        .hearingType("AAA7-TRI")
                                        .build())
            .build();

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation(EPIMS)
                                        .build())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(PaymentDetails.builder()
                                          .status(PaymentStatus.SUCCESS)
                                          .build())
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(Fee.builder()
                            .calculatedAmountInPence(new BigDecimal(123))
                            .build());

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId");
        var expected = HearingNoticeHmc.builder()
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .claimant(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .feeAmount(null)
            .hearingSiteName("SiteName")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays("01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration("2 days")
            .hearingType("trial")
            .hearingDueDate(null)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .build();

        assertEquals(expected, actual);
    }

    @Test
    void shouldGenerateHearingNoticeHmc_1v1_whenHearingFeeHasNotBeenPaid() {

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-TRI")
                                .build())
            .build();

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation(EPIMS)
                                        .build())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(PaymentDetails.builder()
                                          .status(PaymentStatus.FAILED)
                                          .build())
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(Fee.builder()
                            .calculatedAmountInPence(new BigDecimal(123))
                            .build());

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId");
        var expected = HearingNoticeHmc.builder()
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .claimant(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .feeAmount("£1")
            .hearingSiteName("SiteName")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays("01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration("2 days")
            .hearingType("trial")
            .hearingDueDate(LocalDate.of(2023, 1, 1))
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .build();

        assertEquals(expected, actual);
    }

    @Test
    void shouldGenerateHearingNoticeHmc_1v2DS_whenHearingFeeHasBeenPaid() {

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-DIS")
                                .build())
            .build();

        CaseData caseData = CaseDataBuilder.builder().atState1v2DifferentSolicitorClaimDetailsRespondent2NotifiedTimeExtension()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation(EPIMS)
                                        .build())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(PaymentDetails.builder()
                                          .status(PaymentStatus.SUCCESS)
                                          .build())
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(Fee.builder()
                            .calculatedAmountInPence(new BigDecimal(123))
                            .build());

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId");
        var expected = HearingNoticeHmc.builder()
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .claimant(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .defendant2(caseData.getRespondent2().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .defendant2Reference(caseData.getSolicitorReferences().getRespondentSolicitor2Reference())
            .feeAmount(null)
            .hearingSiteName("SiteName")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays("01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration("2 days")
            .hearingType("hearing")
            .hearingDueDate(null)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .build();

        assertEquals(expected, actual);
    }

    @Test
    void shouldGenerateHearingNoticeHmc_2v1_whenHearingFeeHasBeenPaid() {

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-DIS")
                                .build())
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation(EPIMS)
                                        .build())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(PaymentDetails.builder()
                                          .status(PaymentStatus.SUCCESS)
                                          .build())
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(Fee.builder()
                            .calculatedAmountInPence(new BigDecimal(123))
                            .build());

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId");
        var expected = HearingNoticeHmc.builder()
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .claimant(caseData.getApplicant1().getPartyName())
            .claimant2(caseData.getApplicant2().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .claimant2Reference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .feeAmount(null)
            .hearingSiteName("SiteName")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays("01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration("2 days")
            .hearingType("hearing")
            .hearingDueDate(null)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .build();

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnListOfExpectedCaseDocuments() {

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-TRI")
                                .build())
            .build();

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .baseLocation(EPIMS)
                                        .build())
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(PaymentDetails.builder()
                                          .status(PaymentStatus.SUCCESS)
                                          .build())
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(Fee.builder()
                            .calculatedAmountInPence(new BigDecimal(123))
                            .build());

        var actual = generator.generate(caseData, hearing, BEARER_TOKEN,
                                        "SiteName - CourtAddress - Postcode", "hearingId");
        var expected = List.of(CASE_DOCUMENT);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, HEARING_FORM));

        assertEquals(expected, actual);
    }
}



