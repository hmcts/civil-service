package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_FAST;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SDO_SMALL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SdoGeneratorService.class,
    JacksonAutoConfiguration.class
})
public class SdoGeneratorServiceTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileNameSmall = String.format(SDO_SMALL.getDocumentTitle(), REFERENCE_NUMBER);
    private static final String fileNameFast = String.format(SDO_FAST.getDocumentTitle(), REFERENCE_NUMBER);
    private static final String fileNameDisposal = String.format(SDO_DISPOSAL.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT_SMALL = CaseDocumentBuilder.builder()
        .documentName(fileNameSmall)
        .documentType(SDO_ORDER)
        .build();
    private static final CaseDocument CASE_DOCUMENT_FAST = CaseDocumentBuilder.builder()
        .documentName(fileNameFast)
        .documentType(SDO_ORDER)
        .build();
    private static final CaseDocument CASE_DOCUMENT_DISPOSAL = CaseDocumentBuilder.builder()
        .documentName(fileNameDisposal)
        .documentType(SDO_ORDER)
        .build();

    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    protected IdamClient idamClient;

    @Autowired
    private SdoGeneratorService generator;

    @Test
    public void sdoSmall() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_SMALL)))
            .thenReturn(new DocmosisDocument(SDO_SMALL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_SMALL);
        when(idamClient.getUserDetails(any()))
            .thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.smallClaimsTrack)
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameSmall, bytes, SDO_ORDER));
    }

    @Test
    public void sdoFast() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_FAST)))
            .thenReturn(new DocmosisDocument(SDO_FAST.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_FAST);
        when(idamClient.getUserDetails(any()))
            .thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameFast, bytes, SDO_ORDER));
    }

    @Test
    public void sdoDisposal() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(SDO_DISPOSAL)))
            .thenReturn(new DocmosisDocument(SDO_DISPOSAL.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_DISPOSAL);
        when(idamClient.getUserDetails(any()))
            .thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(YesOrNo.NO)
            .orderType(OrderType.DISPOSAL)
            .claimsTrack(ClaimsTrack.fastTrack)
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, SDO_ORDER));
    }
}
