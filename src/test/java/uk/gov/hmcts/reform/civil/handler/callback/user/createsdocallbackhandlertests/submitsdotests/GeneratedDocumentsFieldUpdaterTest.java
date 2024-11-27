package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.submitsdotests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.GeneratedDocumentsFieldUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GeneratedDocumentsFieldUpdaterTest {

    @InjectMocks
    private GeneratedDocumentsFieldUpdater generatedDocumentsFieldUpdater;

    @Test
    void shouldAddGeneratedDocumentToSystemGeneratedCaseDocuments() {
        CaseDocument document = CaseDocument.builder().build();
        List<Element<CaseDocument>> generatedDocuments = new ArrayList<>();

        CaseData caseData = CaseData.builder()
                .sdoOrderDocument(document)
                .systemGeneratedCaseDocuments(generatedDocuments)
                .build();

        CaseData.CaseDataBuilder<?, ?> dataBuilder = CaseData.builder();

        generatedDocumentsFieldUpdater.update(caseData, dataBuilder);

        List<Element<CaseDocument>> updatedDocuments = dataBuilder.build().getSystemGeneratedCaseDocuments();
        assertThat(updatedDocuments).hasSize(1);
        assertThat(updatedDocuments.get(0).getValue()).isEqualTo(document);
        assertThat(dataBuilder.build().getSdoOrderDocument()).isNull();
    }

    @Test
    void shouldNotAddGeneratedDocumentWhenDocumentIsNull() {
        List<Element<CaseDocument>> generatedDocuments = new ArrayList<>();

        CaseData caseData = CaseData.builder()
                .sdoOrderDocument(null)
                .systemGeneratedCaseDocuments(generatedDocuments)
                .build();

        CaseData.CaseDataBuilder<?, ?> dataBuilder = CaseData.builder();

        generatedDocumentsFieldUpdater.update(caseData, dataBuilder);

        assertThat(dataBuilder.build().getSystemGeneratedCaseDocuments()).isEmpty();
        assertThat(dataBuilder.build().getSdoOrderDocument()).isNull();
    }
}