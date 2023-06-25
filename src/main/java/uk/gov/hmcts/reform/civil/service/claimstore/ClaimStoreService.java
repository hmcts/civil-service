package uk.gov.hmcts.reform.civil.service.claimstore;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;
import uk.gov.hmcts.reform.cmc.client.ClaimStoreApi;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;
import uk.gov.hmcts.reform.cmc.model.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimStoreService {

    private final ClaimStoreApi claimStoreApi;
    private final DashboardClaimStatusFactory dashboardClaimStatusFactory;

    public List<DashboardClaimInfo> getClaimsForClaimant(String authorisation, String claimantId) {
        return translateCmcClaimToClaimInfo(claimStoreApi.getClaimsForClaimant(authorisation, claimantId));
    }

    public List<DashboardClaimInfo> getClaimsForDefendant(String authorisation, String defendantId) {
        return translateCmcClaimToClaimInfo(claimStoreApi.getClaimsForDefendant(authorisation, defendantId));
    }

    private List<DashboardClaimInfo> translateCmcClaimToClaimInfo(List<CmcClaim> cmcClaims) {

        return cmcClaims.stream().map(cmcClaim ->
                                          DashboardClaimInfo.builder()
                                              .createdDate(createAtToCreateDate(cmcClaim))
                                              .claimId(cmcClaim.getExternalId())
                                              .claimNumber(cmcClaim.getReferenceNumber())
                                              .claimantName(cmcClaim.getClaimantName())
                                              .defendantName(cmcClaim.getDefendantName())
                                              .responseDeadline(cmcClaim.getResponseDeadline())
                                              .claimAmount(cmcClaim.getTotalAmountTillToday())
                                              .paymentDate(cmcClaim.getBySpecifiedDate())
                                              .ccjRequestedDate(cmcClaim.getCountyCourtJudgmentRequestedAt())
                                              .ocmc(true)
                                              .admittedAmount(cmcClaim.getAdmittedAmount())
                                              .respondToAdmittedClaimOwingAmountPounds(Optional.ofNullable(cmcClaim.getResponse())
                                                                                           .map(Response::getAmount)
                                                                                           .orElse(null))
                                              .status(dashboardClaimStatusFactory.getDashboardClaimStatus(cmcClaim))
                                              .build()
        ).collect(Collectors.toList());
    }

    private LocalDateTime createAtToCreateDate(CmcClaim claim) {
        LocalDateTime createdDate = LocalDateTime.now();

        if (claim.getCreatedAt() != null) {
            createdDate = claim.getCreatedAt();
        }

        return createdDate;
    }
}
