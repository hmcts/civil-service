package uk.gov.hmcts.reform.civil.helpers.bundle.mappers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemGeneratedDocMapperTest {

    @Test
    void testMapSystemGeneratedCaseDocument() {
        SystemGeneratedDocMapper mapper = new SystemGeneratedDocMapper();
        CaseDocument doc = new CaseDocument()
            .setDocumentType(DocumentType.SDO_ORDER)
            .setDocumentLink(new Document().setDocumentUrl("url").setDocumentFileName("f.pdf"))
            .setCreatedDatetime(LocalDateTime.now());

        List<Element<CaseDocument>> input = List.of(ElementUtils.element(doc));

        List<BundlingRequestDocument> output = mapper.mapSystemGeneratedCaseDocument(input, "Order");

        assertEquals(1, output.size());
    }
}
