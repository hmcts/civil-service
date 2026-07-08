package uk.gov.hmcts.reform.civil.service.citizen.events;

import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypesLR;
import uk.gov.hmcts.reform.civil.exceptions.InvalidGeneralApplicationTypeException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class GeneralApplicationTypeValidator {

    private static final String GENERAL_APP_TYPE = "generalAppType";
    private static final String GENERAL_APP_TYPE_LR = "generalAppTypeLR";

    private static final String OTHER_OPTION = "OTHER_OPTION";
    private static final String SUMMARY_JUDGMENT = "SUMMARY_JUDGMENT";

    private static final Set<String> CUI_ALLOWED_TYPES = Set.of(
        GeneralApplicationTypes.STRIKE_OUT.name(),
        GeneralApplicationTypes.SUMMARY_JUDGEMENT.name(),
        GeneralApplicationTypes.STAY_THE_CLAIM.name(),
        GeneralApplicationTypes.EXTEND_TIME.name(),
        GeneralApplicationTypes.AMEND_A_STMT_OF_CASE.name(),
        GeneralApplicationTypes.RELIEF_FROM_SANCTIONS.name(),
        GeneralApplicationTypes.SET_ASIDE_JUDGEMENT.name(),
        GeneralApplicationTypes.SETTLE_BY_CONSENT.name(),
        GeneralApplicationTypes.VARY_ORDER.name(),
        GeneralApplicationTypes.ADJOURN_HEARING.name(),
        GeneralApplicationTypes.UNLESS_ORDER.name(),
        GeneralApplicationTypes.OTHER.name(),
        GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT.name(),
        GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID.name()
    );
    private static final Set<String> LR_ALLOWED_TYPES = Arrays.stream(GeneralApplicationTypesLR.values())
        .map(GeneralApplicationTypesLR::name)
        .collect(Collectors.toUnmodifiableSet());
    private static final Set<String> ALL_CCD_TYPE_CODES = Arrays.stream(GeneralApplicationTypes.values())
        .map(GeneralApplicationTypes::name)
        .collect(Collectors.toUnmodifiableSet());
    private static final Set<String> CUI_UNSUPPORTED_TYPES = ALL_CCD_TYPE_CODES.stream()
        .filter(typeCode -> !CUI_ALLOWED_TYPES.contains(typeCode))
        .collect(Collectors.toUnmodifiableSet());

    private static final Map<String, String> INVALID_CODE_REASONS = invalidCodeReasons();
    private static final List<TypeField> TYPE_FIELDS = List.of(
        new TypeField(
            GENERAL_APP_TYPE,
            new GeneralApplicationTypeFieldValidator(CUI_ALLOWED_TYPES, CUI_UNSUPPORTED_TYPES, INVALID_CODE_REASONS)
        ),
        new TypeField(
            GENERAL_APP_TYPE_LR,
            new GeneralApplicationTypeFieldValidator(LR_ALLOWED_TYPES, Set.of(), INVALID_CODE_REASONS)
        )
    );

    private GeneralApplicationTypeValidator() {
    }

    static void validate(Map<String, Object> caseDataUpdate) {
        presentTypeFieldsOrThrow(requireCaseDataUpdate(caseDataUpdate))
            .forEach(typeField -> typeField.validator().validate(caseDataUpdate.get(typeField.name())));
    }

    private static Map<String, Object> requireCaseDataUpdate(Map<String, Object> caseDataUpdate) {
        if (caseDataUpdate == null) {
            throw invalid("MISSING_CASE_DATA_UPDATE");
        }
        return caseDataUpdate;
    }

    private static List<TypeField> presentTypeFieldsOrThrow(Map<String, Object> caseDataUpdate) {
        List<TypeField> presentTypeFields = presentTypeFields(caseDataUpdate);
        if (presentTypeFields.isEmpty()) {
            throw invalid("MISSING_GENERAL_APP_TYPE");
        }
        return presentTypeFields;
    }

    private static List<TypeField> presentTypeFields(Map<String, Object> caseDataUpdate) {
        return TYPE_FIELDS.stream()
            .filter(typeField -> caseDataUpdate.containsKey(typeField.name()))
            .toList();
    }

    private static Map<String, String> invalidCodeReasons() {
        return Stream.concat(
            Stream.of(
                Map.entry(OTHER_OPTION, "UI_ONLY_OTHER_OPTION"),
                Map.entry(SUMMARY_JUDGMENT, "STALE_CODE")
            ),
            Arrays.stream(GeneralApplicationTypes.values())
                .map(type -> Map.entry(type.getDisplayedValue(), "DISPLAY_LABEL"))
        ).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static InvalidGeneralApplicationTypeException invalid(String reason) {
        return new InvalidGeneralApplicationTypeException(0, Set.of(reason));
    }

    private record TypeField(String name, GeneralApplicationTypeFieldValidator validator) {
    }
}
