package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_APPLICATION_AHN;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_OTHER_AHN;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_SMALL_CLAIMS_AHN;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_TRIAL_AHN;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    HearingFormGenerator.class,
    JacksonAutoConfiguration.class
})
public class HearingFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName_application = String.format(
        HEARING_APPLICATION_AHN.getDocumentTitle(), REFERENCE_NUMBER);
    private static final String fileName_small_claim = String.format(
        HEARING_SMALL_CLAIMS_AHN.getDocumentTitle(), REFERENCE_NUMBER);
    private static final String fileName_fast_track = String.format(
        HEARING_TRIAL_AHN.getDocumentTitle(), REFERENCE_NUMBER);
    private static final String fileName_other_claim = String.format(
        HEARING_OTHER_AHN.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName_application)
        .documentType(DEFAULT_JUDGMENT)
        .build();
    private static final CaseLocationCivil caseManagementLocation = new CaseLocationCivil().setBaseLocation("000000");
    private static LocationRefData locationRefData = new LocationRefData()
        .setSiteName("SiteName")
        .setExternalShortName("ExternalShortName")
        .setVenueName("VenueName")
        .setCourtAddress("1").setPostcode("1")
        .setCourtName("Court Name").setRegion("Region").setRegionId("4").setCourtVenueId("000")
        .setCourtTypeId("10").setCourtLocationCode("121")
        .setEpimmsId("000000");

    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private AssignCategoryId assignCategoryId;
    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    private LocationReferenceDataService locationRefDataService;
    @Autowired
    private HearingFormGenerator generator;
    @MockBean
    private DocumentHearingLocationHelper locationHelper;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
    }

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided_hearing_application_ahn() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_APPLICATION_AHN)))
            .thenReturn(new DocmosisDocument(HEARING_APPLICATION_AHN.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .caseManagementLocation(caseManagementLocation)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION).build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, HEARING_FORM));
    }

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided_hearing_small_claims_ahn() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_SMALL_CLAIMS_AHN)))
            .thenReturn(new DocmosisDocument(HEARING_SMALL_CLAIMS_AHN.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName_small_claim, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .caseManagementLocation(caseManagementLocation)
            .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS).build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_small_claim, bytes, HEARING_FORM));
    }

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided_hearing_fast_track_ahn() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_TRIAL_AHN)))
            .thenReturn(new DocmosisDocument(HEARING_TRIAL_AHN.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName_fast_track, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .caseManagementLocation(caseManagementLocation)
            .hearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL).build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_fast_track, bytes, HEARING_FORM));
    }

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided_hearing_other_ahn() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_OTHER_AHN)))
            .thenReturn(new DocmosisDocument(HEARING_OTHER_AHN.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName_other_claim, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .caseManagementLocation(caseManagementLocation)
            .hearingNoticeList(HearingNoticeList.OTHER).build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_other_claim, bytes, HEARING_FORM));
    }

    @Test
    void shouldShowListingOrRelistingFeeDue_whenListing() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .caseManagementLocation(caseManagementLocation)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION).build();

        assertThat(generator.listingOrRelistingWithFeeDue(caseData)).isEqualTo("SHOW");
    }

    @Test
    void shouldShowListingOrRelistingFeeDue_whenRelistingNotPaid() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.RELISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .caseManagementLocation(caseManagementLocation)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(null)
            .build();

        assertThat(generator.listingOrRelistingWithFeeDue(caseData)).isEqualTo("SHOW");
    }

    @Test
    void shouldNotShowListingOrRelistingFeeDue_whenRelistingAndPaid() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.RELISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .caseManagementLocation(caseManagementLocation)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION)
            .hearingFeePaymentDetails(new PaymentDetails()
                                          .setStatus(SUCCESS)
                                          .setReference("REFERENCE")
                                          )
            .build();

        assertThat(generator.listingOrRelistingWithFeeDue(caseData)).isEqualTo("DO_NOT_SHOW");
    }

    private static Stream<Arguments> provideTestCases() {
        return Stream.of(
            Arguments.of(CaseState.HEARING_READINESS, ListingOrRelisting.LISTING, YesOrNo.NO, YesOrNo.NO,
                         new PaymentDetails().setStatus(PaymentStatus.SUCCESS), null, "DO_NOT_SHOW"),
            Arguments.of(CaseState.HEARING_READINESS, ListingOrRelisting.LISTING, YesOrNo.NO, YesOrNo.NO,
                         null, new FeePaymentOutcomeDetails().setHwfFullRemissionGrantedForHearingFee(YesOrNo.YES), "DO_NOT_SHOW"),
            Arguments.of(CaseState.HEARING_READINESS, ListingOrRelisting.LISTING, YesOrNo.NO, YesOrNo.NO,
                         new PaymentDetails().setStatus(PaymentStatus.FAILED), null, "SHOW"),
            Arguments.of(CaseState.HEARING_READINESS, ListingOrRelisting.LISTING, YesOrNo.NO, YesOrNo.NO,
                         new PaymentDetails().setStatus(PaymentStatus.FAILED), null, "SHOW")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void shouldNotCreateDashboardNotificationsForHearingFee(CaseState ccdState, ListingOrRelisting listingOrRelisting,
                                                            YesOrNo applicant1Represented, YesOrNo respondent1Represented,
                                                            PaymentDetails hearingFeePaymentDetails, FeePaymentOutcomeDetails feePaymentOutcomeDetails,
                                                            String expected) {

        CaseData caseData = CaseData.builder()
            .legacyCaseReference("reference")
            .ccdCaseReference(1234L)
            .ccdState(ccdState)
            .listingOrRelisting(listingOrRelisting)
            .applicant1Represented(applicant1Represented)
            .respondent1Represented(respondent1Represented)
            .hearingFeePaymentDetails(hearingFeePaymentDetails)
            .hearingHelpFeesReferenceNumber("123")
            .feePaymentOutcomeDetails(feePaymentOutcomeDetails)
            .build();

        assertThat(generator.listingOrRelistingWithFeeDue(caseData)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "custom time duration,,FAST_TRACK_TRIAL, INTERMEDIATE_CLAIM",
        "custom time duration,,FAST_TRACK_TRIAL, MULTI_CLAIM",
        ", MINUTES_45 ,SMALL_CLAIMS, MULTI_CLAIM",
        ", MINUTES_45 ,OTHER, INTERMEDIATE_CLAIM",
        ", MINUTES_45 ,FAST_TRACK_TRIAL, FAST_CLAIM",
        ", MINUTES_45 ,SMALL_CLAIMS, FAST_CLAIM",
        ", MINUTES_45 ,OTHER, SMALL_CLAIM"
    })
    void shouldGetHearingDuration(String mintiHearingDuration, String hearingDuration, String hearingNoticeType, String claimType) {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.RELISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .caseManagementLocation(caseManagementLocation)
            .hearingNoticeList(HearingNoticeList.valueOf(hearingNoticeType))
            .hearingFeePaymentDetails(null)
            .allocatedTrack(AllocatedTrack.valueOf(claimType))
            .build();

        if (mintiHearingDuration != null) {
            caseData = caseData.toBuilder().build().toBuilder()
                .hearingDurationMinti(mintiHearingDuration)
                .build();
            assertThat(generator.getHearingDuration(caseData)).isEqualTo(mintiHearingDuration);
        }
        if (hearingDuration != null) {
            caseData = caseData.toBuilder().build().toBuilder()
                .hearingDuration(HearingDuration.valueOf(hearingDuration))
                .build();
            assertThat(generator.getHearingDuration(caseData)).isEqualTo("45 minutes");
        }
    }

    @ParameterizedTest
    @CsvSource({
        "custom time duration, FAST_TRACK_TRIAL, INTERMEDIATE_CLAIM",
        "custom time duration, FAST_TRACK_TRIAL, MULTI_CLAIM",
    })
    void shouldGetHearingDuration_spec(String mintiHearingDuration, String hearingNoticeType, String claimType) {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.RELISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .caseManagementLocation(caseManagementLocation)
            .hearingNoticeList(HearingNoticeList.valueOf(hearingNoticeType))
            .hearingFeePaymentDetails(null)
            .responseClaimTrack(claimType)
            .hearingDurationMinti(mintiHearingDuration)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();

        assertThat(generator.getHearingDuration(caseData)).isEqualTo(mintiHearingDuration);

    }
}
