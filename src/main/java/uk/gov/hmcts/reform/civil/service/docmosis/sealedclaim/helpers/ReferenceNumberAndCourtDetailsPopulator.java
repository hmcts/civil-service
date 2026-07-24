package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.utils.CaseServiceUtil.getCaseServiceId;

@Component
public class ReferenceNumberAndCourtDetailsPopulator {

    private final LocationReferenceDataService locationRefDataService;

    public ReferenceNumberAndCourtDetailsPopulator(LocationReferenceDataService locationRefDataService) {
        this.locationRefDataService = locationRefDataService;
    }

    public void populateReferenceNumberDetails(SealedClaimResponseFormForSpec form, CaseData caseData,
                                               String authorisation) {

        Optional<String> requestedCourt = getRequestedCourt(caseData);

        String caseServiceId = getCaseServiceId(caseData.getCaseAccessCategory());

        List<LocationRefData> courtLocations = requestedCourt
            .map(epimmsId -> locationRefDataService.getCourtLocationsByEpimmsId(authorisation, epimmsId, caseServiceId))
            .orElseGet(Collections::emptyList);

        Optional<LocationRefData> optionalCourtLocation = courtLocations.stream()
            .findFirst();

        String hearingCourtLocation = optionalCourtLocation
            .map(LocationRefData::getCourtName)
            .orElse(null);

        form.setReferenceNumber(caseData.getLegacyCaseReference())
            .setCcdCaseReference(Optional.ofNullable(caseData.getCcdCaseReference()).map(String::valueOf).orElse(""))
            .setCaseName(DocmosisTemplateDataUtils.toCaseName.apply(caseData))
            .setWhyDisputeTheClaim(isRespondent2(caseData) ? caseData.getDetailsOfWhyDoesYouDisputeTheClaim2() : caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .setHearingCourtLocation(hearingCourtLocation);
    }

    private Optional<String> getRequestedCourt(CaseData caseData) {
        if (!isRespondent2(caseData)) {
            return Optional.ofNullable(caseData.getRespondent1DQ())
                .map(Respondent1DQ::getRespondent1DQRequestedCourt)
                .map(RequestedCourt::getCaseLocation)
                .map(CaseLocationCivil::getBaseLocation);
        }

        return Optional.ofNullable(caseData.getRespondent2DQ())
            .map(Respondent2DQ::getRespondent2DQRequestedCourt)
            .map(RequestedCourt::getCaseLocation)
            .map(CaseLocationCivil::getBaseLocation);
    }

    private boolean isRespondent2(CaseData caseData) {
        return (caseData.getRespondent2ResponseDate() != null)
            && (caseData.getRespondent1ResponseDate() == null
            || caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()));
    }
}
