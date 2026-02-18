package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

class OtherPartyQueryResponseHelperTest {

    private final OtherPartyQueryResponseHelper helper = new OtherPartyQueryResponseHelper();

    @Test
    void shouldAddLipProperties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        Map<String, String> properties = new HashMap<>();

        helper.addCustomProperties(properties, caseData, "John Smith", true);

        assertThat(properties)
            .containsEntry(PARTY_NAME, "John Smith")
            .containsEntry(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString())
            .doesNotContainKey(CLAIM_LEGAL_ORG_NAME_SPEC);
    }

    @Test
    void shouldAddLegalRepProperties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        Map<String, String> properties = new HashMap<>();

        helper.addCustomProperties(properties, caseData, "Signer Name", false);

        assertThat(properties)
            .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name")
            .containsEntry(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString())
            .containsEntry(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData))
            .containsEntry(CASEMAN_REF, caseData.getLegacyCaseReference())
            .doesNotContainKey(PARTY_NAME);
    }
}
