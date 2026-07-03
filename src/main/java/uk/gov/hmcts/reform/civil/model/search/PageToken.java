package uk.gov.hmcts.reform.civil.model.search;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

/**
 * Represents a token used for pagination.
 * Encapsulates the value used for 'search_after' and whether it's the initial page.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class PageToken {

    private final String value;
    private final boolean initial;

    /**
     * Creates a PageToken for the initial search.
     *
     * @return a new initial PageToken
     */
    public static PageToken initial() {
        return new PageToken(null, true);
    }

    /**
     * Creates a PageToken for a subsequent search with a given value.
     *
     * @param value the 'search_after' value
     * @return a new PageToken, or an initial token if the value is null
     */
    public static PageToken of(String value) {
        if (value == null) {
            return initial();
        }
        return new PageToken(value, false);
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }
}
