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
        CaseDocument doc = CaseDocument.builder()
            .documentType(DocumentType.SDO_ORDER)
            .documentLink(Document.builder().documentUrl("url").documentFileName("f.pdf").build())
            .createdDatetime(LocalDateTime.now())
            .build();

        List<Element<CaseDocument>> input = List.of(ElementUtils.element(doc));

        List<BundlingRequestDocument> output = mapper.mapSystemGeneratedCaseDocument(input, "Order");

        assertEquals(1, output.size());
    }
}



