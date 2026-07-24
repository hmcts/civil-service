class LoggingPiiExamples {
    void unsafe(CaseData caseData, GeneralApplication application, Party respondent1,
                TelemetryClient telemetryClient) {
        // ruleid: civil.java.pii-accessor-to-log
        log.info("Applicant email: {}", caseData.getEmailAddress());

        // ruleid: civil.java.pii-accessor-to-log
        log.warn("Claim amount=" + caseData.getClaimAmount());

        // ruleid: civil.java.sensitive-object-to-log
        log.info("Application: {}", application);

        // ruleid: civil.java.sensitive-object-to-log
        MDC.put("caseData", caseData);

        // ruleid: civil.java.sensitive-object-to-log
        telemetryClient.trackTrace(application);

        // ruleid: civil.java.sensitive-object-to-log
        log.info("Respondent: {}", respondent1);

        // ruleid: civil.java.pii-accessor-to-log
        MDC.put("party", caseData.getPartyName());

        // ruleid: civil.java.pii-accessor-to-log
        telemetryClient.trackEvent("payment", Map.of("amount", caseData.getPaymentAmount()), null);
    }

    void safe(CaseData caseData, String caseId, String userId) {
        // ok: civil.java.pii-accessor-to-log
        log.info("Processing case {}", caseData.getCcdCaseReference());

        // ok: civil.java.sensitive-object-to-log
        log.info("Processing case {} for user {}", caseId, userId);

        // ok: civil.java.pii-accessor-to-log
        MDC.put("caseId", caseId);
    }

    void safeAfterSensitiveMutation(CaseData caseData, UserDetails userDetails) {
        caseData.setRespondentSolicitor1EmailAddress(userDetails.getEmail());
        String organisationId = OrgPolicyUtils.getRespondent1SolicitorOrgId(caseData);

        // ok: civil.java.pii-accessor-to-log
        log.info("Organisation {} updated for case {}", organisationId,
            caseData.getLegacyCaseReference());
    }
}
