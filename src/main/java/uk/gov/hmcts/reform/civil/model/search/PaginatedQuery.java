package uk.gov.hmcts.reform.civil.model.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates an ElasticSearch query designed for pagination.
 * Handles the inclusion of 'search_after' values and the construction of the JSON query string.
 */
@Getter
public class PaginatedQuery {

    private final QueryBuilder queryBuilder;
    private final List<String> dataToReturn;
    private final int startIndex;
    private final boolean initialSearch;
    private final String searchAfterValue;
    private final int pageSize;
    private final String sortField;

    /**
     * Constructs a new PaginatedQuery.
     *
     * @param queryBuilder the ES QueryBuilder containing the search criteria
     * @param dataToReturn list of fields to return in the '_source' section
     * @param startIndex the starting index (only used for initial searches when not using search_after)
     * @param pageToken the token containing the 'search_after' value
     * @param pageSize the number of results to return
     */
    public PaginatedQuery(QueryBuilder queryBuilder, List<String> dataToReturn,
                          int startIndex, PageToken pageToken, int pageSize) {
        this.queryBuilder = queryBuilder;
        this.dataToReturn = dataToReturn;
        this.startIndex = startIndex;
        this.initialSearch = pageToken.isInitial();
        this.searchAfterValue = pageToken.getValue().orElse(null);
        this.pageSize = pageSize;
        this.sortField = "reference.keyword";
    }

    /**
     * Generates the JSON query string to be sent to ElasticSearch.
     *
     * @param objectMapper the Jackson ObjectMapper to use for serialization
     * @return the JSON string representation of the query
     */
    public String getJsonString(ObjectMapper objectMapper) {
        try {
            Map<String, Object> query = new LinkedHashMap<>();
            query.put("query", objectMapper.readTree(queryBuilder.toString()));
            query.put("_source", dataToReturn);
            query.put("size", pageSize);
            query.put("from", searchAfterValue != null ? 0 : startIndex);
            query.put("sort", Collections.singletonList(Collections.singletonMap(sortField, "asc")));
            query.put("track_total_hits", true);

            if (!initialSearch && searchAfterValue != null) {
                query.put("search_after", Collections.singletonList(searchAfterValue));
            }

            return objectMapper.writeValueAsString(query);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing PaginatedQuery to JSON", e);
        }
    }
}
