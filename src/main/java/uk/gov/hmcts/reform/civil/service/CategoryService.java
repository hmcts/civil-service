package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.crd.client.ListOfValuesApi;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;

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
    public Optional<CategorySearchResult> findCategoryByCategoryIdAndServiceId(String authToken, String categoryId, String serviceId) {
        try {
            log.info("[CategoryService] Cache MISS → calling RD Common Data API to fetch all case categories");
            var result = listOfValuesApi.findCategoryByCategoryIdAndServiceId(categoryId, serviceId, authToken,
                                                                              authTokenGenerator.generate());
            if (result == null) {
                log.warn("[CategoryService] API returned null for categoryId={}, serviceId={}", categoryId, serviceId);
            }

            return Optional.ofNullable(result);
        } catch (FeignException.NotFound ex) {
            log.error("Category not found", ex);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("[CategoryService] Unexpected error occured", ex);
            return Optional.empty();
        }
    }
}
