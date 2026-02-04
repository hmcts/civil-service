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
            new FixedRecoverableCosts()
                .setIsSubjectToFixedRecoverableCostRegime(YES)
                .setComplexityBandingAgreed(YES)
                .setBand(BAND_1)
                .setReasons("Reasons");

        final var actual = FixedRecoverableCostsSection.from(fixedRecoverableCosts);

        FixedRecoverableCostsSection expected = new FixedRecoverableCostsSection();
        expected.setIsSubjectToFixedRecoverableCostRegime(YES);
        expected.setComplexityBandingAgreed(YES);
        expected.setBand(BAND_1);
        expected.setReasons("Reasons");
        expected.setBandText(BAND_1.getLabel());

        assertEquals(expected, actual);
    }

    @Test
    public void testFrom_withNullFixedRecoverableCosts_returnsNull() {
        var actual = FixedRecoverableCostsSection.from(null);

        assertNull(actual);
    }

}
