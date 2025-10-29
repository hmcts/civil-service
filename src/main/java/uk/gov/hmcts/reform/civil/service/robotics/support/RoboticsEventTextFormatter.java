package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

/**
 * Consolidates repeated text templating for robotics events (RPA reason strings, claimant intentions, etc.).
 */
@Component
public class RoboticsEventTextFormatter {

    private static final String RPA_PREFIX = "RPA Reason: ";

    /**
     * Adds the standard RPA prefix to the supplied message. Null messages remain null.
     */
    public String withRpaPrefix(String message) {
        if (message == null) {
            return null;
        }
        return RPA_PREFIX + message;
    }

    /**
     * Formats the template with {@link Locale#UK} to guarantee stable decimal and date separators.
     */
    public String format(String template, Object... args) {
        Objects.requireNonNull(template, "template must not be null");
        return String.format(Locale.UK, template, args);
    }

    /**
     * Convenience wrapper for {@link #withRpaPrefix(String)} combined with {@link #format(String, Object...)}.
     */
    public String formatRpa(String template, Object... args) {
        return withRpaPrefix(format(template, args));
    }

    public String claimDismissedNoActionSinceIssue() {
        return withRpaPrefix("Claim dismissed. Claimant hasn't taken action since the claim was issued.");
    }

    public String claimDismissedNoClaimDetailsWithinWindow() {
        return withRpaPrefix("Claim dismissed. Claimant hasn't notified defendant of the claim details within the allowed 2 weeks.");
    }

    public String claimDismissedAfterNoDefendantResponse() {
        return withRpaPrefix("Claim dismissed after no response from defendant after claimant sent notification.");
    }

    public String claimDismissedNoUserActionForSixMonths() {
        return withRpaPrefix("Claim dismissed. No user action has been taken for 6 months.");
    }

    public String manualDeterminationRequired() {
        return withRpaPrefix("Manual Determination Required.");
    }

    public String judgmentByAdmissionOffline() {
        return withRpaPrefix("Judgment by Admission requested and claim moved offline.");
    }

    public String judgmentRecorded() {
        return withRpaPrefix("Judgment recorded.");
    }

    public String onlyOneRespondentNotified() {
        return withRpaPrefix("Only one of the respondent is notified.");
    }

    public String claimantProceeds() {
        return withRpaPrefix("Claimant proceeds.");
    }

    public String lipVsLrFullOrPartAdmissionReceived() {
        return withRpaPrefix("LiP vs LR - full/part admission received.");
    }

    public String defendantFullyAdmits() {
        return withRpaPrefix("Defendant fully admits.");
    }

    public String defendantPartialAdmission() {
        return withRpaPrefix("Defendant partial admission.");
    }

    public String defendantRejectsAndCounterClaims() {
        return withRpaPrefix("Defendant rejects and counter claims.");
    }

    public String divergentRespond() {
        return withRpaPrefix("Divergent respond.");
    }

    public String unrepresentedDefendants() {
        return withRpaPrefix("Unrepresented defendant(s).");
    }

    public String unregisteredDefendantSolicitorFirms() {
        return withRpaPrefix("Unregistered defendant solicitor firm(s).");
    }

    public String unrepresentedAndUnregisteredDefendantSolicitorFirm() {
        return withRpaPrefix("Unrepresented defendant and unregistered defendant solicitor firm");
    }

    public String claimantsProceed() {
        return withRpaPrefix("Claimant(s) proceeds.");
    }

    public String claimantsIntendNotToProceed() {
        return withRpaPrefix("Claimant(s) intends not to proceed.");
    }

    public String onlyOneDefendantNotified() {
        return withRpaPrefix("Only one of the defendants is notified.");
    }

    public String caseTakenOfflineByStaff() {
        return withRpaPrefix("Case taken offline by staff.");
    }

    public String notSuitableForSdo() {
        return withRpaPrefix("Not suitable for SDO.");
    }

    public String judgementByAdmissionRequested() {
        return withRpaPrefix("Judgement by Admission requested and claim moved offline.");
    }

    public String claimDismissedAfterNoApplicantResponse() {
        return withRpaPrefix("Claim dismissed after no response from applicant past response deadline.");
    }

    public String noDefendantSolicitorAppointed() {
        return withRpaPrefix("No Defendant Solicitor appointed.");
    }

    public String multitrackUnspecOffline() {
        return "RPA Reason:Multitrack Unspec going offline.";
    }

    public String caseProceedsInCaseman() {
        return withRpaPrefix("Case Proceeds in Caseman.");
    }

    public String inMediation() {
        return "IN MEDIATION";
    }

    public String unrepresentedDefendant(String prefix, String defendantName) {
        return formatRpa("%sUnrepresented defendant: %s", prefix, defendantName);
    }

    public String unregisteredSolicitor(String prefix, String defendantName) {
        return formatRpa("%sUnregistered defendant solicitor firm: %s", prefix, defendantName);
    }

    public String unrepresentedAndUnregistered(int index, String date, String body) {
        return formatRpa("[%d of 2 - %s] %s", index, date, body);
    }

    public String caseProceedOffline(String reason) {
        return formatRpa("Case proceeds offline. %s", reason);
    }

    public String noticeOfChangeFiled() {
        return "RPA Reason : Notice of Change filed.";
    }
}
