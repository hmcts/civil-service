package uk.gov.hmcts.reform.civil.service.claimstore;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.cmc.client.ClaimStoreApi;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimStoreService {

    private final ClaimStoreApi claimStoreApi;

    public List<DashboardClaimInfo> getClaimsForClaimant(String authorisation, String claimantId) {
        return translateCmcClaimToClaimInfo(claimStoreApi.getClaimsForClaimant(authorisation, claimantId));
    }

    public List<DashboardClaimInfo> getClaimsForDefendant(String authorisation, String defendantId) {
        return translateCmcClaimToClaimInfo(claimStoreApi.getClaimsForDefendant(authorisation, defendantId));
    }

    private List<DashboardClaimInfo> translateCmcClaimToClaimInfo(List<CmcClaim> cmcClaims) {
        return cmcClaims.stream().map(cmcClaim -> DashboardClaimInfo.builder()
            .claimId(cmcClaim.getExternalId())
            .claimNumber(cmcClaim.getReferenceNumber())
            .claimantName(cmcClaim.getClaimantName())
            .defendantName(cmcClaim.getDefendantName())
            .responseDeadline(cmcClaim.getResponseDeadline())
            .claimAmount(cmcClaim.getTotalAmountTillToday())
            .build()
        ).collect(Collectors.toList());
    }
}
