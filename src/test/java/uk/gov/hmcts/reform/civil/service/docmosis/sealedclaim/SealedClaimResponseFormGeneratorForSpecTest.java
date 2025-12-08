package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.referencedata.LocationRefData;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.PaymentMethod;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers.ReferenceNumberAndCourtDetailsPopulator;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers.StatementOfTruthPopulator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class SealedClaimResponseFormGeneratorForSpecTest {

    private static final String AUTH = "Bearer xyz";

    @Mock
    private RepresentativeService representativeService;
    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private ReferenceNumberAndCourtDetailsPopulator referenceNumberPopulator;
    @Mock
    private StatementOfTruthPopulator statementOfTruthPopulator;

    @Captor
    private ArgumentCaptor<SealedClaimResponseFormForSpec> templateDataCaptor;
    @Captor
    private ArgumentCaptor<DocmosisTemplates> templateEnumCaptor;
    @Captor
    private ArgumentCaptor<PDF> pdfCaptor;

    @InjectMocks
    private SealedClaimResponseFormGeneratorForSpec generator;

    private CaseData base1v1;
    private CaseData base1v2LatestIsResp2;

    @BeforeEach
    void init() {
        base1v1 = CaseData.builder()
            .legacyCaseReference("LEG-123")
            .ccdCaseReference(1234567890123456L)
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant Ltd").build())
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Resp1 Ltd").build())
            .detailsOfWhyDoesYouDisputeTheClaim("why-1v1")
            .respondent1DQ(uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ.builder()
                               .respondent1DQStatementOfTruth(
                                   StatementOfTruth.builder().name("sot1").role("role1").build()
                               ).build())
            .respondent1ResponseDate(LocalDateTime.now())
            .build();

        base1v2LatestIsResp2 = CaseData.builder()
            .legacyCaseReference("LEG-999")
            .ccdCaseReference(9999999999999999L)
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant Ltd").build())
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Resp1 Ltd").build())
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Resp2 Ltd").build())
            .detailsOfWhyDoesYouDisputeTheClaim("why-1v2-r1")
            .detailsOfWhyDoesYouDisputeTheClaim2("why-1v2-r2")
            .respondent1DQ(uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ.builder()
                               .respondent1DQStatementOfTruth(
                                   StatementOfTruth.builder().name("sot1").role("role1").build()
                               ).build())
            .respondent2DQ(uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ.builder()
                               .respondent2DQStatementOfTruth(
                                   StatementOfTruth.builder().name("sot2").role("role2").build()
                               ).build())
            .respondent1ResponseDate(LocalDateTime.now())
            .respondent2ResponseDate(LocalDateTime.now().plusDays(2)) // respondent2 answered last
            .respondent1SpecDefenceResponseDocument(ResponseDocument.builder()
                                                        .file(Document.builder().documentFileName("resp1-def.pdf").build()).build())
            .respondent2SpecDefenceResponseDocument(ResponseDocument.builder()
                                                        .file(Document.builder().documentFileName("resp2-def.pdf").build()).build())
            .specResponseTimelineOfEvents(List.of(
                TimelineOfEvents.builder()
                    .value(TimelineOfEventDetails.builder()
                               .timelineDate(LocalDate.now().minusDays(3))
                               .timelineDescription("r1-timeline").build())
                    .build()))
            .specResponseTimelineOfEvents2(List.of(
                TimelineOfEvents.builder()
                    .value(TimelineOfEventDetails.builder()
                               .timelineDate(LocalDate.now().minusDays(1))
                               .timelineDescription("r2-timeline").build())
                    .build()))
            .respondToAdmittedClaim(RespondToClaim.builder()
                                        .howMuchWasPaid(new BigDecimal("1000")) // £10.00
                                        .howWasThisAmountPaid(
                                          .CREDIT_CARD)
                                        .whenWasThisAmountPaid(LocalDate.now().minusDays(1))
                                        .build())
            .build();

        // Minimal stubs for the two "populator" collaborators to keep this unit test atomic
        lenient().doAnswer(inv -> {
            SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder b = inv.getArgument(0);
            CaseData cd = inv.getArgument(1);
            b.referenceNumber(cd.getLegacyCaseReference());
            b.ccdCaseReference(cd.getCcdCaseReference() == null ? null : cd.getCcdCaseReference().toString());
            return null;
        }).when(referenceNumberPopulator).populateReferenceNumberDetails(any(), any(), any());

        lenient().doAnswer(inv -> {
            SealedClaimResponseFormForSpec.SealedClaimResponseFormForSpecBuilder b = inv.getArgument(0);
            CaseData cd = inv.getArgument(1);
            StatementOfTruth sot = cd.getRespondent2DQ() != null && cd.getRespondent2ResponseDate() != null
                && (cd.getRespondent1ResponseDate() == null
                || cd.getRespondent2ResponseDate().isAfter(cd.getRespondent1ResponseDate()))
                ? cd.getRespondent2DQ().getRespondent2DQStatementOfTruth()
                : (cd.getRespondent1DQ() != null ? cd.getRespondent1DQ().getRespondent1DQStatementOfTruth() : null);
            if (sot != null) {
                b.statementOfTruth(sot);
            }
            return null;
        }).when(statementOfTruthPopulator).populateStatementOfTruthDetails(any(), any());
    }

    @Test
    void getTemplateData_1v1_mapsBasics_andTimelineEntered() {
        // no upload document -> timelineEntered branch (empty timeline since none present)
        SealedClaimResponseFormForSpec dto = generator.getTemplateData(base1v1, AUTH);

        assertThat(dto.getReferenceNumber()).isEqualTo("LEG-123");
        assertThat(dto.getCcdCaseReference()).isEqualTo("1234567890123456");
        assertThat(dto.getWhyDisputeTheClaim()).isEqualTo("why-1v1");
        assertThat(dto.getStatementOfTruth().getName()).isEqualTo("sot1");
        assertThat(dto.getStatementOfTruth().getRole()).isEqualTo("role1");
        assertThat(dto.isTimelineUploaded()).isFalse();
        assertThat(dto.getTimeline()).isNotNull(); // empty list OK
    }

    @Test
    void getTemplateData_1v2_latestIsRespondent2_usesResp2_fields_timeline2_and_resp2_def_doc() {
        SealedClaimResponseFormForSpec dto = generator.getTemplateData(base1v2LatestIsResp2, AUTH);

        // populated by our stub
        assertThat(dto.getReferenceNumber()).isEqualTo("LEG-999");

        // chooses respondent2 branch in handleClaimResponse
        assertThat(dto.getWhyDisputeTheClaim()).isEqualTo("why-1v2-r2");

        // defence doc should be respondent2's file name
        assertThat(dto.getRespondent1SpecDefenceResponseDocument()).isEqualTo("resp2-def.pdf");

        // timeline2 used for respondent2
        assertThat(dto.isTimelineUploaded()).isFalse();
        assertThat(dto.getTimeline()).hasSize(1);
        assertThat(dto.getTimeline().get(0).getTimelineDescription()).isEqualTo("r2-timeline");

        // payments mapping (respondToAdmittedClaim present)
        assertThat(dto.getPoundsPaid()).isEqualTo("10.00"); // 1000 pennies -> £10.00
        assertThat(dto.getPaymentDate()).isEqualTo(base1v2LatestIsResp2.getRespondToAdmittedClaim().getWhenWasThisAmountPaid());
        // human-friendly for credit card comes from enum; we only assert it's non-empty
        assertThat(dto.getPaymentMethod()).isNotBlank();
    }

    @Test
    void getTemplateData_1v2_latestIsRespondent1_switches_to_resp1_fields_doc_and_timeline1() {
        CaseData resp1Latest = base1v2LatestIsResp2.toBuilder()
            .respondent2ResponseDate(LocalDateTime.now().minusDays(5)) // now respondent1 is latest
            .build();

        SealedClaimResponseFormForSpec dto = generator.getTemplateData(resp1Latest, AUTH);

        assertThat(dto.getWhyDisputeTheClaim()).isEqualTo("why-1v2-r1");
        assertThat(dto.getRespondent1SpecDefenceResponseDocument()).isEqualTo("resp1-def.pdf");
        assertThat(dto.getTimeline()).hasSize(1);
        assertThat(dto.getTimeline().get(0).getTimelineDescription()).isEqualTo("r1-timeline");
        // Sot should be respondent1's (as per our populator stub)
        assertThat(dto.getStatementOfTruth().getName()).isEqualTo("sot1");
    }

    @Test
    void getTemplateData_timelineUploaded_branch_sets_flag_and_filename() {
        CaseData uploaded = base1v1.toBuilder()
            .specResponseTimelineDocumentFiles(Document.builder().documentFileName("timeline.pdf").build())
            .build();

        SealedClaimResponseFormForSpec dto = generator.getTemplateData(uploaded, AUTH);

        assertThat(dto.isTimelineUploaded()).isTrue();
        assertThat(dto.getSpecResponseTimelineDocumentFiles()).isEqualTo("timeline.pdf");
        assertThat(dto.getTimeline()).isNull(); // not populated when uploaded=true
    }

    @Test
    void getTemplateData_paymentMethod_Other_uses_custom_text() {
        CaseData withOther = base1v1.toBuilder()
            .respondToClaim(RespondToClaim.builder()
                                .howMuchWasPaid(new BigDecimal("250")) // £2.50
                                .howWasThisAmountPaid(PaymentMethod.OTHER)
                                .howWasThisAmountPaidOther("Gift card")
                                .whenWasThisAmountPaid(LocalDate.now().minusDays(3))
                                .build())
            .build();

        SealedClaimResponseFormForSpec dto = generator.getTemplateData(withOther, AUTH);

        assertThat(dto.getPoundsPaid()).isEqualTo("2.50");
        assertThat(dto.getPaymentMethod()).isEqualTo("Gift card");
        assertThat(dto.getPaymentDate()).isEqualTo(withOther.getRespondToClaim().getWhenWasThisAmountPaid());
    }

    @Test
    void generate_1v1_uses_lr_admission_bulk_template_and_uploads_pdf() {
        when(documentGeneratorService.generateDocmosisDocument(any(), any()))
            .thenReturn(DocmosisDocument.builder()
                            .bytes(new byte[]{1, 2, 3})
                            .build());
        when(documentManagementService.uploadDocument(anyString(), any(PDF.class)))
            .thenReturn(CaseDocument.builder().documentType(DocumentType.SEALED_CLAIM).build());

        generator.generate(base1v1, AUTH);

        verify(documentGeneratorService).generateDocmosisDocument(
            templateDataCaptor.capture(),
            templateEnumCaptor.capture()
        );
        DocmosisTemplates chosen = templateEnumCaptor.getValue();
        assertThat(chosen).isEqualTo(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_SEALED_1V1_INSTALLMENTS_LR_ADMISSION_BULK);

        verify(documentManagementService).uploadDocument(eq(AUTH), pdfCaptor.capture());
        PDF pdf = pdfCaptor.getValue();
        assertThat(pdf.getDocumentType()).isEqualTo(DocumentType.SEALED_CLAIM);
        // filename should be formatted with legacy case reference
        assertThat(pdf.getFileBaseName()).contains("LEG-123");
        assertThat(pdf.getBytes()).isNotEmpty();
    }

    @Test
    void generate_1v2_sameResponses_uses_1v2_lr_admission_bulk_template() {
        CaseData multipartySame = base1v1.toBuilder()
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Resp2 Ltd").build())
            .respondentResponseIsSame(YesOrNo.YES)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(), any()))
            .thenReturn(DocmosisDocument.builder().bytes(new byte[]{9}).build());
        when(documentManagementService.uploadDocument(anyString(), any(PDF.class)))
            .thenReturn(CaseDocument.builder().documentType(DocumentType.SEALED_CLAIM).build());

        generator.generate(multipartySame, AUTH);

        verify(documentGeneratorService).generateDocmosisDocument(
            templateDataCaptor.capture(),
            templateEnumCaptor.capture()
        );
        assertThat(templateEnumCaptor.getValue())
            .isEqualTo(DocmosisTemplates.DEFENDANT_RESPONSE_SPEC_SEALED_1V2_LR_ADMISSION_BULK);
    }
}
