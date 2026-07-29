package uk.gov.hmcts.reform.civil.service.search.judgementbuffer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.search.PageToken;
import uk.gov.hmcts.reform.civil.model.search.PaginatedQuery;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.search.calculator.SearchDateTimeCalculator;
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper.LOCAL_ZONE;

@ExtendWith(MockitoExtension.class)
class JudgementBufferExpiredQueryProviderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 6, 10, 0);
    private static final ZonedDateTime ZONED_NOW = NOW.atZone(LOCAL_ZONE);
    private static final ZonedDateTime TIME_MINUS_48_WORKING_HOURS =
        LocalDateTime.of(2026, 7, 2, 10, 0).atZone(LOCAL_ZONE);
    private static final long JUDGEMENT_BUFFER_WORKING_HOURS = 48L;

    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CommonQueryConstructs commonQueryConstructs;

    @Mock
    private SearchDateTimeCalculator dateTimeCalculator;

    @Mock
    private Time time;

    @InjectMocks
    private JudgementBufferExpiredQueryProvider provider;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(NOW);
        when(dateTimeCalculator.minusWorkingHours(eq(ZONED_NOW), eq(JUDGEMENT_BUFFER_WORKING_HOURS))).thenReturn(TIME_MINUS_48_WORKING_HOURS);
    }

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
        assertThat(json.get("from").asInt()).isEqualTo(0);
        assertThat(json.get("track_total_hits").asBoolean()).isTrue();
        assertThat(json.has("search_after")).isFalse();

        assertThat(json.get("_source").get(0).asText()).isEqualTo("reference");
        assertThat(json.get("sort").get(0).get("reference.keyword").asText()).isEqualTo("asc");

        assertThat(json.toString()).contains("data.joDJCreatedDate");
        assertThat(json.toString()).contains("2026-07-02T10:00:00");
        assertThat(json.toString()).contains("JUDGMENT_REQUESTED");
        assertThat(json.toString()).contains("data.businessProcess");

        verify(dateTimeCalculator).minusWorkingHours(ZONED_NOW, JUDGEMENT_BUFFER_WORKING_HOURS);
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
        assertThat(json.get("from").asInt()).isEqualTo(0);
        assertThat(json.get("search_after").get(0).asText()).isEqualTo("12345");
        assertThat(json.get("sort").get(0).get("reference.keyword").asText()).isEqualTo("asc");

        verify(dateTimeCalculator).minusWorkingHours(ZONED_NOW, JUDGEMENT_BUFFER_WORKING_HOURS);
    }
}
