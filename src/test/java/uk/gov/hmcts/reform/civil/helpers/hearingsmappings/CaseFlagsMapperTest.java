package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseFlags;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PartyFlagsModel;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseFlagsMapperTest {

    @Test
    void shouldReturnEmptyObject_whenNoCaseFlagsAvailable() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .build();

        CaseFlags expectedCaseFlags = CaseFlags.builder()
            .flags(List.of(PartyFlagsModel.builder()
                               .build())).build();

        CaseFlagsMapper.getCaseFlags(caseData);

        assertThat(CaseFlagsMapper.getCaseFlags(caseData)).isEqualTo(expectedCaseFlags);
    }

    @Test
    void shouldReturnCaseFlags() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .withRespondent1Flags()
            .build();

        CaseFlags expectedCaseFlags = CaseFlags.builder()
            .flags(List.of(PartyFlagsModel.builder()
                               .partyID("res-1-party-id")
                               .partyName(caseData.getRespondent1().getPartyName())
                               .flagParentId("")
                               .flagId("AB001")
                               .flagDescription("Vulnerable user")
                               .flagStatus("Active")
                               .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
                               .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
                               .build(),
                           PartyFlagsModel.builder()
                               .partyID("res-1-party-id")
                               .partyName(caseData.getRespondent1().getPartyName())
                               .flagParentId("")
                               .flagId("SM001")
                               .flagDescription("Flight risk")
                               .flagStatus("Active")
                               .dateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0))
                               .dateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0))
                               .build()))
            .build();

        CaseFlagsMapper.getCaseFlags(caseData);

        assertThat(CaseFlagsMapper.getCaseFlags(caseData)).isEqualTo(expectedCaseFlags);
    }
}
