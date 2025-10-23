package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.ga.service.GeneralAppLocationRefDataService;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.ga.service.docmosis.DocumentGeneratorService.DATE_FORMATTER;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class DocmosisService {

    private final GeneralAppLocationRefDataService generalAppLocationRefDataService;
    @Value("${court-location.specified-claim.epimms-id}")
    private String cnbcEpimmId;

    public LocationRefData getCaseManagementLocationVenueName(GeneralApplicationCaseData caseData, String authorisation) {
        List<LocationRefData> courtLocations = null;
        Boolean cnbcCourt = checkIfCnbc(caseData);
        if (cnbcCourt) {
            courtLocations = generalAppLocationRefDataService.getCnbcLocation(authorisation);
        } else {
            courtLocations = generalAppLocationRefDataService.getCourtLocations(authorisation);
        }
        assert courtLocations != null;
        var caseLocation = caseData.getCaseManagementLocation();
        var matchingLocations =
            courtLocations
                .stream()
                .filter(location -> caseLocation != null
                    && location.getEpimmsId().equals(caseLocation.getBaseLocation()))
                .toList();

        if (!matchingLocations.isEmpty()) {
            return matchingLocations.get(0);
        } else {
            throw new IllegalArgumentException("Court Name is not found in location data");
        }
    }

    public YesOrNo reasonAvailable(GeneralApplicationCaseData caseData) {
        if (Objects.nonNull(caseData.getJudicialDecisionMakeOrder().getShowReasonForDecision())
            && caseData.getJudicialDecisionMakeOrder().getShowReasonForDecision().equals(YesOrNo.NO)) {
            return YesOrNo.NO;
        }
        return YesOrNo.YES;
    }

    public String populateJudgeReason(GeneralApplicationCaseData caseData) {
        if (Objects.nonNull(caseData.getJudicialDecisionMakeOrder().getShowReasonForDecision())
            && caseData.getJudicialDecisionMakeOrder().getShowReasonForDecision().equals(YesOrNo.NO)) {
            return "";
        }
        return caseData.getJudicialDecisionMakeOrder().getReasonForDecisionText() != null
            ? caseData.getJudicialDecisionMakeOrder().getReasonForDecisionText()
            : "";
    }

    public String populateJudicialByCourtsInitiative(GeneralApplicationCaseData caseData) {

        if (caseData.getJudicialDecisionMakeOrder().getJudicialByCourtsInitiative().equals(GAByCourtsInitiativeGAspec
                                                                                               .OPTION_3)) {
            return StringUtils.EMPTY;
        }

        if (caseData.getJudicialDecisionMakeOrder().getJudicialByCourtsInitiative()
            .equals(GAByCourtsInitiativeGAspec.OPTION_1)) {
            return caseData.getJudicialDecisionMakeOrder().getOrderCourtOwnInitiative() + " "
                .concat(caseData.getJudicialDecisionMakeOrder().getOrderCourtOwnInitiativeDate()
                            .format(DATE_FORMATTER));
        } else {
            return caseData.getJudicialDecisionMakeOrder().getOrderWithoutNotice() + " "
                .concat(caseData.getJudicialDecisionMakeOrder().getOrderWithoutNoticeDate()
                            .format(DATE_FORMATTER));
        }
    }

    public Boolean checkIfCnbc(GeneralApplicationCaseData caseData) {
        var caseLocation = caseData.getCaseManagementLocation();
        return caseLocation != null && cnbcEpimmId.equals(caseLocation.getBaseLocation());
    }
}
