package uk.gov.hmcts.reform.civil.service.search.breathingspace;

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
class BreathingSpaceLiftPendingQueryProviderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 15, 10, 0);

    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CommonQueryConstructs commonQueryConstructs;

    @Mock
    private Time time;

    @InjectMocks
    private BreathingSpaceLiftPendingQueryProvider provider;

    @BeforeEach
    void setUp() {
        when(time.now()).thenReturn(NOW);
    }

    @Test
    void shouldReturnCasesWithAPendingLiftDueTodayOrEarlier() throws Exception {
        PaginatedQuery query = provider.getPaginatedQuery(PageToken.initial(), 50);

        JsonNode json = objectMapper.readTree(query.getJsonString(objectMapper));

        assertThat(json.get("size").asInt()).isEqualTo(50);
        assertThat(json.get("from").asInt()).isZero();
        assertThat(json.has("search_after")).isFalse();
        assertThat(json.get("_source").get(0).asText()).isEqualTo("reference");
        assertThat(json.toString()).contains("data.breathingSpaceActive");
        assertThat(json.toString()).contains("data.breathingSpaceLiftPending");
        assertThat(json.toString()).contains("data.liftBreathing.expectedEnd");
        assertThat(json.toString()).contains("2026-01-15");
        assertThat(json.toString()).contains("data.businessProcess");
    }
}
