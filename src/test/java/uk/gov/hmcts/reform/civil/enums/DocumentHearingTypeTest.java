package uk.gov.hmcts.reform.civil.enums;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.DIS;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.DRH;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.TRI;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.getContentText;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.getTitleText;

public class DocumentHearingTypeTest {

    @Nested
    class GetType {
        @Test
        void shouldReturnExpectedType_withTRI() {
            // Test valid hearing types
            assertEquals(TRI, DocumentHearingType.getType("AAA7-TRI"));
        }

        @Test
        void shouldReturnExpectedType_withTRI_withoutHyphen() {
            // Test valid hearing types
            assertEquals(TRI, DocumentHearingType.getType("TRI"));
        }

        @Test
        void shouldReturnExpectedType_withDIS() {
            // Test valid hearing types
            assertEquals(DIS, DocumentHearingType.getType("AAA7-DIS"));
        }

        @Test
        void shouldReturnExpectedType_withDIS_withoutHyphen() {
            // Test valid hearing types
            assertEquals(DIS, DocumentHearingType.getType("DIS"));
        }

        @Test
        void shouldReturnExpectedType_withDRH() {
            // Test valid hearing types
            assertEquals(DRH, DocumentHearingType.getType("AAA7-DRH"));
        }

        @Test
        void shouldReturnExpectedType_withDRH_withoutHyphen() {
            // Test valid hearing types
            assertEquals(DRH, DocumentHearingType.getType("DRH"));
        }

        @Test
        void shouldThrowIllegalArgumentException_withInvalidInput() {
            assertThrows(IllegalArgumentException.class, () -> DocumentHearingType.getType("BAD"));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "TRI, SMALL_CLAIM, hearing",
        "TRI, FAST_CLAIM, trial",
        "DRH, SMALL_CLAIM, dispute resolution hearing",
        "DRH, FAST_CLAIM, dispute resolution hearing",
        "DIS, SMALL_CLAIM, disposal hearing",
        "DIS, FAST_CLAIM, disposal hearing"
    })
    void shouldReturnExpectedTitleText(DocumentHearingType hearingType, AllocatedTrack allocatedTrack, String expected) {
        assertEquals(expected, getTitleText(hearingType, allocatedTrack, false));
    }

    @ParameterizedTest
    @CsvSource({
        "TRI, SMALL_CLAIM, hearing",
        "TRI, FAST_CLAIM, trial",
        "DRH, SMALL_CLAIM, hearing",
        "DRH, FAST_CLAIM, hearing",
        "DIS, SMALL_CLAIM, hearing",
        "DIS, FAST_CLAIM, hearing"
    })
    void shouldReturnExpectedContentText(DocumentHearingType hearingType, AllocatedTrack allocatedTrack, String expected) {
        assertEquals(expected, getContentText(hearingType, allocatedTrack, false));
    }

    @ParameterizedTest
    @CsvSource({
        "TRI, SMALL_CLAIM, wrandawiad",
        "TRI, FAST_CLAIM, dreial",
        "DRH, SMALL_CLAIM, wrandawiad datrys anghydfod",
        "DRH, FAST_CLAIM, wrandawiad datrys anghydfod",
        "DIS, SMALL_CLAIM, wrandawiad gwaredu",
        "DIS, FAST_CLAIM, wrandawiad gwaredu"
    })
    void shouldReturnExpectedTitleTextWelsh(DocumentHearingType hearingType, AllocatedTrack allocatedTrack, String expected) {
        assertEquals(expected, getTitleText(hearingType, allocatedTrack, true));
    }

    @ParameterizedTest
    @CsvSource({
        "TRI, SMALL_CLAIM, wrandawiad",
        "TRI, FAST_CLAIM, dreial",
        "DRH, SMALL_CLAIM, wrandawiad",
        "DRH, FAST_CLAIM, wrandawiad",
        "DIS, SMALL_CLAIM, wrandawiad",
        "DIS, FAST_CLAIM, wrandawiad"
    })
    void shouldReturnExpectedContentTextWelsh(DocumentHearingType hearingType, AllocatedTrack allocatedTrack, String expected) {
        assertEquals(expected, getContentText(hearingType, allocatedTrack, true));
    }
}
