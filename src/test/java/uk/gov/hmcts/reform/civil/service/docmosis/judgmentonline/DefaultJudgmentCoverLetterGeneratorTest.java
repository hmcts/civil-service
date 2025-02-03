package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DEFAULT_JUDGMENT_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LEGAL_ORG;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getOrganisationByPolicy;

@ExtendWith(MockitoExtension.class)
class DefaultJudgmentCoverLetterGeneratorTest {

    private static final CaseDocument COVER_LETTER = CaseDocumentBuilder.builder()
        .documentName("cover letter")
        .documentType(DocumentType.DEFAULT_JUDGMENT_COVER_LETTER)
        .build();

    @Mock
    private DocumentDownloadService documentDownloadService;

    @Mock
    private DocumentGeneratorService documentGeneratorService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private CivilStitchService civilStitchService;

    @InjectMocks
    private DefaultJudgmentCoverLetterGenerator defaultJudgmentCoverLetterGenerator;
    private static final String DEFAULT_JUDGMENT_COVER_LETTER = "default-judgment-cover-letter";
    public static final String TASK_ID = "SendCoverLetterToDefendantLR";
    private static final String TEST = "test";
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private static final ContactInformation CONTACT_INFORMATION_ORG_1 = ContactInformation.builder()
        .addressLine1("Org1AddressLine1").addressLine2("Org1AddressLine2").townCity("Org1TownCity")
        .postCode("Org1PostCode").build();

    private static final ContactInformation CONTACT_INFORMATION_ORG_2 = ContactInformation.builder()
        .addressLine1("Org2AddressLine1").addressLine2("Org2AddressLine2").townCity("Org2TownCity")
        .postCode("Org2PostCode").build();

    private final List<Element<CaseDocument>> claimantResponseDocuments = new ArrayList<>();

