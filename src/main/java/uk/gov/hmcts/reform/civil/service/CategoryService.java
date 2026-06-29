package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.crd.client.ListOfValuesApi;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.exceptions.RetryableCategoryException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final ListOfValuesApi listOfValuesApi;
    private final AuthTokenGenerator authTokenGenerator;

    @Cacheable(
        value = "civilCaseCategoryCache",
        key = "T(String).format('%s-%s', #categoryId, #serviceId)"
    )
    @Retryable(retryFor = RetryableCategoryException.class, backoff = @Backoff(delay = 500))
    public Optional<CategorySearchResult> findCategoryByCategoryIdAndServiceId(String authToken, String categoryId, String serviceId) {
        try {
            log.info("[CategoryService] Cache MISS → calling RD Common Data API to fetch all case categories");
            var result = listOfValuesApi.findCategoryByCategoryIdAndServiceId(categoryId, serviceId, authToken,
                                                                              authTokenGenerator.generate());
            if (result == null) {
                log.warn("[CategoryService] API returned null for categoryId={}, serviceId={}", categoryId, serviceId);
            }

            return Optional.ofNullable(result);
        } catch (FeignException.GatewayTimeout | FeignException.BadGateway | FeignException.ServiceUnavailable e) {
            throw new RetryableCategoryException(e.getMessage(), e);
        } catch (FeignException.NotFound ex) {
            log.error("Category not found", ex);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("[CategoryService] Unexpected error occured", ex);
            return Optional.empty();
        }
    }

    @Recover
    public Optional<CategorySearchResult> recover(RetryableCategoryException ex) {
        log.error("[CategoryService] Retryable category lookup failed after retries", ex);
        return Optional.empty();
    }
}
