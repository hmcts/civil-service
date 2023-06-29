package uk.gov.hmcts.reform.civil.service.citizenui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CcdDashboardClaimMatcher;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardClaimInfoService {

    private final ClaimStoreService claimStoreService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final DashboardClaimStatusFactory dashboardClaimStatusFactory;

    public List<DashboardClaimInfo> getClaimsForClaimant(String authorisation, String claimantId) {
        return claimStoreService.getClaimsForClaimant(authorisation, claimantId);
    }

    public List<DashboardClaimInfo> getClaimsForDefendant(String authorisation, String defendantId) {
        log.info("-----------getClaimsForDefendant() started-------------");
        log.info("-----------calling ocmc getClaimsForDefendant()-------------");
        List<DashboardClaimInfo> ocmcClaims = claimStoreService.getClaimsForDefendant(authorisation, defendantId);
        log.info("-----------ocmcClaims received-------------size " + ocmcClaims.size());
        log.info("-----------calling ccd getCases-------------");
        List<DashboardClaimInfo> ccdCases = getCases(authorisation);
        log.info("-----------ccdCases received-------------size " + ccdCases.size());
        return Stream.concat(ocmcClaims.stream(), ccdCases.stream())
            .sorted(Comparator.comparing(DashboardClaimInfo::getCreatedDate).reversed())
            .collect(Collectors.toList());
    }

    private List<DashboardClaimInfo> getCases(String authorisation) {
        SearchResult claims = coreCaseDataService.getCasesUptoMaxsize(authorisation);
        log.info("-----------ccdCases received-------------total " + claims.getTotal());
        return translateSearchResultToDashboardItems(claims);
    }

    private List<DashboardClaimInfo> translateSearchResultToDashboardItems(SearchResult claims) {
        return claims.getCases().stream().map(caseDetails -> translateCaseDataToDashboardClaimInfo(caseDetails))
            .collect(Collectors.toList());
    }

    private DashboardClaimInfo translateCaseDataToDashboardClaimInfo(CaseDetails caseDetails) {
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        DashboardClaimInfo item = DashboardClaimInfo.builder().claimId(String.valueOf(caseData.getCcdCaseReference()))
            .createdDate(submittedDateToCreatedDate(caseData))
            .claimNumber(caseData.getLegacyCaseReference())
            .claimantName(nonNull(caseData.getApplicant1()) ? caseData.getApplicant1().getPartyName() : null)
            .defendantName(nonNull(caseData.getRespondent1()) ? caseData.getRespondent1().getPartyName() : null)
            .claimAmount(nonNull(caseData.getTotalClaimAmount()) ? caseData.getTotalClaimAmount() : null)
            .status(dashboardClaimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(caseData)))
            .build();
        if (caseData.getRespondent1ResponseDeadline() != null) {
            item.setResponseDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate());
        }
        if (caseData.getRespondToClaimAdmitPartLRspec() != null) {
            item.setPaymentDate(caseData.getDateForRepayment());
        }

        if (caseData.getRespondToAdmittedClaimOwingAmountPounds() != null) {
            item.setRespondToAdmittedClaimOwingAmountPounds(caseData.getRespondToAdmittedClaimOwingAmountPounds());
        }

        return item;
    }

    private LocalDateTime submittedDateToCreatedDate(CaseData caseData) {
        LocalDateTime createdDate = LocalDateTime.now();
        if (!Objects.isNull(caseData.getSubmittedDate())) {
            createdDate = caseData.getSubmittedDate();
        }

        return createdDate;
    }
}
