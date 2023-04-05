package uk.gov.hmcts.reform.civil.service.hearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.ManageCaseBaseUrlConfiguration;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

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
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingValuesService {

    private final PaymentsConfiguration paymentsConfiguration;
    private final ManageCaseBaseUrlConfiguration manageCaseBaseUrlConfiguration;
    private final CaseCategoriesService caseCategoriesService;
    private final CoreCaseDataService caseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final OrganisationService organisationService;
    private final DeadlinesCalculator deadlinesCalculator;

    public ServiceHearingValuesModel getValues(Long caseId, String hearingId, String authToken) {
        CaseData caseData = retrieveCaseData(caseId);

        String baseUrl = manageCaseBaseUrlConfiguration.getManageCaseBaseUrl();

        return ServiceHearingValuesModel.builder()
            .hmctsServiceID(getHmctsServiceID(caseData, paymentsConfiguration))
            .hmctsInternalCaseName(getHmctsInternalCaseName(caseData))
            .publicCaseName(getPublicCaseName(caseData)) //todo civ-7030
            .caseAdditionalSecurityFlag(getCaseAdditionalSecurityFlag(caseData))
            .caseCategories(getCaseCategories(caseData, caseCategoriesService, authToken))
            .caseDeepLink(getCaseDeepLink(caseId, baseUrl))
            .caseRestrictedFlag(getCaseRestrictedFlag())
            .externalCaseReference(getExternalCaseReference())
            .caseManagementLocationCode(getCaseManagementLocationCode(caseData))
            .caseSLAStartDate(getCaseSLAStartDate(deadlinesCalculator.getSlaStartDate(caseData)))
            .autoListFlag(getAutoListFlag())
            .hearingType(getHearingType())
            .hearingWindow(getHearingWindow())
            .duration(getDuration())
            .hearingPriorityType(getHearingPriorityType())
            .numberOfPhysicalAttendees(getNumberOfPhysicalAttendees())
            .hearingInWelshFlag(getHearingInWelshFlag())
            .hearingLocations(getHearingLocations(caseData))
            .facilitiesRequired(getFacilitiesRequired(caseData)) // todo civ-6888
            .listingComments(getListingComments(caseData)) // todo CIV-6855
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
            .hearingChannels(getHearingChannels(caseData)) //todo civ-6261
            .caseFlags(getCaseFlags(caseData)) // todo civ-7029 for party id
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
}
