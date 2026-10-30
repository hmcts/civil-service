package uk.gov.hmcts.reform.civil.notification.handlers.ga;

import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;

/**
 * Base for General Application email generators, holding the conventions that differ from civil.
 *
 * <p>The reference is the parent case reference rather than
 * {@code String.format(getReferenceTemplate(), legacyCaseReference)}, which is what the GA
 * notification services have always sent under, so migrating a GA BPMN onto the framework does not
 * change the reference on the email.</p>
 */
public abstract class GaEmailDTOGenerator extends EmailDTOGenerator<GaNotificationContext> {

    @Override
    protected String buildEmailReference(GaNotificationContext context) {
        return context.gaCaseData().getGeneralAppParentCaseLink().getCaseReference();
    }
}
