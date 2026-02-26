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

    private HearingGetResponse baseHearing = new HearingGetResponse();

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
                 .getHearingCourtLocations(BEARER_TOKEN)).thenReturn(List.of(new LocationRefData()
                                                                                             .setEpimmsId(EPIMS)
                                                                                             .setExternalShortName("VenueName")
                                                                                             .setWelshExternalShortName("WelshVenueValue")
                                                                                             .setSiteName("CML-Site")
                                                                                             .setWelshSiteName("CML-Site-Welsh")
                                                                                             .setCourtAddress(
                                                                                                 "CourtAddress")
                                                                                             .setPostcode("Postcode")
                                                                                             ));

        List<HearingIndividual> hearingIndividuals = List.of(
                HearingIndividual.attendingHearingInPerson("Chloe", "Landale"),
                HearingIndividual.attendingHearingByVideo("Michael", "Carver"),
                HearingIndividual.attendingHearingByPhone("Jenny", "Harper"),
                HearingIndividual.nonAttending("James", "Allen")
        );

        HearingDay hearingDay = new HearingDay()
            .setHearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 01, 01, 12, 0, 0));
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 02, 02, 0, 0, 0);
        baseHearing
                .setPartyDetails(hearingIndividuals.stream().map(HearingIndividual::buildPartyDetails).toList())
                .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                                List.of(
                                        new HearingDaySchedule()
                                                .setAttendees(hearingIndividuals.stream().map(HearingIndividual::buildAttendee).toList())
                                                .setHearingVenueId(EPIMS)
                                                .setHearingStartDateTime(hearingDay.getHearingStartDateTime())
                                                .setHearingEndDateTime(hearingDay.getHearingEndDateTime())))
                        .setReceivedDateTime(hearingResponseDate))
                .setRequestDetails(new HearingRequestDetails()
                        .setVersionNumber(VERSION_NUMBER));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGenerateHearingNoticeHmc_1v1_whenHearingFeeHasBeenPaid() {
        var hearing = baseHearing
            .setHearingDetails(new HearingDetails().setHearingType("AAA7-TRI"))
            .setCaseDetails(new CaseDetailsHearing().setCaseRef("1234567812345678"));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(EPIMS)
                                        )
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(new PaymentDetails()
                                          .setStatus(PaymentStatus.SUCCESS)
                                          )
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(new Fee().setCalculatedAmountInPence(new BigDecimal(123)));

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId",
                                                            HEARING_NOTICE_HMC);
        var expected = new HearingNoticeHmc()
            .setTitle("trial")
            .setCaseNumber(caseData.getCcdCaseReference())
            .setCreationDate(LocalDate.now())
            .setClaimant(caseData.getApplicant1().getPartyName())
            .setDefendant(caseData.getRespondent1().getPartyName())
            .setClaimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .setDefendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .setFeeAmount(null)
            .setHearingSiteName("VenueName")
            .setCaseManagementLocation("CML-Site - CourtAddress - Postcode")
            .setHearingLocation("SiteName - CourtAddress - Postcode")
            .setHearingDays("01 January 2023 at 00:00 for 12 hours")
            .setTotalHearingDuration("2 days")
            .setHearingType("trial")
            .setHearingDueDate(null)
            .setHearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .setPartiesAttendingInPerson("Chloe Landale")
            .setPartiesAttendingByVideo("Michael Carver")
            .setPartiesAttendingByTelephone("Jenny Harper");

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldOnlyIgnoreFeeDetails_1v1_whenHearingFeeHasBeenPaidThroughHwFAndCPToggleEnabled() {
        var hearing = baseHearing
            .setHearingDetails(new HearingDetails()
                                .setHearingType("AAA7-TRI")
            )
            .setCaseDetails(new CaseDetailsHearing().setCaseRef("1234567812345678"));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(EPIMS)
                                        )
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingHelpFeesReferenceNumber("123")
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                                          .setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES))
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(new Fee().setCalculatedAmountInPence(new BigDecimal(123)));

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId",
                                                            HEARING_NOTICE_HMC);
        var expected = new HearingNoticeHmc()
            .setTitle("trial")
            .setCaseNumber(caseData.getCcdCaseReference())
            .setCreationDate(LocalDate.now())
            .setClaimant(caseData.getApplicant1().getPartyName())
            .setDefendant(caseData.getRespondent1().getPartyName())
            .setClaimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .setDefendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .setFeeAmount(null)
            .setHearingSiteName("VenueName")
            .setCaseManagementLocation("CML-Site - CourtAddress - Postcode")
            .setHearingLocation("SiteName - CourtAddress - Postcode")
            .setHearingDays("01 January 2023 at 00:00 for 12 hours")
            .setTotalHearingDuration("2 days")
            .setHearingType("trial")
            .setHearingTypePluralWelsh(null)
            .setHearingDueDate(null)
            .setPartiesAttendingInPerson("Chloe Landale")
            .setPartiesAttendingByVideo("Michael Carver")
            .setPartiesAttendingByTelephone("Jenny Harper");

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, false", "true, true", "false, false"})
    void shouldGenerateHearingNoticeHmc_1v1_whenHearingFeeHasNotBeenPaid(boolean isWelsh) {
        var hearing = baseHearing
            .setHearingDetails(new HearingDetails().setHearingType("AAA7-TRI"))
            .setCaseDetails(new CaseDetailsHearing().setCaseRef("1234567812345678"));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(EPIMS)
                                        )
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(new PaymentDetails()
                                          .setStatus(PaymentStatus.FAILED)
                                          )
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(new Fee().setCalculatedAmountInPence(new BigDecimal(123)));

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId",
                                                            isWelsh ? HEARING_NOTICE_HMC_WELSH : HEARING_NOTICE_HMC);
        var creationDate = LocalDate.now();
        var expected = new HearingNoticeHmc()
            .setTitle(isWelsh ? "dreial" : "trial")
            .setCaseNumber(caseData.getCcdCaseReference())
            .setCreationDate(creationDate)
            .setCreationDateWelshText(isWelsh ? DateUtils.formatDateInWelsh(creationDate, true) : null)
            .setClaimant(caseData.getApplicant1().getPartyName())
            .setDefendant(caseData.getRespondent1().getPartyName())
            .setClaimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .setDefendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .setFeeAmount("£1")
            .setHearingSiteName(isWelsh ? "WelshVenueValue" : "VenueName")
            .setCaseManagementLocation("CML-Site" + (isWelsh ? "-Welsh" : "") + " - CourtAddress - Postcode")
            .setHearingLocation("SiteName - CourtAddress - Postcode")
            .setHearingDays(isWelsh ? "01 Ionawr 2023 am 00:00 am 12 awr" : "01 January 2023 at 00:00 for 12 hours")
            .setTotalHearingDuration(isWelsh ? "2 ddiwrnod" : "2 days")
            .setHearingType(isWelsh ? "treial" : "trial")
            .setHearingTypePluralWelsh(isWelsh ? "dreialon" : null)
            .setHearingDueDate(LocalDate.of(2023, 1, 1))
            .setHearingDueDateWelshText(isWelsh ? "01 Ionawr 2023" : null)
            .setHearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .setPartiesAttendingInPerson("Chloe Landale")
            .setPartiesAttendingByVideo("Michael Carver")
            .setPartiesAttendingByTelephone("Jenny Harper");

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, false", "true, true", "false, false"})
    void shouldGenerateHearingNoticeHmc_1v2DS_whenHearingFeeHasBeenPaid(boolean isWelsh) {
        var hearing = baseHearing
            .setHearingDetails(new HearingDetails()
                                .setHearingType("AAA7-DIS"))
            .setCaseDetails(new CaseDetailsHearing().setCaseRef("1234567812345678"));

        CaseData caseData = CaseDataBuilder.builder().atState1v2DifferentSolicitorClaimDetailsRespondent2NotifiedTimeExtension()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(EPIMS)
                                        )
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(new PaymentDetails()
                                          .setStatus(PaymentStatus.SUCCESS)
                                          )
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(new Fee().setCalculatedAmountInPence(new BigDecimal(123)));

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId", isWelsh
                                                                ? HEARING_NOTICE_HMC_WELSH
                                                                : HEARING_NOTICE_HMC);
        var expected = new HearingNoticeHmc()
            .setTitle(isWelsh ? "wrandawiad gwaredu" : "disposal hearing")
            .setCaseNumber(caseData.getCcdCaseReference())
            .setCreationDate(LocalDate.now())
            .setCreationDateWelshText(isWelsh ? DateUtils.formatDateInWelsh(LocalDate.now(), true) : null)
            .setClaimant(caseData.getApplicant1().getPartyName())
            .setDefendant(caseData.getRespondent1().getPartyName())
            .setDefendant2(caseData.getRespondent2().getPartyName())
            .setClaimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .setDefendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .setDefendant2Reference(caseData.getSolicitorReferences().getRespondentSolicitor2Reference())
            .setFeeAmount(null)
            .setHearingSiteName(isWelsh ? "WelshVenueValue" : "VenueName")
            .setCaseManagementLocation("CML-Site" + (isWelsh ? "-Welsh" : "") + " - CourtAddress - Postcode")
            .setHearingLocation("SiteName - CourtAddress - Postcode")
            .setHearingDays(isWelsh ? "01 Ionawr 2023 am 00:00 am 12 awr" : "01 January 2023 at 00:00 for 12 hours")
            .setTotalHearingDuration(isWelsh ? "2 ddiwrnod" : "2 days")
            .setHearingType(isWelsh ? "gwrandawiad" : "hearing")
            .setHearingTypePluralWelsh(isWelsh ? "wrandawiadau" : null)
            .setHearingDueDate(null)
            .setHearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
                .setPartiesAttendingInPerson("Chloe Landale")
                .setPartiesAttendingByVideo("Michael Carver")
                .setPartiesAttendingByTelephone("Jenny Harper");

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, false", "true, true", "false, false"})
    void shouldGenerateHearingNoticeHmcDisputeResolution_1v2DS_whenHearingFeeHasBeenPaid(boolean isWelsh) {
        var hearing = baseHearing
            .setHearingDetails(new HearingDetails()
                                .setHearingType("AAA7-DRH"))
            .setCaseDetails(new CaseDetailsHearing().setCaseRef("1234567812345678"));

        CaseData caseData = CaseDataBuilder.builder().atState1v2DifferentSolicitorClaimDetailsRespondent2NotifiedTimeExtension()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(EPIMS)
                                        )
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(new PaymentDetails()
                                          .setStatus(PaymentStatus.SUCCESS)
                                          )
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(new Fee().setCalculatedAmountInPence(new BigDecimal(123)));

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId", isWelsh
                                                                ? HEARING_NOTICE_HMC_WELSH
                                                                : HEARING_NOTICE_HMC);
        var expected = new HearingNoticeHmc()
            .setTitle(isWelsh ? "wrandawiad datrys anghydfod" : "dispute resolution hearing")
            .setCaseNumber(caseData.getCcdCaseReference())
            .setCreationDate(LocalDate.now())
            .setCreationDateWelshText(isWelsh ? DateUtils.formatDateInWelsh(LocalDate.now(), true) : null)
            .setClaimant(caseData.getApplicant1().getPartyName())
            .setDefendant(caseData.getRespondent1().getPartyName())
            .setDefendant2(caseData.getRespondent2().getPartyName())
            .setClaimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .setDefendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .setDefendant2Reference(caseData.getSolicitorReferences().getRespondentSolicitor2Reference())
            .setFeeAmount(null)
            .setHearingSiteName(isWelsh ? "WelshVenueValue" : "VenueName")
            .setCaseManagementLocation("CML-Site" + (isWelsh ? "-Welsh" : "") + " - CourtAddress - Postcode")
            .setHearingLocation("SiteName - CourtAddress - Postcode")
            .setHearingDays(isWelsh ? "01 Ionawr 2023 am 00:00 am 12 awr" : "01 January 2023 at 00:00 for 12 hours")
            .setTotalHearingDuration(isWelsh ? "2 ddiwrnod" : "2 days")
            .setHearingType(isWelsh ? "gwrandawiad" : "hearing")
            .setHearingTypePluralWelsh(isWelsh ? "wrandawiadau" : null)
            .setHearingDueDate(null)
            .setHearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .setPartiesAttendingInPerson("Chloe Landale")
            .setPartiesAttendingByVideo("Michael Carver")
            .setPartiesAttendingByTelephone("Jenny Harper");

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
        "AAA7-DIS, disposal hearing, true",
        "AAA7-DIS, disposal hearing, false",
        "AAA7-DRH, dispute resolution hearing, true",
        "AAA7-DRH, dispute resolution hearing, false"
    })
    void shouldGenerateHearingNoticeHmc_2v1_whenHearingFeeHasBeenPaid_whenHearingType(String hearingType, String expectedTitle) {
        var hearing = baseHearing
            .setHearingDetails(new HearingDetails()
                                .setHearingType(hearingType))
            .setCaseDetails(new CaseDetailsHearing().setCaseRef("1234567812345678"));

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(EPIMS)
                                        )
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(new PaymentDetails()
                                          .setStatus(PaymentStatus.SUCCESS)
                                          )
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(new Fee().setCalculatedAmountInPence(new BigDecimal(123)));

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId",
                                                            HEARING_NOTICE_HMC);
        var expected = new HearingNoticeHmc()
            .setTitle(expectedTitle)
            .setCaseNumber(caseData.getCcdCaseReference())
            .setCreationDate(LocalDate.now())
            .setClaimant(caseData.getApplicant1().getPartyName())
            .setClaimant2(caseData.getApplicant2().getPartyName())
            .setDefendant(caseData.getRespondent1().getPartyName())
            .setClaimantReference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .setClaimant2Reference(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .setDefendantReference(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .setFeeAmount(null)
            .setHearingSiteName("VenueName")
            .setCaseManagementLocation("CML-Site - CourtAddress - Postcode")
            .setHearingLocation("SiteName - CourtAddress - Postcode")
            .setHearingDays("01 January 2023 at 00:00 for 12 hours")
            .setTotalHearingDuration("2 days")
            .setHearingType("hearing")
            .setHearingDueDate(null)
            .setHearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .setPartiesAttendingInPerson("Chloe Landale")
            .setPartiesAttendingByVideo("Michael Carver")
            .setPartiesAttendingByTelephone("Jenny Harper");

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldGenerateHearingNoticeHmc_2v1_whenHearingFeeHasBeenPaid_noSolicitorReferences() {
        var hearing = baseHearing
            .setHearingDetails(new HearingDetails()
                                .setHearingType("AAA7-DRH"))
            .setCaseDetails(new CaseDetailsHearing().setCaseRef("1234567812345678"));

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(EPIMS)
                                        )
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(new PaymentDetails()
                                          .setStatus(PaymentStatus.SUCCESS)
                                          )
            .solicitorReferences(null)
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(new Fee().setCalculatedAmountInPence(new BigDecimal(123)));

        var actual = generator.getHearingNoticeTemplateData(caseData, hearing, BEARER_TOKEN,
                                                            "SiteName - CourtAddress - Postcode", "hearingId",
                                                            HEARING_NOTICE_HMC);
        var expected = new HearingNoticeHmc()
                .setTitle("dispute resolution hearing")
            .setCaseNumber(caseData.getCcdCaseReference())
            .setCreationDate(LocalDate.now())
            .setClaimant(caseData.getApplicant1().getPartyName())
            .setClaimant2(caseData.getApplicant2().getPartyName())
            .setDefendant(caseData.getRespondent1().getPartyName())
            .setFeeAmount(null)
            .setHearingSiteName("VenueName")
            .setCaseManagementLocation("CML-Site - CourtAddress - Postcode")
            .setHearingLocation("SiteName - CourtAddress - Postcode")
            .setHearingDays("01 January 2023 at 00:00 for 12 hours")
            .setTotalHearingDuration("2 days")
            .setHearingType("hearing")
            .setHearingDueDate(null)
            .setHearingFeePaymentDetails(caseData.getHearingFeePaymentDetails())
            .setPartiesAttendingInPerson("Chloe Landale")
            .setPartiesAttendingByVideo("Michael Carver")
            .setPartiesAttendingByTelephone("Jenny Harper");

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnListOfExpectedCaseDocuments() {
        var hearing = baseHearing
            .setHearingDetails(new HearingDetails().setHearingType("AAA7-TRI"))
            .setCaseDetails(new CaseDetailsHearing().setCaseRef("1234567812345678"));

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(EPIMS)
                                        )
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(new PaymentDetails()
                                          .setStatus(PaymentStatus.SUCCESS)
                                          )
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(new Fee().setCalculatedAmountInPence(new BigDecimal(123)));

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
    void shouldReturnListOfExpectedCaseDocumentsSpec() {
        var hearing = baseHearing
            .setHearingDetails(new HearingDetails()
                                .setHearingType("AAA7-TRI"))
            .setCaseDetails(new CaseDetailsHearing().setCaseRef("1234567812345678"));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(EPIMS)
                                        )
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(new PaymentDetails()
                                          .setStatus(PaymentStatus.SUCCESS)
                                          )
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(new Fee().setCalculatedAmountInPence(new BigDecimal(123)));

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
        var hearing = baseHearing
            .setHearingDetails(new HearingDetails()
                                .setHearingType("AAA7-TRI"));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC()
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .caseManagementLocation(new CaseLocationCivil()
                                        .setBaseLocation(EPIMS)
                                        )
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(new PaymentDetails()
                                          .setStatus(PaymentStatus.SUCCESS)
                                          )
            .build();

        when(hearingFeesService
                 .getFeeForHearingFastTrackClaims(caseData.getClaimValue().toPounds()))
            .thenReturn(new Fee().setCalculatedAmountInPence(new BigDecimal(123)));

        var actual = generator.generate(caseData, hearing, BEARER_TOKEN,
                                        "SiteName - CourtAddress - Postcode", "hearingId",
                                        HEARING_NOTICE_HMC_WELSH);
        var expected = List.of(CASE_DOCUMENT);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_application_welsh, bytes, HEARING_FORM_WELSH));
    }

}



