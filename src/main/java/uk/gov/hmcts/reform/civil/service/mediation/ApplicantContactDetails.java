package uk.gov.hmcts.reform.civil.service.mediation;

public interface ApplicantContactDetails {

    String getApplicantContactName(MediationParams params);

    String getApplicantContactNumber(MediationParams params);

    String getApplicantContactEmail(MediationParams params);
}
