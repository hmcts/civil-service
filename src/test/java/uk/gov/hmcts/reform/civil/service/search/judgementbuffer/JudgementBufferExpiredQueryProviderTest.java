package uk.gov.hmcts.reform.civil.service.search.judgementbuffer;

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
class JudgementBufferExpiredQueryProviderTest {

    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Spy
    private CommonQueryConstructs commonQueryConstructs;

    @InjectMocks
    private JudgementBufferExpiredQueryProvider provider;

    @Test
    void shouldReturnCorrectQuery() {
        // Given
        PageToken pageToken = PageToken.initial();
        int pageSize = 50;

        // When
        PaginatedQuery query = provider.getPaginatedQuery(pageToken, pageSize);

        // Then
        String json = query.getJsonString(objectMapper);
        assertThat(json).contains("\"size\":50");
        assertThat(json).contains("data.joDJCreatedDate");
        assertThat(json).contains("\"reference.keyword\"");
    }

    @Test
    void shouldReturnCorrectQueryWithSearchAfter() {
        // Given
        PageToken pageToken = PageToken.of("12345");
        int pageSize = 10;

        // When
        PaginatedQuery query = provider.getPaginatedQuery(pageToken, pageSize);

        // Then
        String json = query.getJsonString(objectMapper);
        assertThat(json).contains("\"size\":10");
        assertThat(json).contains("\"search_after\":[\"12345\"]");
    }
}
