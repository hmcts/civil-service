package uk.gov.hmcts.reform.civil.referencedata.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JudgeSearchRequestTest {

    @Test
    void judgeSearchRequest_NoArgsConstructor_CreatesInstance() {
        // Test no-args constructor
        JudgeSearchRequest request = new JudgeSearchRequest();
        assertThat(request).isNotNull();
    }

    @Test
    void judgeSearchRequest_AllArgsConstructor_CreatesInstance() {
        // Test all-args constructor
        JudgeSearchRequest request = new JudgeSearchRequest("searchValue", "serviceValue", "locationValue");
        assertThat(request).isNotNull();
        assertThat(request.getSearchString()).isEqualTo("searchValue");
        assertThat(request.getServiceCode()).isEqualTo("serviceValue");
        assertThat(request.getLocation()).isEqualTo("locationValue");
    }

    @Test
    void judgeSearchRequest_Builder_CreatesInstance() {
        // Test builder pattern
        JudgeSearchRequest request = JudgeSearchRequest.builder()
            .searchString("test")
            .serviceCode("AAA7")
            .location("London")
            .build();

        assertThat(request).isNotNull();
        assertThat(request.getSearchString()).isEqualTo("test");
        assertThat(request.getServiceCode()).isEqualTo("AAA7");
        assertThat(request.getLocation()).isEqualTo("London");
    }

    @Test
    void setSearchString_TrimsValue() {
        JudgeSearchRequest request = new JudgeSearchRequest();
        request.setSearchString("  test  ");
        assertThat(request.getSearchString()).isEqualTo("test");
    }

    @Test
    void setServiceCode_WithValue_TrimsAndLowercases() {
        JudgeSearchRequest request = new JudgeSearchRequest();
        request.setServiceCode("  AAA7  ");
        assertThat(request.getServiceCode()).isEqualTo("aaa7");
    }

    @Test
    void setServiceCode_WithNull_SetsNull() {
        JudgeSearchRequest request = new JudgeSearchRequest();
        request.setServiceCode(null);
        assertThat(request.getServiceCode()).isNull();
    }

    @Test
    void setLocation_WithValue_TrimsAndLowercases() {
        JudgeSearchRequest request = new JudgeSearchRequest();
        request.setLocation("  LONDON  ");
        assertThat(request.getLocation()).isEqualTo("london");
    }

    @Test
    void setLocation_WithNull_SetsNull() {
        JudgeSearchRequest request = new JudgeSearchRequest();
        request.setLocation(null);
        assertThat(request.getLocation()).isNull();
    }
}
