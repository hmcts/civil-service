package uk.gov.hmcts.reform.civil.model.search;

import org.elasticsearch.index.query.QueryBuilder;
import java.util.List;

import static net.minidev.json.JSONValue.toJSONString;

public class PaginatedQuery {

    private final QueryBuilder queryBuilder;
    private final List<String> dataToReturn;
    private final int startIndex;
    private final boolean initialSearch;
    private final String searchAfterValue;

    public PaginatedQuery(QueryBuilder queryBuilder, List<String> dataToReturn, int startIndex,
                          boolean initialSearch, String searchAfterValue) {
        this.queryBuilder = queryBuilder;
        this.dataToReturn = dataToReturn;
        this.startIndex = startIndex;
        this.initialSearch = initialSearch;
        this.searchAfterValue = searchAfterValue;
    }

    @Override
    public String toString() {
        if (initialSearch || (searchAfterValue == null && startIndex == 0)) {
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
            + "\"from\": " + startIndex + ","
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
    private static final String SEARCH_AFTER = "\"search_after\": [\"%s\"]";
}
