package uk.gov.hmcts.reform.unspec.model.search;

import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryTest {

    public static final String EXPECTED_QUERY =
        "{\"query\": {\"match_all\": {\"boost\": 1.0 }}, \"_source\": [\"reference\",\"other field\"], \"from\": 0}";

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

    @Test
    void shouldFormatSourceInCorrectFormat_whenListOfItems() throws JSONException {
        Query query = new Query(matchAllQuery(), List.of("reference", "other field"), 0);

        JSONAssert.assertEquals(EXPECTED_QUERY, query.toString(), true);
    }
}
