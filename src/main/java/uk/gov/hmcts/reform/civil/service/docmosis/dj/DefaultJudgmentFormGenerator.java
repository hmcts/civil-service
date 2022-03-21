package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121;

@Service
@RequiredArgsConstructor
public class DefaultJudgmentFormGenerator implements TemplateDataGenerator<DefaultJudgmentForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public List<CaseDocument> generate(CaseData caseData, String authorisation) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        DocmosisDocument docmosisDocument2;
        List<DefaultJudgmentForm> templateData = getDefaultJudgmentForm(caseData);
        DocmosisTemplates docmosisTemplate = getDocmosisTemplate(caseData);
        DocmosisDocument docmosisDocument1 =
            documentGeneratorService.generateDocmosisDocument(templateData.get(0), docmosisTemplate);
        caseDocuments.add(documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, docmosisTemplate),
                docmosisDocument1.getBytes(),
                DocumentType.DEFAULT_JUDGMENT
            )
        ));
        if (templateData.size() > 1) {
            docmosisDocument2 =
                documentGeneratorService.generateDocmosisDocument(templateData.get(1), docmosisTemplate);
            caseDocuments.add(documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                    getFileName(caseData, docmosisTemplate),
                    docmosisDocument2.getBytes(),
                    DocumentType.DEFAULT_JUDGMENT
                )
            ));
        }
        return caseDocuments;
    }

    @Override
    public DefaultJudgmentForm getTemplateData(CaseData caseData) throws IOException {

        return null;

    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }


    private List<DefaultJudgmentForm> getDefaultJudgmentForm(CaseData caseData) {
        List<DefaultJudgmentForm> defaultJudgmentForms = new ArrayList<>();

        defaultJudgmentForms.add(DefaultJudgmentForm.builder()
                                     .caseNumber(caseData.getLegacyCaseReference())
                                     .formText("No Acknowledgement of service")
                                     .applicant(getApplicant(caseData.getApplicant1(), caseData.getApplicant2()))
                                     .respondent(getResondent(caseData.getRespondent1()))
                                     .applicantReference(Objects.isNull(caseData.getSolicitorReferences())
                                                             ? null : caseData.getSolicitorReferences()
                                         .getApplicantSolicitor1Reference())
                                     .respondentReference(Objects.isNull(caseData.getSolicitorReferences())
                                                              ? null : caseData.getSolicitorReferences()
                                         .getRespondentSolicitor1Reference()).build());
        if (caseData.getRespondent2() != null) {
            defaultJudgmentForms.add(DefaultJudgmentForm.builder()
                                         .caseNumber(caseData.getLegacyCaseReference())
                                         .formText("No Acknowledgement of service")
                                         .applicant(getApplicant(caseData.getApplicant1(), caseData.getApplicant2()))
                                         .respondent(getResondent(caseData.getRespondent2()))
                                         .applicantReference(Objects.isNull(caseData.getSolicitorReferences())
                                                                 ? null : caseData.getSolicitorReferences()
                                             .getApplicantSolicitor1Reference())
                                         .respondentReference(Objects.isNull(caseData.getSolicitorReferences())
                                                                  ? null : caseData.getSolicitorReferences()
                                             .getRespondentSolicitor2Reference()).build());
        }
        return defaultJudgmentForms;

    }

    private DocmosisTemplates getDocmosisTemplate(CaseData caseData) {

        return N121;
    }

    private Party getResondent(uk.gov.hmcts.reform.civil.model.Party respondent) {

        return Party.builder()
            .name(respondent.getPartyName())
            .primaryAddress(respondent.getPrimaryAddress())
            .build();
    }

    private List<Party> getApplicant(uk.gov.hmcts.reform.civil.model.Party applicant1,
                                     uk.gov.hmcts.reform.civil.model.Party applicant2) {

        List<Party> applicants = new ArrayList<>();
        applicants.add(Party.builder()
                           .name(applicant1.getPartyName())
                           .primaryAddress(applicant1.getPrimaryAddress())
                           .build());
        if (applicant2 != null) {
            applicants.add(Party.builder()
                               .name(applicant2.getPartyName())
                               .primaryAddress(applicant2.getPrimaryAddress())
                               .build());
        }
        return applicants;
    }


}