    @BeforeEach
    void setup() {
        lenient().when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(
            DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LEGAL_ORG
                .getDocumentTitle(), LETTER_CONTENT, DocumentType.DEFAULT_JUDGMENT_COVER_LETTER)))
            .thenReturn(COVER_LETTER);

        lenient().when(documentGeneratorService.generateDocmosisDocument(
            any(MappableObject.class),
            eq(DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LEGAL_ORG)
        ))
            .thenReturn(new DocmosisDocument(
                DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LEGAL_ORG.getDocumentTitle(),
                LETTER_CONTENT
            ));

        lenient().when(documentDownloadService.downloadDocument(
            any(),
            any()
        )).thenReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), TEST, TEST));

        uk.gov.hmcts.reform.civil.prd.model.Organisation testOrg1 = uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().organisationIdentifier("123").build();
        uk.gov.hmcts.reform.civil.prd.model.Organisation testOrg2 = uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().organisationIdentifier("123").build();

        lenient().when(organisationService.findOrganisationById("1234"))
            .thenReturn(Optional.of(testOrg1));

        given(organisationService.findOrganisationById("3456"))
            .willReturn(Optional.of(testOrg2));

        OrganisationPolicy organisation1Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("1234").build()).build();

        OrganisationPolicy organisation2Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("3456").build()).build();

        lenient().when(getOrganisationByPolicy(organisation1Policy, organisationService))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                                        .organisationIdentifier("1234").name("Org1Name")
                                        .contactInformation(List.of(CONTACT_INFORMATION_ORG_1)).build()));

        lenient().when(getOrganisationByPolicy(organisation2Policy, organisationService))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder()
                                        .organisationIdentifier("3456").name("Org2Name")
                                        .contactInformation(List.of(CONTACT_INFORMATION_ORG_2)).build()));
    }

    @Test
    void shouldReturnEmptyIfStitchingIsDisabled() {
        // given
        ReflectionTestUtils.setField(defaultJudgmentCoverLetterGenerator, "stitchEnabled", false);
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1L)
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment().build();

        // when
        byte[] output = defaultJudgmentCoverLetterGenerator.generateAndPrintDjCoverLettersPlusDocument(caseData, BEARER_TOKEN, false);

        // then
        assertThat(output).isEqualTo(new byte[0]);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldDownloadDocumentAndPrintLetterSuccessfully(boolean toSecondDefendantLegalOrg) {
        // given
        ReflectionTestUtils.setField(defaultJudgmentCoverLetterGenerator, "stitchEnabled", true);

        OrganisationPolicy organisation1Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("1234").build()).build();
        OrganisationPolicy organisation2Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("3456").build()).build();

        claimantResponseDocuments.add(element(CaseDocument.builder()
                .createdDatetime(LocalDateTime.now())
                .documentName(format(
                        DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LEGAL_ORG.getDocumentTitle(),
                        "default-judgment-cover-letter",
                        "CaseReference")).documentType(DEFAULT_JUDGMENT_DEFENDANT1)
                                                  .documentLink(Document.builder().documentUrl("URL1").build())
                                                  .build()));
        claimantResponseDocuments.add(element(CaseDocument.builder()
                             .createdDatetime(LocalDateTime.now())
                             .documentName(format(
                                 DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LEGAL_ORG.getDocumentTitle(),
                                 "default-judgment-cover-letter",
                                 "CaseReference")).documentType(DocumentType.DEFAULT_JUDGMENT_DEFENDANT2)
                             .documentLink(Document.builder().documentUrl("URL2").build()).build()));

        CaseData caseData = CaseDataBuilder.builder()
            .ccdCaseReference(1L)
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .legacyCaseReference("100DC001")
            .respondent1Represented(YesOrNo.YES)
            .respondent2Represented(YesOrNo.YES)
            .respondent2SameLegalRepresentative(NO)
            .applicant1(Party.builder().organisationName("Applicant1 name").type(Party.Type.INDIVIDUAL).build())
            .applicant2(Party.builder().partyName("Applicant2 name").type(Party.Type.INDIVIDUAL).build())
            .respondent1(Party.builder().partyName("Respondent1 name").type(Party.Type.INDIVIDUAL).build())
            .respondent2(Party.builder().partyName("Respondent2 name").type(Party.Type.INDIVIDUAL).build())
            .respondent1OrganisationPolicy(organisation1Policy)
            .respondent2OrganisationPolicy(organisation2Policy)
            .defaultJudgmentDocuments(claimantResponseDocuments)
            .build();

        DocumentType documentType = toSecondDefendantLegalOrg ? DocumentType.DEFAULT_JUDGMENT_DEFENDANT2 : DEFAULT_JUDGMENT_DEFENDANT1;
        lenient().when(civilStitchService.generateStitchedCaseDocument(anyList(), anyString(), anyLong(), eq(documentType),
                                                                       anyString())).thenReturn(buildStitchedDocument());

        // when
        defaultJudgmentCoverLetterGenerator.generateAndPrintDjCoverLettersPlusDocument(caseData, BEARER_TOKEN, toSecondDefendantLegalOrg);

        // then
        verify(civilStitchService).generateStitchedCaseDocument(anyList(), anyString(), anyLong(),
                                                                any(DocumentType.class), anyString());

        verify(bulkPrintService, times(1))
            .printLetter(
                LETTER_CONTENT,
                caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(),
                DEFAULT_JUDGMENT_COVER_LETTER,
                List.of(toSecondDefendantLegalOrg ? "Org2Name" : "Org1Name")
            );
    }

    private CaseDocument buildStitchedDocument() {
        return CaseDocument.builder()
            .createdBy("John")
            .documentName("stitched doc")
            .documentSize(0L)
            .documentType(DocumentType.DEFAULT_JUDGMENT_COVER_LETTER)
            .createdDatetime(LocalDateTime.now())
            .documentLink(Document.builder()
                              .documentUrl("http://local/fake-url")
                              .documentFileName("file-name")
                              .documentBinaryUrl("http://local/fake-url/binary-url")
                              .build())
            .build();
    }
}
