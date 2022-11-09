package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.UnsecuredDocumentManagementService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.DEFAULT_JUDGMENT_SDO_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_TRIAL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DefaultJudgmentOrderFormGenerator.class,
    JacksonAutoConfiguration.class
})
public class DefaultJudgmentOrderFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileNameDisposal = String.format(DJ_SDO_DISPOSAL.getDocumentTitle(), REFERENCE_NUMBER);
    private static final String fileNameTrial = String.format(DJ_SDO_TRIAL.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT_DISPOSAL = CaseDocumentBuilder.builder()
        .documentName(fileNameDisposal)
        .documentType(DEFAULT_JUDGMENT_SDO_ORDER)
        .build();
    private static final CaseDocument CASE_DOCUMENT_TRIAL = CaseDocumentBuilder.builder()
        .documentName(fileNameTrial)
        .documentType(DEFAULT_JUDGMENT_SDO_ORDER)
        .build();
    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private DefaultJudgmentOrderFormGenerator generator;

    @Test
    void shouldDefaultJudgmentDisposalOrderFormGeneratorOneForm_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_DISPOSAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_DISPOSAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_DISPOSAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedDisposalHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedDisposalSDOVideoCall()
            .atStateClaimIssuedDisposalHearingInPerson()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameDisposal, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);
        when(featureToggleService.isHearingAndListingSDOEnabled())
            .thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateClaimIssuedTrialHearingInfo()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGeneratorHNLisEnabled_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);
        when(featureToggleService.isHearingAndListingSDOEnabled())
            .thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateClaimIssuedTrialHearingInfo()
            .atStateClaimIssuedCaseManagementLocationInPerson()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }
}
