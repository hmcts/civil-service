package uk.gov.hmcts.reform.civil.enums;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.DIS;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.DRH;
import static uk.gov.hmcts.reform.civil.enums.DocumentHearingType.TRI;

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

}
