package uk.gov.hmcts.reform.civil.scheduler.common.interceptor;

import lombok.Value;

/**
 * A type-safe key for storing and retrieving attributes in {@link InterceptorContext}.
 *
 * @param <T> the type of the value associated with this key
 */
@Value(staticConstructor = "of")
public class AttributeKey<T> {

    String name;
    Class<T> type;
}
