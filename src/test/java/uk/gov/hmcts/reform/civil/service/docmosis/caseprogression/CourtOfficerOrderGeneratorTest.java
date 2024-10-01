package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.time.LocalDate;
import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.config.JacksonConfiguration.DATE_FORMAT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.COURT_OFFICER_ORDER;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.COURT_OFFICER_ORDER_PDF;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    CourtOfficerOrderGenerator.class,
})
class CourtOfficerOrderGeneratorTest {

    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String courtOrderFileName = format(COURT_OFFICER_ORDER_PDF.getDocumentTitle(),  formatLocalDate(LocalDate.now(), DATE_FORMAT));
    private static final CaseDocument COURT_OFFICER_ORDER_DOC = CaseDocumentBuilder.builder()
        .documentName(courtOrderFileName)
        .documentType(COURT_OFFICER_ORDER)
        .build();
    private static LocationRefData locationRefData = LocationRefData.builder().siteName("SiteName")
        .courtAddress("12").postcode("34")
        .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
        .externalShortName("Court Name Short")
        .courtTypeId("10").courtLocationCode("121")
        .epimmsId("123456").build();
    private static final CaseLocationCivil caseManagementLocation = CaseLocationCivil.builder().baseLocation("123456").build();

    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private DocumentHearingLocationHelper locationHelper;
    @MockBean
    private LocationReferenceDataService locationRefDataService;
    @Autowired
    private CourtOfficerOrderGenerator generator;

    @BeforeEach
    public void setUp() {
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenReturn(locationRefData);
        when(locationRefDataService.getCcmccLocation(any())).thenReturn(locationRefData);
        when(locationRefDataService.getCourtLocationsByEpimmsId(anyString(), anyString())).thenReturn(List.of(
            locationRefData
        ));
        when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(List.of(locationRefData));
    }

    @Test
    void shouldGenerateCourtOfficerOrder_whenInvoked1v1() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(COURT_OFFICER_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(COURT_OFFICER_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument("BEARER_TOKEN", new PDF(courtOrderFileName, bytes, COURT_OFFICER_ORDER)))
            .thenReturn(COURT_OFFICER_ORDER_DOC);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .courtOfficerOrdered("apples and bananas")
            .hearingLocation(DynamicList.builder().value(DynamicListElement.dynamicElement("A hearing location")).build())
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, "BEARER_TOKEN");

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument("BEARER_TOKEN", new PDF(courtOrderFileName, bytes, COURT_OFFICER_ORDER));
    }

    @Test
    void shouldGenerateCourtOfficerOrder_whenInvoked2v1() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(COURT_OFFICER_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(COURT_OFFICER_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument("BEARER_TOKEN", new PDF(courtOrderFileName, bytes, COURT_OFFICER_ORDER)))
            .thenReturn(COURT_OFFICER_ORDER_DOC);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .courtOfficerOrdered("apples and bananas")
            .applicant2(Party.builder().partyName("applicant2").type(Party.Type.INDIVIDUAL).build())
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, "BEARER_TOKEN");

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument("BEARER_TOKEN", new PDF(courtOrderFileName, bytes, COURT_OFFICER_ORDER));
    }

    @Test
    void shouldGenerateCourtOfficerOrder_whenInvoked1v2() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(COURT_OFFICER_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(COURT_OFFICER_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument("BEARER_TOKEN", new PDF(courtOrderFileName, bytes, COURT_OFFICER_ORDER)))
            .thenReturn(COURT_OFFICER_ORDER_DOC);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .courtOfficerOrdered("apples and bananas")
            .respondent2(Party.builder().partyName("respondent2").type(Party.Type.INDIVIDUAL).build())
            .caseManagementLocation(caseManagementLocation)
            .build();
        CaseDocument caseDocument = generator.generate(caseData, "BEARER_TOKEN");

        assertNotNull(caseDocument);
        verify(documentManagementService)
            .uploadDocument("BEARER_TOKEN", new PDF(courtOrderFileName, bytes, COURT_OFFICER_ORDER));
    }

    @Test
    void shouldThrowException_whenBaseCourtLocationNotFound() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(COURT_OFFICER_ORDER_PDF)))
            .thenReturn(new DocmosisDocument(COURT_OFFICER_ORDER_PDF.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument("BEARER_TOKEN", new PDF(courtOrderFileName, bytes, COURT_OFFICER_ORDER)))
            .thenReturn(COURT_OFFICER_ORDER_DOC);
        when(locationHelper.getCaseManagementLocationDetailsNro(any(), any(), any())).thenThrow(IllegalArgumentException.class);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("1111111").build())
            .build();

        assertThrows(IllegalArgumentException.class, () -> generator.generate(caseData, "BEARER_TOKEN"));
    }

}
