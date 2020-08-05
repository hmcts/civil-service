package uk.gov.hmcts.reform.unspec.service.bankholidays;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "bank-holidays-api", url = "${bankHolidays.api.url}")
public interface BankHolidaysApi {

    @GetMapping(path = "/bank-holidays.json")
    BankHolidays retrieveAll();
}
