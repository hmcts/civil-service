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

        CaseFlags expectedCaseFlags = new CaseFlags();
        PartyFlagsModel emptyFlag = new PartyFlagsModel();
        expectedCaseFlags.setFlags(List.of(emptyFlag));

        CaseFlagsMapper.getCaseFlags(caseData);

        assertThat(CaseFlagsMapper.getCaseFlags(caseData)).isEqualTo(expectedCaseFlags);
    }

    @Test
    void shouldReturnCaseFlags() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .withRespondent1Flags()
            .build();

        PartyFlagsModel flagOne = new PartyFlagsModel();
        flagOne.setPartyID("res-1-party-id");
        flagOne.setPartyName(caseData.getRespondent1().getPartyName());
        flagOne.setFlagParentId("");
        flagOne.setFlagId("AB001");
        flagOne.setFlagDescription("Vulnerable user");
        flagOne.setFlagStatus("Active");
        flagOne.setDateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0));
        flagOne.setDateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0));

        PartyFlagsModel flagTwo = new PartyFlagsModel();
        flagTwo.setPartyID("res-1-party-id");
        flagTwo.setPartyName(caseData.getRespondent1().getPartyName());
        flagTwo.setFlagParentId("");
        flagTwo.setFlagId("SM001");
        flagTwo.setFlagDescription("Flight risk");
        flagTwo.setFlagStatus("Active");
        flagTwo.setDateTimeCreated(LocalDateTime.of(2024, 1, 1,  9, 0, 0));
        flagTwo.setDateTimeModified(LocalDateTime.of(2024, 2, 1,  12, 0, 0));

        CaseFlags expectedCaseFlags = new CaseFlags();
        expectedCaseFlags.setFlags(List.of(flagOne, flagTwo));

        CaseFlagsMapper.getCaseFlags(caseData);

        assertThat(CaseFlagsMapper.getCaseFlags(caseData)).isEqualTo(expectedCaseFlags);
    }
}
