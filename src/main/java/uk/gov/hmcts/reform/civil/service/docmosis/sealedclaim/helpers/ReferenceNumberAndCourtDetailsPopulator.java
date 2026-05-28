package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.SealedClaimResponseFormForSpec;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;

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

        String requestedCourt = null;

        if (caseData.getRespondent1DQ().getRespondent1DQRequestedCourt() != null && !isRespondent2(caseData)) {
            requestedCourt = caseData.getRespondent1DQ().getRespondent1DQRequestedCourt().getCaseLocation().getBaseLocation();
        } else if (caseData.getRespondent2DQ().getRespondent2DQRequestedCourt() != null && isRespondent2(caseData)) {
            requestedCourt = caseData.getRespondent2DQ().getRespondent2DQRequestedCourt().getCaseLocation().getBaseLocation();
        }

        String caseServiceId = getCaseServiceId(caseData.getCaseAccessCategory());

        List<LocationRefData> courtLocations = (locationRefDataService
            .getCourtLocationsByEpimmsId(
                authorisation,
                requestedCourt,
                caseServiceId));

        Optional<LocationRefData> optionalCourtLocation = courtLocations.stream()
            .filter(id -> caseServiceId.equals(id.getServiceId()))
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

    private boolean isRespondent2(CaseData caseData) {
        return (caseData.getRespondent2ResponseDate() != null)
            && (caseData.getRespondent1ResponseDate() == null
            || caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate()));
    }
}
