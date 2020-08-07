package uk.gov.hmcts.reform.unspec.model.search;

import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryTest {

    @Test
    void shouldThrowException_WhenIndexLessThan0() {
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("field", "value");
        List<String> strings = List.of();
        assertThrows(
            IllegalArgumentException.class,
            () -> new Query(queryBuilder, strings, -1), "Start index cannot be less than 0"
        );
    }

    @Test
    void shouldThrowException_WhenQueryIsNull() {
        List<String> strings = List.of();
        assertThrows(
            NullPointerException.class,
            () -> new Query(null, strings, 0), "QueryBuilder cannot be null in search"
        );
    }
}
