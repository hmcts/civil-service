package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.hearing.HearingNoticeHmc;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.sampledata.HearingIndividual;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.hmc.model.hearing.CaseDetailsHearing;
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
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM_WELSH;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC_WELSH;

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

    private static final String FILE_NAME_APPLICATION = String.format(
        HEARING_NOTICE_HMC.getDocumentTitle(), REFERENCE_NUMBER);

    private static final String fileName_application_welsh = String.format(
        HEARING_NOTICE_HMC_WELSH.getDocumentTitle(), REFERENCE_NUMBER);

    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(FILE_NAME_APPLICATION)
        .documentType(HEARING_FORM)
        .build();

    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private HearingNoticeHmcGenerator generator;
    @MockBean
    private LocationReferenceDataService locationRefDataService;
    @MockBean
    private HearingFeesService hearingFeesService;
    @MockBean
    private AssignCategoryId assignCategoryId;
    @MockBean
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setupTest() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_NOTICE_HMC)))
            .thenReturn(new DocmosisDocument(HEARING_NOTICE_HMC.getDocumentTitle(), bytes));
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_NOTICE_HMC_WELSH)))
            .thenReturn(new DocmosisDocument(HEARING_NOTICE_HMC_WELSH.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_APPLICATION, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        when(locationRefDataService
                 .getHearingCourtLocations(BEARER_TOKEN)).thenReturn(List.of(LocationRefData.builder()
                                                                                             .epimmsId(EPIMS)
                                                                                             .externalShortName("VenueName")
                                                                                             .welshExternalShortName("WelshVenueValue")
                                                                                             .siteName("CML-Site")
                                                                                             .courtAddress(
                                                                                                 "CourtAddress")
                                                                                             .postcode("Postcode")
                                                                                             .build()));

        List<HearingIndividual> hearingIndividuals = List.of(
                HearingIndividual.attendingHearingInPerson("Chloe", "Landale"),
                HearingIndividual.attendingHearingByVideo("Michael", "Carver"),
                HearingIndividual.attendingHearingByPhone("Jenny", "Harper"),
                HearingIndividual.nonAttending("James", "Allen")
        );

        HearingDay hearingDay = HearingDay.builder()
            .hearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0))
            .hearingEndDateTime(LocalDateTime.of(2023, 01, 01, 12, 0, 0))
            .build();
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 02, 02, 0, 0, 0);
        baseHearing = HearingGetResponse.builder()
                .partyDetails(hearingIndividuals.stream().map(HearingIndividual::buildPartyDetails).toList())
                .hearingResponse(HearingResponse.builder().hearingDaySchedule(
                                List.of(
                                        HearingDaySchedule.builder()
                                                .attendees(hearingIndividuals.stream().map(HearingIndividual::buildAttendee).toList())
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGenerateHearingNoticeHmc_1v1_whenHearingFeeHasBeenPaid(boolean isCaseProgressionEnabled) {

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(isCaseProgressionEnabled);

        var hearing = baseHearing.toBuilder()
                    .hearingDetails(HearingDetails.builder()
                                        .hearingType("AAA7-TRI")
                                        .build()
                    )
            .caseDetails(CaseDetailsHearing.builder().caseRef("1234567812345678").build())
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
                                                            "SiteName - CourtAddress - Postcode", "hearingId",
                                                            HEARING_NOTICE_HMC);
        var expected = HearingNoticeHmc.builder()
            .title("trial")
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .claimant(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .feeAmount(null)
            .hearingSiteName("VenueName")
            .caseManagementLocation("CML-Site - CourtAddress - Postcode")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays("01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration("2 days")
            .hearingType("trial")
            .hearingDueDate(null)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .partiesAttendingInPerson("Chloe Landale")
            .partiesAttendingByVideo("Michael Carver")
            .partiesAttendingByTelephone("Jenny Harper")
            .build();

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldOnlyIgnoreFeeDetails_1v1_whenHearingFeeHasBeenPaidThroughHwFAndCPToggleEnabled(boolean isCaseProgressionEnabled) {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(isCaseProgressionEnabled);

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-TRI")
                                .build()
            )
            .caseDetails(CaseDetailsHearing.builder().caseRef("1234567812345678").build())
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
            .hearingHelpFeesReferenceNumber("123")
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .feePaymentOutcomeDetails(FeePaymentOutcomeDetails.builder()
                                          .hwfFullRemissionGrantedForHearingFee(YesOrNo.YES)
                                          .build())
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(Fee.builder()
                            .calculatedAmountInPence(new BigDecimal(123))
                            .build());

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId",
                                                            HEARING_NOTICE_HMC);
        var expected = HearingNoticeHmc.builder()
            .title("trial")
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .claimant(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .feeAmount(isCaseProgressionEnabled ? null : "£1")
            .hearingSiteName("VenueName")
            .caseManagementLocation("CML-Site - CourtAddress - Postcode")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays("01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration("2 days")
            .hearingType("trial")
            .hearingTypePluralWelsh(null)
            .hearingDueDate(isCaseProgressionEnabled ? null : LocalDate.of(2023, 1, 1))
            .partiesAttendingInPerson("Chloe Landale")
            .partiesAttendingByVideo("Michael Carver")
            .partiesAttendingByTelephone("Jenny Harper")
            .build();

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, false", "true, true", "false, false"})
    void shouldGenerateHearingNoticeHmc_1v1_whenHearingFeeHasNotBeenPaid(boolean isCaseProgressionEnabled, boolean isWelsh) {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(isCaseProgressionEnabled);

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-TRI")
                                .build())
            .caseDetails(CaseDetailsHearing.builder().caseRef("1234567812345678").build())
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
                                                            "SiteName - CourtAddress - Postcode", "hearingId",
                                                            isWelsh ? HEARING_NOTICE_HMC_WELSH : HEARING_NOTICE_HMC);
        var creationDate = LocalDate.now();
        var expected = HearingNoticeHmc.builder()
            .title(isWelsh ? "dreial" : "trial")
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(creationDate)
            .creationDateWelshText(isWelsh ? DateUtils.formatDateInWelsh(creationDate, true) : null)
            .claimant(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .feeAmount("£1")
            .hearingSiteName(isWelsh ? "WelshVenueValue" : "VenueName")
            .caseManagementLocation("CML-Site - CourtAddress - Postcode")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays(isWelsh ? "01 Ionawr 2023 am 00:00 am 12 oriau" : "01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration(isWelsh ? "2 dyddiau" : "2 days")
            .hearingType(isWelsh ? "treial" : "trial")
            .hearingTypePluralWelsh(isWelsh ? "dreialon" : null)
            .hearingDueDate(LocalDate.of(2023, 1, 1))
            .hearingDueDateWelshText(isWelsh ? "01 Ionawr 2023" : null)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .partiesAttendingInPerson("Chloe Landale")
            .partiesAttendingByVideo("Michael Carver")
            .partiesAttendingByTelephone("Jenny Harper")
            .build();

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, false", "true, true", "false, false"})
    void shouldGenerateHearingNoticeHmc_1v2DS_whenHearingFeeHasBeenPaid(boolean isCaseProgressionEnabled, boolean isWelsh) {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(isCaseProgressionEnabled);

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-DIS")
                                .build())
            .caseDetails(CaseDetailsHearing.builder().caseRef("1234567812345678").build())
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
                                                            "SiteName - CourtAddress - Postcode", "hearingId", isWelsh
                                                                ? HEARING_NOTICE_HMC_WELSH
                                                                : HEARING_NOTICE_HMC);
        var expected = HearingNoticeHmc.builder()
            .title(isWelsh ? "wrandawiad gwaredu" : "disposal hearing")
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .creationDateWelshText(isWelsh ? DateUtils.formatDateInWelsh(LocalDate.now(), true) : null)
            .claimant(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .defendant2(caseData.getRespondent2().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .defendant2Reference(caseData.getSolicitorReferences().getRespondentSolicitor2Reference())
            .feeAmount(null)
            .hearingSiteName(isWelsh ? "WelshVenueValue" : "VenueName")
            .caseManagementLocation("CML-Site - CourtAddress - Postcode")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays(isWelsh ? "01 Ionawr 2023 am 00:00 am 12 oriau" : "01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration(isWelsh ? "2 dyddiau" : "2 days")
            .hearingType(isWelsh ? "gwrandawiad" : "hearing")
            .hearingTypePluralWelsh(isWelsh ? "wrandawiadau" : null)
            .hearingDueDate(null)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
                .partiesAttendingInPerson("Chloe Landale")
                .partiesAttendingByVideo("Michael Carver")
                .partiesAttendingByTelephone("Jenny Harper")
                .build();

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, false", "true, true", "false, false"})
    void shouldGenerateHearingNoticeHmcDisputeResolution_1v2DS_whenHearingFeeHasBeenPaid(boolean isCaseProgressionEnabled, boolean isWelsh) {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(isCaseProgressionEnabled);

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-DRH")
                                .build())
            .caseDetails(CaseDetailsHearing.builder().caseRef("1234567812345678").build())
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
                                                            "SiteName - CourtAddress - Postcode", "hearingId", isWelsh
                                                                ? HEARING_NOTICE_HMC_WELSH
                                                                : HEARING_NOTICE_HMC);
        var expected = HearingNoticeHmc.builder()
            .title(isWelsh ? "wrandawiad datrys anghydfod" : "dispute resolution hearing")
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .creationDateWelshText(isWelsh ? DateUtils.formatDateInWelsh(LocalDate.now(), true) : null)
            .claimant(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .defendant2(caseData.getRespondent2().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .defendant2Reference(caseData.getSolicitorReferences().getRespondentSolicitor2Reference())
            .feeAmount(null)
            .hearingSiteName(isWelsh ? "WelshVenueValue" : "VenueName")
            .caseManagementLocation("CML-Site - CourtAddress - Postcode")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays(isWelsh ? "01 Ionawr 2023 am 00:00 am 12 oriau" : "01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration(isWelsh ? "2 dyddiau" : "2 days")
            .hearingType(isWelsh ? "gwrandawiad" : "hearing")
            .hearingTypePluralWelsh(isWelsh ? "wrandawiadau" : null)
            .hearingDueDate(null)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .partiesAttendingInPerson("Chloe Landale")
            .partiesAttendingByVideo("Michael Carver")
            .partiesAttendingByTelephone("Jenny Harper")
            .build();

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
        "AAA7-DIS, disposal hearing, true",
        "AAA7-DIS, disposal hearing, false",
        "AAA7-DRH, dispute resolution hearing, true",
        "AAA7-DRH, dispute resolution hearing, false"
    })
    void shouldGenerateHearingNoticeHmc_2v1_whenHearingFeeHasBeenPaid_whenHearingType(String hearingType, String expectedTitle, boolean isCaseProgressionEnabled) {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(isCaseProgressionEnabled);

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType(hearingType)
                                .build())
            .caseDetails(CaseDetailsHearing.builder().caseRef("1234567812345678").build())
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
                                                            "SiteName - CourtAddress - Postcode", "hearingId",
                                                            HEARING_NOTICE_HMC);
        var expected = HearingNoticeHmc.builder()
            .title(expectedTitle)
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .claimant(caseData.getApplicant1().getPartyName())
            .claimant2(caseData.getApplicant2().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .claimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .claimant2Reference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .feeAmount(null)
            .hearingSiteName("VenueName")
            .caseManagementLocation("CML-Site - CourtAddress - Postcode")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays("01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration("2 days")
            .hearingType("hearing")
            .hearingDueDate(null)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .partiesAttendingInPerson("Chloe Landale")
            .partiesAttendingByVideo("Michael Carver")
            .partiesAttendingByTelephone("Jenny Harper")
            .build();

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGenerateHearingNoticeHmc_2v1_whenHearingFeeHasBeenPaid_noSolicitorReferences(boolean isCaseProgressionEnabled) {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(isCaseProgressionEnabled);

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-DRH")
                                .build())
            .caseDetails(CaseDetailsHearing.builder().caseRef("1234567812345678").build())
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
            .solicitorReferences(null)
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(Fee.builder()
                            .calculatedAmountInPence(new BigDecimal(123))
                            .build());

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId",
                                                            HEARING_NOTICE_HMC);
        var expected = HearingNoticeHmc.builder()
                .title("dispute resolution hearing")
            .caseNumber(caseData.getCcdCaseReference())
            .creationDate(LocalDate.now())
            .claimant(caseData.getApplicant1().getPartyName())
            .claimant2(caseData.getApplicant2().getPartyName())
            .defendant(caseData.getRespondent1().getPartyName())
            .feeAmount(null)
            .hearingSiteName("VenueName")
            .caseManagementLocation("CML-Site - CourtAddress - Postcode")
            .hearingLocation("SiteName - CourtAddress - Postcode")
            .hearingDays("01 January 2023 at 00:00 for 12 hours")
            .totalHearingDuration("2 days")
            .hearingType("hearing")
            .hearingDueDate(null)
            .hearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .partiesAttendingInPerson("Chloe Landale")
            .partiesAttendingByVideo("Michael Carver")
            .partiesAttendingByTelephone("Jenny Harper")
            .build();

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnListOfExpectedCaseDocuments(boolean isCaseProgressionEnabled) {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(isCaseProgressionEnabled);

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-TRI")
                                .build())
            .caseDetails(CaseDetailsHearing.builder().caseRef("1234567812345678").build())
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
                                        "SiteName - CourtAddress - Postcode", "hearingId",
                                        HEARING_NOTICE_HMC);
        var expected = List.of(CASE_DOCUMENT);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_APPLICATION, bytes, HEARING_FORM));

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnListOfExpectedCaseDocumentsSpec(boolean isCaseProgressionEnabled) {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(isCaseProgressionEnabled);

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-TRI")
                                .build())
            .caseDetails(CaseDetailsHearing.builder().caseRef("1234567812345678").build())
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC()
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
                                        "SiteName - CourtAddress - Postcode", "hearingId",
                                        HEARING_NOTICE_HMC);
        var expected = List.of(CASE_DOCUMENT);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_APPLICATION, bytes, HEARING_FORM));

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnListOfExpectedCaseDocumentsSpec_WhenIsWelshHearingNotice() {
        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(true);

        var hearing = baseHearing.toBuilder()
            .hearingDetails(HearingDetails.builder()
                                .hearingType("AAA7-TRI")
                                .build())
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC()
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
                                        "SiteName - CourtAddress - Postcode", "hearingId",
                                        HEARING_NOTICE_HMC_WELSH);
        var expected = List.of(CASE_DOCUMENT);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_application_welsh, bytes, HEARING_FORM_WELSH));
    }

}



