package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_APPLICATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_APPLICATION_AHN;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_FAST_TRACK;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_FAST_TRACK_AHN;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_OTHER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_OTHER_AHN;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_SMALL_CLAIMS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_SMALL_CLAIMS_AHN;

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
        HEARING_APPLICATION.getDocumentTitle(), REFERENCE_NUMBER);
    private static final String fileName_small_claim = String.format(
        HEARING_SMALL_CLAIMS.getDocumentTitle(), REFERENCE_NUMBER);
    private static final String fileName_fast_track = String.format(
        HEARING_FAST_TRACK.getDocumentTitle(), REFERENCE_NUMBER);
    private static final String fileName_other_claim = String.format(
        HEARING_OTHER.getDocumentTitle(), REFERENCE_NUMBER);

    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName_application)
        .documentType(DEFAULT_JUDGMENT)
        .build();

    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private AssignCategoryId assignCategoryId;
    @MockBean

    FeatureToggleService featureToggleService;
    private LocationRefDataService locationRefDataService;

    @MockBean
    private CourtLocationUtils courtLocationUtils;

    @Autowired
    private HearingFormGenerator generator;

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided_hearing_application() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_APPLICATION)))
            .thenReturn(new DocmosisDocument(HEARING_APPLICATION.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);
        when(courtLocationUtils.findPreferredLocationData(any(), any())).thenReturn(LocationRefData.builder().build());

        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.HEARING_OF_APPLICATION).build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, HEARING_FORM));
    }

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided_hearing_small_claims() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_SMALL_CLAIMS)))
            .thenReturn(new DocmosisDocument(HEARING_SMALL_CLAIMS.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_small_claim, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS).build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_small_claim, bytes, HEARING_FORM));
    }

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided_hearing_fast_track() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_FAST_TRACK)))
            .thenReturn(new DocmosisDocument(HEARING_FAST_TRACK.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_fast_track, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.FAST_TRACK_TRIAL).build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_fast_track, bytes, HEARING_FORM));
    }

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided_hearing_oher() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_OTHER)))
            .thenReturn(new DocmosisDocument(HEARING_OTHER.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_other_claim, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.OTHER).build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_other_claim, bytes, HEARING_FORM));
    }

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided_hearing_application_ahn() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_APPLICATION_AHN)))
            .thenReturn(new DocmosisDocument(HEARING_APPLICATION_AHN.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_application, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
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

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_small_claim, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.SMALL_CLAIMS).build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_small_claim, bytes, HEARING_FORM));
    }

    @Test
    void shouldHearingFormGeneratorOneForm_whenValidDataIsProvided_hearing_fast_track_ahn() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(HEARING_FAST_TRACK_AHN)))
            .thenReturn(new DocmosisDocument(HEARING_FAST_TRACK_AHN.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_fast_track, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
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

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName_other_claim, bytes, HEARING_FORM)))
            .thenReturn(CASE_DOCUMENT);

        when(featureToggleService.isAutomatedHearingNoticeEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .listingOrRelisting(ListingOrRelisting.LISTING)
            .totalClaimAmount(new BigDecimal(2000))
            .build().toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build())
                                 .build())
            .hearingTimeHourMinute("0800")
            .channel(HearingChannel.IN_PERSON)
            .hearingDuration(HearingDuration.DAY_1)
            .hearingNoticeList(HearingNoticeList.OTHER).build();
        List<CaseDocument> caseDocuments = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocuments.size()).isEqualTo(1);

        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName_other_claim, bytes, HEARING_FORM));
    }
}
