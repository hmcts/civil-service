package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload.documenthandler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentCategory {

    APPLICANT_ONE_DISCLOSURE("ApplicantDisclosure"),
    APPLICANT_TWO_DISCLOSURE("ApplicantTwoDisclosure"),
    APPLICANT_ONE_DISCLOSURE_LIST("ApplicantDisclosureList"),
    APPLICANT_TWO_DISCLOSURE_LIST("ApplicantTwoDisclosureList"),
    APPLICANT_ONE_EXPERT_ANSWERS("ApplicantExpertAnswers"),
    APPLICANT_TWO_EXPERT_ANSWERS("ApplicantTwoExpertAnswers"),
    APPLICANT_ONE_EXPERT_JOINT_STATEMENT("ApplicantExpertJointStatement"),
    APPLICANT_TWO_EXPERT_JOINT_STATEMENT("ApplicantTwoExpertJointStatement"),
    APPLICANT_ONE_EXPERT_QUESTIONS("ApplicantExpertQuestions"),
    APPLICANT_TWO_EXPERT_QUESTIONS("ApplicantTwoExpertQuestions"),
    APPLICANT_ONE_EXPERT_REPORT("ApplicantExpertReport"),
    APPLICANT_TWO_EXPERT_REPORT("ApplicantTwoExpertReport"),
    APPLICANT_ONE_PRE_TRIAL_SUMMARY("ApplicantPreTrialSummary"),
    APPLICANT_TWO_PRE_TRIAL_SUMMARY("ApplicantTwoPreTrialSummary"),
    APPLICANT_SCHEDULES_OF_COSTS("ApplicantSchedulesOfCost"),
    APPLICANT_TWO_SCHEDULE_OF_COSTS("ApplicantTwoSchedulesOfCost"),
    APPLICANT_ONE_TRIAL_DOC_CORRESPONDENCE("ApplicantTrialDocCorrespondence"),
    APPLICANT_TWO_TRIAL_DOC_CORRESPONDENCE("ApplicantTwoTrialDocCorrespondence"),
    APPLICANT_ONE_TRIAL_SKELETON("ApplicantTrialSkeleton"),
    APPLICANT_TWO_TRIAL_SKELETON("ApplicantTwoTrialSkeleton"),
    APPLICANT_ONE_WITNESS_HEARSAY("ApplicantWitnessHearsay"),
    APPLICANT_TWO_WITNESS_HEARSAY("ApplicantTwoWitnessHearsay"),
    APPLICANT_ONE_WITNESS_REFERRED("ApplicantWitnessReferred"),
    APPLICANT_TWO_WITNESS_REFERRED("ApplicantTwoWitnessReferred"),
    APPLICANT_ONE_WITNESS_STATEMENT("ApplicantWitnessStatement"),
    APPLICANT_ONE_WITNESS_OTHER_STATEMENT("ApplicantOneOtherWitnessStatement"),
    APPLICANT_TWO_WITNESS_OTHER_STATEMENT("ApplicantTwoOtherWitnessStatement"),
    APPLICANT_TWO_WITNESS_STATEMENT("ApplicantTwoWitnessStatement"),
    APPLICANT_ONE_WITNESS_SUMMARY("ApplicantWitnessSummary"),
    APPLICANT_TWO_WITNESS_SUMMARY("ApplicantTwoWitnessSummary"),

    RESPONDENT_ONE_DISCLOSURE("RespondentOneDisclosure"),
    RESPONDENT_TWO_DISCLOSURE("RespondentTwoDisclosure"),
    RESPONDENT_ONE_DISCLOSURE_LIST("RespondentOneDisclosureList"),
    RESPONDENT_TWO_DISCLOSURE_LIST("RespondentTwoDisclosureList"),
    RESPONDENT_ONE_EXPERT_ANSWERS("RespondentOneExpertAnswers"),
    RESPONDENT_TWO_EXPERT_ANSWERS("RespondentTwoExpertAnswers"),
    RESPONDENT_ONE_EXPERT_JOINT_STATEMENT("RespondentOneExpertJointStatement"),
    RESPONDENT_TWO_EXPERT_JOINT_STATEMENT("RespondentTwoExpertJointStatement"),
    RESPONDENT_ONE_EXPERT_QUESTIONS("RespondentOneExpertQuestions"),
    RESPONDENT_TWO_EXPERT_QUESTIONS("RespondentTwoExpertQuestions"),
    RESPONDENT_ONE_EXPERT_REPORT("RespondentOneExpertReport"),
    RESPONDENT_TWO_EXPERT_REPORT("RespondentTwoExpertReport"),
    RESPONDENT_ONE_PRE_TRIAL_SUMMARY("RespondentOnePreTrialSummary"),
    RESPONDENT_TWO_PRE_TRIAL_SUMMARY("RespondentTwoPreTrialSummary"),
    RESPONDENT_SCHEDULES_OF_COSTS("RespondentSchedulesOfCost"),
    RESPONDENT_TWO_SCHEDULE_OF_COSTS("RespondentTwoSchedulesOfCost"),
    RESPONDENT_ONE_TRIAL_DOC_CORRESPONDENCE("RespondentOneTrialDocCorrespondence"),
    RESPONDENT_TWO_TRIAL_DOC_CORRESPONDENCE("RespondentTwoTrialDocCorrespondence"),
    RESPONDENT_ONE_TRIAL_SKELETON("RespondentOneTrialSkeleton"),
    RESPONDENT_TWO_TRIAL_SKELETON("RespondentTwoTrialSkeleton"),
    RESPONDENT_ONE_WITNESS_HEARSAY("RespondentOneWitnessHearsay"),
    RESPONDENT_TWO_WITNESS_HEARSAY("RespondentTwoWitnessHearsay"),
    RESPONDENT_ONE_WITNESS_REFERRED("RespondentOneWitnessReferred"),
    RESPONDENT_TWO_WITNESS_REFERRED("RespondentTwoWitnessReferred"),
    RESPONDENT_ONE_WITNESS_STATEMENT("RespondentOneWitnessStatement"),
    RESPONDENT_TWO_WITNESS_STATEMENT("RespondentTwoWitnessStatement"),
    RESPONDENT_ONE_WITNESS_SUMMARY("RespondentOneWitnessSummary"),
    RESPONDENT_TWO_WITNESS_SUMMARY("RespondentTwoWitnessSummary"),
    RESPONDENT_ONE_WITNESS_OTHER_STATEMENT("RespondentOneOtherWitnessStatement"),
    RESPONDENT_TWO_WITNESS_OTHER_STATEMENT("RespondentTwoOtherWitnessStatement"),

    APPLICANT_ONE_UPLOADED_PRECEDENT_H("ApplicantUploadedPrecedentH"),
    APPLICANT_ONE_PRECEDENT_AGREED("ApplicantPrecedentAgreed"),
    APPLICANT_ONE_ANY_PRECEDENT_H("ApplicantAnyPrecedentH"),
    APPLICANT_TWO_UPLOADED_PRECEDENT_H("ApplicantTwoUploadedPrecedentH"),
    APPLICANT_TWO_PRECEDENT_AGREED("ApplicantTwoPrecedentAgreed"),
    APPLICANT_TWO_ANY_PRECEDENT_H("ApplicantTwoAnyPrecedentH"),
    RESPONDENT_ONE_UPLOADED_PRECEDENT_H("RespondentUploadedPrecedentH"),
    RESPONDENT_ONE_PRECEDENT_AGREED("RespondentPrecedentAgreed"),
    RESPONDENT_ONE_ANY_PRECEDENT_H("RespondentAnyPrecedentH"),
    RESPONDENT_TWO_UPLOADED_PRECEDENT_H("RespondentTwoUploadedPrecedentH"),
    RESPONDENT_TWO_PRECEDENT_AGREED("RespondentTwoPrecedentAgreed"),
    RESPONDENT_TWO_ANY_PRECEDENT_H("RespondentTwoAnyPrecedentH"),

    APPLICANT_ONE_TRIAL_DOC_TIME_TABLE("ApplicantTrialDocTimetable"),
    RESPONDENT_ONE_TRIAL_DOC_TIME_TABLE("RespondentOneTrialDocTimetable"),
    APPLICANT_TWO_TRIAL_DOC_TIME_TABLE("ApplicantTwoTrialDocTimetable"),
    RESPONDENT_TWO_TRIAL_DOC_TIME_TABLE("RespondentTwoTrialDocTimetable"),


    BUNDLE_EVIDENCE_UPLOAD("bundles");

    private final String categoryId;

}
