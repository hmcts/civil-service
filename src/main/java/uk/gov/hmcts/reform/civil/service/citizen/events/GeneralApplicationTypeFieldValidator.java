package uk.gov.hmcts.reform.civil.service.citizen.events;

import uk.gov.hmcts.reform.civil.exceptions.InvalidGeneralApplicationTypeException;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

record GeneralApplicationTypeFieldValidator(
    Set<String> allowedTypeCodes,
    Set<String> unsupportedTypeCodes,
    Map<String, String> invalidCodeReasons
) {

    private static final String TYPES = "types";

    private static final String NULL_VALUE = "NULL_VALUE";
    private static final String NON_STRING_VALUE = "NON_STRING_VALUE";
    private static final String BLANK_VALUE = "BLANK_VALUE";
    private static final String CUI_UNSUPPORTED_TYPE = "CUI_UNSUPPORTED_TYPE";
    private static final String UNKNOWN_CODE = "UNKNOWN_CODE";

    GeneralApplicationTypeFieldValidator(
            Set<String> allowedTypeCodes,
            Set<String> unsupportedTypeCodes,
            Map<String, String> invalidCodeReasons
    ) {
        this.allowedTypeCodes = Set.copyOf(allowedTypeCodes);
        this.unsupportedTypeCodes = Set.copyOf(unsupportedTypeCodes);
        this.invalidCodeReasons = Map.copyOf(invalidCodeReasons);
    }

    void validate(Object fieldValue) {
        InvalidTypeValues invalidTypeValues = invalidTypeValues(typeValues(fieldValue));
        if (invalidTypeValues.hasInvalidValues()) {
            throw invalid(invalidTypeValues.invalidValueCount(), invalidTypeValues.reasons());
        }
    }

    private Collection<?> typeValues(Object fieldValue) {
        Collection<?> typeValues = typeValuesFrom(typeFieldObject(fieldValue));
        requireNonEmpty(typeValues);
        return typeValues;
    }

    private Map<?, ?> typeFieldObject(Object fieldValue) {
        if (!(fieldValue instanceof Map<?, ?> fieldValueMap)) {
            throw invalid("GENERAL_APP_TYPE_NOT_OBJECT");
        }
        return fieldValueMap;
    }

    private Collection<?> typeValuesFrom(Map<?, ?> fieldValueMap) {
        Object types = fieldValueMap.get(TYPES);
        if (!(types instanceof Collection<?> typeValues)) {
            throw invalid(typeValuesError(types));
        }
        return typeValues;
    }

    private String typeValuesError(Object types) {
        return types == null ? "MISSING_TYPES" : "TYPES_NOT_ARRAY";
    }

    private void requireNonEmpty(Collection<?> typeValues) {
        if (typeValues.isEmpty()) {
            throw invalid("EMPTY_TYPES");
        }
    }

    private InvalidTypeValues invalidTypeValues(Collection<?> typeValues) {
        Set<String> reasons = new LinkedHashSet<>();
        int invalidValueCount = 0;
        for (Object typeValue : typeValues) {
            String reason = invalidReason(typeValue);
            if (reason != null) {
                invalidValueCount++;
                reasons.add(reason);
            }
        }
        return new InvalidTypeValues(invalidValueCount, reasons);
    }

    private String invalidReason(Object typeValue) {
        return typeValue instanceof String typeCode ? invalidTypeCodeReason(typeCode) : nonStringReason(typeValue);
    }

    private String nonStringReason(Object typeValue) {
        return typeValue == null ? NULL_VALUE : NON_STRING_VALUE;
    }

    private String invalidTypeCodeReason(String typeCode) {
        String fixedReason = fixedInvalidTypeCodeReason(typeCode);
        return fixedReason != null ? fixedReason : policyInvalidTypeCodeReason(typeCode);
    }

    private String fixedInvalidTypeCodeReason(String typeCode) {
        if (typeCode.isBlank()) {
            return BLANK_VALUE;
        }

        return invalidCodeReasons.get(typeCode);
    }

    private String policyInvalidTypeCodeReason(String typeCode) {
        if (unsupportedTypeCodes.contains(typeCode)) {
            return CUI_UNSUPPORTED_TYPE;
        }

        if (!allowedTypeCodes.contains(typeCode)) {
            return UNKNOWN_CODE;
        }

        return null;
    }

    private static InvalidGeneralApplicationTypeException invalid(String reason) {
        return invalid(0, Set.of(reason));
    }

    private static InvalidGeneralApplicationTypeException invalid(int invalidValueCount, Set<String> reasons) {
        return new InvalidGeneralApplicationTypeException(invalidValueCount, reasons);
    }

    private record InvalidTypeValues(int invalidValueCount, Set<String> reasons) {

        private boolean hasInvalidValues() {
            return invalidValueCount > 0;
        }
    }
}
