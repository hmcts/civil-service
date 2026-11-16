package uk.gov.hmcts.reform.civil.notification.handlers.ga;

import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifiableCaseData;

/**
 * The General Application case plus its parent civil case, which is what a GA email needs.
 *
 * <p>General Application property assembly takes both: {@code NotificationDataGA.addProperties}
 * is declared as {@code addProperties(gaCaseData, mainCaseData)}, and every GA notification service
 * fetches the parent case before building anything. The civil
 * {@link uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator} shape passes one case
 * data, so GA generators are parameterised on this instead of on
 * {@code GeneralApplicationCaseData} directly.</p>
 *
 * <p>A context object was rejected at the {@code Notifier} level on DTSCCI-6726 because all 73
 * existing generators would have had to unwrap it. Here there are no existing GA generators to
 * change, and the parent case is a genuine second input rather than a wrapper, so the trade goes
 * the other way. {@link AllGaPartiesEmailGenerator} builds it and the context never travels above
 * that, so {@code Notifier<GeneralApplicationCaseData>} is unaffected.</p>
 *
 * @param gaCaseData   the General Application case, with solicitor emails already validated
 * @param mainCaseData the parent civil case the application was made on
 */
public record GaNotificationContext(
    GeneralApplicationCaseData gaCaseData,
    GeneralApplicationCaseData mainCaseData
) implements NotifiableCaseData {

    @Override
    public Long getCcdCaseReference() {
        return gaCaseData.getCcdCaseReference();
    }
}
