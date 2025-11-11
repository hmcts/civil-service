package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentSDOOrderForm;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentHearingLocationHelper;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_SDO_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_R2_DISPOSAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DJ_SDO_R2_TRIAL;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    DefaultJudgmentOrderFormGenerator.class,
    DjDisposalTemplateService.class,
    DjDisposalTemplateFieldService.class,
    DjTrialTemplateService.class,
    DjTrialTemplateFieldService.class,
    DjPartyFieldService.class,
    DjHearingMethodFieldService.class,
    DjTemplateFieldService.class,
    JacksonAutoConfiguration.class
})
class DefaultJudgmentOrderFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static String fileNameDisposal = null;
    private static String FILE_NAME_DISPOSAL_HNL = null;
    private static String fileNameTrial = null;
    private static final CaseDocument CASE_DOCUMENT_DISPOSAL = CaseDocumentBuilder.builder()
        .documentName(fileNameDisposal)
        .documentType(DEFAULT_JUDGMENT_SDO_ORDER)
        .build();
    private static final CaseDocument CASE_DOCUMENT_TRIAL = CaseDocumentBuilder.builder()
        .documentName(fileNameTrial)
        .documentType(DEFAULT_JUDGMENT_SDO_ORDER)
        .build();
    @MockBean
    private SecuredDocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private DocumentHearingLocationHelper documentHearingLocationHelper;

    @MockBean
    private UserService userService;

    @Autowired
    private DefaultJudgmentOrderFormGenerator generator;

    @BeforeEach
    void setUp() {
        fileNameDisposal = LocalDate.now() + "_Judge Dredd" + ".pdf";
        fileNameTrial = LocalDate.now() + "_Judge Dredd" + ".pdf";
        FILE_NAME_DISPOSAL_HNL = LocalDate.now() + "_Judge Dredd" + ".pdf";

        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                    .forename("Judge")
                                                                    .surname("Dredd")
                                                                    .roles(Collections.emptyList()).build());
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_R2_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_R2_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateSdoTrialDj()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenValidDataIsProvidedAndTelephoneHearing() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_R2_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_R2_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOTelephoneHearing()
            .atStateSdoTrialDj()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenValidDataIsProvidedAndVidoeHearing() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_R2_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_R2_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOVideoHearing()
            .atStateSdoTrialDj()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgementDisposalFormGenerator_HnlFieldsWhenToggled() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class),
                                                               eq(DJ_SDO_R2_DISPOSAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_R2_DISPOSAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DISPOSAL_HNL, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_DISPOSAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedDisposalHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedDisposalSDOVideoCall()
            .atStateClaimIssuedDisposalHearingInPersonDJ()
            .atStateDisposalHearingOrderMadeWithoutHearing()
            .build();
        LocationRefData locationRefData = LocationRefData.builder().build();
        Mockito.when(documentHearingLocationHelper.getHearingLocation(
            nullable(String.class), eq(caseData), eq(BEARER_TOKEN)
        )).thenReturn(locationRefData);
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(FILE_NAME_DISPOSAL_HNL, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
        verify(documentGeneratorService).generateDocmosisDocument(
            argThat((MappableObject arg) ->
                arg instanceof DefaultJudgmentSDOOrderForm
                    && locationRefData.equals(((DefaultJudgmentSDOOrderForm) arg).getHearingLocation())
            ),
            any(DocmosisTemplates.class)
        );
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenNoticeOfChangeEnabled() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_R2_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_R2_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateSdoTrialDj()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgmentTrialOrderFormGenerator_whenSdoR2Enabled() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_R2_TRIAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_R2_TRIAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_TRIAL);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedTrialHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedTrialSDOInPersonHearing()
            .atStateClaimIssuedTrialLocationInPerson()
            .atStateSdoTrialDj()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

    @Test
    void shouldDefaultJudgmentDisposalOrderFormGenerator_whenSdoR2Enabled() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(DJ_SDO_R2_DISPOSAL)))
            .thenReturn(new DocmosisDocument(DJ_SDO_R2_DISPOSAL.getDocumentTitle(), bytes));
        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER)))
            .thenReturn(CASE_DOCUMENT_DISPOSAL);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .atStateClaimIssuedDisposalHearing()
            .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
            .atStateClaimIssuedDisposalSDOVideoCall()
            .atStateClaimIssuedDisposalHearingInPersonDJ()
            .atStateDisposalHearingOrderMadeWithoutHearing()
            .build();
        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);

        assertThat(caseDocument).isNotNull();
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileNameTrial, bytes, DEFAULT_JUDGMENT_SDO_ORDER));
    }

}
