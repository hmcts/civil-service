package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("java:S5960")
class ResetLanguagePreferenceWorkflowTest extends WorkflowIntegrationTest {

    private static final String NOC_CLAIM_ISSUED = "noc-claim-issued";

    private CaseData bilingualClaimantFixture() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "changeOfRepresentation", new ChangeOfRepresentation()
                .setCaseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                .setOrganisationToAddID("NEW-ORG-001")
                .setTimestamp(LocalDateTime.of(2026, 6, 15, 10, 0)));
            CaseDataTemplates.set(template, "claimantBilingualLanguagePreference",
                Language.BOTH.toString());
            CaseDataTemplates.set(template, "claimantLanguagePreferenceDisplay",
                PreferredLanguage.WELSH);
        });
    }

    private CaseData bilingualDefendantFixture() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "changeOfRepresentation", new ChangeOfRepresentation()
                .setCaseRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                .setOrganisationToAddID("NEW-ORG-001")
                .setTimestamp(LocalDateTime.of(2026, 6, 15, 10, 0)));
            CaseDataTemplates.set(template, "respondent1LiPResponse", new RespondentLiPResponse()
                .setRespondent1ResponseLanguage(Language.BOTH.toString()));
            CaseDataTemplates.set(template, "defendantLanguagePreferenceDisplay",
                PreferredLanguage.WELSH);
        });
    }

    private CaseData nonBilingualClaimantFixture() {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template -> {
            CaseDataTemplates.set(template, "changeOfRepresentation", new ChangeOfRepresentation()
                .setCaseRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                .setOrganisationToAddID("NEW-ORG-001")
                .setTimestamp(LocalDateTime.of(2026, 6, 15, 10, 0)));
            CaseDataTemplates.set(template, "claimantBilingualLanguagePreference",
                Language.ENGLISH.toString());
        });
    }

    @Test
    void shouldResetClaimantLanguagePreferenceAfterApplicantNoC() throws Exception {
        CaseData fixture = bilingualClaimantFixture();

        startWorkflow(fixture)
            .eventId(CaseEvent.RESET_LANGUAGE_PREFERENCE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).doesNotContainKey("claimantBilingualLanguagePreference");
                assertThat(result.response().getData()).doesNotContainKey("claimantLanguagePreferenceDisplay");
            });
    }

    @Test
    void shouldResetDefendantLanguagePreferenceAfterRespondent1NoC() throws Exception {
        CaseData fixture = bilingualDefendantFixture();

        startWorkflow(fixture)
            .eventId(CaseEvent.RESET_LANGUAGE_PREFERENCE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.response().getData()).doesNotContainKey("defendantLanguagePreferenceDisplay");
            });
    }

    @Test
    void shouldNotResetLanguageWhenNotBilingual() throws Exception {
        CaseData fixture = nonBilingualClaimantFixture();

        startWorkflow(fixture)
            .eventId(CaseEvent.RESET_LANGUAGE_PREFERENCE)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getClaimantBilingualLanguagePreference())
                    .isEqualTo(Language.ENGLISH.toString());
            });
    }
}
