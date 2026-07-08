package uk.gov.hmcts.reform.civil.service.citizen.events;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.exceptions.InvalidGeneralApplicationTypeException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneralApplicationTypeValidatorTest {

    @Test
    void shouldAllowValidGeneralApplicationTypes() {
        assertDoesNotThrow(() -> GeneralApplicationTypeValidator.validate(
            generalApplicationTypeUpdates("EXTEND_TIME", "STRIKE_OUT", "OTHER", "CONFIRM_CCJ_DEBT_PAID")
        ));
    }

    @Test
    void shouldAllowValidGeneralApplicationTypeLrTypes() {
        assertDoesNotThrow(() -> GeneralApplicationTypeValidator.validate(
            generalApplicationTypeLrUpdates("EXTEND_TIME", "STRIKE_OUT", "OTHER", "PROCEEDS_IN_HERITAGE")
        ));
    }

    @Test
    void shouldRejectMissingCaseDataUpdate() {
        assertInvalidPayload(null, 0, "MISSING_CASE_DATA_UPDATE");
    }

    @Test
    void shouldRejectMissingGeneralApplicationType() {
        assertInvalidPayload(Map.of(), 0, "MISSING_GENERAL_APP_TYPE");
    }

    @Test
    void shouldRejectGeneralApplicationTypeThatIsNotAnObject() {
        assertInvalidPayload(Map.of("generalAppType", "EXTEND_TIME"), 0, "GENERAL_APP_TYPE_NOT_OBJECT");
    }

    @Test
    void shouldRejectGeneralApplicationTypeThatIsNull() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("generalAppType", null);

        assertInvalidPayload(payload, 0, "GENERAL_APP_TYPE_NOT_OBJECT");
    }

    @Test
    void shouldRejectMissingGeneralApplicationTypes() {
        assertInvalidPayload(Map.of("generalAppType", Map.of()), 0, "MISSING_TYPES");
    }

    @Test
    void shouldRejectGeneralApplicationTypesThatAreNotAnArray() {
        assertInvalidPayload(Map.of("generalAppType", Map.of("types", "EXTEND_TIME")), 0, "TYPES_NOT_ARRAY");
    }

    @Test
    void shouldRejectEmptyGeneralApplicationTypes() {
        assertInvalidPayload(Map.of("generalAppType", Map.of("types", List.of())), 0, "EMPTY_TYPES");
    }

    @Test
    void shouldRejectOtherOptionAsUiOnlyValue() {
        assertInvalidPayload(generalApplicationTypeUpdates("OTHER_OPTION"), 1, "UI_ONLY_OTHER_OPTION");
    }

    @Test
    void shouldRejectStaleSummaryJudgmentCode() {
        assertInvalidPayload(generalApplicationTypeUpdates("SUMMARY_JUDGMENT"), 1, "STALE_CODE");
    }

    @Test
    void shouldRejectDisplayLabel() {
        assertInvalidPayload(generalApplicationTypeUpdates("Summary judgment"), 1, "DISPLAY_LABEL");
    }

    @Test
    void shouldRejectUnsupportedCuiGeneralApplicationType() {
        assertInvalidPayload(generalApplicationTypeUpdates("PROCEEDS_IN_HERITAGE"), 1, "CUI_UNSUPPORTED_TYPE");
    }

    @Test
    void shouldRejectUnknownCode() {
        assertInvalidPayload(generalApplicationTypeUpdates("NOT_A_REAL_TYPE"), 1, "UNKNOWN_CODE");
    }

    @Test
    void shouldRejectBlankNullAndNonStringValues() {
        assertInvalidPayload(
            generalApplicationTypeUpdates("", null, 123),
            3,
            "BLANK_VALUE",
            "NULL_VALUE",
            "NON_STRING_VALUE"
        );
    }

    @Test
    void shouldRejectInvalidGeneralApplicationTypeLrType() {
        assertInvalidPayload(generalApplicationTypeLrUpdates("OTHER_OPTION"), 1, "UI_ONLY_OTHER_OPTION");
    }

    private Map<String, Object> generalApplicationTypeUpdates(Object... typeValues) {
        return Map.of("generalAppType", Map.of("types", Arrays.asList(typeValues)));
    }

    private Map<String, Object> generalApplicationTypeLrUpdates(Object... typeValues) {
        return Map.of("generalAppTypeLR", Map.of("types", Arrays.asList(typeValues)));
    }

    private void assertInvalidPayload(
        Map<String, Object> payload,
        int invalidValueCount,
        String... reasonCategories
    ) {
        InvalidGeneralApplicationTypeException exception = assertThrows(
            InvalidGeneralApplicationTypeException.class,
            () -> GeneralApplicationTypeValidator.validate(payload)
        );

        assertThat(exception.getMessage()).isEqualTo("Invalid general application type");
        assertThat(exception.getInvalidValueCount()).isEqualTo(invalidValueCount);
        assertThat(exception.getReasonCategories()).containsExactlyInAnyOrder(reasonCategories);
    }
}
