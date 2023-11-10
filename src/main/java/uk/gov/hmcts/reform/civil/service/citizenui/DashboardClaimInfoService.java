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
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardResponse;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardClaimInfoService {

    private static final int CASES_PER_PAGE = 10;

    private final CaseDetailsConverter caseDetailsConverter;
    private final ClaimStoreService claimStoreService;
    private final CoreCaseDataService coreCaseDataService;
    private final DashboardClaimStatusFactory dashboardClaimStatusFactory;
    private final FeatureToggleService featureToggleService;

    public List<DashboardClaimInfo> getOcmcDefendantClaims(String authorisation, String defendantId) {
        log.info("-----------calling ocmc getOCMCDefendantClaims()-------------");
        List<DashboardClaimInfo> ocmcClaims = claimStoreService.getClaimsForDefendant(authorisation, defendantId);
        log.info("-----------ocmcClaims received-------------size " + ocmcClaims.size());
        return ocmcClaims;
    }

    public DashboardResponse getDashboardDefendantResponse(String authorisation, String defendantId,
                                                           int currentPage) {
        log.info("-----------Claims for Defendant started-------------");
        List<DashboardClaimInfo> ocmcClaims = getOcmcDefendantClaims(authorisation, defendantId);
        int startIndex = (currentPage - 1) * CASES_PER_PAGE;
        var ccdData = coreCaseDataService.getCCDClaimsForLipDefendant(authorisation, startIndex);
        int totalPages = getTotalPagesToBeListed(ccdData.getTotal() + ocmcClaims.size());
        List<DashboardClaimInfo> currentPageItems = currentPage <= totalPages
            ? getDashboardItemsForCurrentPage(ocmcClaims, currentPage, ccdData) :
            Collections.emptyList();
        return DashboardResponse.builder().totalPages(totalPages).claims(currentPageItems).build();
    }

    public DashboardResponse getDashboardClaimantResponse(String authorisation, String claimantId, int currentPage) {
        List<DashboardClaimInfo> ocmcClaims = getClaimsForClaimant(authorisation, claimantId);

        int startIndex = (currentPage - 1) * CASES_PER_PAGE;
        SearchResult ccdData = null;

        if (featureToggleService.isLipVLipEnabled()) {
            ccdData = coreCaseDataService.getCCDClaimsForLipClaimant(authorisation, startIndex);
        }

        int totalPages = getTotalPagesToBeListed(getCcdClaimsCount(ccdData) + ocmcClaims.size());
        List<DashboardClaimInfo> currentPageItems = currentPage <= totalPages
            ? getDashboardItemsForCurrentPage(ocmcClaims, currentPage, ccdData) :
            Collections.emptyList();
        return DashboardResponse.builder().totalPages(totalPages).claims(currentPageItems).build();
    }

    private List<DashboardClaimInfo> getClaimsForClaimant(String authorisation, String claimantId) {
        log.info("-----------calling ocmc claimant claims-------------");
        List<DashboardClaimInfo> ocmcClaims = claimStoreService.getClaimsForClaimant(authorisation, claimantId);
        return ocmcClaims;
    }

    private List<DashboardClaimInfo> getDashboardItemsForCurrentPage(List<DashboardClaimInfo> ocmcClaims,
                                                                     int currentPage,
                                                                     SearchResult ccdClaims) {

        int startIndex = (currentPage - 1) * CASES_PER_PAGE;
        int endIndex = startIndex + CASES_PER_PAGE;
        int ccdClaimsCount = getCcdClaimsCount(ccdClaims);

        List<DashboardClaimInfo> dashBoardClaimInfo = new ArrayList<>();
        if (startIndex >= ccdClaimsCount) {
            int ocmcStartIndex = startIndex - ccdClaimsCount;
            int end = Math.min(currentPage * CASES_PER_PAGE, ocmcClaims.size());
            dashBoardClaimInfo.addAll(sortOcmcCases(ocmcClaims.subList(ocmcStartIndex, end)));
        } else {
            var ccdData = translateSearchResultToDashboardItems(ccdClaims);
            dashBoardClaimInfo.addAll(ccdData);
            if (ccdData.size() < CASES_PER_PAGE && endIndex > ccdClaimsCount) {
                int remainingRecords = CASES_PER_PAGE - ccdData.size();
                int end = Math.min(remainingRecords, ocmcClaims.size());
                dashBoardClaimInfo.addAll(sortOcmcCases(ocmcClaims.subList(0, end)));

            }
        }
        return dashBoardClaimInfo;
    }

    private List<DashboardClaimInfo> sortOcmcCases(List<DashboardClaimInfo> ocmcCases) {
        return ocmcCases.stream()
            .sorted(Comparator.comparing(DashboardClaimInfo::getCreatedDate).reversed())
            .collect(Collectors.toList());
    }

    private List<DashboardClaimInfo> translateSearchResultToDashboardItems(SearchResult claims) {
        if (claims == null) {
            return Collections.emptyList();
        }

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
            .admittedAmount(caseData.getPartAdmitPaidValuePounds())
            .responseDeadlineTime(caseData.getRespondent1ResponseDeadline())
            .status(dashboardClaimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimMatcher(caseData, featureToggleService)))
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
        if (Objects.nonNull(caseData.getSubmittedDate())) {
            createdDate = caseData.getSubmittedDate();
        }

        return createdDate;
    }

    private int getTotalPagesToBeListed(int totalClaims) {
        int totalPages = 1;
        if (totalClaims > CASES_PER_PAGE) {
            totalPages = (int) Math.ceil(totalClaims / (double) CASES_PER_PAGE);
        }
        return totalPages;
    }

    private int getCcdClaimsCount(final SearchResult ccdClaims) {

        return Optional.ofNullable(ccdClaims).map(SearchResult::getTotal).orElse(0);
    }
}
