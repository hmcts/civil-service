package uk.gov.hmcts.reform.draftstore;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DraftTypeTest {

    @Test
    void shouldCalculate180DayExpiryWhenDraftTypeIsClaim() {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T00:00:00Z");

        OffsetDateTime expiresAt = DraftType.DRAFT_CLAIM.calculateExpiry(createdAt);

        assertThat(expiresAt).isEqualTo(createdAt.plusDays(180));
    }

    @Test
    void shouldUse30DayDefaultWhenRetentionIsNotDefined() {
        assertThat(DraftType.resolveRetentionDays(null)).isEqualTo(30L);
    }

    @Test
    void shouldUseConfiguredRetentionWhenRetentionIsDefined() {
        assertThat(DraftType.resolveRetentionDays(45L)).isEqualTo(45L);
    }

    @Test
    void shouldRejectRetentionWhenRetentionIsNotPositive() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> {
                long retentionDays = DraftType.resolveRetentionDays(0L);
                assertThat(retentionDays).isPositive();
            })
            .withMessage("retentionDays must be positive");
    }
}
