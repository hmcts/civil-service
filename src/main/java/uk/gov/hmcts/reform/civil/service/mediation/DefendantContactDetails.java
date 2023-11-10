package uk.gov.hmcts.reform.civil.service.mediation;

public interface DefendantContactDetails {

    String getDefendantContactName(MediationParams params);

    String getDefendantContactNumber(MediationParams params);

    String getDefendantContactEmail(MediationParams params);
}
