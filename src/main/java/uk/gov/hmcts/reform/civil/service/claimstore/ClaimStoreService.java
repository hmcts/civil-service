package uk.gov.hmcts.reform.civil.service.claimstore;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cmc.client.ClaimStoreApi;
import uk.gov.hmcts.reform.cmc.model.ClaimInfo;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimStoreService {
    private final ClaimStoreApi claimStoreApi;

    public List<ClaimInfo> getClaimsForClaimant(String authorisation, String claimantId){
          List<CmcClaim> cmcClaims = claimStoreApi.getClaimsForClaimant(authorisation, claimantId);
          return cmcClaims.stream().map(cmcClaim -> ClaimInfo.builder()
              .claimNumber(cmcClaim.getReferenceNumber())
              .claimantName(cmcClaim.getClaimantName())
              .defendantName(cmcClaim.getDefendantName())
              .responseDeadLine(cmcClaim.getReferenceNumber())
              .build()
          ).collect(Collectors.toList());
    }
}
