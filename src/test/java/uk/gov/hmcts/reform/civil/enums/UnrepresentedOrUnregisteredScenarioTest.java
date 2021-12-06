package uk.gov.hmcts.reform.civil.enums;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREPRESENTED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.getDefendantNames;

class UnrepresentedOrUnregisteredScenarioTest {

    @Nested
    class Unrepresented {
        @Test
        void shouldReturnUnrepresentedDefendantNames_WhenBothDefendantsUnrepresentedScenario() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendants().build();

            assertThat(getDefendantNames(UNREPRESENTED, caseData)).isEqualTo(
                List.of(
                    caseData.getRespondent1().getPartyName(),
                    caseData.getRespondent2().getPartyName()
                )
            );
        }

        @Test
        void shouldReturnUnrepresentedDefendantNames_WhenDefendant1UnrepresentedScenario() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant1().build();

            assertThat(getDefendantNames(UNREPRESENTED, caseData)).isEqualTo(
                List.of(
                    caseData.getRespondent1().getPartyName()
                )
            );
        }

        @Test
        void shouldReturnUnrepresentedDefendantNames_WhenDefendant2UnrepresentedScenario() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnrepresentedDefendant2().build();

            assertThat(getDefendantNames(UNREPRESENTED, caseData)).isEqualTo(
                List.of(
                    caseData.getRespondent2().getPartyName()
                )
            );
        }

        @Test
        void shouldReturnUnrepresentedDefendantNames_WhenUnrepresentedDefendant1UnregisteredDefendant2Scenario() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2().build();

            assertThat(getDefendantNames(UNREPRESENTED, caseData)).isEqualTo(
                List.of(
                    caseData.getRespondent1().getPartyName()
                )
            );
        }

        @Test
        void shouldReturnUnrepresentedDefendantNames_WhenUnregisteredDefendant1UnrepresentedDefendant2Scenario() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateProceedsOfflineUnregisteredDefendant1UnrepresentedDefendant2().build();

            assertThat(getDefendantNames(UNREPRESENTED, caseData)).isEqualTo(
                List.of(
                    caseData.getRespondent2().getPartyName()
                )
            );
        }
    }

    @Nested
    class Unregistered {
        @Test
        void shouldReturnUnregisteredDefendantNames_WhenBothDefendantsUnregisteredScenario() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendants().build();

            assertThat(getDefendantNames(UNREGISTERED, caseData)).isEqualTo(
                List.of(
                    caseData.getRespondent1().getPartyName(),
                    caseData.getRespondent2().getPartyName()
                )
            );
        }

        @Test
        void shouldReturnUnregisteredDefendantNames_WhenUnregisteredDefendant1Scenario() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendant1().build();

            assertThat(getDefendantNames(UNREGISTERED, caseData)).isEqualTo(
                List.of(
                    caseData.getRespondent1().getPartyName()
                )
            );
        }

        @Test
        void shouldReturnUnregisteredDefendantNames_WhenUnregisteredDefendant2Scenario() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineUnregisteredDefendant2().build();

            assertThat(getDefendantNames(UNREGISTERED, caseData)).isEqualTo(
                List.of(
                    caseData.getRespondent2().getPartyName()
                )
            );
        }

        @Test
        void shouldReturnUnregisteredDefendantNames_WhenUnrepresentedDefendant1UnregisteredDefendant2Scenario() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2().build();

            assertThat(getDefendantNames(UNREGISTERED, caseData)).isEqualTo(
                List.of(
                    caseData.getRespondent2().getPartyName()
                )
            );
        }

        @Test
        void shouldReturnUnregisteredDefendantNames_WhenUnregisteredDefendant1UnrepresentedDefendant2Scenario() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateProceedsOfflineUnregisteredDefendant1UnrepresentedDefendant2().build();

            assertThat(getDefendantNames(UNREGISTERED, caseData)).isEqualTo(
                List.of(
                    caseData.getRespondent1().getPartyName()
                )
            );
        }
    }

}
