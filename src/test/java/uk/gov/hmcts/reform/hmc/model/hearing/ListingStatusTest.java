package uk.gov.hmcts.reform.hmc.model.hearing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ListingStatusTest {

    @Test
    void shouldHaveCorrectEnumValues() {
        assertThat(ListingStatus.values()).hasSize(4);

        assertThat(ListingStatus.DRAFT).isNotNull();
        assertThat(ListingStatus.PROVISIONAL).isNotNull();
        assertThat(ListingStatus.FIXED).isNotNull();
        assertThat(ListingStatus.CNCL).isNotNull();
    }

    @Test
    void shouldHaveCorrectLabels() {
        assertEquals("Draft", ListingStatus.DRAFT.getLabel());
        assertEquals("Provisional", ListingStatus.PROVISIONAL.getLabel());
        assertEquals("Fixed", ListingStatus.FIXED.getLabel());
        assertEquals("Cancel", ListingStatus.CNCL.getLabel());
    }

    @Test
    void shouldReturnCorrectValueForValueOf() {
        assertEquals(ListingStatus.DRAFT, ListingStatus.valueOf("DRAFT"));
        assertEquals(ListingStatus.PROVISIONAL, ListingStatus.valueOf("PROVISIONAL"));
        assertEquals(ListingStatus.FIXED, ListingStatus.valueOf("FIXED"));
        assertEquals(ListingStatus.CNCL, ListingStatus.valueOf("CNCL"));
    }

    @Test
    void shouldHaveCorrectToString() {
        assertEquals("DRAFT", ListingStatus.DRAFT.toString());
        assertEquals("PROVISIONAL", ListingStatus.PROVISIONAL.toString());
        assertEquals("FIXED", ListingStatus.FIXED.toString());
        assertEquals("CNCL", ListingStatus.CNCL.toString());
    }

    @Test
    void shouldHaveCorrectOrdinals() {
        assertEquals(0, ListingStatus.DRAFT.ordinal());
        assertEquals(1, ListingStatus.PROVISIONAL.ordinal());
        assertEquals(2, ListingStatus.FIXED.ordinal());
        assertEquals(3, ListingStatus.CNCL.ordinal());
    }

    @Test
    void testEnumCoverage() {
        // Test each enum constant to achieve 100% coverage
        for (ListingStatus status : ListingStatus.values()) {
            assertThat(status.getLabel()).isNotNull();
            assertThat(status.name()).isNotNull();
            assertThat(status.toString()).isNotNull();
        }
    }

    @Test
    void testEquality() {
        assertEquals(ListingStatus.DRAFT, ListingStatus.DRAFT);
        assertEquals(ListingStatus.DRAFT, ListingStatus.valueOf("DRAFT"));
        assertThat(ListingStatus.DRAFT).isNotEqualTo(ListingStatus.PROVISIONAL);
    }

    @Test
    void testHashCode() {
        assertEquals(ListingStatus.DRAFT.hashCode(), ListingStatus.DRAFT.hashCode());
        assertThat(ListingStatus.DRAFT.hashCode()).isNotEqualTo(ListingStatus.PROVISIONAL.hashCode());
    }

    @Test
    void shouldReturnCorrectLabelViaGetter() {
        assertThat(ListingStatus.DRAFT.getLabel()).isEqualTo("Draft");
        assertThat(ListingStatus.PROVISIONAL.getLabel()).isEqualTo("Provisional");
        assertThat(ListingStatus.FIXED.getLabel()).isEqualTo("Fixed");
        assertThat(ListingStatus.CNCL.getLabel()).isEqualTo("Cancel");
    }

    @Test
    void shouldReturnNonNullLabels() {
        for (ListingStatus status : ListingStatus.values()) {
            assertThat(status.getLabel())
                .isNotEmpty();
        }
    }

    @Test
    void shouldReturnConsistentLabels() {
        ListingStatus draft = ListingStatus.DRAFT;
        String firstCall = draft.getLabel();
        String secondCall = draft.getLabel();

        assertThat(firstCall).isEqualTo(secondCall)
            .isSameAs(secondCall);
    }

}
