package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.helper.CaseDataTemplates;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("java:S5960")
class ClearFormerSolicitorInfoWorkflowTest extends WorkflowIntegrationTest {

    private static final String NOC_CLAIM_ISSUED = "noc-claim-issued";

    private CaseData fixtureWithFormerSolicitorInfo(String caseRole) {
        return CaseDataTemplates.load(NOC_CLAIM_ISSUED, template ->
            CaseDataTemplates.set(template, "changeOfRepresentation", new ChangeOfRepresentation()
                .setCaseRole(caseRole)
                .setOrganisationToAddID("NEW-ORG-001")
                .setOrganisationToRemoveID("OLD-ORG-001")
                .setTimestamp(LocalDateTime.of(2026, 6, 15, 10, 0))
                .setFormerRepresentationEmailAddress("formersolicitor@example.com")
                .setFormerRepresentationReference("FORMER-REF-001"))
        );
    }

    @Test
    void shouldClearFormerSolicitorEmailForRespondent1() throws Exception {
        CaseData fixture = fixtureWithFormerSolicitorInfo(
            CaseRole.RESPONDENTSOLICITORONE.getFormattedName());

        startWorkflow(fixture)
            .eventId(CaseEvent.CLEAR_FORMER_SOLICITOR_INFO_AFTER_NOTIFY_NOC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                ChangeOfRepresentation cor = result.caseData().getChangeOfRepresentation();
                assertThat(cor.getFormerRepresentationEmailAddress()).isNull();
                assertThat(cor.getFormerRepresentationReference()).isNull();

                assertThat(cor.getCaseRole())
                    .isEqualTo(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
                assertThat(cor.getOrganisationToAddID()).isEqualTo("NEW-ORG-001");
            });
    }

    @Test
    void shouldClearFormerSolicitorEmailForApplicant() throws Exception {
        CaseData fixture = fixtureWithFormerSolicitorInfo(
            CaseRole.APPLICANTSOLICITORONE.getFormattedName());

        startWorkflow(fixture)
            .eventId(CaseEvent.CLEAR_FORMER_SOLICITOR_INFO_AFTER_NOTIFY_NOC)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                ChangeOfRepresentation cor = result.caseData().getChangeOfRepresentation();
                assertThat(cor.getFormerRepresentationEmailAddress()).isNull();
                assertThat(cor.getFormerRepresentationReference()).isNull();

                assertThat(cor.getCaseRole())
                    .isEqualTo(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
            });
    }
}
