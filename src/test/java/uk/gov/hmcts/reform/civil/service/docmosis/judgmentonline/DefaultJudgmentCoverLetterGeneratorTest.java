package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
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
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LR;

@SpringBootTest(classes = {
    DefaultJudgmentCoverLetterGenerator.class,
    JacksonAutoConfiguration.class
})
class DefaultJudgmentCoverLetterGeneratorTest {

    @MockBean
    private DocumentDownloadService documentDownloadService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private BulkPrintService bulkPrintService;
    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private DefaultJudgmentCoverLetterGenerator defaultJudgmentCoverLetterGenerator;
    private static final String DEFAULT_JUDGMENT_COVER_LETTER = "default-judgment-cover-letter";
    public static final String TASK_ID = "SendCoverLetterToDefendantLR";
    private static final String TEST = "test";
    private static final Document DOCUMENT_LINK = new Document("document/url", TEST, TEST, TEST, TEST);
    private static final byte[] LETTER_CONTENT = new byte[]{37, 80, 68, 70, 45, 49, 46, 53, 10, 37, -61, -92};
    private static final String BEARER_TOKEN = "BEARER_TOKEN";

    private static final CaseDocument COVER_LETTER = CaseDocumentBuilder.builder()
        .documentName(null)
        .documentType(DocumentType.DEFAULT_JUDGMENT_COVER_LETTER)
        .build();

    @Test
    void shouldDownloadDocumentAndPrintLetterSuccessfully() {
        // given
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder()
                              .organisationID(null)
                              .build())
            .orgPolicyReference("orgreference")
            .orgPolicyCaseAssignedRole("orgassignedrole")
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .respondent1Represented(YesOrNo.YES)
            .respondent2Represented(YesOrNo.YES)
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondent1(Party.builder().partyName("John Dhoe").build())
            .respondent2(Party.builder().partyName("Mark Smith").build())

            .respondent1OrganisationIDCopy("ORG ID 1")
            .respondent1OrganisationPolicy(organisationPolicy)
            .respondent2OrganisationIDCopy("ORG ID 2")
            .respondent2OrganisationPolicy(organisationPolicy)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(
            any(MappableObject.class),
            eq(DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LR)
        ))
            .thenReturn(new DocmosisDocument(
                DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LR.getDocumentTitle(),
                LETTER_CONTENT
            ));
        when(documentManagementService
                 .uploadDocument(
                     BEARER_TOKEN,
                     new PDF(DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LR.getDocumentTitle(),
                             LETTER_CONTENT,
                             DocumentType.DEFAULT_JUDGMENT_COVER_LETTER
                     )
                 ))
            .thenReturn(COVER_LETTER);

        given(documentDownloadService.downloadDocument(
            any(),
            any()
        )).willReturn(new DownloadedDocumentResponse(new ByteArrayResource(LETTER_CONTENT), TEST, TEST));

        // when
        defaultJudgmentCoverLetterGenerator.generateAndPrintDjCoverLetter(caseData, BEARER_TOKEN);
        // then
        verify(bulkPrintService)
            .printLetter(
                LETTER_CONTENT,
                caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(),
                DEFAULT_JUDGMENT_COVER_LETTER,
                List.of(caseData.getRespondent1().getPartyName()) //TODO should fail here, legal ORG name
            );
    }

}
