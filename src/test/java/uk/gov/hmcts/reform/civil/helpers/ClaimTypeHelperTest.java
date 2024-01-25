package uk.gov.hmcts.reform.civil.helpers;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.ClaimTypeUnspec;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimTypeHelperTest {

    @Test
    void test_get_claimType_based_on_unspec_claim_type() {
        assertThat(ClaimTypeHelper.getClaimTypeFromClaimTypeUnspec(ClaimTypeUnspec.PERSONAL_INJURY)).isEqualTo(ClaimType.PERSONAL_INJURY);
        assertThat(ClaimTypeHelper.getClaimTypeFromClaimTypeUnspec(ClaimTypeUnspec.CLINICAL_NEGLIGENCE)).isEqualTo(ClaimType.CLINICAL_NEGLIGENCE);
        assertThat(ClaimTypeHelper.getClaimTypeFromClaimTypeUnspec(ClaimTypeUnspec.PROFESSIONAL_NEGLIGENCE)).isEqualTo(ClaimType.PROFESSIONAL_NEGLIGENCE);
        assertThat(ClaimTypeHelper.getClaimTypeFromClaimTypeUnspec(ClaimTypeUnspec.BREACH_OF_CONTRACT)).isEqualTo(ClaimType.BREACH_OF_CONTRACT);
        assertThat(ClaimTypeHelper.getClaimTypeFromClaimTypeUnspec(ClaimTypeUnspec.CONSUMER)).isEqualTo(ClaimType.CONSUMER);
        assertThat(ClaimTypeHelper.getClaimTypeFromClaimTypeUnspec(ClaimTypeUnspec.CONSUMER_CREDIT)).isEqualTo(ClaimType.CONSUMER_CREDIT);
        assertThat(ClaimTypeHelper.getClaimTypeFromClaimTypeUnspec(ClaimTypeUnspec.OTHER)).isEqualTo(ClaimType.OTHER);

    }
}
