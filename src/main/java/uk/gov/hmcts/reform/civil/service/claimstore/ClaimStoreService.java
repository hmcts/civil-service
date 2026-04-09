package uk.gov.hmcts.reform.civil.service.claimstore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;
import uk.gov.hmcts.reform.cmc.client.ClaimStoreApi;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;
import uk.gov.hmcts.reform.cmc.model.DefendantLinkStatus;
import uk.gov.hmcts.reform.cmc.model.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimStoreService {

    private final ClaimStoreApi claimStoreApi;
    private final DashboardClaimStatusFactory dashboardClaimStatusFactory;

    public List<DashboardClaimInfo> getClaimsForClaimant(String authorisation, String claimantId) {
        try {
            return translateCmcClaimToClaimInfo(claimStoreApi.getClaimsForClaimant(authorisation, claimantId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<DashboardClaimInfo> getClaimsForDefendant(String authorisation, String defendantId) {
        try {
            return translateCmcClaimToClaimInfo(claimStoreApi.getClaimsForDefendant(authorisation, defendantId));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public DefendantLinkStatus isOcmcDefendantLinked(String caseReference) {
        try {
            return claimStoreApi.isDefendantLinked(caseReference);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new DefendantLinkStatus().setLinked(false);
        }
    }

    private List<DashboardClaimInfo> translateCmcClaimToClaimInfo(List<CmcClaim> cmcClaims) {
        return cmcClaims.stream().map(cmcClaim ->
                                          new DashboardClaimInfo()
                                              .setCreatedDate(createAtToCreateDate(cmcClaim))
                                              .setClaimId(cmcClaim.getExternalId())
                                              .setClaimNumber(cmcClaim.getReferenceNumber())
                                              .setClaimantName(cmcClaim.getClaimantName())
                                              .setDefendantName(cmcClaim.getDefendantName())
                                              .setResponseDeadline(cmcClaim.getResponseDeadline())
                                              .setResponseDeadlineTime(Optional.ofNullable(cmcClaim.getResponseDeadline()).map(
                                                                        LocalDate::atStartOfDay).orElse(null)
                                                  )
                                              .setClaimAmount(cmcClaim.getTotalAmountTillToday())
                                              .setPaymentDate(cmcClaim.getBySpecifiedDate())
                                              .setCcjRequestedDate(cmcClaim.getCountyCourtJudgmentRequestedAt())
                                              .setOcmc(true)
                                              .setAdmittedAmount(cmcClaim.getAdmittedAmount())
                                              .setRespondToAdmittedClaimOwingAmountPounds(Optional.ofNullable(cmcClaim.getResponse())
                                                                                           .map(Response::getAmount)
                                                                                           .orElse(null))
                                              .setStatus(dashboardClaimStatusFactory.getDashboardClaimStatus(cmcClaim))
        ).toList();
    }

    private LocalDateTime createAtToCreateDate(CmcClaim claim) {
        LocalDateTime createdDate = LocalDateTime.now();

        if (claim.getCreatedAt() != null) {
            createdDate = claim.getCreatedAt();
        }

        return createdDate;
    }
}
