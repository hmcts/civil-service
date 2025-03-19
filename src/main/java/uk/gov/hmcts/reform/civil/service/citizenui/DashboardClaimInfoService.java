package uk.gov.hmcts.reform.civil.service.citizenui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CcdDashboardClaimantClaimMatcher;
import uk.gov.hmcts.reform.civil.model.citizenui.CcdDashboardDefendantClaimMatcher;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatus;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardClaimStatusFactory;
import uk.gov.hmcts.reform.civil.model.citizenui.DashboardResponse;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.CoreCaseEventDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private final CoreCaseEventDataService eventDataService;

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
            ? getDashboardItemsForCurrentPage(ocmcClaims, currentPage, ccdData, false) :
            Collections.emptyList();
        return DashboardResponse.builder().totalPages(totalPages).claims(currentPageItems).build();
    }

    public DashboardResponse getDashboardClaimantResponse(String authorisation, String claimantId, int currentPage) {
        List<DashboardClaimInfo> ocmcClaims = getClaimsForClaimant(authorisation, claimantId);

        int startIndex = (currentPage - 1) * CASES_PER_PAGE;
        SearchResult ccdData = null;

        ccdData = coreCaseDataService.getCCDClaimsForLipClaimant(authorisation, startIndex);

        int totalPages = getTotalPagesToBeListed(getCcdClaimsCount(ccdData) + ocmcClaims.size());
        List<DashboardClaimInfo> currentPageItems = currentPage <= totalPages
            ? getDashboardItemsForCurrentPage(ocmcClaims, currentPage, ccdData, true) :
            Collections.emptyList();
        return DashboardResponse.builder().totalPages(totalPages).claims(currentPageItems).build();
    }

    private List<DashboardClaimInfo> getClaimsForClaimant(String authorisation, String claimantId) {
        log.info("-----------calling ocmc claimant claims-------------");
        return claimStoreService.getClaimsForClaimant(authorisation, claimantId);
    }

    private List<DashboardClaimInfo> getDashboardItemsForCurrentPage(List<DashboardClaimInfo> ocmcClaims,
                                                                     int currentPage,
                                                                     SearchResult ccdClaims,
                                                                     boolean isClaimant) {

        int startIndex = (currentPage - 1) * CASES_PER_PAGE;
        int endIndex = startIndex + CASES_PER_PAGE;
        int ccdClaimsCount = getCcdClaimsCount(ccdClaims);

        List<DashboardClaimInfo> dashBoardClaimInfo = new ArrayList<>();
        if (startIndex >= ccdClaimsCount) {
            int ocmcStartIndex = startIndex - ccdClaimsCount;
            int end = Math.min(currentPage * CASES_PER_PAGE, ocmcClaims.size());
            dashBoardClaimInfo.addAll(sortOcmcCases(ocmcClaims.subList(ocmcStartIndex, end)));
        } else {
            var ccdData = translateSearchResultToDashboardItems(ccdClaims, isClaimant);
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
            .toList();
    }

    private List<DashboardClaimInfo> translateSearchResultToDashboardItems(SearchResult claims, boolean isClaimant) {
        if (claims == null) {
            return Collections.emptyList();
        }

        return claims.getCases().stream().map(caseDetails -> translateCaseDataToDashboardClaimInfo(
                caseDetails,
                isClaimant
            ))
            .toList();
    }

    private DashboardClaimInfo translateCaseDataToDashboardClaimInfo(CaseDetails caseDetails, boolean isClaimant) {
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetails);
        DashboardClaimInfo item = DashboardClaimInfo.builder().claimId(String.valueOf(caseData.getCcdCaseReference()))
            .createdDate(submittedDateToCreatedDate(caseData))
            .claimNumber(caseData.getLegacyCaseReference())
            .claimantName(nonNull(caseData.getApplicant1()) ? caseData.getApplicant1().getPartyName() : null)
            .defendantName(nonNull(caseData.getRespondent1()) ? caseData.getRespondent1().getPartyName() : null)
            .claimAmount(nonNull(caseData.getTotalClaimAmount()) ? caseData.getTotalClaimAmount() : null)
            .admittedAmount(caseData.getPartAdmitPaidValuePounds())
            .responseDeadlineTime(caseData.getRespondent1ResponseDeadline())
            .status(getStatus(isClaimant, caseData))
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

        if (caseData.hasApplicant1AcceptedCcj()) {
            item.setCcjRequestedDate(caseData.getApplicant1ResponseDate());
        }

        if (caseData.getActiveJudgment() != null) {
            item.setCcjRequestedDate(caseData.getActiveJudgment().getCreatedTimestamp());
        } else if (caseData.isCcjRequestJudgmentByAdmission() && caseData.hasApplicant1AcceptedCcj()) {
            item.setCcjRequestedDate(caseData.getApplicant1ResponseDate());
        } else {
            item.setCcjRequestedDate(caseData.getTakenOfflineDate());
        }

        if (caseData.getActiveJudgment() != null
            && caseData.getActiveJudgment().getType().equals(JudgmentType.DEFAULT_JUDGMENT)
            && caseData.getActiveJudgment().getIssueDate() != null) {
            item.setDefaultJudgementIssuedDate(caseData.getActiveJudgment().getIssueDate());
        } else if (caseData.getActiveJudgment() == null && caseData.getDefaultJudgmentDocuments() != null) {
            caseData.getDefaultJudgmentDocuments().stream()
                .map(el -> el.getValue())
                .filter(doc -> doc.getDocumentType().equals(DocumentType.DEFAULT_JUDGMENT))
                .map(doc -> doc.getCreatedDatetime().toLocalDate())
                .findFirst()
                .ifPresent(item::setDefaultJudgementIssuedDate);
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

    private DashboardClaimStatus getStatus(boolean isClaimant, CaseData caseData) {
        List<CaseEventDetail> events = Optional.ofNullable(caseData.getCcdCaseReference())
                .map(s -> eventDataService.getEventsForCase(s.toString()))
                    .orElse(Collections.emptyList());
        return isClaimant
            ? dashboardClaimStatusFactory.getDashboardClaimStatus(new CcdDashboardClaimantClaimMatcher(
            caseData,
            featureToggleService, events
        ))
            : dashboardClaimStatusFactory.getDashboardClaimStatus(new CcdDashboardDefendantClaimMatcher(
            caseData,
            featureToggleService, events
        ));
    }
}
