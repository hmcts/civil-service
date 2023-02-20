package uk.gov.hmcts.reform.crd.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.crd.model.Category;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "rd-commondata-api", url = "${rd_commondata.api.url}")
public interface ListOfValuesApi {

    @GetMapping("refdata/commondata/lov/categories/{category-id}?serviceId={service-id}")
    Category findCategoryByCategoryIdAndServiceId(
        @PathVariable("category-id") String categoryId,
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam("service-id") String serviceId
    );
}
