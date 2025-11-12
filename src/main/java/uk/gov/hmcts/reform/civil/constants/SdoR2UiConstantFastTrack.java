package uk.gov.hmcts.reform.civil.constants;

import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_INSPECTION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_DISCLOSURE_STANDARD_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_SCHEDULE_CLAIMANT_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_SCHEDULE_DEFENDANT_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_SCHEDULE_FUTURE_LOSS_SDO;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_PERIOD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_ADDENDUM_REPORT_PERMISSION;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_APPLICATION_TO_RELY_DETAILS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_CLAIMANT_PERMISSION_TO_RELY;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_DEFENDANT_MAY_ASK;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_ENT_QUESTIONS_SHALL_BE_ANSWERED;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_ENT_UPLOAD_WITHIN_SEVEN_DAYS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_ENT_WRITTEN_QUESTIONS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_ENT_WRITTEN_QUESTIONS_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_EVIDENCE_ACOUSTIC_ENGINEER;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_EXPERT_REPORT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_EXPERT_REPORT_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_FURTHER_AUDIOGRAM_REQUEST;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_FURTHER_AUDIOGRAM_SERVICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_INSTRUCTION_OF_EXPERT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_INSTRUCTION_OF_EXPERT_FOLLOWUP;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_JOINT_MEETING_OF_EXPERTS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_PERMISSION_TO_RELY_ON_EXPERT;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_QUESTIONS_SHALL_BE_ANSWERED;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_REPLIES;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_REPLIES_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_SERVICE_OF_ORDER;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_UPLOAD_OF_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_UPLOAD_WITHIN_SEVEN_DAYS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_UPLOAD_TO_DIGITAL_PORTAL;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_WRITTEN_QUESTIONS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.NIHL_WRITTEN_QUESTIONS_UPLOAD;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_WITNESS_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.SMALL_CLAIMS_WITNESS_LATE_WARNING;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.WITNESS_COUNT_LIMIT_NOTE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.WITNESS_PAGE_LIMIT_PREFIX;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.WITNESS_PAGE_LIMIT_SUFFIX;

public final class SdoR2UiConstantFastTrack {

    private SdoR2UiConstantFastTrack() {
        //To satisfy sonarQube
    }

