package uk.gov.hmcts.reform.civil.service.search.hearingfee;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HearingFeeDueQueryProviderTest {

    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CommonQueryConstructs commonQueryConstructs;

    @InjectMocks
    private HearingFeeDueQueryProvider provider;

    @Test
    void shouldReturnCorrectInitialPaginatedQuery() throws Exception {
        // Given
        PageToken pageToken = PageToken.initial();
        int pageSize = 50;

        // When
        PaginatedQuery query = provider.getPaginatedQuery(pageToken, pageSize);

        // Then
        JsonNode json = objectMapper.readTree(query.getJsonString(objectMapper));

        assertThat(json.get("size").asInt()).isEqualTo(50);
        assertThat(json.get("from").asInt()).isZero();
        assertThat(json.get("track_total_hits").asBoolean()).isTrue();
        assertThat(json.has("search_after")).isFalse();

        assertThat(json.get("_source").get(0).asText()).isEqualTo("reference");
        assertThat(json.get("sort").get(0).get("reference.keyword").asText()).isEqualTo("asc");

        assertThat(json.toString()).contains("HEARING_READINESS");
    }

    @Test
    void shouldReturnCorrectQueryWithSearchAfter() throws Exception {
        // Given
        PageToken pageToken = PageToken.of("12345");
        int pageSize = 10;

        // When
        PaginatedQuery query = provider.getPaginatedQuery(pageToken, pageSize);

        // Then
        JsonNode json = objectMapper.readTree(query.getJsonString(objectMapper));

        assertThat(json.get("size").asInt()).isEqualTo(10);
        assertThat(json.get("from").asInt()).isZero();
        assertThat(json.get("search_after").get(0).asText()).isEqualTo("12345");
        assertThat(json.get("sort").get(0).get("reference.keyword").asText()).isEqualTo("asc");

        assertThat(json.toString()).contains("HEARING_READINESS");
    }
}
