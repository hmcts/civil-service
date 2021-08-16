package uk.gov.hmcts.reform.civil.service.docmosis;

public enum DocmosisTemplates {
    N1("CV-UNS-CLM-ENG-00727.docx", "sealed_claim_form_%s.pdf"),
    N2("CV-SPEC-CLM-ENG-00001-v01.docx", "sealed_claim_form_spec%s.pdf"),
    N9("CV-UNS-ACK-ENG-00653.docx", "acknowledgement_of_claim_form_%s.pdf"),
    N10("CV-SPEC-ACK-ENG-00001-v01.docx", "acknowledgement_of_service_form_spec_%s.pdf"),
    N181("CV-UNS-HRN-ENG-00651.docx", "%s_directions_questionnaire_form_%s.pdf");

    private final String template;
    private final String documentTitle;

    DocmosisTemplates(String template, String documentTitle) {
        this.template = template;
        this.documentTitle = documentTitle;
    }

    public String getTemplate() {
        return template;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }
}
