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
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.RECONSIDERATION_UPHELD_DECISION_OUTPUT_PDF;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    RequestReconsiderationGeneratorService.class,
    JacksonAutoConfiguration.class
})
class RequestReconsiderationGeneratorServiceTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = String.format(RECONSIDERATION_UPHELD_DECISION_OUTPUT_PDF.getDocumentTitle(), REFERENCE_NUMBER);

    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(DECISION_MADE_ON_APPLICATIONS)
        .build();

    @Autowired
    private RequestReconsiderationGeneratorService requestReconsiderationGeneratorService;

    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    protected UserService userService;

    @Test
    void shouldGenerateReconsiderationUpheldDocument() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(RECONSIDERATION_UPHELD_DECISION_OUTPUT_PDF)))
            .thenReturn(new DocmosisDocument(RECONSIDERATION_UPHELD_DECISION_OUTPUT_PDF.getDocumentTitle(), bytes));
        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DECISION_MADE_ON_APPLICATIONS)))
            .thenReturn(CASE_DOCUMENT);
        when(userService.getUserDetails(any()))
            .thenReturn(new UserDetails("1", "test@email.com", "Test", "User", null));

        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateSdoFastTrackTrial()
            .atReconsiderationUpheld()
            .build()
            .toBuilder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .build();

        CaseDocument caseDocument = requestReconsiderationGeneratorService.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, DECISION_MADE_ON_APPLICATIONS));
    }

}
