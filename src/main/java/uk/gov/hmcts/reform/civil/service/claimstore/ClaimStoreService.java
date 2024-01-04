package uk.gov.hmcts.reform.civil.service.claimstore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;
import uk.gov.hmcts.reform.cmc.client.ClaimStoreApi;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;
import uk.gov.hmcts.reform.cmc.model.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<DashboardClaimInfo> getClaimsForDefendant(String authorisation, String defendantId) {
        try {
            return translateCmcClaimToClaimInfo(claimStoreApi.getClaimsForDefendant(authorisation, defendantId));
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

 private List<DashboardClaimInfo> translateCmcClaimToClaimInfo(List<CmcClaim> cmcClaims) {
        return cmcClaims.stream().map(cmcClaim ->
                                      {
                                          System.out.println(
                                              "====================CUI DATA======================================");
                                          System.out.println("Claim Number " + cmcClaim.getReferenceNumber());
                                          DashboardClaimInfo dashboardClaimInfo = DashboardClaimInfo.builder()
                                              .createdDate(createAtToCreateDate(cmcClaim))
                                              .claimId(cmcClaim.getExternalId())
                                              .claimNumber(cmcClaim.getReferenceNumber())
                                              .claimantName(cmcClaim.getClaimantName())
                                              .defendantName(cmcClaim.getDefendantName())
                                              .responseDeadline(cmcClaim.getResponseDeadline())
                                              .responseDeadlineTime(Optional.ofNullable(cmcClaim.getResponseDeadline()).map(
                                                  LocalDate::atStartOfDay).orElse(null)
                                              )
                                              .claimAmount(cmcClaim.getTotalAmountTillToday())
                                              .paymentDate(cmcClaim.getBySpecifiedDate())
                                              .ccjRequestedDate(cmcClaim.getCountyCourtJudgmentRequestedAt())
                                              .ocmc(true)
                                              .admittedAmount(cmcClaim.getAdmittedAmount())
                                              .respondToAdmittedClaimOwingAmountPounds(Optional.ofNullable(cmcClaim.getResponse())
                                                                                           .map(Response::getAmount)
                                                                                           .orElse(null))
                                              .status(dashboardClaimStatusFactory.getDashboardClaimStatus(cmcClaim))
                                              .build();
                                          System.out.println("Claim Dashboard Status" + dashboardClaimInfo.getStatus());
                                          System.out.println(
                                              "====================END CUI DATA======================================");
                                          return dashboardClaimInfo;
                                      }
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
