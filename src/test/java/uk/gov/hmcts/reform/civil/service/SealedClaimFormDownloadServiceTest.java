package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    SealedClaimFormDownloadService.class,
    JacksonAutoConfiguration.class
})
class SealedClaimFormDownloadServiceTest {

    @MockBean
    private DocumentDownloadService documentDownloadService;
    @Autowired
    private SealedClaimFormDownloadService sealedClaimFormDownloadService;
    private static final String TEST = "test";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST);
    private static final byte[] DOCUMENT_CONTENT = new byte[]{1,2,3,4,5};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    @Test
    void shouldDownloadDocument() {
        // given
        Party respondent1 = PartyBuilder.builder().soleTrader().build();
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(wrapElements(CaseDocument.builder().documentType(SEALED_CLAIM).documentLink(DOCUMENT_LINK).build()))
            .respondent1(respondent1)
            .build();
        given(documentDownloadService.downloadDocument(any(), any())).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(DOCUMENT_CONTENT), "test", "test"));

        // when
        byte [] document = sealedClaimFormDownloadService.downloadDocument(BEARER_TOKEN, caseData);
        // then
        assertThat(document).isEqualTo(DOCUMENT_CONTENT);
    }

    @Test
    void shouldNotDownloadDocument_whenNull() {
        // given
        CaseData caseData = CaseDataBuilder.builder()
            .systemGeneratedCaseDocuments(null).build();

        // when
        byte [] document = sealedClaimFormDownloadService.downloadDocument(BEARER_TOKEN, caseData);

        // then
        assertThat(document).isNullOrEmpty();
    }
}
