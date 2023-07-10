package uk.gov.hmcts.reform.civil.service.hearings;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.ManageCaseBaseUrlConfiguration;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.exceptions.PartyIdsUpdatedException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_PARTY_IDS;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsMapper.getCaseFlags;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsToHearingValueMapper.hasCaseInterpreterRequiredFlag;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getDuration;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getFacilitiesRequired;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getHearingChannels;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getHearingInWelshFlag;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getHearingIsLinkedFlag;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getHearingLocations;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getHearingPriorityType;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getHearingRequester;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getHearingType;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getHearingWindow;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getJudiciary;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getLeadJudgeContractType;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getListingComments;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getNumberOfPhysicalAttendees;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getPanelRequirements;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getPrivateHearingRequiredFlag;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingsPartyMapper.buildPartyObjectForHearingPayload;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ScreenFlowMapper.getScreenFlow;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ServiceHearingsCaseLevelMapper.getAutoListFlag;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ServiceHearingsCaseLevelMapper.getCaseAdditionalSecurityFlag;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ServiceHearingsCaseLevelMapper.getCaseCategories;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ServiceHearingsCaseLevelMapper.getCaseDeepLink;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ServiceHearingsCaseLevelMapper.getCaseManagementLocationCode;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ServiceHearingsCaseLevelMapper.getCaseRestrictedFlag;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ServiceHearingsCaseLevelMapper.getCaseSLAStartDate;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ServiceHearingsCaseLevelMapper.getExternalCaseReference;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ServiceHearingsCaseLevelMapper.getHmctsInternalCaseName;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ServiceHearingsCaseLevelMapper.getPublicCaseName;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.VocabularyMapper.getVocabulary;
import static uk.gov.hmcts.reform.civil.utils.HmctsServiceIDUtils.getHmctsServiceID;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateWithPartyIds;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingValuesService {

    private final PaymentsConfiguration paymentsConfiguration;
    private final ManageCaseBaseUrlConfiguration manageCaseBaseUrlConfiguration;
    private final CategoryService categoryService;
    private final CaseCategoriesService caseCategoriesService;
    private final CoreCaseDataService caseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final OrganisationService organisationService;
    private final ObjectMapper mapper;

    public ServiceHearingValuesModel getValues(Long caseId, String hearingId, String authToken) throws Exception {
        CaseData caseData = retrieveCaseData(caseId);
        populateMissingPartyIds(caseId, caseData);

        String baseUrl = manageCaseBaseUrlConfiguration.getManageCaseBaseUrl();
        String hmctsServiceID = getHmctsServiceID(caseData, paymentsConfiguration);

        return ServiceHearingValuesModel.builder()
            .hmctsServiceID(hmctsServiceID)
            .hmctsInternalCaseName(getHmctsInternalCaseName(caseData))
            .publicCaseName(getPublicCaseName(caseData)) //todo civ-7030
            .caseAdditionalSecurityFlag(getCaseAdditionalSecurityFlag(caseData))
            .caseCategories(getCaseCategories(caseData, caseCategoriesService, authToken))
            .caseDeepLink(getCaseDeepLink(caseId, baseUrl))
            .caseRestrictedFlag(getCaseRestrictedFlag())
            .externalCaseReference(getExternalCaseReference())
            .caseManagementLocationCode(getCaseManagementLocationCode(caseData))
            .caseSLAStartDate(getCaseSLAStartDate(caseData))
            .autoListFlag(getAutoListFlag())
            .hearingType(getHearingType())
            .hearingWindow(getHearingWindow())
            .duration(getDuration())
            .hearingPriorityType(getHearingPriorityType())
            .numberOfPhysicalAttendees(getNumberOfPhysicalAttendees())
            .hearingInWelshFlag(getHearingInWelshFlag(caseData))
            .hearingLocations(getHearingLocations(caseData))
            .facilitiesRequired(getFacilitiesRequired(caseData)) // todo civ-6888
            .listingComments(getListingComments(caseData))
            .hearingRequester(getHearingRequester())
            .privateHearingRequiredFlag(getPrivateHearingRequiredFlag())
            .caseInterpreterRequiredFlag(hasCaseInterpreterRequiredFlag(caseData))
            .panelRequirements(getPanelRequirements())
            .leadJudgeContractType(getLeadJudgeContractType())
            .judiciary(getJudiciary())
            .hearingIsLinkedFlag(getHearingIsLinkedFlag())
            .parties(buildPartyObjectForHearingPayload(caseData, organisationService)) //todo civ-7690
            .screenFlow(getScreenFlow())
            .vocabulary(getVocabulary())
            .hearingChannels(getHearingChannels(authToken, hmctsServiceID, caseData, categoryService))
            .caseFlags(getCaseFlags(caseData)) // todo civ-7690 for party id
            .build();
    }

    private CaseData retrieveCaseData(long caseId) {
        try {
            return caseDetailsConverter.toCaseData(caseDataService.getCase(caseId).getData());
        } catch (Exception ex) {
            log.error(String.format("No case found for %d", caseId));
            throw new CaseNotFoundException();
        }
    }

    /**
     * Tactical solution to updated partyIds if they do not already exist.
     * The partyIds within applicant1 field is checked as that is the very first party field
     * that gets populated during claim creation. If no partyIds exist it's safe to assume there
     * are missing partyIds to populate.
     *
     * @param caseData given case data.
     * @throws PartyIdsUpdatedException If party ids have been updated, to force the consumer to request
     *                                  the hearing values endpoint again.
     * @throws FeignException If an error is returned from case data service when triggering the event.
     */
    private void populateMissingPartyIds(Long caseId, CaseData caseData) throws Exception {
        if (caseData.getApplicant1().getPartyID() == null) {
            var builder = caseData.toBuilder();
            // Even if party ids creation is released and cases are
            // in an inconsistent state where app/res fields have no party ids
            // and litfriends, witnesses and experts do it's still safe to call populateWithPartyFlags
            // as it was created to not overwrite partyId fields if they exist.
            populateWithPartyIds(builder);

            try {
                caseDataService.triggerEvent(
                    caseId, UPDATE_PARTY_IDS, builder.build().toMap(mapper));
            } catch (FeignException e) {
                log.error("Updating case data with party ids failed: {}", e);
                throw e;
            }

            throw new PartyIdsUpdatedException();
        }
    }
}
