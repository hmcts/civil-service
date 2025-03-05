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
    N121_SPEC_NON_IMMEDIATE("CV-SPC-DEC-ENG-00913.docx", "default_judgment_spec_form_%s.pdf"),
    N121_SPEC_DEFENDANT("CV-SPC-DEC-ENG-00911.docx", "default_judgment_spec_form_%s.pdf"),
    N121_SPEC_CLAIMANT("CV-SPC-DEC-ENG-00912.docx", "default_judgment_spec_form_%s.pdf"),
    N121_SPEC_CLAIMANT_WELSH("CV-SPC-DEC-WEL-00912.docx", "default_judgment_spec_form_%s.pdf"),
    N121("CV-UNS-DEC-ENG-01280.docx", "default_judgment_form_%s.pdf"),
    DEFENDANT_RESPONSE_SPEC_SEALED_1V1("CV-SPC-HRN-ENG-01361.docx", "%s_response_sealed_form.pdf"),
    DEFENDANT_RESPONSE_SPEC_SEALED_1V2("CV-SPC-HRN-ENG-01360.docx", "%s_response_sealed_form.pdf"),
    DEFENDANT_RESPONSE_SPEC_SEALED_1V1_INSTALLMENTS("CV-SPC-HRN-ENG-01362.docx", "%s_response_sealed_form.pdf"),
    LIP_CLAIM_FORM("CV-UNS-CLM-ENG-01096.docx", "litigant_in_person_claim_form_%s.pdf"),
    SDO_DISPOSAL("CV-UNS-STD-ENG-01381.docx", "disposal_hearing_sdo_%s.pdf"),
    SDO_R2_DISPOSAL("CV-UNS-STD-ENG-01390.docx", "disposal_hearing_sdo_%s.pdf"),
    DJ_SDO_DISPOSAL("CV-UNS-DEC-ENG-01329.docx", "Order_disposal_%s.pdf"),
    DJ_SDO_TRIAL("CV-UNS-DEC-ENG-01376.docx", "Order_trial_%s.pdf"),
    DJ_SDO_R2_DISPOSAL("CV-UNS-DEC-ENG-01331.docx", "Order_disposal_%s.pdf"),
    DJ_SDO_R2_TRIAL("CV-UNS-DEC-ENG-01379.docx", "Order_trial_%s.pdf"),
    SDO_SMALL("CV-UNS-STD-ENG-01382.docx", "small_claims_sdo_%s.pdf"),
    SDO_SMALL_R2("CV-UNS-STD-ENG-01388.docx", "small_claims_sdo_%s.pdf"),
    SDO_SMALL_DRH("CV-UNS-STD-ENG-01389.docx", "small_claims_sdo_%s.pdf"),
    SDO_FAST("CV-UNS-STD-ENG-01380.docx", "fast_track_sdo_%s.pdf"),
    SDO_FAST_R2("CV-UNS-STD-ENG-01380_13066.docx", "fast_track_sdo_%s.pdf"),
    SDO_FAST_TRACK_NIHL("CV-UNS-STD-ENG-01387.docx", "fast_track_nihl_sdo_%s.pdf"),
    SDO_FAST_FAST_TRACK_INT("CV-UNS-STD-ENG-01378.docx", "fast_track_sdo_%s.pdf"),
    SDO_FAST_FAST_TRACK_INT_R2("CV-UNS-STD-ENG-01378_13066.docx", "fast_track_sdo_%s.pdf"),
    DEFENDANT_RESPONSE_LIP_SPEC("CV-SPC-CLM-ENG-01065-LIP.docx", "response_sealed_form_%s.pdf"),
    PIN_IN_THE_POST_LETTER("CV-CMC-LET-ENG-PIP0001.docx", "PIP_letter.pdf"),
    SET_ASIDE_JUDGMENT_IN_ERROR_LIP_DEFENDANT_LETTER("CV-CMC-LET-ENG-LIP-JO0001.docx", "set_aside_letter.pdf"),
    JUDGMENT_BY_ADMISSION_PIN_IN_POST_LIP_DEFENDANT_LETTER("CV-CMC-LET-ENG-PIP-0002.docx", "judgment_by_admission_non_divergent_spec_pip_letter.pdf"),
    RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LIP_DEFENDANT_LETTER("CV-CMC-LET-ENG-LIP-JO0003.docx", "record_judgment_determination_of_means_letter.pdf"),
    DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_LIP_DEFENDANT_LETTER("CV-CMC-LET-ENG-LIP-JO0004.docx", "default_judgment_non_divergent_spec_pin_letter.pdf"),
    SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER("CV-SPC-LET-ENG-LIP-SD0001.docx", "settle_claim_paid_in_full_letter.pdf"),
    SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_WELSH("CV-SPC-LET-WEL-LIP-SD0001.docx", "settle_claim_paid_in_full_letter.pdf"),
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
    HEARING_NOTICE_HMC_WELSH("CV-UNS-HNO-WEL-01200.docx", "hearing_notice_welsh_%s.pdf"),

    DRAFT_CLAIM_FORM("CV-SPC-CLM-ENG-00001-DRAFT.docx", "draft_claim_form_%s.pdf"),
    HEARING_SMALL_CLAIMS_AHN("CV-UNS-HNO-ENG-01202.docx", "hearing_small_claim_%s.pdf"),
    HEARING_TRIAL_AHN("CV-UNS-HNO-ENG-01203.docx", "trial_%s.pdf"),
    HEARING_APPLICATION_AHN("CV-UNS-HNO-ENG-01204.docx", "hearing_application_%s.pdf"),
    HEARING_OTHER_AHN("CV-UNS-HNO-ENG-01201.docx", "hearing_other_claim_%s.pdf"),
    REQUEST_FOR_RECONSIDERATION("CV-SPC-CLM-ENG-REQUEST-RECONSIDERATION.docx", "%s_request_for_reconsideration.pdf"),

    // judge final Order
    FREE_FORM_ORDER_PDF("CV-UNS-DEC-ENG-01099.docx", "Order_%s.pdf"),
    ASSISTED_ORDER_PDF("CV-UNS-DEC-ENG-01283.docx", "Order_%s.pdf"),

    // Judge order download order
    BLANK_TEMPLATE_AFTER_HEARING_DOCX("CV-UNS-DEC-ENG-01286.docx", "%s_order.docx"),
    BLANK_TEMPLATE_BEFORE_HEARING_DOCX("CV-UNS-DEC-ENG-01301.docx", "%s_Directions order.docx"),
    FIX_DATE_CCMC_DOCX("CV-UNS-DEC-ENG-01287.docx", "%s_Directions order.docx"),
    FIX_DATE_CMC_DOCX("CV-UNS-DEC-ENG-01302.docx", "%s_Directions order.docx"),

    //Court officer order
    COURT_OFFICER_ORDER_PDF("CV-UNS-DEC-ENG-01299.docx", "Order_%s.pdf"),
    SETTLEMENT_AGREEMENT_PDF("CV-SPC-CLM-ENG-00001-SETTLEMENT.docx", "%s-settlement-agreement.pdf"),
    CLAIMANT_LIP_MANUAL_DETERMINATION_PDF("CV-SPC-CLM-ENG-00001-LIP-MD.docx", "%s_request-org-repayment-amount.pdf"),
    JUDGMENT_BY_ADMISSION_OR_DETERMINATION("CV-SPC-CLM-ENG-00001-JBA-JBD.docx", "%s-ccj-request-%s.pdf"),
    INTERLOCUTORY_JUDGEMENT_DOCUMENT("CV-SPC-CLM-ENG-INTERLOCUTORY-JUDGEMENT.docx", "%s-request-interloc-judgment.pdf"),
    GENERATE_LIP_CLAIMANT_CLAIM_FORM("CV-SPC-CLM-ENG-CLAIMANT-CLAIM-FORM.docx", "%s-claim-form-claimant-copy.pdf"),
    GENERATE_LIP_DEFENDANT_CLAIM_FORM("CV-SPC-CLM-ENG-DEFENDANT-CLAIM-FORM.docx", "%s-sealed-claim-form.pdf"),
    RECONSIDERATION_UPHELD_DECISION_OUTPUT_PDF("CV-UNS-DEC-ENG-00001.docx", "Decision_on_reconsideration_request_%s.pdf"),
    JUDGMENT_BY_ADMISSION_CLAIMANT("CV-SPC-DEC-ENG-00916.docx", "Judgment_by_admission_claimant.pdf"),
    JUDGMENT_BY_ADMISSION_DEFENDANT("CV-SPC-DEC-ENG-00915.docx", "Judgment_by_admission_defendant.pdf"),
    JUDGMENT_BY_ADMISSION_CLAIMANT_BILINGUAL("CV-SPC-DEC-WEL-00916.docx", "Judgment_by_admission_claimant.pdf"),
    JUDGMENT_BY_ADMISSION_DEFENDANT_BILINGUAL("CV-SPC-DEC-WEL-00915.docx", "Judgment_by_admission_defendant.pdf"),
    JUDGMENT_BY_DETERMINATION_CLAIMANT("CV-SPC-DEC-ENG-00982.docx", "Judgment_by_determination_claimant.pdf"),
    JUDGMENT_BY_DETERMINATION_DEFENDANT("CV-SPC-DEC-ENG-00981.docx", "Judgment_by_determination_defendant.pdf"),
    NOTICE_OF_DISCONTINUANCE_PDF("CV-SPC-GNO-ENG-SD0001.docx", "notice_of_discontinuance_%s.pdf"),
    NOTICE_OF_DISCONTINUANCE_BILINGUAL_PDF("CV-SPC-GNO-WEL-SD0001.docx", "notice_of_discontinuance_%s.pdf"),
    CERTIFICATE_OF_DEBT_PAYMENT("CV-SPC-STD-ENG-N441A.docx", "Certificate_of_debt_payment_%s.pdf"),
    COVER_LETTER("CV-SPC-LET-ENG-COVER-LETTER.docx", "cover_letter.pdf"),
    SDO_COVER_LETTER("CV-SPC-STD-ENG-COVER-LETTER.docx", "sdo_cover_letter.pdf"),
    QUERY_DOCUMENT("CV-UNS-CLM-ENG-0451.docx", "queries.pdf"),
    ;

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
