package uk.gov.hmcts.reform.civil.handler.callback.camunda.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DETAILS_AFTER_NOC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.Address.fromContactInformation;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCaseDetailsAfterNoCHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(UPDATE_CASE_DETAILS_AFTER_NOC);

    public static final String TASK_ID = "UpdateCaseDetailsAfterNoC";

    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::updateCaseDetails
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    // TODO waiting on CCD-3538 to update the user email
    private UserDetails getInvokerDetails(String authToken, CaseDetails caseDetails) {
        return UserDetails.builder().build();
    }

    private CallbackResponse updateCaseDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        // nullify this field since it was persisted to auto approve noc
        caseDataBuilder.changeOrganisationRequestField(null);

        ChangeOfRepresentation changeOfRepresentation = caseData.getChangeOfRepresentation();

        uk.gov.hmcts.reform.prd.model.Organisation addedOrganisation = organisationService.findOrganisationById(
            changeOfRepresentation.getOrganisationToAddID()).orElse(null);
        if (addedOrganisation == null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of("Organisation to add is null"))
                .build();
        }

        UserDetails addedSolicitorDetails = getInvokerDetails(
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            callbackParams.getRequest().getCaseDetails()
        );

        String replacedSolicitorCaseRole = changeOfRepresentation.getCaseRole();

        boolean isApplicantSolicitorRole = isApplicantOrRespondent(replacedSolicitorCaseRole);

        if (isApplicantSolicitorRole) {
            if (addedOrganisation != null) {
                updateApplicantSolicitorDetails(caseDataBuilder, addedSolicitorDetails, addedOrganisation);
            }
        } else {
            if (replacedSolicitorCaseRole.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                && addedOrganisation != null) {
                updateRespondentSolicitor1Details(caseDataBuilder, addedOrganisation, addedSolicitorDetails);
                updateAddLegalRepDeadlineIfRespondent1Replaced(caseData, caseDataBuilder);
            } else {
                if (replacedSolicitorCaseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                    && addedOrganisation != null) {
                    updateRespondentSolicitor2Details(caseDataBuilder, addedOrganisation, addedSolicitorDetails);
                    updateAddLegalRepDeadlineIfRespondent2Replaced(caseData, caseDataBuilder);
                }
            }
        }

        updateSolicitorReferences(caseData, caseDataBuilder, replacedSolicitorCaseRole);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void updateAddLegalRepDeadlineIfRespondent1Replaced(
        CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        if (caseData.getAddLegalRepDeadline() != null) {
            if (YES.equals(caseData.getAddRespondent2())
                && NO.equals(caseData.getRespondent2Represented())) {
                caseDataBuilder.addLegalRepDeadline(caseData.getAddLegalRepDeadline());
            } else {
                caseDataBuilder.addLegalRepDeadline(null);
            }
        }
    }

    private void updateAddLegalRepDeadlineIfRespondent2Replaced(
        CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        if (caseData.getAddLegalRepDeadline() != null) {
            if (NO.equals(caseData.getRespondent1Represented())) {
                caseDataBuilder.addLegalRepDeadline(caseData.getAddLegalRepDeadline());
            } else {
                caseDataBuilder.addLegalRepDeadline(null);
            }
        }
    }

    private void updateRespondentSolicitor2Details(
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
        Organisation addedOrganisation,
        UserDetails addedSolicitorDetails) {

        SolicitorOrganisationDetails updatedSolicitorAddress = getUpdatedSolicitorAddress(addedOrganisation);

        caseDataBuilder.respondentSolicitor2ServiceAddress(updatedSolicitorAddress.getAddress())
            .respondentSolicitor2ServiceAddressRequired(YES)
            .respondentSolicitor2OrganisationDetails(updatedSolicitorAddress)
            .respondent2OrganisationIDCopy(addedOrganisation.getOrganisationIdentifier());

        caseDataBuilder.respondent2Represented(YES)
            .respondent2OrgRegistered(YES);

        if (addedSolicitorDetails.getEmail() != null) {
            caseDataBuilder.respondentSolicitor2EmailAddress(addedSolicitorDetails.getEmail());
        } else {
            caseDataBuilder.respondentSolicitor2EmailAddress(null);
        }

        //todo multiparty 1v2 ds -> ss or vice versa
        //if (caseData.getRespondent1Represented().equals(YES)
        // && caseData.getRespondent1OrganisationPolicy().getOrganisation().equals(
        //    addedOrganisation)
        //    && caseData.getRespondentSolicitor1EmailAddress().equals(addedSolicitorDetails.getEmail())) {
        //    caseDataBuilder.respondent2SameLegalRepresentative(YES);
        //}
    }

    private void updateRespondentSolicitor1Details(CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                   Organisation addedOrganisation, UserDetails addedSolicitorDetails) {
        SolicitorOrganisationDetails updatedSolicitorAddress = getUpdatedSolicitorAddress(addedOrganisation);

        caseDataBuilder.respondentSolicitor1ServiceAddress(updatedSolicitorAddress.getAddress())
            .respondentSolicitor1ServiceAddressRequired(YES)
            .respondentSolicitor1OrganisationDetails(updatedSolicitorAddress)
            .respondent1OrganisationIDCopy(addedOrganisation.getOrganisationIdentifier());

        caseDataBuilder.respondent1Represented(YES)
            .respondent1OrgRegistered(YES);

        if (addedSolicitorDetails.getEmail() != null) {
            caseDataBuilder.respondentSolicitor1EmailAddress(addedSolicitorDetails.getEmail());
        } else {
            caseDataBuilder.respondentSolicitor1EmailAddress(null);
        }

        //todo multiparty 1v2 ds -> ss or vice versa

        //if (caseData.getRespondent2() != null && caseData.getRespondent2Represented().equals(YES)
        // && caseData.getRespondent2OrganisationPolicy().getOrganisation().equals(
        //   addedOrganisation)
        //   && caseData.getRespondentSolicitor2EmailAddress().equals(addedSolicitorDetails.getEmail())) {
        //    caseDataBuilder.respondent2SameLegalRepresentative(YES);
        //}
    }

    private void updateApplicantSolicitorDetails(CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                                                 UserDetails addedSolicitorDetails, Organisation addedOrganisation) {
        if (addedOrganisation.getPaymentAccount() != null && !addedOrganisation.getPaymentAccount().isEmpty()) {
            caseDataBuilder.applicantSolicitor1PbaAccounts(DynamicList.fromList(addedOrganisation.getPaymentAccount()))
                .applicantSolicitor1PbaAccountsIsEmpty(NO);
        } else {
            caseDataBuilder.applicantSolicitor1PbaAccountsIsEmpty(YES);
        }
        caseDataBuilder.applicantSolicitor1ServiceAddress(getUpdatedSolicitorAddress(addedOrganisation).getAddress())
            .applicantSolicitor1ServiceAddressRequired(YES);

        if (addedSolicitorDetails.getEmail() != null) {
            caseDataBuilder.applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                               .id(addedSolicitorDetails.getId())
                                                               .email(addedSolicitorDetails.getEmail())
                                                               .build());
        } else {
            caseDataBuilder.applicantSolicitor1UserDetails(null);
        }

    }

    // todo SolicitorOrganisationDetails field is spec!
    private SolicitorOrganisationDetails getUpdatedSolicitorAddress(Organisation addedOrganisation) {
        List<ContactInformation> contactInformation = addedOrganisation.getContactInformation();
        SolicitorOrganisationDetails.SolicitorOrganisationDetailsBuilder solicitorOrganisationDetailsBuilder
            = SolicitorOrganisationDetails.builder();

        if (contactInformation != null && !contactInformation.isEmpty()) {
            ContactInformation info = contactInformation.get(0);
            if (info != null) {
                solicitorOrganisationDetailsBuilder.address(fromContactInformation(contactInformation.get(0)));
                if (info.getDxAddress() != null) {
                    solicitorOrganisationDetailsBuilder.dx(info.getDxAddress().toString());
                }
                solicitorOrganisationDetailsBuilder.organisationName(addedOrganisation.getName());
                // todo is phonenumber == company number?
                solicitorOrganisationDetailsBuilder.phoneNumber(addedOrganisation.getCompanyNumber());
            }
        }
        return solicitorOrganisationDetailsBuilder.build();
    }

    private void updateSolicitorReferences(CaseData caseData,
                                           CaseData.CaseDataBuilder<?, ?> caseDataBuilder, String replacedCaseRole) {
        SolicitorReferences solicitorReferences = caseData.getSolicitorReferences();
        String applicantReference = replacedCaseRole.equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
            ? null : solicitorReferences.getApplicantSolicitor1Reference();

        String respondent1Reference = replacedCaseRole.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
            ? null : solicitorReferences.getRespondentSolicitor1Reference();

        String respondent2Reference = replacedCaseRole.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
            ? null : solicitorReferences.getRespondentSolicitor2Reference();

        SolicitorReferences updatedSolicitorReferences = SolicitorReferences.builder()
            .applicantSolicitor1Reference(applicantReference)
            .respondentSolicitor1Reference(respondent1Reference)
            .respondentSolicitor2Reference(respondent2Reference)
            .build();

        caseDataBuilder.solicitorReferences(updatedSolicitorReferences);

        // only update if it's been created during acknowledge claim
        if (caseData.getSolicitorReferencesCopy() != null) {
            caseDataBuilder.solicitorReferencesCopy(updatedSolicitorReferences);
        }
    }

    private boolean isApplicantOrRespondent(String addedSolicitorRole) {
        return addedSolicitorRole.equals(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
    }
}
