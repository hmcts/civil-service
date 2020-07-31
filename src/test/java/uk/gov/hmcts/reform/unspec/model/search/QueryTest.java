package uk.gov.hmcts.reform.unspec.model.search;

import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryTest {

    @Test
    void shouldThrowException_WhenIndexLessThan0() {
        assertThrows(IllegalArgumentException.class, () ->
                         new Query(QueryBuilders.matchQuery("field", "value"), List.of(), -1),
                     "Start index cannot be less than 0"
        );
    }

    @Test
    void shouldThrowException_WhenQueryIsNull() {
        assertThrows(NullPointerException.class, () ->
                         new Query(null, List.of(), 0),
                     "QueryBuilder cannot be null in search"
        );
    }
}
