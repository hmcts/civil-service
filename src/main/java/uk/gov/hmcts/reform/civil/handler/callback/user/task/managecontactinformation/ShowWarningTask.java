package uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.PartyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;

@Slf4j
@Component
public class ShowWarningTask {

    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;
    private final PartyValidator partyValidator;
    private final FeatureToggleService featureToggleService;
    private final PostcodeValidator postcodeValidator;
    private static final String CHECK_LITIGATION_FRIEND_ERROR_TITLE = "Check the litigation friend's details";
    private static final String CHECK_LITIGATION_FRIEND_ERROR = "After making these changes, please ensure that the "
        + "litigation friend's contact information is also up to date.";
    private static final String CHECK_LITIGATION_FRIEND_WARNING = "There is another litigation friend on this case. "
        + "If the parties are using the same litigation friend you must update the other litigation friend's details too.";

    public ShowWarningTask(CaseDetailsConverter caseDetailsConverter, ObjectMapper objectMapper,
                           PartyValidator partyValidator, FeatureToggleService featureToggleService,
                           PostcodeValidator postcodeValidator) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.objectMapper = objectMapper;
        this.partyValidator = partyValidator;
        this.featureToggleService = featureToggleService;
        this.postcodeValidator = postcodeValidator;
    }

    public CallbackResponse showWarning(CaseData caseData, CaseDetails caseDetailsBefore) {
        String partyChosen = caseData.getUpdateDetailsForm().getPartyChosen().getValue().getCode();
        ArrayList<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        CaseData oldCaseData = caseDetailsConverter.toCaseData(caseDetailsBefore);
        log.info("Show warning for case ID {}", caseData.getCcdCaseReference());

        // oldCaseData needed because Litigation friend gets nullified in mid event
        if (partyHasLitigationFriend(partyChosen, oldCaseData)) {
            warnings.add(CHECK_LITIGATION_FRIEND_ERROR_TITLE);
            warnings.add(CHECK_LITIGATION_FRIEND_ERROR);
        }

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            errors = postcodeValidator.validate(getPostCode(partyChosen, caseData));
            if (featureToggleService.isJudgmentOnlineLive()) {
                Party partyDetails = getPartyDetails(partyChosen, caseData);
                if (partyDetails != null  && partyDetails.getPrimaryAddress() != null) {
                    errors = partyValidator.validateAddress(partyDetails.getPrimaryAddress(), errors);
                }
                if (partyDetails != null && partyDetails.getPartyName() != null) {
                    errors = partyValidator.validateName(partyDetails.getPartyName(), errors);
                }
            }
        }

        if (showLitigationFriendUpdateWarning(partyChosen, oldCaseData)) {
            warnings.add(CHECK_LITIGATION_FRIEND_WARNING);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .warnings(warnings)
            .errors(errors)
            .build();
    }

    private Boolean partyHasLitigationFriend(String partyChosen, CaseData caseData) {
        return hasLitigationFriend(CLAIMANT_ONE_ID, partyChosen, caseData.getApplicant1LitigationFriendRequired())
            || hasLitigationFriend(CLAIMANT_TWO_ID, partyChosen, caseData.getApplicant2LitigationFriendRequired())
            || hasLitigationFriend(DEFENDANT_ONE_ID, partyChosen, caseData.getRespondent1LitigationFriend())
            || hasLitigationFriend(DEFENDANT_TWO_ID, partyChosen, caseData.getRespondent2LitigationFriend());
    }

    private String getPostCode(String partyChosen, CaseData caseData) {
        switch (partyChosen) {
            case CLAIMANT_ONE_ID: {
                return getPartyPostCode(caseData.getApplicant1());
            }
            case CLAIMANT_TWO_ID: {
                return getPartyPostCode(caseData.getApplicant2());
            }
            case DEFENDANT_ONE_ID: {
                return getPartyPostCode(caseData.getRespondent1());
            }
            case DEFENDANT_TWO_ID: {
                return getPartyPostCode(caseData.getRespondent2());
            }
            case CLAIMANT_ONE_LITIGATION_FRIEND_ID: {
                return getPartyPostCode(caseData.getApplicant1LitigationFriend());
            }
            case CLAIMANT_TWO_LITIGATION_FRIEND_ID: {
                return getPartyPostCode(caseData.getApplicant2LitigationFriend());
            }
            case DEFENDANT_ONE_LITIGATION_FRIEND_ID: {
                return getPartyPostCode(caseData.getRespondent1LitigationFriend());
            }
            case DEFENDANT_TWO_LITIGATION_FRIEND_ID: {
                return getPartyPostCode(caseData.getRespondent2LitigationFriend());
            }
            default: {
                return null;
            }
        }
    }

    private Party getPartyDetails(String partyChosen, CaseData caseData) {
        switch (partyChosen) {
            case CLAIMANT_ONE_ID: {
                return caseData.getApplicant1();
            }
            case CLAIMANT_TWO_ID: {
                return caseData.getApplicant2();
            }
            case DEFENDANT_ONE_ID: {
                return caseData.getRespondent1();
            }
            case DEFENDANT_TWO_ID: {
                return caseData.getRespondent2();
            }
            default: {
                return null;
            }
        }
    }

    private boolean showLitigationFriendUpdateWarning(String partyChosen, CaseData caseData) {
        return ((CLAIMANT_ONE_LITIGATION_FRIEND_ID.equals(partyChosen) || CLAIMANT_TWO_LITIGATION_FRIEND_ID.equals(partyChosen))
            && bothClaimantsHaveLitigationFriends(caseData))
            || ((DEFENDANT_ONE_LITIGATION_FRIEND_ID.equals(partyChosen) || DEFENDANT_TWO_LITIGATION_FRIEND_ID.equals(partyChosen))
            && ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && bothDefendantsHaveLitigationFriends(caseData));
    }

    private Boolean hasLitigationFriend(String id, String partyChosen, YesOrNo litigationFriend) {
        return id.equals(partyChosen) && YES.equals(litigationFriend);
    }

    private Boolean hasLitigationFriend(String id, String partyChosen, LitigationFriend litigationFriend) {
        return id.equals(partyChosen) && litigationFriend != null;
    }

    private String getPartyPostCode(Party party) {
        return party.getPrimaryAddress().getPostCode();
    }

    private String getPartyPostCode(LitigationFriend party) {
        return party.getPrimaryAddress().getPostCode();
    }

    private boolean bothClaimantsHaveLitigationFriends(CaseData caseData) {
        return nonNull(caseData.getApplicant1LitigationFriend()) && nonNull(caseData.getApplicant2LitigationFriend());
    }

    private boolean bothDefendantsHaveLitigationFriends(CaseData caseData) {
        return nonNull(caseData.getRespondent1LitigationFriend()) && nonNull(caseData.getRespondent2LitigationFriend());
    }

}
