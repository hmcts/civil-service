package uk.gov.hmcts.reform.civil.service.docmosis.claimantresponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CCJ_REQUEST_ADMISSION;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.CCJ_REQUEST_DETERMINATION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION_OR_DETERMINATION;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.UnsecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse.JudgmentByAdmissionOrDetermination;
import uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse.JudgmentByAdmissionOrDeterminationMapper;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

@ExtendWith(MockitoExtension.class)
public class RequestJudgmentByAdmissionOrDeterminationResponseDocGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};

    @Mock
    private JudgmentByAdmissionOrDeterminationMapper judgmentByAdmissionOrDeterminationMapper;

    @Mock
    private UnsecuredDocumentManagementService documentManagementService;

    @Mock
    private DocumentGeneratorService documentGeneratorService;

    @InjectMocks
    private RequestJudgmentByAdmissionOrDeterminationResponseDocGenerator generator;

    @Test
    void shouldGenerateJudgementByAdmissionDocument() {
        // Given
        String fileName = String.format(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), REFERENCE_NUMBER, "admission");
        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(CCJ_REQUEST_ADMISSION)
            .build();
        when(documentGeneratorService.generateDocmosisDocument(any(JudgmentByAdmissionOrDetermination.class), eq(JUDGMENT_BY_ADMISSION_OR_DETERMINATION)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(
            BEARER_TOKEN, new PDF(fileName, bytes, CCJ_REQUEST_ADMISSION))
        ).thenReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build();
        when(judgmentByAdmissionOrDeterminationMapper.toClaimantResponseForm(caseData)).thenReturn(JudgmentByAdmissionOrDetermination.builder().build());

        // When
        CaseDocument actual = generator.generate(GENERATE_JUDGMENT_BY_ADMISSION_RESPONSE_DOC, caseData, BEARER_TOKEN);

        // Then
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, CCJ_REQUEST_ADMISSION));
        assertThat(actual).isEqualTo(caseDocument);
    }

    @Test
    void shouldGenerateJudgementByDeterminationDocument() {
        // Given
        String fileName = String.format(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), REFERENCE_NUMBER, "determination");
        CaseDocument caseDocument = CaseDocumentBuilder.builder()
            .documentName(fileName)
            .documentType(CCJ_REQUEST_DETERMINATION)
            .build();
        when(documentGeneratorService.generateDocmosisDocument(any(JudgmentByAdmissionOrDetermination.class), eq(JUDGMENT_BY_ADMISSION_OR_DETERMINATION)))
            .thenReturn(new DocmosisDocument(JUDGMENT_BY_ADMISSION_OR_DETERMINATION.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(
            BEARER_TOKEN, new PDF(fileName, bytes, CCJ_REQUEST_DETERMINATION))
        ).thenReturn(caseDocument);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .build();
        when(judgmentByAdmissionOrDeterminationMapper.toClaimantResponseForm(caseData)).thenReturn(JudgmentByAdmissionOrDetermination.builder().build());

        // When
        CaseDocument actual = generator.generate(GENERATE_JUDGMENT_BY_DETERMINATION_RESPONSE_DOC, caseData, BEARER_TOKEN);

        // Then
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, CCJ_REQUEST_DETERMINATION));
        assertThat(actual).isEqualTo(caseDocument);
    }
}
