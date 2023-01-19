package uk.gov.hmcts.reform.civil.service.docmosis;

import javax.validation.constraints.NotNull;

public enum DocmosisTemplates {
    N1("CV-UNS-CLM-ENG-01126.docx", "sealed_claim_form_%s.pdf"),
    N1_MULTIPARTY_SAME_SOL("CV-UNS-CLM-ENG-01125.docx", "sealed_claim_form_%s.pdf"),
    N2("CV-SPC-CLM-ENG-01074.docx", "sealed_claim_form_spec%s.pdf"),
    N2_LIP("CV-SPC-CLM-ENG-01181.docx", "sealed_claim_form_spec%s.pdf"),
    N2_2V1_LIP("CV-SPC-CLM-ENG-01183.docx", "sealed_claim_form_spec%s.pdf"),
    N2_1V2_SAME_SOL("CV-SPC-CLM-ENG-01076.docx", "sealed_claim_form_spec%s.pdf"),
    N2_1V2_DIFFERENT_SOL("CV-SPC-CLM-ENG-01075.docx", "sealed_claim_form_spec%s.pdf"),
    N2_1V2_DIFFERENT_SOL_LIP("CV-SPC-CLM-ENG-01182.docx", "sealed_claim_form_spec%s.pdf"),
    N2_2V1("CV-SPC-CLM-ENG-01077.docx", "sealed_claim_form_spec%s.pdf"),
    N181("CV-UNS-HRN-ENG-01175.docx", "%s_directions_questionnaire_form_%s.pdf"),
    N181_MULTIPARTY_SAME_SOL("CV-UNS-HRN-ENG-01178.docx", "%s_directions_questionnaire_form_%s.pdf"),
    N10("CV-SPEC-ACK-ENG-00001-v01.docx", "acknowledgement_of_service_form_spec_%s.pdf"),
    N9("CV-UNS-ACK-ENG-00653.docx", "acknowledgement_of_claim_form_%s.pdf"),
    N9_MULTIPARTY_SAME_SOL("CV-UNS-ACK-ENG-01142.docx", "acknowledgement_of_claim_form_%s.pdf"),
    N11("CV-UNS-ACK-ENG-01146.docx", " acknowledgement_of_claim_form_%s.pdf"),
    N121_SPEC("CV-SPC-DEC-ENG-00910.docx", "default_judgment_spec_form_%s.pdf"),
    N121("CV-UNS-DEC-ENG-00829.docx", "default_judgment_form_%s.pdf"),
    N181_2V1("CV-UNS-HRN-ENG-01172.docx", "%s_directions_questionnaire_form_%s.pdf"),
    N181_CLAIMANT_MULTIPARTY_DIFF_SOLICITOR("CV-UNS-HRN-ENG-01173.docx",
                                            "%s_directions_questionnaire_form_%s.pdf"),
    DEFENDANT_RESPONSE_SPEC("CV-SPC-HRN-ENG-01046.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DEFENDANT_RESPONSE_SPEC_SEALED_1v1(
        "CV-SPC-HRN-ENG-01065.docx", "%s_response_sealed_form.pdf"
    ),
    DEFENDANT_RESPONSE_SPEC_SEALED_1v2(
        "CV-SPC-HRN-ENG-01064.docx", "%s_response_sealed_form.pdf"
    ),
    LIP_CLAIM_FORM("CV-UNS-CLM-ENG-01096.docx", "litigant_in_person_claim_form_%s.pdf"),
    DJ_SDO_DISPOSAL("CV-UNS-DEC-ENG-01132.docx", "Order_disposal_%s.pdf"),
    DJ_SDO_TRIAL("CV-UNS-DEC-ENG-01133.docx", "Order_trial_%s.pdf"),
    DJ_SDO_HNL_TRIAL("CV-UNS-DEC-ENG-01133-HNL.docx", "Order_trial_%s.pdf"),
    CLAIMANT_RESPONSE_SPEC("CV-SPC-HRN-ENG-01062.docx", "%s_directions_questionnaire_form_%s.pdf"),
    CLAIMANT_RESPONSE_SPEC_HNL("CV-SPC-HRN-ENG-01066.docx", "%s_directions_questionnaire_form_%s.pdf"),
    SDO_SMALL("CV-DAM-STD-ENG-00001.docx", "small_claims_sdo_%s.pdf"),
    SDO_SMALL_HNL("CV-DAM-STD-ENG-00004.docx", "small_claims_sdo_%s.pdf"),
    SDO_FAST("CV-DAM-STD-ENG-00002.docx", "fast_track_sdo_%s.pdf"),
    SDO_HNL_FAST("CV-DAM-STD-ENG-00002-HNL.docx", "fast_track_sdo_%s.pdf"),
    SDO_DISPOSAL("CV-DAM-STD-ENG-00003.docx", "disposal_hearing_sdo_%s.pdf"),
    HEARING_SMALL_CLAIMS("CV-UNS-HNO-ENG-01197.docx", "hearing_small_claim_%s.pdf"),
    HEARING_FAST_TRACK("CV-UNS-HNO-ENG-01198.docx", "hearing_fast_track_%s.pdf"),
    HEARING_APPLICATION("CV-UNS-HNO-ENG-01199.docx", "hearing_application_%s.pdf"),
    HEARING_OTHER("CV-UNS-HNO-ENG-01196.docx", "hearing_other_claim_%s.pdf"),

    PIN_IN_THE_POST_LETTER("CV-CMC-LET-ENG-PIP0001.docx", "PIP_letter.pdf"),

    //HNL uplifted docs to be replaced with original once ready
    SDO_HNL_DISPOSAL("CV-DAM-STD-ENG-00003-HNL.docx", "disposal_hearing_sdo_%s.pdf"),
    DJ_SDO_HNL_DISPOSAL("CV-UNS-DEC-ENG-01200.docx", "Order_disposal_%s.pdf"),
    DEFENDANT_RESPONSE_SPEC_HNL("CV-SPC-HRN-ENG-01209.docx", "%s_response_sealed_form.pdf"),
    HNL_DQ_RESPONSE_1V1("CV-UNS-HRN-ENG-01213.docx", "%s_directions_questionnaire_form_%s.pdf"),
    HNL_DQ_RESPONSE_1V2_DS("CV-UNS-HRN-ENG-01217.docx", "%s_directions_questionnaire_form_%s.pdf"),
    HNL_DQ_RESPONSE_1V2_SS("CV-UNS-HRN-ENG-01218.docx", "%s_directions_questionnaire_form_%s.pdf"),
    HNL_DQ_RESPONSE_2V1("CV-UNS-HRN-ENG-01219.docx", "%s_directions_questionnaire_form_%s.pdf");


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
