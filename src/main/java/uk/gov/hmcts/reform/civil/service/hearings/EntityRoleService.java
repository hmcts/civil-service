package uk.gov.hmcts.reform.civil.service.hearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.utils.HmctsServiceIDUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntityRoleService {

    private final CategoryService categoryService;
    private final PaymentsConfiguration paymentsConfiguration;

    private static final String CATEGORY_ID = "EntityRoleCode";

    public List<String> getEntityRoleCode(CaseData caseData, String authToken) {
        String hmctsServiceID = HmctsServiceIDUtils.getHmctsServiceID(caseData, paymentsConfiguration);
        Optional<CategorySearchResult> result = categoryService.findCategoryByCategoryIdAndServiceId(
            authToken,
            CATEGORY_ID,
            hmctsServiceID
        );

        return result.map(r -> r.getCategories().stream().map(Category::getKey).collect(Collectors.toList())).orElse(null);
    }
}
