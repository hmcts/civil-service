package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.crd.client.ListOfValuesApi;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;

import java.util.Map;
import java.util.Optional;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class CategoryServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";
    private static final String AUTH_TOKEN_UNAUTHORISED = "Bearer token unauthorised";
    private static final String SERVICE_AUTH_TOKEN = "Bearer service-token";
    private static final String CATEGORY_ID = "HearingChannel";
    private static final String SERVICE_ID = "AAA6";
    private static final String WRONG_SERVICE_ID = "ABCDE";

    private final FeignException notFoundFeignException = new FeignException.NotFound(
        "not found message",
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
        "not found response body".getBytes(UTF_8));

    private final FeignException forbiddenException = new FeignException.Forbidden(
        "forbidden message",
        Request.create(GET, "", Map.of(), new byte[]{}, UTF_8, null),
        "forbidden response body".getBytes(UTF_8));

    private final CategorySearchResult categorySearchResult = CategorySearchResult.builder().build();

    @Mock
    private ListOfValuesApi listOfValuesApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        given(listOfValuesApi.findCategoryByCategoryIdAndServiceId(
            any(),
            any(),
            any(),
            any())).willReturn(categorySearchResult);
        given(authTokenGenerator.generate()).willReturn(SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldReturnCategory_whenInvoked() {
        var category = categoryService.findCategoryByCategoryIdAndServiceId(AUTH_TOKEN, CATEGORY_ID, SERVICE_ID);

        verify(listOfValuesApi).findCategoryByCategoryIdAndServiceId(CATEGORY_ID, SERVICE_ID, AUTH_TOKEN,
                                                                     authTokenGenerator.generate());
        assertThat(category).isEqualTo(Optional.of(categorySearchResult));
    }

    @Test
    void shouldReturnEmptyOptional_whenCategoryNotFound() {
        given(listOfValuesApi.findCategoryByCategoryIdAndServiceId(any(), eq(WRONG_SERVICE_ID), any(), any()))
            .willThrow(notFoundFeignException);
        var category = categoryService.findCategoryByCategoryIdAndServiceId(AUTH_TOKEN, CATEGORY_ID, WRONG_SERVICE_ID);
        verify(listOfValuesApi).findCategoryByCategoryIdAndServiceId(CATEGORY_ID, WRONG_SERVICE_ID, AUTH_TOKEN,
                                                                     authTokenGenerator.generate());
        assertThat(category).isEmpty();
    }

    @Test
    void shouldReturnEmptyOptional_whenAccessForbidden() {
        given(listOfValuesApi.findCategoryByCategoryIdAndServiceId(any(), any(), eq(AUTH_TOKEN_UNAUTHORISED), any()))
            .willThrow(forbiddenException);
        var category = categoryService.findCategoryByCategoryIdAndServiceId(AUTH_TOKEN_UNAUTHORISED, CATEGORY_ID, SERVICE_ID);
        verify(listOfValuesApi).findCategoryByCategoryIdAndServiceId(CATEGORY_ID, SERVICE_ID, AUTH_TOKEN_UNAUTHORISED,
                                                                     authTokenGenerator.generate());
        assertThat(category).isEmpty();
    }
}
