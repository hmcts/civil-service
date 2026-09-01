package uk.gov.hmcts.reform.civil.service.search.fulladmitpayimmediatelynopayfromdef;

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
import uk.gov.hmcts.reform.civil.service.search.common.CommonQueryConstructs;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FullAdmitPayImmediatelyNoPaymentFromDefendantQueryProviderTest {

    // Thursday 15 January 2026
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 15, 10, 0);

    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CommonQueryConstructs commonQueryConstructs;

    @Mock
    private Time time;

    @InjectMocks
    private FullAdmitPayImmediatelyNoPaymentFromDefendantQueryProvider provider;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(NOW);
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
        assertThat(json.get("from").asInt()).isZero();
        assertThat(json.get("track_total_hits").asBoolean()).isTrue();
        assertThat(json.has("search_after")).isFalse();

        assertThat(json.get("_source").get(0).asText()).isEqualTo("reference");
        assertThat(json.get("sort").get(0).get("reference.keyword").asText()).isEqualTo("asc");

        assertThat(json.toString()).contains("data.fullAdmitNoPaymentSchedulerProcessed");
        assertThat(json.toString()).contains("data.respondToClaimAdmitPartLRspec.whenWillThisAmountBePaid");
        assertThat(json.toString()).contains("FULL_ADMISSION");
        assertThat(json.toString()).contains("AWAITING_APPLICANT_INTENTION");
    }

    @Test
    void shouldBoundThePaymentDateRangeToYesterdayAndEightDaysAgoSkippingWeekends() throws Exception {
        // When
        PaginatedQuery query = provider.getPaginatedQuery(PageToken.initial(), 50);

        // Then
        JsonNode json = objectMapper.readTree(query.getJsonString(objectMapper));

        // 8 days before Thu 15 Jan 2026 is Wed 7 Jan 2026, a working day, so it is unchanged
        assertThat(json.toString()).contains("2026-01-07");
        // 1 day before Thu 15 Jan 2026
        assertThat(json.toString()).contains("2026-01-14");
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
    }
}
