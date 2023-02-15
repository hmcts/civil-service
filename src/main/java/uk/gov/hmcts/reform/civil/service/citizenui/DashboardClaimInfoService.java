package uk.gov.hmcts.reform.civil.service.citizenui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CcdDashboardClaimMatcher;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;
import uk.gov.hmcts.reform.civil.model.search.Query;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

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
        List<DashboardClaimInfo> ocmcClaims = claimStoreService.getClaimsForDefendant(authorisation, defendantId);
        log.info(ocmcClaims.toString());
        List<DashboardClaimInfo> ccdCases = getCases(authorisation);
        return Stream.concat(ocmcClaims.stream(), ccdCases.stream()).collect(Collectors.toList());
    }

    private List<DashboardClaimInfo> getCases(String authorisation) {
        Query query = new Query(QueryBuilders.matchAllQuery(), emptyList(), 0);
        SearchResult claims = coreCaseDataService.searchCases(query, authorisation);
        if (claims.getTotal() == 0) {
            log.info("no claims");
            return Collections.emptyList();
        }
        return translateSearchResultToDashboardItems(claims);
    }

    private List<DashboardClaimInfo> translateSearchResultToDashboardItems(SearchResult claims) {
        return claims.getCases().stream().map(caseDetails -> translateCaseDataToDashboardClaimInfo(caseDetails))
            .collect(Collectors.toList());
    }

    private DashboardClaimInfo translateCaseDataToDashboardClaimInfo(CaseDetails caseDetails) {
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);

        DashboardClaimInfo item = DashboardClaimInfo.builder().claimId(String.valueOf(caseData.getCcdCaseReference()))
            .claimNumber(caseData.getLegacyCaseReference())
            .claimantName(caseData.getApplicant1().getPartyName())
            .defendantName(caseData.getRespondent1().getPartyName())
            .claimAmount(caseData.getTotalClaimAmount())
            .status(dashboardClaimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(caseData)))
            .build();
        if (caseData.getRespondent1ResponseDeadline() != null) {
            item.setResponseDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate());
        }
        if(caseData.getRespondToClaimAdmitPartLRspec() != null) {
            item.setPaymentDate(caseData.getDateForRepayment());
        }
        log.info(item.toString());
        return item;
    }
}
