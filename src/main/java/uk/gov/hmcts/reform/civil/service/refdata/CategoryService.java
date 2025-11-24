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

    @Cacheable(value = "civilCaseCategoryCache", key = "'allCaseCategories'")
    public Optional<CategorySearchResult> findCategoryByCategoryIdAndServiceId(String authToken, String categoryId, String serviceId) {
        try {
            log.info("[CategoryService] Cache MISS → calling RD Common Data API to fetch all case categories");
            return Optional.ofNullable(listOfValuesApi.findCategoryByCategoryIdAndServiceId(categoryId, serviceId, authToken,
                                                                                            authTokenGenerator.generate()));
        } catch (FeignException.NotFound ex) {
            log.error("Category not found", ex);
            return Optional.empty();
        } catch (FeignException.Forbidden ex) {
            log.error("Access forbidden for this user", ex);
            return Optional.empty();
        }
    }
}
