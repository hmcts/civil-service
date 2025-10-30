package uk.gov.hmcts.reform.civil.handler.migration;

import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public final class PartyDataMigrationUtils {

    private PartyDataMigrationUtils() {

    }

    public static String defaultIfNull(String value) {
        return value == null ? "TBC" : value;
    }

    public static String generatePartyIdIfNull(String value) {
        return value == null ? PartyUtils.createPartyId() : value;
    }

    public static <T> List<Element<T>> updateElements(List<Element<T>> elements, UnaryOperator<T> transformer) {
        return Optional.ofNullable(elements)
            .orElse(Collections.emptyList())
            .stream()
            .map(element -> Element.<T>builder()
                .id(element.getId())
                .value(transformer.apply(element.getValue()))
                .build())
            .toList();
    }
}
