package uk.gov.hmcts.reform.civil.service.docmosis.sdo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.REQUEST_FOR_RECONSIDERATION;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    LiPRequestReconsiderationGeneratorService.class,
    JacksonAutoConfiguration.class
})
public class LiPRequestReconsiderationGeneratorServiceTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String claimantFileName = String.format(REQUEST_FOR_RECONSIDERATION.getDocumentTitle(), "claimant");

    private static final String defendantFileName = String.format(REQUEST_FOR_RECONSIDERATION.getDocumentTitle(), "defendant");

    private static final CaseDocument CLAIMANT_CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(claimantFileName)
        .documentType(DocumentType.REQUEST_FOR_RECONSIDERATION)
        .build();

    private static final CaseDocument DEFENDANT_CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(defendantFileName)
        .documentType(DocumentType.REQUEST_FOR_RECONSIDERATION)
        .build();

    @Autowired
    private LiPRequestReconsiderationGeneratorService requestReconsiderationGeneratorService;

    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private DocumentHearingLocationHelper locationHelper;

    @Test
    void shouldGenerateReconsiderationDocumentForApplicant() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(REQUEST_FOR_RECONSIDERATION)))
            .thenReturn(new DocmosisDocument(REQUEST_FOR_RECONSIDERATION.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(claimantFileName, bytes, DocumentType.REQUEST_FOR_RECONSIDERATION)))
            .thenReturn(CLAIMANT_CASE_DOCUMENT);
        when(locationHelper.getHearingLocation(anyString(), any(), anyString())).thenReturn(null);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateSdoFastTrackTrial()
            .build()
            .toBuilder()
            .reasonForReconsiderationApplicant(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Yes").build())
            .build();

        CaseDocument caseDocument = requestReconsiderationGeneratorService.generateLiPDocument(caseData, BEARER_TOKEN, true);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(claimantFileName, bytes, DocumentType.REQUEST_FOR_RECONSIDERATION));
    }

    @Test
    void shouldGenerateReconsiderationDocumentForRespondent() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(REQUEST_FOR_RECONSIDERATION)))
            .thenReturn(new DocmosisDocument(REQUEST_FOR_RECONSIDERATION.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(defendantFileName, bytes, DocumentType.REQUEST_FOR_RECONSIDERATION)))
            .thenReturn(DEFENDANT_CASE_DOCUMENT);
        when(locationHelper.getHearingLocation(anyString(), any(), anyString())).thenReturn(null);

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateSdoFastTrackTrial()
            .build()
            .toBuilder()
            .reasonForReconsiderationRespondent1(ReasonForReconsideration.builder().reasonForReconsiderationTxt("Yes").build())
            .build();

        CaseDocument caseDocument = requestReconsiderationGeneratorService.generateLiPDocument(caseData, BEARER_TOKEN, false);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(defendantFileName, bytes, DocumentType.REQUEST_FOR_RECONSIDERATION));
    }
}
