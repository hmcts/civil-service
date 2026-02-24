package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Cacheable(
        value = "civilCaseCategoryCache",
        key = "T(String).format('%s-%s', #categoryId, #serviceId)"
    )
    public Optional<CategorySearchResult> findCategoryByCategoryIdAndServiceId(String authToken, String categoryId, String serviceId) {
        try {
            log.info("[CategoryService] Cache MISS → calling RD Common Data API for categoryId={}, serviceId={}", categoryId, serviceId);
            var result = listOfValuesApi.findCategoryByCategoryIdAndServiceId(categoryId, serviceId, authToken,
                                                                              authTokenGenerator.generate());
            if (result == null) {
                log.warn("[CategoryService] API returned null for categoryId={}, serviceId={}", categoryId, serviceId);
            } else {
                try {
                    log.info("[CategoryService] API returned result for categoryId={}, serviceId={}: {}",
                             categoryId, serviceId, objectMapper.writeValueAsString(result));
                } catch (JsonProcessingException e) {
                    log.info("[CategoryService] API returned result for categoryId={}, serviceId={}: {}", categoryId, serviceId, result);
                }
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
