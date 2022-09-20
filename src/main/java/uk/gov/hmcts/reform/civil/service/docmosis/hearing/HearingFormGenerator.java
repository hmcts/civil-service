package uk.gov.hmcts.reform.civil.service.docmosis.hearing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.hearing.HearingForm;
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

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_FORM;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.N121_SPEC;

@Service
@RequiredArgsConstructor
public class HearingFormGenerator implements TemplateDataGenerator<HearingForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public List<CaseDocument> generate(CaseData caseData, String authorisation, String event) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        DocmosisDocument docmosisDocument2;
        List<HearingForm> templateData = getHearingForms(caseData, event);
        DocmosisTemplates docmosisTemplate = getDocmosisTemplate(event);
        DocmosisDocument docmosisDocument1 =
            documentGeneratorService.generateDocmosisDocument(templateData.get(0), docmosisTemplate);
        caseDocuments.add(documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, docmosisTemplate),
                docmosisDocument1.getBytes(),
                DocumentType.HEARING_FORM
            )
        ));
        return caseDocuments;
    }

    @Override
    public HearingForm getTemplateData(CaseData caseData) throws IOException {

        return null;

    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private List<HearingForm> getHearingForms(CaseData caseData, String event) {
        List<HearingForm> hearingForms = new ArrayList<>();
        hearingForms.add(getHearingForm(caseData, caseData.getRespondent1(), event));

        return hearingForms;
    }

    private HearingForm getHearingForm(CaseData caseData,
                                                       uk.gov.hmcts.reform.civil.model.Party respondent,
                                                       String event) {
        return HearingForm.builder()
            .caseNumber(caseData.getLegacyCaseReference()).build();

    }

    private DocmosisTemplates getDocmosisTemplate(String event) {
        return event.equals(GENERATE_HEARING_FORM.name()) ? N121_SPEC : N121;

    }

    private Party getRespondent(uk.gov.hmcts.reform.civil.model.Party respondent) {
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
        return applicants;
    }

}

