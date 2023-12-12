package uk.gov.hmcts.reform.civil.service.docmosis;

import javax.validation.constraints.NotNull;

public enum DocmosisTemplates {
    N1("CV-UNS-CLM-ENG-01126.docx", "sealed_claim_form_%s.pdf"),
    N1_MULTIPARTY_SAME_SOL("CV-UNS-CLM-ENG-01125.docx", "sealed_claim_form_%s.pdf"),
    N2("CV-SPC-CLM-ENG-01349.docx", "sealed_claim_form_spec%s.pdf"),
    N2_LIP("CV-SPC-CLM-ENG-01353.docx", "sealed_claim_form_spec%s.pdf"),
    N2_2V1_LIP("CV-SPC-CLM-ENG-01355.docx", "sealed_claim_form_spec%s.pdf"),
    N2_1V2_SAME_SOL("CV-SPC-CLM-ENG-01351.docx", "sealed_claim_form_spec%s.pdf"),
    N2_1V2_DIFFERENT_SOL("CV-SPC-CLM-ENG-01350.docx", "sealed_claim_form_spec%s.pdf"),
    N2_1V2_DIFFERENT_SOL_LIP("CV-SPC-CLM-ENG-01354.docx", "sealed_claim_form_spec%s.pdf"),
    N2_2V1("CV-SPC-CLM-ENG-01352.docx", "sealed_claim_form_spec%s.pdf"),
    N10("CV-SPEC-ACK-ENG-00001-v01.docx", "acknowledgement_of_service_form_spec_%s.pdf"),
    N9("CV-UNS-ACK-ENG-00653.docx", "acknowledgement_of_claim_form_%s.pdf"),
    N9_MULTIPARTY_SAME_SOL("CV-UNS-ACK-ENG-01142.docx", "acknowledgement_of_claim_form_%s.pdf"),
    N11("CV-UNS-ACK-ENG-01146.docx", " acknowledgement_of_claim_form_%s.pdf"),
    N121_SPEC("CV-SPC-DEC-ENG-00910.docx", "default_judgment_spec_form_%s.pdf"),
    N121("CV-UNS-DEC-ENG-01280.docx", "default_judgment_form_%s.pdf"),
    DEFENDANT_RESPONSE_SPEC_SEALED_1v1("CV-SPC-HRN-ENG-01361.docx", "%s_response_sealed_form.pdf"),
    DEFENDANT_RESPONSE_SPEC_SEALED_1v2("CV-SPC-HRN-ENG-01360.docx", "%s_response_sealed_form.pdf"),
    DEFENDANT_RESPONSE_SPEC_SEALED_1V1_INSTALLMENTS("CV-SPC-HRN-ENG-01362.docx", "%s_response_sealed_form.pdf"),
    LIP_CLAIM_FORM("CV-UNS-CLM-ENG-01096.docx", "litigant_in_person_claim_form_%s.pdf"),
    SDO_DISPOSAL("CV-UNS-STD-ENG-01381.docx", "disposal_hearing_sdo_%s.pdf"),
    DJ_SDO_DISPOSAL("CV-UNS-DEC-ENG-01329.docx", "Order_disposal_%s.pdf"),
    DJ_SDO_TRIAL("CV-UNS-DEC-ENG-01376.docx", "Order_trial_%s.pdf"),
    SDO_SMALL("CV-UNS-STD-ENG-01382.docx", "small_claims_sdo_%s.pdf"),
    SDO_FAST("CV-UNS-STD-ENG-01380.docx", "fast_track_sdo_%s.pdf"),
    SDO_FAST_FAST_TRACK_INT("CV-UNS-STD-ENG-01378.docx", "fast_track_sdo_%s.pdf"),
    DEFENDANT_RESPONSE_LIP_SPEC("CV-SPC-CLM-ENG-01065-LIP.docx", "response_sealed_form_%s.pdf"),
    HEARING_SMALL_CLAIMS("CV-UNS-HNO-ENG-01197.docx", "hearing_small_claim_%s.pdf"),
    HEARING_FAST_TRACK("CV-UNS-HNO-ENG-01198.docx", "hearing_fast_track_%s.pdf"),
    HEARING_APPLICATION("CV-UNS-HNO-ENG-01199.docx", "hearing_application_%s.pdf"),
    HEARING_OTHER("CV-UNS-HNO-ENG-01196.docx", "hearing_other_claim_%s.pdf"),
    PIN_IN_THE_POST_LETTER("CV-CMC-LET-ENG-PIP0001.docx", "PIP_letter.pdf"),
    SDO_HNL_DISPOSAL("CV-UNS-STD-ENG-01377.docx", "disposal_hearing_sdo_%s.pdf"),
    DJ_SDO_HNL_DISPOSAL("CV-UNS-DEC-ENG-01229.docx", "Order_disposal_%s.pdf"),
    DEFENDANT_RESPONSE_SPEC("CV-SPC-HRN-ENG-01357.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DEFENDANT_RESPONSE_SPEC_FAST_TRACK_INT("CV-SPC-HRN-ENG-01358.docx", "%s_directions_questionnaire_form_%s.pdf"),
    CLAIMANT_RESPONSE_SPEC("CV-SPC-HRN-ENG-01356.docx", "%s_directions_questionnaire_form_%s.pdf"),
    CLAIMANT_RESPONSE_SPEC_FAST_TRACK_INT("CV-SPC-HRN-ENG-01359.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DQ_RESPONSE_1V1("CV-UNS-HRN-ENG-01343.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DQ_RESPONSE_1V1_FAST_TRACK_INT("CV-UNS-HRN-ENG-01345.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DQ_RESPONSE_1V2_DS("CV-UNS-HRN-ENG-01341.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DQ_RESPONSE_1V2_DS_FAST_TRACK_INT("CV-UNS-HRN-ENG-01346.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DQ_RESPONSE_1V2_SS("CV-UNS-HRN-ENG-01342.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DQ_RESPONSE_1V2_SS_FAST_TRACK_INT("CV-UNS-HRN-ENG-01347.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DQ_RESPONSE_2V1("CV-UNS-HRN-ENG-01344.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DQ_RESPONSE_2V1_FAST_TRACK_INT("CV-UNS-HRN-ENG-01348.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DQ_LR_V_LIP_RESPONSE("CV-SPC-HRN-ENG-LIP-01282.docx", "%s_directions_questionnaire_form_%s.pdf"),
    DQ_LIP_RESPONSE("CV-SPC-HRN-ENG-LIP-01283.docx", "%s_directions_questionnaire_form_%s.pdf"),
    TRIAL_READY("CV-UNS-HRN-ENG-01247-10180.docx", "%s_%s_Trial_Arrangements.pdf"),
    HEARING_NOTICE_HMC("CV-UNS-HNO-ENG-01200.docx", "hearing_notice_%s.pdf"),

    DRAFT_CLAIM_FORM("CV-SPC-CLM-ENG-00001-DRAFT.docx", "draft_claim_form_%s.pdf"),
    HEARING_SMALL_CLAIMS_AHN("CV-UNS-HNO-ENG-01202.docx", "hearing_small_claim_%s.pdf"),
    HEARING_FAST_TRACK_AHN("CV-UNS-HNO-ENG-01203.docx", "hearing_fast_track_%s.pdf"),
    HEARING_APPLICATION_AHN("CV-UNS-HNO-ENG-01204.docx", "hearing_application_%s.pdf"),
    HEARING_OTHER_AHN("CV-UNS-HNO-ENG-01201.docx", "hearing_other_claim_%s.pdf"),

    // judge final Order
    FREE_FORM_ORDER_PDF("CV-UNS-DEC-ENG-01099.docx", "Order_%s.pdf"),
    ASSISTED_ORDER_PDF("CV-UNS-DEC-ENG-01283.docx", "Order_%s.pdf"),
    CLAIMANT_LIP_MANUAL_DETERMINATION_PDF("CV-SPC-CLM-ENG-00001-LIP-MD.docx", "%s_request-org-repayment-amount.pdf");
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
