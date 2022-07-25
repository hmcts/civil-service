package uk.gov.hmcts.reform.civil.service.docmosis;

import javax.validation.constraints.NotNull;

public enum DocmosisTemplates {
    N1("CV-UNS-CLM-ENG-01126.docx", "sealed_claim_form_%s.pdf"),
    N1_MULTIPARTY_SAME_SOL("CV-UNS-CLM-ENG-01125.docx", "sealed_claim_form_%s.pdf"),
    N2("CV-SPC-CLM-ENG-01074.docx", "sealed_claim_form_spec%s.pdf"),
    N2_1V2_SAME_SOL("CV-SPC-CLM-ENG-01076.docx", "sealed_claim_form_spec%s.pdf"),
    N2_1V2_DIFFERENT_SOL("CV-SPC-CLM-ENG-01075.docx", "sealed_claim_form_spec%s.pdf"),
    N2_2V1("CV-SPC-CLM-ENG-01077.docx", "sealed_claim_form_spec%s.pdf"),
    N181("CV-UNS-HRN-ENG-01128.docx", "%s_directions_questionnaire_form_%s.pdf"),
    N181_MULTIPARTY_SAME_SOL("CV-UNS-HRN-ENG-01116.docx", "%s_directions_questionnaire_form_%s.pdf"),
    N10("CV-SPEC-ACK-ENG-00001-v01.docx", "acknowledgement_of_service_form_spec_%s.pdf"),
    N9("CV-UNS-ACK-ENG-00653.docx", "acknowledgement_of_claim_form_%s.pdf"),
    N9_MULTIPARTY_SAME_SOL("CV-UNS-ACK-ENG-00789.docx", "acknowledgement_of_claim_form_%s.pdf"),
    N11("CV-UNS-ACK-ENG-00799.docx", " acknowledgement_of_claim_form_%s.pdf"),
    N121_SPEC("CV-SPC-DEC-ENG-00910.docx", "default_judgment_spec_form_%s.pdf"),
    N121("CV-UNS-DEC-ENG-00829.docx", "default_judgment_form_%s.pdf"),
    N181_2V1("CV-UNS-HRN-ENG-01114.docx", "%s_directions_questionnaire_form_%s.pdf"),
    N181_CLAIMANT_MULTIPARTY_DIFF_SOLICITOR("CV-UNS-HRN-ENG-01115.docx",
                                            "%s_directions_questionnaire_form_%s.pdf"),
    DEFENDANT_RESPONSE_SPEC("CV-SPC-HRN-ENG-01046.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DEFENDANT_RESPONSE_SPEC_SEALED_1v1(
        "CV-SPC-HRN-ENG-01065.docx", "%s_response_sealed_form.pdf"
    ),
    DEFENDANT_RESPONSE_SPEC_SEALED_1v2(
        "CV-SPC-HRN-ENG-01064.docx", "%s_response_sealed_form.pdf"
    ),
    LIP_CLAIM_FORM("CV-UNS-CLM-ENG-01096.docx", "litigant_in_person_claim_form_%s.pdf"),
    DJ_SDO_DISPOSAL("CV-UNS-DEC-ENG-09999.docx", "Order_disposal_pdf_%s.pdf"),
    DJ_SDO_TRIAL("CV-UNS-DEC-ENG-09998.docx", "Order_trial_pdf_%s.pdf"),
    CLAIMANT_RESPONSE_SPEC("CV-SPC-HRN-ENG-01062.docx", "%s_directions_questionnaire_form_%s.pdf");


    private final String template;
    private final String documentTitle;

    DocmosisTemplates(String template, @NotNull String documentTitle) {
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