    //SDO R2 NIHL default text
    public static final String CLAIMANT_PERMISSION_TO_RELY = NIHL_CLAIMANT_PERMISSION_TO_RELY;
    public static final String ADDENDUM_REPORT = NIHL_ADDENDUM_REPORT_PERMISSION;
    public static final String CLAIMANT_SHALL_UNDERGO = NIHL_FURTHER_AUDIOGRAM_REQUEST;
    public static final String SERVICE_REPORT = NIHL_FURTHER_AUDIOGRAM_SERVICE;
    public static final String DEFENDANT_MAY_ASK = NIHL_DEFENDANT_MAY_ASK;
    public static final String QUESTIONS_SHALL_BE_ANSWERED = NIHL_QUESTIONS_SHALL_BE_ANSWERED;
    public static final String UPLOADED_TO_DIGITAL_PORTAL = NIHL_UPLOAD_TO_DIGITAL_PORTAL;
    public static final String APPLICATION_TO_RELY_DETAILS = NIHL_APPLICATION_TO_RELY_DETAILS;
    public static final String PERMISSION_TO_RELY_ON_EXPERT = NIHL_PERMISSION_TO_RELY_ON_EXPERT;
    public static final String JOINT_MEETING_OF_EXPERTS = NIHL_JOINT_MEETING_OF_EXPERTS;
    public static final String UPLOADED_TO_DIGITAL_PORTAL_7_DAYS = NIHL_UPLOAD_WITHIN_SEVEN_DAYS;
    public static final String EVIDENCE_ACOUSTIC_ENGINEER = NIHL_EVIDENCE_ACOUSTIC_ENGINEER;
    public static final String INSTRUCTION_OF_EXPERT = NIHL_INSTRUCTION_OF_EXPERT;
    public static final String INSTRUCTION_OF_EXPERT_TA = NIHL_INSTRUCTION_OF_EXPERT_FOLLOWUP;
    public static final String EXPERT_REPORT = NIHL_EXPERT_REPORT;
    public static final String EXPERT_REPORT_DIGITAL_PORTAL = NIHL_EXPERT_REPORT_UPLOAD;
    public static final String WRITTEN_QUESTIONS = NIHL_WRITTEN_QUESTIONS;
    public static final String WRITTEN_QUESTIONS_DIGITAL_PORTAL = NIHL_WRITTEN_QUESTIONS_UPLOAD;
    public static final String REPLIES = NIHL_REPLIES;
    public static final String REPLIES_DIGITAL_PORTAL = NIHL_REPLIES_UPLOAD;
    public static final String SERVICE_OF_ORDER = NIHL_SERVICE_OF_ORDER;
    public static final String ENT_WRITTEN_QUESTIONS = NIHL_ENT_WRITTEN_QUESTIONS;
    public static final String ENT_WRITTEN_QUESTIONS_DIG_PORTAL = NIHL_ENT_WRITTEN_QUESTIONS_UPLOAD;
    public static final String ENT_QUESTIONS_SHALL_BE_ANSWERED = NIHL_ENT_QUESTIONS_SHALL_BE_ANSWERED;
    public static final String ENT_SHALL_BE_UPLOADED = NIHL_ENT_UPLOAD_WITHIN_SEVEN_DAYS;
    public static final String UPLOAD_OF_DOCUMENTS = NIHL_UPLOAD_OF_DOCUMENTS;
    public static final String TRUE = "true";
    public static final String JUDGE_RECITAL = JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_PERIOD;
    public static final String STANDARD_DISCLOSURE = FAST_TRACK_DISCLOSURE_STANDARD_SDO;
    public static final String INSPECTION = FAST_TRACK_DISCLOSURE_INSPECTION;
    public static final String REQUEST_COMPILED_WITH = "within 7 days of receipt.";
    public static final String STATEMENT_WITNESS = "Each party must upload to the Digital Portal copies of the" +
        " statements of all witnesses of fact on whom they intend to rely.";
    public static final String DEADLINE = SMALL_CLAIMS_WITNESS_DEADLINE;
    public static final String DEADLINE_EVIDENCE = SMALL_CLAIMS_WITNESS_LATE_WARNING;
    public static final String SCHEDULE_OF_LOSS_CLAIMANT = FAST_TRACK_SCHEDULE_CLAIMANT_UPLOAD;
    public static final String SCHEDULE_OF_LOSS_DEFENDANT = FAST_TRACK_SCHEDULE_DEFENDANT_UPLOAD;
    public static final String IMPORTANT_NOTES = "This Order has been made without hearing. Each party has the " +
        "right to apply to have this Order set aside or varied. Any such application must be received " +
        "by the Court (together with the appropriate fee) by 4pm on";
    public static final String RESTRICT_WITNESS_TEXT = WITNESS_COUNT_LIMIT_NOTE;
    public static final String RESTRICT_NUMBER_PAGES_TEXT1 = WITNESS_PAGE_LIMIT_PREFIX;
    public static final String RESTRICT_NUMBER_PAGES_TEXT2 = WITNESS_PAGE_LIMIT_SUFFIX;
    public static final String PECUNIARY_LOSS = FAST_TRACK_SCHEDULE_FUTURE_LOSS_SDO;
    public static final String PHYSICAL_TRIAL_BUNDLE = "The Claimant's solicitor shall bring to the court, on the day of the hearing, a paper copy of the hearing bundle.";

    public static final String WELSH_LANG_DESCRIPTION =
        """
        If any party is legally represented then when filing any witness evidence, the legal representatives must notify the Court in writing that:
        a) they have advised their client of the entitlement of any party or witness to give evidence in the Welsh Language in accordance with the Welsh Language Act 1993 \
        (which is not dependant on whether they are fluent in English)
        b) instructions have been taken as to whether any party or witness will exercise that entitlement, in which case the legal representatives must so inform the Court \
        so that arrangements can be made by the Court for instantaneous translation facilities to be made available without charge

        Any unrepresented party or witness for such a party being entitled to give evidence in the Welsh Language in accordance with the principle \
        of the Welsh Language Act 1993 must notify the Court when sending to the Court their witness evidence whether any party or witness will exercise \
        that entitlement whereupon the Court will make arrangements for instantaneous translation facilities to be made available without charge.""";
}
