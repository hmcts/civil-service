package uk.gov.hmcts.reform.unspec.service.docmosis.sealedclaim;

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
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.Address;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.LitigationFriend;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.SolicitorReferences;
import uk.gov.hmcts.reform.unspec.model.common.MappableObject;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.unspec.model.docmosis.common.Applicant;
import uk.gov.hmcts.reform.unspec.model.docmosis.common.Respondent;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.SealedClaimForm;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.documents.PDF;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.unspec.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.unspec.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.unspec.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.unspec.utils.DocmosisTemplateDataUtils;

import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.unspec.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.unspec.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.unspec.service.docmosis.sealedclaim.SealedClaimFormGenerator.TEMP_CLAIM_DETAILS;
import static uk.gov.hmcts.reform.unspec.utils.DocmosisTemplateDataUtils.toCaseName;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SealedClaimFormGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class SealedClaimFormGeneratorTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String fileName = format(N1.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(SEALED_CLAIM)
        .build();

    private final Representative representative = Representative.builder().organisationName("test org").build();

    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private SealedClaimFormGenerator sealedClaimFormGenerator;
    @MockBean
    private RepresentativeService representativeService;

    @BeforeEach
    void setup() {
        when(representativeService.getRespondentRepresentative(any())).thenReturn(representative);
    }

    @Test
    void shouldGenerateSealedClaimForm_whenValidDataIsProvided() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N1)))
            .thenReturn(new DocmosisDocument(N1.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondentRepresentative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(any(SealedClaimForm.class), eq(N1));
    }

    @Nested
    class GetTemplateData {

        @Test
        void whenCaseIsAtClaimCreated_shouldGetSealedClaimFormData() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build().toBuilder()
                .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                .build();

            var templateData = sealedClaimFormGenerator.getTemplateData(caseData);

            verify(representativeService).getRespondentRepresentative(caseData);
            assertThatFieldsAreCorrect(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect(SealedClaimForm templateData, CaseData caseData) {
            Assertions.assertAll(
                "SealedClaimForm data should be as expected",
                () -> assertEquals(templateData.getCaseName(), toCaseName.apply(caseData)),

                () -> assertEquals(templateData.getApplicants(), getApplicants(caseData)),
                () -> assertEquals(templateData.getRespondents(), getRespondents(caseData)),
                () -> assertEquals(templateData.getClaimValue(), caseData.getClaimValue().formData()),
                () -> assertEquals(
                    templateData.getStatementOfTruth(),
                    caseData.getApplicantSolicitor1ClaimStatementOfTruth()
                ),
                () -> assertEquals(TEMP_CLAIM_DETAILS, templateData.getClaimDetails()),
                () -> assertEquals(
                    templateData.getHearingCourtLocation(),
                    caseData.getCourtLocation().getApplicantPreferredCourt()
                ),
                () -> assertEquals(templateData.getApplicantRepresentative(), getRepresentative()),
                () -> assertEquals(templateData.getReferenceNumber(), caseData.getLegacyCaseReference()),
                () -> assertEquals(templateData.getIssueDate(), caseData.getIssueDate()),
                () -> assertEquals(templateData.getSubmittedOn(), caseData.getSubmittedDate().toLocalDate()),
                () -> assertEquals(
                    templateData.getApplicantExternalReference(),
                    ofNullable(caseData.getSolicitorReferences())
                        .map(SolicitorReferences::getApplicantSolicitor1Reference)
                        .orElse("")
                ),
                () -> assertEquals(
                    templateData.getRespondentExternalReference(),
                    ofNullable(caseData.getSolicitorReferences())
                        .map(SolicitorReferences::getRespondentSolicitor1Reference)
                        .orElse("")
                ),
                () -> assertEquals(templateData.getCaseName(), DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            );
        }

        private List<Respondent> getRespondents(CaseData caseData) {
            Party respondent = caseData.getRespondent1();
            return List.of(Respondent.builder()
                               .name(respondent.getPartyName())
                               .primaryAddress(respondent.getPrimaryAddress())
                               .representative(representative)
                               .build());
        }

        private Representative getRepresentative() {
            return Representative.builder()
                .organisationName("MiguelSpooner")
                .dxAddress("DX 751Newport")
                .organisationName("DBE Law")
                .phoneNumber("0800 206 1592")
                .emailAddress("jim.smith@slatergordon.com")
                .serviceAddress(Address.builder()
                                    .addressLine1("AdmiralHouse")
                                    .addressLine2("Queensway")
                                    .postTown("Newport")
                                    .postCode("NP204AG")
                                    .build())
                .build();
        }

        private List<Applicant> getApplicants(CaseData caseData) {
            Party applicant = caseData.getApplicant1();
            return List.of(Applicant.builder()
                               .name(applicant.getPartyName())
                               .primaryAddress(applicant.getPrimaryAddress())
                               .litigationFriendName("applicant LF")
                               .build());
        }
    }
}
