package uk.gov.hmcts.reform.civil.model.docmosis;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.enums.ComplexityBand.BAND_1;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class FixedRecoverableCostsSectionTest {

    @Test
    public void testFrom_withNonNullFixedRecoverableCosts_returnsExpectedFixedRecoverableCostsSection() {
        var fixedRecoverableCosts =
            FixedRecoverableCosts.builder()
                .isSubjectToFixedRecoverableCostRegime(YES)
                .complexityBandingAgreed(YES)
                .band(BAND_1)
                .reasons("Reasons")
                .build();

        var actual = FixedRecoverableCostsSection.from(fixedRecoverableCosts);

        FixedRecoverableCostsSection expected = FixedRecoverableCostsSection.builder()
            .isSubjectToFixedRecoverableCostRegime(YES)
            .complexityBandingAgreed(YES)
            .band(BAND_1)
            .bandText(BAND_1.getLabel())
            .reasons("Reasons")
            .build();

        assertEquals(expected, actual);
    }

    @Test
    public void testFrom_withNullFixedRecoverableCosts_returnsNull() {
        var actual = FixedRecoverableCostsSection.from(null);

        assertNull(actual);
    }

}
