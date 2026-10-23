package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.utils.MaskHelper.maskEmail;

/**
 * Builds the email for one recipient of a notification.
 *
 * <p>Typed on the case data so that General Application notifiers can have generators too. It was
 * {@code CaseData} only, which is why the first GA notifier on the framework had to implement
 * {@link PartiesEmailGenerator} directly and build its {@link EmailDTO} by hand. The civil
 * behaviour, which is the common properties and the legacy case reference, lives in
 * {@link CivilEmailDTOGenerator} so that none of its roughly 255 leaf subclasses had to change.</p>
 *
 * @param <T> the case data this generator reads
 */
@Slf4j
public abstract class EmailDTOGenerator<T extends NotifiableCaseData> {

    public abstract Boolean getShouldNotify(T caseData);

    /**
     * The emails this generator contributes, which is normally the one it builds.
     *
     * <p>Overridden where a single generator stands for a collection of recipients rather than a
     * fixed party. General Application respondents are a list rather than the respondent1 and
     * respondent2 slots the civil side has, so a GA generator can fan out here instead of the
     * aggregator having to know about GA party shapes.</p>
     *
     * <p>Only {@link uk.gov.hmcts.reform.civil.notification.handlers.ga.AllGaPartiesEmailGenerator}
     * calls this. The civil aggregators deliberately still call {@link #buildEmailDTO}, because
     * their parties are fixed slots and because routing them through here would silently return
     * nothing for the many tests that mock {@code buildEmailDTO} on a generator.</p>
     */
    public Set<EmailDTO> buildEmailDTOs(T caseData, String taskId) {
        return Set.of(buildEmailDTO(caseData, taskId));
    }

    public EmailDTO buildEmailDTO(T caseData, String taskId) {
        Map<String, String> properties = addProperties(caseData);
        addCustomProperties(properties, caseData);
        String emailReference = buildEmailReference(caseData);
        log.info("buildEmailDTO for taskId: {} and email: {} and reference: {}",
                 taskId, maskEmail(getEmailAddress(caseData)), emailReference);
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setTargetEmail(getEmailAddress(caseData));
        emailDTO.setEmailTemplate(getEmailTemplateId(caseData, taskId));
        emailDTO.setParameters(properties);
        emailDTO.setReference(emailReference);
        return emailDTO;
    }

    public abstract Map<String, String> addProperties(T caseData);

    /**
     * The reference the email is sent under. Civil formats its own reference template with the
     * legacy case reference; General Application uses the parent case reference, which is why this
     * is not fixed here.
     */
    protected abstract String buildEmailReference(T caseData);

    protected abstract String getEmailAddress(T caseData);

    protected abstract String getEmailTemplateId(T caseData);

    protected String getEmailTemplateId(T caseData, String taskId) {
        return getEmailTemplateId(caseData);
    }

    protected abstract Map<String, String> addCustomProperties(Map<String, String> properties, T caseData);
}
