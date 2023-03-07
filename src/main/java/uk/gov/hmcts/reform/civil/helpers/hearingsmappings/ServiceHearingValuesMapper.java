package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ServiceHearingValuesModel;

import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsMapper.getCaseFlags;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.getCaseInterpreterRequiredFlag;
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
import static uk.gov.hmcts.reform.civil.utils.HMCTSServiceIDUtils.getHmctsServiceID;

public class ServiceHearingValuesMapper {

    private static final String CASE_TYPE = "CIVIL";

    private ServiceHearingValuesMapper() {
        //NO-OP
    }

    public static ServiceHearingValuesModel createServiceHearingPayload(String caseReference,
                                                                        String hearingId,
                                                                        CaseData caseData) {
        return ServiceHearingValuesModel.builder()
            .hmctsServiceID(getHmctsServiceID(caseData))
            .hmctsInternalCaseName(getHmctsInternalCaseName(caseData))
            .publicCaseName(getPublicCaseName(caseData))
            .caseAdditionalSecurityFlag(getCaseAdditionalSecurityFlag())
            .caseCategories(getCaseCategories(caseData))
            .caseDeepLink(getCaseDeepLink(caseReference, caseData))
            .caseRestrictedFlag(getCaseRestrictedFlag())
            .externalCaseReference(getExternalCaseReference())
            .caseManagementLocationCode(getCaseManagementLocationCode(caseData))
            .caseSLAStartDate(getCaseSLAStartDate(caseData))
            .autoListFlag(getAutoListFlag())
            .hearingType(getHearingType())
            .hearingWindow(getHearingWindow())
            .duration(getDuration())
            .hearingPriorityType(getHearingPriorityType(caseData))
            .numberOfPhysicalAttendees(getNumberOfPhysicalAttendees())
            .hearingInWelshFlag(getHearingInWelshFlag())
            .hearingLocations(getHearingLocations(caseData))
            .facilitiesRequired(getFacilitiesRequired(caseData))
            .listingComments(getListingComments(caseData))
            .hearingRequester(getHearingRequester())
            .privateHearingRequiredFlag(getPrivateHearingRequiredFlag())
            .caseInterpreterRequiredFlag(getCaseInterpreterRequiredFlag())
            .panelRequirements(getPanelRequirements())
            .leadJudgeContractType(getLeadJudgeContractType())
            .judiciary(getJudiciary())
            .hearingIsLinkedFlag(getHearingIsLinkedFlag())
            .parties(null) //todo civ-
            .screenFlow(getScreenFlow())
            .vocabulary(getVocabulary())
            .hearingChannels(getHearingChannels(caseData)) //todo civ-6261
            .caseFlags(getCaseFlags(caseData))
            .build();
    }

}
