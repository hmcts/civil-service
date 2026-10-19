package uk.gov.hmcts.reform.civil.notification.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;
import java.util.Map;

/**
 * The civil half of {@link EmailDTOGenerator}, holding everything that was on it before it was
 * typed: the common template properties and the legacy case reference format.
 *
 * <p>Every generator that previously extended {@code EmailDTOGenerator} extends this instead, so
 * the change is a rename for them and their own subclasses are untouched. It also keeps
 * {@link NotificationData} on the civil side, whose {@code addProperties} is declared on
 * {@code CaseData} and so cannot sit on the generic base.</p>
 */
public abstract class CivilEmailDTOGenerator extends EmailDTOGenerator<CaseData> implements NotificationData {

    @Autowired
    protected TemplateCommonPropertiesHelper templateCommonPropertiesHelper;

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        HashMap<String, String> properties = new HashMap<>();
        templateCommonPropertiesHelper.addBaseProperties(caseData, properties);
        templateCommonPropertiesHelper.addCommonFooterSignature(properties);
        templateCommonPropertiesHelper.addCnbcContact(caseData, properties);
        templateCommonPropertiesHelper.addSpecAndUnspecContact(caseData, properties);
        templateCommonPropertiesHelper.addCommonFooterSignatureWelsh(properties);
        templateCommonPropertiesHelper.addLipContact(caseData, properties);
        templateCommonPropertiesHelper.addLipContactWelsh(caseData, properties);
        return properties;
    }

    @Override
    protected String buildEmailReference(CaseData caseData) {
        return String.format(getReferenceTemplate(), caseData.getLegacyCaseReference());
    }

    /**
     * Declared here rather than on {@link EmailDTOGenerator} because only the civil reference
     * format uses it. General Application sends under the parent case reference verbatim, so a
     * GA generator would otherwise have to supply a template it never applies.
     */
    protected abstract String getReferenceTemplate();
}
