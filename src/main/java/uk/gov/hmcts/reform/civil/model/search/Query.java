package uk.gov.hmcts.reform.civil.model.search;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;
import java.util.Objects;

import static net.minidev.json.JSONValue.toJSONString;

public class Query {

    private final QueryBuilder queryBuilder;
    private final List<String> dataToReturn;
    private final int startIndex;
    private boolean initialSearch;
    private String searchAfterValue;

    public Query(QueryBuilder queryBuilder, List<String> dataToReturn, int startIndex) {
        Objects.requireNonNull(queryBuilder, "QueryBuilder cannot be null in search");
        if (startIndex < 0) {
            throw new IllegalArgumentException("Start index cannot be less than 0");
        }
        this.queryBuilder = queryBuilder;
        this.dataToReturn = dataToReturn;
        this.startIndex = startIndex;
    }

    public Query(QueryBuilder queryBuilder, List<String> dataToReturn, int startIndex,
                 boolean initialSearch, String searchAfterValue) {
        this.queryBuilder = queryBuilder;
        this.dataToReturn = dataToReturn;
        this.startIndex = startIndex;
        this.initialSearch = initialSearch;
        this.searchAfterValue = searchAfterValue;
    }

    @Override
    public String toString() {
        return "{"
            + "\"query\": " + queryBuilder.toString() + ", "
            + "\"_source\": " + toJSONString(dataToReturn) + ", "
            + "\"from\": " + startIndex
            + "}";
    }

    public String toMediationQueryString() {
        if (initialSearch) {
            return getInitialQuery();
        } else {
            return getSubsequentQuery();
        }
    }

    private String getStartQuery() {
        return "{"
            + "\"query\": " + queryBuilder.toString() + ", "
            + "\"_source\": " + toJSONString(dataToReturn) + ", "
            + " \"size\": 10,"
            + "\"sort\": ["
            + "{"
            + "\"reference.keyword\": \"asc\""
            + "            }"
            + "          ]";
    }

    private String getInitialQuery() {
        return getStartQuery() + END_QUERY;
    }

    private String getSubsequentQuery() {
        return getStartQuery() + "," + String.format(SEARCH_AFTER, searchAfterValue) + END_QUERY;
    }

    private static final String END_QUERY = "\n}";

    private static final String SEARCH_AFTER = "\"search_after\": [%s]";
}
