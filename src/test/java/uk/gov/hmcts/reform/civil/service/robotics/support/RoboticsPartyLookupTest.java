package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoboticsPartyLookupTest {

    private final RoboticsPartyLookup lookup = new RoboticsPartyLookup();

    @Test
    void applicantIdReturnsFirstAndSecond() {
        assertThat(lookup.applicantId(0)).isEqualTo("001");
        assertThat(lookup.applicantId(1)).isEqualTo("004");
    }

    @Test
    void applicantIdRejectsOutOfRange() {
        assertThatThrownBy(() -> lookup.applicantId(2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("applicant");
    }

    @Test
    void respondentIdReturnsFirstAndSecond() {
        assertThat(lookup.respondentId(0)).isEqualTo("002");
        assertThat(lookup.respondentId(1)).isEqualTo("003");
    }

    @Test
    void respondentIdRejectsOutOfRange() {
        assertThatThrownBy(() -> lookup.respondentId(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("respondent");
    }

    @Test
    void truncateReferenceRespectsLimit() {
        String longReference = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        assertThat(lookup.truncateReference(longReference))
            .isEqualTo("ABCDEFGHIJKLMNOPQRSTUVWX");
    }

    @Test
    void truncateReferenceReturnsSameWhenWithinLimit() {
        assertThat(lookup.truncateReference("REF123")).isEqualTo("REF123");
    }

    @Test
    void truncateReferenceKeepsNull() {
        assertThat(lookup.truncateReference(null)).isNull();
    }
}
