package uk.gov.hmcts.reform.civil.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.UploadDocumentByType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.DocUploadUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.STRING_CONSTANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

public class DocUploadUtilsTest {

    private static final String DUMMY_EMAIL = "test@gmail.com";

    @Test
    public void should_addToAddl() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        List<Element<CaseDocument>> tobeAdded = new ArrayList<>();
        tobeAdded.add(element(CaseDocument.builder().createdBy("civil")
                .documentLink(Document.builder()
                        .documentFileName("witness_document.pdf")
                        .documentUrl("http://dm-store:8080")
                        .documentBinaryUrl("http://dm-store:8080/documents")
                        .build())
                .documentName("witness_document.docx")
                .createdDatetime(LocalDateTime.now()).build()));
        tobeAdded.add(element(CaseDocument.builder().createdBy("civil")
                .documentLink(Document.builder()
                        .documentFileName("witness_document.pdf")
                        .documentUrl("http://dm-store:8080")
                        .documentBinaryUrl("http://dm-store:8080/documents")
                        .build())
                .documentName("witness_document.docx")
                .createdDatetime(LocalDateTime.now()).build()));

        DocUploadUtils.addToAddl(caseData, builder, tobeAdded, DocUploadUtils.APPLICANT, false);
        caseData = builder.build();
        assertThat(caseData.getGaAddlDocClaimant().size()).isEqualTo(2);
        assertThat(caseData.getCaseDocumentUploadDate()).isNull();
        builder = caseData.toBuilder();
        DocUploadUtils.addToAddl(caseData, builder, tobeAdded, DocUploadUtils.RESPONDENT_ONE, false);
        caseData = builder.build();
        assertThat(caseData.getGaAddlDocRespondentSol().size()).isEqualTo(2);
        assertThat(caseData.getCaseDocumentUploadDateRes()).isNull();
        builder = caseData.toBuilder();
        DocUploadUtils.addToAddl(caseData, builder, tobeAdded, DocUploadUtils.RESPONDENT_TWO, true);
        caseData = builder.build();
        assertThat(caseData.getGaAddlDocRespondentSolTwo().size()).isEqualTo(2);
        assertThat(caseData.getCaseDocumentUploadDateRes()).isNotNull();
        assertThat(caseData.getGaAddlDocRespondentSolTwo().size()).isEqualTo(2);
        assertThat(caseData.getGaAddlDoc().size()).isEqualTo(2);
        assertThat(caseData.getGaAddlDocStaff().size()).isEqualTo(2);
    }

    @Test
    public void should_prepareUploadDocumentByType() {
        List<Element<UploadDocumentByType>> uploadDocument = new ArrayList<>();
        uploadDocument.add(element(UploadDocumentByType.builder()
                .documentType("Witness")
                .additionalDocument(Document.builder()
                        .documentFileName("witness_document.pdf")
                        .documentUrl("http://dm-store:8080")
                        .documentBinaryUrl("http://dm-store:8080/documents")
                        .build()).build()));
        List<Element<CaseDocument>> result = DocUploadUtils.prepareUploadDocumentByType(uploadDocument, "role");
        assertThat(result.get(0).getValue().getCreatedBy()).isEqualTo("role");
        assertThat(result.get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo(AssignCategoryId.APPLICATIONS);
    }

    @Test
    public void should_getUserRole() {
        List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
        gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                .id("222")
                .email(DUMMY_EMAIL)
                .organisationIdentifier("2").build()));
        gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                .id("id1")
                .email(DUMMY_EMAIL)
                .organisationIdentifier("2").build()));
        gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                .id("id3")
                .email(DUMMY_EMAIL)
                .organisationIdentifier("2").build()));
        gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                .id(STRING_CONSTANT)
                .email(DUMMY_EMAIL)
                .organisationIdentifier("3").build()));
        gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                .id("id33")
                .email(DUMMY_EMAIL)
                .organisationIdentifier("3").build()));
        GASolicitorDetailsGAspec applicantSolicitor = GASolicitorDetailsGAspec.builder()
            .id("2")
            .forename("GAApplnSolicitor")
            .email(DUMMY_EMAIL)
            .organisationIdentifier("1")
            .build();

        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(gaRespSolicitors)
                .parentClaimantIsApplicant(NO)
                .isMultiParty(YES)
                .generalAppApplnSolicitor(applicantSolicitor)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();

        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseData.builder()
            .parentClaimantIsApplicant(NO)
            .isMultiParty(YES)
            .generalAppApplnSolicitor(applicantSolicitor)
            .generalAppRespondentSolicitors(gaRespSolicitors)
            .isGaApplicantLip(NO)
            .isGaRespondentOneLip(NO)
            .build();

        assertThat(DocUploadUtils.getUserRole(caseData, gaCaseData, "2")).isEqualTo(DocUploadUtils.APPLICANT);
        assertThat(DocUploadUtils.getUserRole(caseData, gaCaseData, "id1")).isEqualTo(DocUploadUtils.RESPONDENT_ONE);
        assertThat(DocUploadUtils.getUserRole(caseData, gaCaseData, "id33")).isEqualTo(DocUploadUtils.RESPONDENT_TWO);
    }

    @Test
    public void should_addDocuments() {
        List<Element<CaseDocument>> from = new ArrayList<>();
        from.add(element(CaseDocument.builder().createdBy("civil")
                .documentLink(Document.builder()
                        .documentFileName("witness_document.pdf")
                        .documentUrl("http://dm-store:8080")
                        .documentBinaryUrl("http://dm-store:8080/documents")
                        .build())
                .documentName("witness_document.docx")
                .createdDatetime(LocalDateTime.now()).build()));
        from.add(element(CaseDocument.builder().createdBy("civil")
                .documentLink(Document.builder()
                        .documentFileName("witness_document.pdf")
                        .documentUrl("http://dm-store:8080")
                        .documentBinaryUrl("http://dm-store:8080/documents")
                        .build())
                .documentName("witness_document.docx")
                .createdDatetime(LocalDateTime.now()).build()));
        UUID sameId = from.get(1).getId();
        List<Element<CaseDocument>> to = new ArrayList<>();
        to.add(Element.<CaseDocument>builder()
                .id(sameId).value(CaseDocument.builder().createdBy("civil")
                .documentLink(Document.builder()
                        .documentFileName("witness_document.pdf")
                        .documentUrl("http://dm-store:8080")
                        .documentBinaryUrl("http://dm-store:8080/documents")
                        .build())
                .documentName("witness_document.docx")
                .createdDatetime(LocalDateTime.now()).build()).build());
        assertThat(DocUploadUtils.addDocuments(from, to).size()).isEqualTo(2);
    }

    @Test
    public void should_prepareDocuments() {
        List<Element<Document>> source = List.of(element(Document.builder()
                .documentFileName("witness_document.pdf")
                .documentUrl("http://dm-store:8080")
                .documentBinaryUrl("http://dm-store:8080/documents")
                .build()));
        List<Element<CaseDocument>> result = DocUploadUtils
                .prepareDocuments(source, "role", CaseEvent.INITIATE_GENERAL_APPLICATION);
        assertThat(result.get(0).getValue().getDocumentLink().getCategoryID()).isEqualTo(AssignCategoryId.APPLICATIONS);
        assertThat(result.get(0).getValue().getDocumentName()).isEqualTo("Supporting evidence");
        assertThat(result.get(0).getValue().getCreatedBy()).isEqualTo("role");
    }

    @ParameterizedTest
    @CsvSource({
        "INITIATE_GENERAL_APPLICATION,Supporting evidence",
        "RESPOND_TO_JUDGE_ADDITIONAL_INFO,Additional information",
        "RESPOND_TO_JUDGE_DIRECTIONS,Directions order",
        "RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION,Written representation",
        "RESPOND_TO_APPLICATION,Respond evidence",
        "LINK_GENERAL_APPLICATION_CASE_TO_PARENT_CASE,Unsupported event"
    })
    public void should_getDocumentName(String event, String name) {
        CaseEvent caseEvent = CaseEvent.valueOf(event);
        assertThat(DocUploadUtils.getDocumentName(caseEvent)).isEqualTo(name);
    }

    @ParameterizedTest
    @CsvSource({
        "RESPOND_TO_JUDGE_ADDITIONAL_INFO,REQUEST_FOR_INFORMATION",
        "RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION,WRITTEN_REPRESENTATION_SEQUENTIAL"
    })
    public void should_getDocumentType(String event, DocumentType name) {
        CaseEvent caseEvent = CaseEvent.valueOf(event);
        assertThat(DocUploadUtils.getDocumentType(caseEvent)).isEqualTo(name);
    }

    @Test
    public void shouldNotVisible_whenWithoutNotice() {
        CaseData caseData = CaseDataBuilder.builder()
                .buildFeeValidationCaseData(null, false, false);
        assertThat(DocUploadUtils.isDocumentVisible(caseData)).isEqualTo(YesOrNo.NO);
    }

    @Test
    public void shouldVisible_whenUnCloaked() {
        CaseData caseData = CaseData.builder()
                .applicationIsCloaked(YesOrNo.NO).build();
        assertThat(DocUploadUtils.isDocumentVisible(caseData)).isEqualTo(YesOrNo.YES);
    }

    @Test
    public void shouldVisible_whenConsent() {
        CaseData caseData = CaseData.builder()
                .generalAppConsentOrder(YesOrNo.YES).build();
        assertThat(DocUploadUtils.isDocumentVisible(caseData)).isEqualTo(YesOrNo.YES);
    }

    @Test
    public void shouldVisible_whenWithNotice() {
        CaseData caseData = CaseDataBuilder.builder().withNoticeCaseData();
        assertThat(DocUploadUtils.isDocumentVisible(caseData)).isEqualTo(YesOrNo.YES);
    }

    @Test
    public void shouldSetApplicantRespondentWhenRoleIsApplicant() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();
        DocUploadUtils.setRespondedValues(builder, DocUploadUtils.APPLICANT);
        caseData = builder.build();
        assertThat(caseData.getIsApplicantResponded()).isEqualTo(YesOrNo.YES);
    }

    @Test
    public void shouldSetApplicantRespondentWhenRoleIsRespondent() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();
        DocUploadUtils.setRespondedValues(builder, DocUploadUtils.RESPONDENT_ONE);
        caseData = builder.build();
        assertThat(caseData.getIsRespondentResponded()).isEqualTo(YesOrNo.YES);
    }

    @Test
    public void should_addToPreTranslationApplicant() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        List<Element<Document>> tobeAdded = new ArrayList<>();
        tobeAdded.add(element(Document.builder()
                                  .documentFileName("witness_document.pdf")
                                  .documentUrl("http://dm-store:8080")
                                  .documentBinaryUrl("http://dm-store:8080/documents").build()));
        tobeAdded.add(element(Document.builder()
                                  .documentFileName("witness_document.pdf")
                                  .documentUrl("http://dm-store:8080")
                                  .documentBinaryUrl("http://dm-store:8080/documents").build()));

        DocUploadUtils.addDocumentToPreTranslation(caseData, builder, tobeAdded, DocUploadUtils.APPLICANT, RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION);
        caseData = builder.build();
        assertThat(caseData.getPreTranslationGaDocsApplicant().size()).isEqualTo(1);
        assertThat(caseData.getGaAddlDocClaimant().size()).isEqualTo(1);
        assertThat(caseData.getPreTranslationGaDocuments().size()).isEqualTo(1);
    }

    @Test
    public void should_addToPreTranslationRespondent() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        List<Element<Document>> tobeAdded = new ArrayList<>();
        tobeAdded.add(element(Document.builder()
                                  .documentFileName("witness_document.pdf")
                                  .documentUrl("http://dm-store:8080")
                                  .documentBinaryUrl("http://dm-store:8080/documents").build()));
        tobeAdded.add(element(Document.builder()
                                  .documentFileName("witness_document.pdf")
                                  .documentUrl("http://dm-store:8080")
                                  .documentBinaryUrl("http://dm-store:8080/documents").build()));

        DocUploadUtils.addDocumentToPreTranslation(caseData, builder, tobeAdded, DocUploadUtils.RESPONDENT_ONE, RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION);
        caseData = builder.build();
        assertThat(caseData.getPreTranslationGaDocsRespondent().size()).isEqualTo(1);
        assertThat(caseData.getGaAddlDocRespondentSol().size()).isEqualTo(1);
        assertThat(caseData.getPreTranslationGaDocuments().size()).isEqualTo(1);
    }

    @Test
    public void should_notAddToAddlDocs_ifOnlyOneDoc() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        List<Element<Document>> tobeAdded = new ArrayList<>();
        tobeAdded.add(element(Document.builder()
                                  .documentFileName("witness_document.pdf")
                                  .documentUrl("http://dm-store:8080")
                                  .documentBinaryUrl("http://dm-store:8080/documents").build()));
        DocUploadUtils.addDocumentToPreTranslation(caseData, builder, tobeAdded, DocUploadUtils.APPLICANT, RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION);
        caseData = builder.build();
        assertThat(caseData.getGaAddlDocClaimant()).isNull();
        assertThat(caseData.getPreTranslationGaDocuments().size()).isEqualTo(1);
    }

    @Test
    public void should_notAddToPreTranslation_ifEmptySource() {
        CaseData caseData = CaseData.builder().build();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        List<Element<Document>> tobeAdded = new ArrayList<>();
        DocUploadUtils.addDocumentToPreTranslation(caseData, builder, tobeAdded, DocUploadUtils.APPLICANT, RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION);
        caseData = builder.build();
        assertThat(caseData.getPreTranslationGaDocuments().size()).isEqualTo(0);
    }
}
