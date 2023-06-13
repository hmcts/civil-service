package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    SendSDOBulkPrintService.class,
    JacksonAutoConfiguration.class
})
public class SendSDOBulkPrintServiceTest {

    @MockBean
    private SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;
    @MockBean
    private BulkPrintService bulkPrintService;
    @Autowired
    private SendSDOBulkPrintService sendSDOBulkPrintService;
    private static final String SDO_ORDER_PACK_LETTER_TYPE = "sdo-order-pack";
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // Given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SDO_ORDER).build())).build();
        given(sealedClaimFormGeneratorForSpec.downloadDocument(any())).willReturn(LETTER_CONTENT);

        // When
        sendSDOBulkPrintService.sendSDOToDefendantLIP(caseData);

        // Then
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(),
                SDO_ORDER_PACK_LETTER_TYPE
            );
    }
}
