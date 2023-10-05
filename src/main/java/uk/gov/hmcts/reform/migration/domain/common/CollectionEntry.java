package uk.gov.hmcts.reform.migration.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CollectionEntry<T> {
    private final String id;
    private final T value;
}
