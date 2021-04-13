package uk.gov.hmcts.reform.unspec.service.docmosis.aos;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.LitigationFriend;
import uk.gov.hmcts.reform.unspec.model.common.MappableObject;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.docmosis.aos.AcknowledgementOfClaimForm;
import uk.gov.hmcts.reform.unspec.model.docmosis.common.Respondent;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.ACKNOWLEDGEMENT_OF_CLAIM;
import static uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N9;
import static uk.gov.hmcts.reform.unspec.utils.DocmosisTemplateDataUtils.fetchSolicitorReferences;
import static uk.gov.hmcts.reform.unspec.utils.DocmosisTemplateDataUtils.toCaseName;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    AcknowledgementOfClaimGenerator.class,
    JacksonAutoConfiguration.class
})
class AcknowledgementOfClaimGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000LR001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = format(N9.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(ACKNOWLEDGEMENT_OF_CLAIM)
        .build();

    private final Representative representative = Representative.builder().organisationName("test org").build();

    @MockBean
    private DocumentManagementService documentManagementService;

    @MockBean
    private DocumentGeneratorService documentGeneratorService;

    @MockBean
    private RepresentativeService representativeService;

    @Autowired
    private AcknowledgementOfClaimGenerator generator;

    @BeforeEach
    void setup() {
        when(representativeService.getRespondentRepresentative(any())).thenReturn(representative);
    }

    @Test
    void shouldGenerateAcknowledgementOfClaim_whenValidDataIsProvided() {
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N9)))
            .thenReturn(new DocmosisDocument(N9.getDocumentTitle(), bytes));

        when(documentManagementService
                 .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimAcknowledge().build();

        AcknowledgementOfClaimForm expectedDocmosisData = AcknowledgementOfClaimForm.builder()
            .caseName("Mr. John Rambo \nvs Mr. Sole Trader T/A Sole Trader co")
            .referenceNumber(LEGACY_CASE_REFERENCE)
            .solicitorReferences(caseData.getSolicitorReferences())
            .issueDate(caseData.getIssueDate())
            .responseDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate())
            .respondent(Respondent.builder()
                            .name(caseData.getRespondent1().getPartyName())
                            .primaryAddress(caseData.getRespondent1().getPrimaryAddress())
                            .representative(representative)
                            .litigationFriendName("")
                            .build())
            .build();

        CaseDocument caseDocument = generator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondentRepresentative(caseData);
        verify(documentManagementService)
            .uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, ACKNOWLEDGEMENT_OF_CLAIM));
        verify(documentGeneratorService)
            .generateDocmosisDocument(expectedDocmosisData, N9);
    }

    @Nested
    class GetTemplateData {

        @Test
        void whenCaseIsAtClaimAcknowledge_shouldGetAcknowledgementOfClaimFormData() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimAcknowledge().build().toBuilder()
                .respondent1LitigationFriend(LitigationFriend.builder().fullName("LF name").build())
                .build();

            var templateData = generator.getTemplateData(caseData);

            verify(representativeService).getRespondentRepresentative(caseData);
            assertThatFieldsAreCorrect(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect(AcknowledgementOfClaimForm templateData, CaseData caseData) {
            Assertions.assertAll(
                "AcknowledgementOfClaim data should be as expected",
                () -> assertEquals(templateData.getCaseName(), toCaseName.apply(caseData)),
                () -> assertEquals(templateData.getReferenceNumber(), caseData.getLegacyCaseReference()),
                () -> assertEquals(
                    templateData.getSolicitorReferences(),
                    fetchSolicitorReferences(caseData.getSolicitorReferences())
                ),
                () -> assertEquals(templateData.getIssueDate(), caseData.getIssueDate()),
                () -> assertEquals(
                    templateData.getResponseDeadline(),
                    caseData.getRespondent1ResponseDeadline().toLocalDate()
                ),
                () -> assertEquals(templateData.getRespondent(), Respondent.builder()
                    .name(caseData.getRespondent1().getPartyName())
                    .representative(representative)
                    .litigationFriendName(caseData.getRespondent1LitigationFriend().getFullName())
                    .primaryAddress(caseData.getRespondent1().getPrimaryAddress())
                    .build()
                )
            );
        }
    }
}
