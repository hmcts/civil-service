package uk.gov.hmcts.reform.civil.service.docmosis.consentorder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.ConsentOrderForm;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.CONSENT_ORDER_FORM;

@SuppressWarnings("ALL")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    ConsentOrderGenerator.class
})

class ConsentOrderGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    ConsentOrderGenerator consentOrderGenerator;
    @MockBean
    private DocmosisService docmosisService;

    @Test
    void shouldThrowExceptionWhenNoLocationMatch() {
        CaseData caseData = CaseDataBuilder.builder().consentOrderApplication()
            .gaCaseManagementLocation(GACaseLocation.builder()
                                        .siteName("County Court")
                                        .baseLocation("8")
                                        .region("4").build()).build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(CONSENT_ORDER_FORM)))
            .thenReturn(new DocmosisDocument(CONSENT_ORDER_FORM.getDocumentTitle(), bytes));
        doThrow(new IllegalArgumentException("Court Name is not found in location data"))
            .when(docmosisService).getCaseManagementLocationVenueName(any(), any());
        Exception exception =
            assertThrows(IllegalArgumentException.class, ()
                -> consentOrderGenerator.generate(caseData, BEARER_TOKEN));
        String expectedMessage = "Court Name is not found in location data";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldGenerateConsentOrderDocument() {
        CaseData caseData = CaseDataBuilder.builder().consentOrderApplication().build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(CONSENT_ORDER_FORM)))
            .thenReturn(new DocmosisDocument(CONSENT_ORDER_FORM.getDocumentTitle(), bytes));
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("London").build());
        consentOrderGenerator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.CONSENT_ORDER)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(ConsentOrderForm.class),
                                                                  eq(CONSENT_ORDER_FORM));
    }

    @Test
    void whenCaseWorkerMakeDecision_ShouldGetConsentOrderData() {
        CaseData caseData = CaseDataBuilder.builder().consentOrderApplication().build().toBuilder().isMultiParty(YES)
            .build();
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("London").build());
        var templateData = consentOrderGenerator.getTemplateData(caseData, "auth");
        assertThatFieldsAreCorrect_GeneralOrder(templateData, caseData);
    }

    private void assertThatFieldsAreCorrect_GeneralOrder(ConsentOrderForm templateData, CaseData caseData) {
        Assertions.assertAll(
            "ConsentOrderDocument data should be as expected",
            () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
            () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
            () -> assertEquals(YES, templateData.getIsMultiParty()),
            () -> assertEquals(templateData.getClaimant2Name(), caseData.getClaimant2PartyName()),
            () -> assertEquals(templateData.getCourtName(), "London"),
            () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
            () -> assertEquals(templateData.getDefendant2Name(), caseData.getDefendant2PartyName()),
            () -> assertEquals(templateData.getConsentOrder(),
                               caseData.getApproveConsentOrder().getConsentOrderDescription())
        );
    }

    @Test
    void whenCaseWorkerMakeDecision_ShouldGetConsentOrderData_1v1() {
        CaseData caseData = CaseDataBuilder.builder().consentOrderApplication().build().toBuilder()
            .defendant2PartyName(null)
            .claimant2PartyName(null)
            .gaCaseManagementLocation(GACaseLocation.builder().baseLocation("3").build())
            .isMultiParty(NO)
            .build();
        when(docmosisService.getCaseManagementLocationVenueName(any(), any()))
            .thenReturn(LocationRefData.builder().epimmsId("2").externalShortName("Manchester").build());
        var templateData = consentOrderGenerator.getTemplateData(caseData, "auth");
        assertThatFieldsAreCorrect_GeneralOrder_1v1(templateData, caseData);
    }

    private void assertThatFieldsAreCorrect_GeneralOrder_1v1(ConsentOrderForm templateData, CaseData caseData) {
        Assertions.assertAll(
            "ConsentOrderDocument data should be as expected",
            () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
            () -> assertEquals(templateData.getClaimant1Name(), caseData.getClaimant1PartyName()),
            () -> assertEquals(NO, templateData.getIsMultiParty()),
            () -> assertNull(templateData.getClaimant2Name()),
            () -> assertEquals(templateData.getDefendant1Name(), caseData.getDefendant1PartyName()),
            () -> assertNull(templateData.getDefendant2Name()),
            () -> assertEquals(templateData.getCourtName(), "Manchester"),
            () -> assertEquals(templateData.getConsentOrder(),
                               caseData.getApproveConsentOrder().getConsentOrderDescription()),
            () -> assertEquals(templateData.getAddress(), caseData.getGaCaseManagementLocation().getAddress()),
            () -> assertEquals(templateData.getPostcode(), caseData.getGaCaseManagementLocation().getPostcode())
        );
    }

    @Test
    void test_getDateFormatted() {
        String dateString = consentOrderGenerator.getDateFormatted(LocalDate.EPOCH);
        assertThat(dateString).isEqualTo(" 1 January 1970");
    }

}
