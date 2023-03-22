package uk.gov.hmcts.reform.civil.enums;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.access.ApplicantAccess;
import uk.gov.hmcts.reform.civil.access.CaseworkerCaaAccess;

public enum State {
    @CCD(
        label = "Pending case issued",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    PENDING_CASE_ISSUED,
    @CCD(
        label = "Case issued",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    CASE_ISSUED,
    @CCD(
        label = "Awaiting case details notification",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    AWAITING_CASE_DETAILS_NOTIFICATION,
    @CCD(
        label = "Awaiting respondent acknowledgement",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    AWAITING_RESPONDENT_ACKNOWLEDGEMENT,
    @CCD(
        label = "Case dismissed",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    CASE_DISMISSED,
    @CCD(
        label = "Awaiting applicant intention",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    AWAITING_APPLICANT_INTENTION,
    @CCD(
        label = "Proceeds in heritage system",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    PROCEEDS_IN_HERITAGE_SYSTEM,
    @CCD(
        label = "Judicial referral",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    JUDICIAL_REFERRAL,
    @CCD(
        label = "Case progression",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    CASE_PROGRESSION,
    @CCD(
        label = "Hearing readiness",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    HEARING_READINESS,
    @CCD(
        label = "Prepare for hearing conduct hearing",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    PREPARE_FOR_HEARING_CONDUCT_HEARING,
    @CCD(
        label = "Decision outcome",
        access = {ApplicantAccess.class, CaseworkerCaaAccess.class}
    )
    DECISION_OUTCOME
}
