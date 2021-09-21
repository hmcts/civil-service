package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

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
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDocumentBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;

import java.util.List;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.model.documents.DocumentType.SEALED_CLAIM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N1_MULTIPARTY_SAME_SOL;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.toCaseName;

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
    private static final String fileNameDiffSol = format(N1_MULTIPARTY_SAME_SOL.getDocumentTitle(), REFERENCE_NUMBER);
    private static final CaseDocument CASE_DOCUMENT = CaseDocumentBuilder.builder()
        .documentName(fileName)
        .documentType(SEALED_CLAIM)
        .build();

    private final Representative representative1 = Representative.builder().organisationName("test org").build();
    private final Representative representative2 = Representative.builder().organisationName("test org2").build();

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
        when(representativeService.getRespondent1Representative(any())).thenReturn(representative1);
        when(representativeService.getRespondent2Representative(any())).thenReturn(representative2);
        when(representativeService.getApplicantRepresentative(any())).thenReturn(getRepresentative());
    }

    @Test
    void shouldGenerateSealedClaimForm_when1V1DataIsProvided() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N1)))
            .thenReturn(new DocmosisDocument(N1.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(any(SealedClaimForm.class), eq(N1));
    }

    @Test
    void shouldGenerateSealedClaimForm_when1V2DifferentSolicitorDataIsProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .multiPartyClaimTwoDefendantSolicitors().build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N1)))
            .thenReturn(new DocmosisDocument(N1.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(representativeService).getRespondent2Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(any(SealedClaimForm.class), eq(N1));
    }

    @Test
    void shouldGenerateSealedClaimForm_when2V1DataIsProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .multiPartyClaimTwoApplicants().build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N1_MULTIPARTY_SAME_SOL)))
            .thenReturn(new DocmosisDocument(N1_MULTIPARTY_SAME_SOL.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameDiffSol, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(SealedClaimForm.class),
            eq(N1_MULTIPARTY_SAME_SOL)
        );
    }

    @Test
    void shouldGenerateSealedClaimForm_when1V2SameSolicitorDataIsProvided() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .multiPartyClaimOneDefendantSolicitor().build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(N1_MULTIPARTY_SAME_SOL)))
            .thenReturn(new DocmosisDocument(N1_MULTIPARTY_SAME_SOL.getDocumentTitle(), bytes));

        when(documentManagementService.uploadDocument(BEARER_TOKEN, new PDF(fileNameDiffSol, bytes, SEALED_CLAIM)))
            .thenReturn(CASE_DOCUMENT);

        CaseDocument caseDocument = sealedClaimFormGenerator.generate(caseData, BEARER_TOKEN);
        assertThat(caseDocument).isNotNull().isEqualTo(CASE_DOCUMENT);

        verify(representativeService).getRespondent1Representative(caseData);
        verify(documentManagementService).uploadDocument(BEARER_TOKEN, new PDF(fileName, bytes, SEALED_CLAIM));
        verify(documentGeneratorService).generateDocmosisDocument(
            any(SealedClaimForm.class),
            eq(N1_MULTIPARTY_SAME_SOL)
        );
    }

    private Representative getRepresentative() {
        return Representative.builder()
            .organisationName("MiguelSpooner")
            .dxAddress("DX 751Newport")
            .organisationName("DBE Law")
            .emailAddress("jim.smith@slatergordon.com")
            .serviceAddress(Address.builder()
                                .addressLine1("AdmiralHouse")
                                .addressLine2("Queensway")
                                .postTown("Newport")
                                .postCode("NP204AG")
                                .build())
            .build();
    }

    @Nested
    class GetTemplateData {

        @Test
        void whenCaseIsAtClaimDetailsNotified_shouldGetSealedClaimFormDataFor1V1() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
                .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                .build();

            var templateData = sealedClaimFormGenerator.getTemplateData(caseData);

            verify(representativeService).getApplicantRepresentative(caseData);
            verify(representativeService).getRespondent1Representative(caseData);
            assertThatFieldsAreCorrect_For1V1(templateData, caseData);
        }

        @Test
        void whenCaseIsAtClaimDetailsNotified_shouldGetSealedClaimFormDataFor1V2DifferentSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoDefendantSolicitors().build().toBuilder()
                .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                .build();

            final var templateData = sealedClaimFormGenerator.getTemplateData(caseData);

            verify(representativeService).getApplicantRepresentative(caseData);
            verify(representativeService).getRespondent1Representative(caseData);
            verify(representativeService).getRespondent2Representative(caseData);
            assertThatFieldsAreCorrect_For1V2DifferentSolicitor(templateData, caseData);
        }

        @Test
        void whenCaseIsAtClaimDetailsNotified_shouldGetSealedClaimFormDataFor2V1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimTwoApplicants().build().toBuilder()
                .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                .applicant2LitigationFriend(LitigationFriend.builder().fullName("applicant2 LF").build())
                .build();

            var templateData = sealedClaimFormGenerator.getTemplateData(caseData);

            verify(representativeService).getApplicantRepresentative(caseData);
            verify(representativeService).getRespondent1Representative(caseData);
            assertThatFieldsAreCorrect_For2V1(templateData, caseData);
        }

        @Test
        void whenCaseIsAtClaimDetailsNotified_shouldGetSealedClaimFormDataFor1V2SameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .multiPartyClaimOneDefendantSolicitor().build().toBuilder()
                .applicant1LitigationFriend(LitigationFriend.builder().fullName("applicant LF").build())
                .build();

            var templateData = sealedClaimFormGenerator.getTemplateData(caseData);

            verify(representativeService).getApplicantRepresentative(caseData);
            verify(representativeService).getRespondent1Representative(caseData);
            assertThatFieldsAreCorrect_For1V2SameSolicitor(templateData, caseData);
        }

        private void assertThatFieldsAreCorrect_For1V1(SealedClaimForm templateData, CaseData caseData) {
            Assertions.assertAll(
                "SealedClaimForm data should be as expected",
                () -> assertEquals(templateData.getCaseName(), toCaseName.apply(caseData)),
                () -> assertEquals(templateData.getApplicants(), getApplicant(caseData)),
                () -> assertEquals(templateData.getRespondents(), getRespondent(caseData)),
                () -> assertEquals(templateData.getClaimValue(), caseData.getClaimValue().formData()),
                () -> assertEquals(
                    templateData.getStatementOfTruth(),
                    caseData.getApplicantSolicitor1ClaimStatementOfTruth()
                ),
                () -> assertEquals(templateData.getClaimDetails(), caseData.getDetailsOfClaim()),
                () -> assertEquals(
                    templateData.getHearingCourtLocation(),
                    caseData.getCourtLocation().getApplicantPreferredCourt()
                ),
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
                    templateData.getRespondent1ExternalReference(),
                    ofNullable(caseData.getSolicitorReferences())
                        .map(SolicitorReferences::getRespondentSolicitor1Reference)
                        .orElse("")
                ),
                () -> assertEquals(templateData.getCaseName(), DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            );
        }

        private void assertThatFieldsAreCorrect_For1V2SameSolicitor(SealedClaimForm templateData, CaseData caseData) {
            Assertions.assertAll(
                "SealedClaimForm data should be as expected",
                () -> assertEquals(templateData.getCaseName(), toCaseName.apply(caseData)),
                () -> assertEquals(templateData.getApplicants(), getApplicant(caseData)),
                () -> assertEquals(templateData.getRespondents(), getRespondentsSameSolicitor(caseData)),
                () -> assertEquals(templateData.getClaimValue(), caseData.getClaimValue().formData()),
                () -> assertEquals(
                    templateData.getStatementOfTruth(),
                    caseData.getApplicantSolicitor1ClaimStatementOfTruth()
                ),
                () -> assertEquals(templateData.getClaimDetails(), caseData.getDetailsOfClaim()),
                () -> assertEquals(
                    templateData.getHearingCourtLocation(),
                    caseData.getCourtLocation().getApplicantPreferredCourt()
                ),
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
                    templateData.getRespondent1ExternalReference(),
                    ofNullable(caseData.getSolicitorReferences())
                        .map(SolicitorReferences::getRespondentSolicitor1Reference)
                        .orElse("")
                ),
                () -> assertEquals(templateData.getCaseName(), DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            );
        }

        private void assertThatFieldsAreCorrect_For2V1(SealedClaimForm templateData, CaseData caseData) {
            Assertions.assertAll(
                "SealedClaimForm data should be as expected",
                () -> assertEquals(templateData.getCaseName(), toCaseName.apply(caseData)),
                () -> assertEquals(templateData.getApplicants(), getApplicants(caseData)),
                () -> assertEquals(templateData.getRespondents(), getRespondent(caseData)),
                () -> assertEquals(templateData.getClaimValue(), caseData.getClaimValue().formData()),
                () -> assertEquals(
                    templateData.getStatementOfTruth(),
                    caseData.getApplicantSolicitor1ClaimStatementOfTruth()
                ),
                () -> assertEquals(templateData.getClaimDetails(), caseData.getDetailsOfClaim()),
                () -> assertEquals(
                    templateData.getHearingCourtLocation(),
                    caseData.getCourtLocation().getApplicantPreferredCourt()
                ),
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
                    templateData.getRespondent1ExternalReference(),
                    ofNullable(caseData.getSolicitorReferences())
                        .map(SolicitorReferences::getRespondentSolicitor1Reference)
                        .orElse("")
                ),
                () -> assertEquals(templateData.getCaseName(), DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            );
        }

        private void assertThatFieldsAreCorrect_For1V2DifferentSolicitor(
            SealedClaimForm templateData,
            CaseData caseData
        ) {
            Assertions.assertAll(
                "SealedClaimForm data should be as expected",
                () -> assertEquals(templateData.getCaseName(), toCaseName.apply(caseData)),
                () -> assertEquals(templateData.getApplicants(), getApplicant(caseData)),
                () -> assertEquals(templateData.getRespondents(), getRespondentsDifferentSolicitor(caseData)),
                () -> assertEquals(templateData.getClaimValue(), caseData.getClaimValue().formData()),
                () -> assertEquals(templateData.getCourtFee(), caseData.getClaimFee().formData()),
                () -> assertEquals(
                    templateData.getStatementOfTruth(),
                    caseData.getApplicantSolicitor1ClaimStatementOfTruth()
                ),
                () -> assertEquals(templateData.getClaimDetails(), caseData.getDetailsOfClaim()),
                () -> assertEquals(
                    templateData.getHearingCourtLocation(),
                    caseData.getCourtLocation().getApplicantPreferredCourt()
                ),
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
                    templateData.getRespondent1ExternalReference(),
                    ofNullable(caseData.getSolicitorReferences())
                        .map(SolicitorReferences::getRespondentSolicitor1Reference)
                        .orElse("")
                ),
                () -> assertEquals(
                    templateData.getRespondent2ExternalReference(),
                    ofNullable(caseData.getSolicitorReferences())
                        .map(SolicitorReferences::getRespondentSolicitor2Reference)
                        .orElse("")
                ),
                () -> assertEquals(templateData.getCaseName(), DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            );
        }

        private List<Party> getRespondent(CaseData caseData) {
            var respondent = caseData.getRespondent1();
            return List.of(Party.builder()
                               .name(respondent.getPartyName())
                               .primaryAddress(respondent.getPrimaryAddress())
                               .representative(representative1)
                               .build());
        }

        private List<Party> getRespondentsSameSolicitor(CaseData caseData) {
            var respondent1 = caseData.getRespondent1();
            var respondent2 = caseData.getRespondent2();
            return List.of(
                Party.builder()
                    .name(respondent1.getPartyName())
                    .primaryAddress(respondent1.getPrimaryAddress())
                    .representative(representative1)
                    .build(),
                Party.builder()
                    .name(respondent2.getPartyName())
                    .primaryAddress(respondent2.getPrimaryAddress())
                    .representative(representative1)
                    .build()
            );
        }

        private List<Party> getRespondentsDifferentSolicitor(CaseData caseData) {
            var respondent1 = caseData.getRespondent1();
            var respondent2 = caseData.getRespondent2();
            return List.of(
                Party.builder()
                    .name(respondent1.getPartyName())
                    .primaryAddress(respondent1.getPrimaryAddress())
                    .representative(representative1)
                    .build(),
                Party.builder()
                    .name(respondent2.getPartyName())
                    .primaryAddress(respondent2.getPrimaryAddress())
                    .representative(representative2)
                    .build()
            );
        }

        private List<Party> getApplicant(CaseData caseData) {
            var applicant = caseData.getApplicant1();
            return List.of(Party.builder()
                               .name(applicant.getPartyName())
                               .primaryAddress(applicant.getPrimaryAddress())
                               .litigationFriendName("applicant LF")
                               .representative(getRepresentative())
                               .build());
        }

        private List<Party> getApplicants(CaseData caseData) {
            var applicant = caseData.getApplicant1();
            var applicant2 = caseData.getApplicant2();
            return List.of(
                Party.builder()
                    .name(applicant.getPartyName())
                    .primaryAddress(applicant.getPrimaryAddress())
                    .litigationFriendName("applicant LF")
                    .representative(getRepresentative())
                    .build(),
                Party.builder()
                    .name(applicant2.getPartyName())
                    .primaryAddress(applicant2.getPrimaryAddress())
                    .litigationFriendName("applicant2 LF")
                    .representative(getRepresentative())
                    .build()
            );
        }
    }
}
