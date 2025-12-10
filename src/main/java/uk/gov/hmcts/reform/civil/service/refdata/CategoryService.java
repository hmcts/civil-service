package uk.gov.hmcts.reform.civil.service.refdata;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.crd.client.ListOfValuesApi;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service("refdataCategoryService")
@Primary
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
            if (result != null) {
                log.info("[CategoryService] Category received → id={}, serviceId={}, authToken={}, authTokenGeneratorgenerate={}",
                         categoryId,
                         serviceId,
                         authToken,
                         authTokenGenerator.generate()
                );
                log.info("[CategoryService] result {}", result);
            } else {
                log.warn("[CategoryService] API returned null for categoryId={}, serviceId={}", categoryId, serviceId);
            }

            return Optional.ofNullable(result);
        } catch (FeignException.NotFound ex) {
            log.error("Category not found", ex);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Access forbidden for this user", ex);
            return Optional.empty();
        }
    }
}
