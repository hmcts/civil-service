package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseFlagCaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CASE_FLAGS;

class CreateCaseLevelCaseFlagTaskTest {

    private static final String CASE_REFERENCE = "1234567890123456";
    private final CreateCaseLevelCaseFlagTask task = new CreateCaseLevelCaseFlagTask();

    @Test
    void shouldCreateOtherFlagAtCaseLevel() {
        CaseData caseData = CaseData.builder().build();

        CaseData result = task.migrateCaseData(caseData, reference("Migration flag comment"));

        assertThat(result.getCaseFlags()).isNotNull();
        assertThat(result.getCaseFlags().getDetails()).hasSize(1);
        Element<FlagDetail> element = result.getCaseFlags().getDetails().getFirst();
        assertThat(element.getId()).isNotNull();
        assertThat(element.getValue().getName()).isEqualTo("Other");
        assertThat(element.getValue().getFlagComment()).isEqualTo("Migration flag comment");
        assertThat(element.getValue().getStatus()).isEqualTo("Active");
        assertThat(task.getCaseEvent()).isEqualTo(CREATE_CASE_FLAGS);
    }

    @Test
    void shouldPreserveExistingCaseLevelFlagsAndMetadata() {
        Element<FlagDetail> existingDetail = new Element<>(
            UUID.randomUUID(),
            new FlagDetail().setName("Urgent case").setStatus("Active")
        );
        Flags existingFlags = new Flags()
            .setPartyName("Case")
            .setRoleOnCase("Case-level")
            .setDetails(List.of(existingDetail));
        CaseData caseData = CaseData.builder().caseFlags(existingFlags).build();

        CaseData result = task.migrateCaseData(caseData, reference("Additional comment"));

        assertThat(result.getCaseFlags().getPartyName()).isEqualTo("Case");
        assertThat(result.getCaseFlags().getRoleOnCase()).isEqualTo("Case-level");
        assertThat(result.getCaseFlags().getDetails()).hasSize(2);
        assertThat(result.getCaseFlags().getDetails().getFirst()).isEqualTo(existingDetail);
        assertThat(result.getCaseFlags().getDetails().get(1).getValue().getFlagComment())
            .isEqualTo("Additional comment");
    }

    @Test
    void shouldRejectBlankComment() {
        assertThatThrownBy(() -> task.migrateCaseData(CaseData.builder().build(), reference(" ")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Case flag comment must not be blank");
    }

    private CaseFlagCaseReference reference(String comment) {
        CaseFlagCaseReference reference = new CaseFlagCaseReference();
        reference.setCaseReference(CASE_REFERENCE);
        reference.setComment(comment);
        return reference;
    }
}
