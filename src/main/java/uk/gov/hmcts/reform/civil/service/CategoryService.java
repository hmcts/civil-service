package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.crd.client.ListOfValuesApi;
import uk.gov.hmcts.reform.crd.model.CategorySearchResult;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final ListOfValuesApi listOfValuesApi;
    private final AuthTokenGenerator authTokenGenerator;

    public Optional<CategorySearchResult> findCategoryByCategoryIdAndServiceId(String authToken, String categoryId, String serviceId) {
        try {
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
