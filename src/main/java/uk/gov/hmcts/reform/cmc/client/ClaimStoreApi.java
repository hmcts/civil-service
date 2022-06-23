package uk.gov.hmcts.reform.cmc.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "cmc-claim-store", url = "${cmc-claim-store.api.url}")
public interface ClaimStoreApi {

    @GetMapping("/claims/claimant/{submitterId}")
    List<CmcClaim> getClaimsForClaimant(@RequestHeader(AUTHORIZATION) String authorisation,
                                        @PathVariable String submitterId);

    @GetMapping("/claims/defendant/{submitterId}")
    List<CmcClaim> getClaimsForDefendant(@RequestHeader(AUTHORIZATION) String authorisation,
                                         @PathVariable String submitterId);
}
