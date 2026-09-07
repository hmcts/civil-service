package uk.gov.hmcts.reform.civil.model.search;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageTokenTest {

    @Test
    void shouldCreateInitialPageToken() {
        PageToken pageToken = PageToken.initial();

        assertThat(pageToken.isInitial()).isTrue();
        assertThat(pageToken.getValue()).isEmpty();
    }

    @Test
    void shouldCreatePageTokenWithValue() {
        String value = "some-value";
        PageToken pageToken = PageToken.of(value);

        assertThat(pageToken.isInitial()).isFalse();
        assertThat(pageToken.getValue()).contains(value);
    }

    @Test
    void shouldCreateInitialPageTokenWhenValueIsNull() {
        PageToken pageToken = PageToken.of(null);

        assertThat(pageToken.isInitial()).isTrue();
        assertThat(pageToken.getValue()).isEmpty();
    }

    @Test
    void shouldTestEquality() {
        PageToken token1 = PageToken.of("value");
        PageToken token2 = PageToken.of("value");
        PageToken token3 = PageToken.of("other");
        PageToken initial1 = PageToken.initial();
        PageToken initial2 = PageToken.of(null);

        assertThat(token1).isEqualTo(token2);
        assertThat(token1).isNotEqualTo(token3);
        assertThat(token1).isNotEqualTo(initial1);
        assertThat(initial1).isEqualTo(initial2);
        assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
    }

    @Test
    void shouldTestToString() {
        PageToken token = PageToken.of("value");
        assertThat(token.toString()).contains("value=Optional[value]", "initial=false");
    }
}
