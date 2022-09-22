package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * choosing which preferred court should be used among those declared as preferred by the parties may not be
 * straightforward, especially when we consider that they may change (for instance, giving priority to claimant
 * vs giving priority to individuals instead of companies or organizations and so on).
 *
 * <p>Thus, this class is created to make explicit how the right preferred location should be selected.</p>
 */
@Component
public class RequestedCourtSelector {

    /**
     * Selects which should be the hearing court, if any, considering the information in caseData and
     * business rules.
     *
     * @param caseData the case's data
     * @return preferred hearing court, if any.
     */
    public Optional<RequestedCourt> getPreferredRequestedCourt(CaseData caseData) {
        return getClaimantFirst(caseData);
    }

    /**
     * Gives priority to claimant first, defendant second. If there are two, gives priority to first (of each)
     *
     * @param caseData case data
     * @return requested court (only with case location and court code) with at least one of the two fields
     *     filled in.
     */
    private Optional<RequestedCourt> getClaimantFirst(CaseData caseData) {
        return Stream.of(
                Optional.ofNullable(caseData.getCourtLocation())
                    .map(courtLocation -> RequestedCourt.builder()
                        .responseCourtCode(courtLocation.getApplicantPreferredCourt())
                        .caseLocation(courtLocation.getCaseLocation())
                        .build()),
                Optional.ofNullable(caseData.getApplicant1DQ()).map(Applicant1DQ::getApplicant1DQRequestedCourt),
                Optional.ofNullable(caseData.getApplicant2DQ()).map(Applicant2DQ::getApplicant2DQRequestedCourt),
                Optional.ofNullable(caseData.getRespondent1DQ()).map(Respondent1DQ::getRequestedCourt),
                Optional.ofNullable(caseData.getRespondent2DQ()).map(Respondent2DQ::getRequestedCourt)
            ).filter(Optional::isPresent)
            .map(Optional::get)
            .filter(requestedCourt -> requestedCourt.getCaseLocation() != null
                || StringUtils.isNotBlank(requestedCourt.getResponseCourtCode()))
            .findFirst();
    }
}
