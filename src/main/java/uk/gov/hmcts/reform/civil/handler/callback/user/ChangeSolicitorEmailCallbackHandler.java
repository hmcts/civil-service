package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CHANGE_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class ChangeSolicitorEmailCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(CHANGE_SOLICITOR_EMAIL);

    private final ValidateEmailService validateEmailService;

    private final UserService userService;

    private final CoreCaseUserService coreCaseUserService;

    private final ObjectMapper objectMapper;

    private final PostcodeValidator postcodeValidator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::aboutToStart,
            callbackKey(MID, "validate-applicant1-solicitor-email"), this::validateApplicant1SolicitorEmail,
            callbackKey(MID, "validate-respondent1-solicitor-email"), this::validateRespondent1SolicitorEmail,
            callbackKey(MID, "validate-respondent2-solicitor-email"), this::validateRespondent2SolicitorEmail,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        List<String> userRoles = getUserRoles(callbackParams);
        CaseData caseData = callbackParams.getCaseData();

        boolean isApplicant1 = userRoles.contains(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
        boolean isRespondent1 = userRoles.contains(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
        boolean isRespondent2 = userRoles.contains(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
        caseData.setIsApplicant1(isApplicant1 ? YES : NO);
        caseData.setIsRespondent1(isRespondent1 ? YES : NO);
        caseData.setIsRespondent2(isRespondent2 ? YES : NO);

        // depending on flags, keep the current reference so we know if it was changed
        String applicantReference = getSolicitorReference(
            caseData.getApplicant1OrganisationPolicy(),
            Optional.ofNullable(caseData.getSolicitorReferences())
                .map(SolicitorReferences::getApplicantSolicitor1Reference)
                .orElse(null)
        );
        String respondent1Reference = getSolicitorReference(
            caseData.getRespondent1OrganisationPolicy(),
            Optional.ofNullable(caseData.getSolicitorReferences())
                .map(SolicitorReferences::getRespondentSolicitor1Reference)
                .orElse(null)
        );
        String respondent2Reference = getSolicitorReference(
            caseData.getRespondent2OrganisationPolicy(),
            Optional.ofNullable(caseData.getSolicitorReferences())
                .map(SolicitorReferences::getRespondentSolicitor2Reference)
                .orElse(null)
        );
        SolicitorReferences solicitorReferencesCopy = new SolicitorReferences();
        solicitorReferencesCopy.setApplicantSolicitor1Reference(applicantReference);
        solicitorReferencesCopy.setRespondentSolicitor1Reference(respondent1Reference);
        solicitorReferencesCopy.setRespondentSolicitor2Reference(respondent2Reference);
        caseData.setSolicitorReferencesCopy(solicitorReferencesCopy);
        Optional.ofNullable(caseData.getApplicant1OrganisationPolicy())
            .ifPresent(policy -> policy.setOrgPolicyReference(applicantReference));
        Optional.ofNullable(caseData.getRespondent1OrganisationPolicy())
            .ifPresent(policy -> policy.setOrgPolicyReference(respondent1Reference));
        Optional.ofNullable(caseData.getRespondent2OrganisationPolicy())
            .ifPresent(policy -> policy.setOrgPolicyReference(respondent2Reference));

        prepareCorrespondenceAddresses(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    /**
     * We want to send only serviceAddress, but if user says "no" to correspondence address, instead of removing
     * what we have, we have to keep it. However, in that case, serviceAddress come as null. So, in spec cases,
     * we copy spec's addresses over to serviceAddress, and in unspec we use the spec fields as a backup holder.
     *
     * @param caseData        original case data
     */
    private static void prepareCorrespondenceAddresses(CaseData caseData) {
        if (caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM) {
            caseData.setApplicantSolicitor1ServiceAddressRequired(
                caseData.getSpecApplicantCorrespondenceAddressRequired()
            );
            caseData.setApplicantSolicitor1ServiceAddress(
                caseData.getSpecApplicantCorrespondenceAddressdetails()
            );
            caseData.setRespondentSolicitor1ServiceAddressRequired(
                caseData.getSpecRespondentCorrespondenceAddressRequired()
            );
            caseData.setRespondentSolicitor1ServiceAddress(
                caseData.getSpecRespondentCorrespondenceAddressdetails()
            );
            caseData.setRespondentSolicitor2ServiceAddressRequired(
                caseData.getSpecRespondent2CorrespondenceAddressRequired()
            );
            caseData.setRespondentSolicitor2ServiceAddress(
                caseData.getSpecRespondent2CorrespondenceAddressdetails()
            );
        } else {
            caseData.setSpecApplicantCorrespondenceAddressRequired(
                caseData.getApplicantSolicitor1ServiceAddressRequired()
            );
            caseData.setSpecApplicantCorrespondenceAddressdetails(
                caseData.getApplicantSolicitor1ServiceAddress()
            );
            caseData.setSpecRespondentCorrespondenceAddressRequired(
                caseData.getRespondentSolicitor1ServiceAddressRequired()
            );
            caseData.setSpecRespondentCorrespondenceAddressdetails(
                caseData.getRespondentSolicitor1ServiceAddress()
            );
            caseData.setSpecRespondent2CorrespondenceAddressRequired(
                caseData.getRespondentSolicitor2ServiceAddressRequired()
            );
            caseData.setSpecRespondent2CorrespondenceAddressdetails(
                caseData.getRespondentSolicitor2ServiceAddress()
            );
        }
    }

    @Nullable
    private static String getSolicitorReference(OrganisationPolicy policy, String fromForm) {
        return Stream.of(
            Optional.ofNullable(policy)
                .map(OrganisationPolicy::getOrgPolicyReference)
                .orElse(null),
            fromForm
        ).filter(StringUtils::isNotBlank).findFirst().orElse(null);
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        prepareForSubmit(caseData);

        updateSolicitorReferences(callbackParams, caseData);
        updateSpecCorrespondenceAddresses(callbackParams, caseData);
        clearTempInfo(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private void prepareForSubmit(CaseData caseData) {
        prepareAddress(
            caseData.getApplicantSolicitor1ServiceAddressRequired(),
            caseData.getSpecApplicantCorrespondenceAddressdetails(),
            caseData::setApplicantSolicitor1ServiceAddressRequired,
            caseData::setApplicantSolicitor1ServiceAddress
        );
        prepareAddress(
            caseData.getRespondentSolicitor1ServiceAddressRequired(),
            caseData.getSpecRespondentCorrespondenceAddressdetails(),
            caseData::setRespondentSolicitor1ServiceAddressRequired,
            caseData::setRespondentSolicitor1ServiceAddress
        );
        prepareAddress(
            caseData.getRespondentSolicitor2ServiceAddressRequired(),
            caseData.getSpecRespondent2CorrespondenceAddressdetails(),
            caseData::setRespondentSolicitor2ServiceAddressRequired,
            caseData::setRespondentSolicitor2ServiceAddress
        );
        if (MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP == MultiPartyScenario.getMultiPartyScenario(caseData)) {
            // copy 1 into 2
            caseData.setRespondentSolicitor2ServiceAddressRequired(caseData.getRespondentSolicitor1ServiceAddressRequired());
            caseData.setRespondentSolicitor2ServiceAddress(caseData.getRespondentSolicitor1ServiceAddress());
            caseData.setRespondentSolicitor2EmailAddress(caseData.getRespondentSolicitor1EmailAddress());
            if (caseData.getRespondent1OrganisationPolicy() != null) {
                OrganisationPolicy respondent2Policy = new OrganisationPolicy();
                respondent2Policy.setOrganisation(caseData.getRespondent1OrganisationPolicy().getOrganisation());
                respondent2Policy.setOrgPolicyReference(caseData.getRespondent1OrganisationPolicy().getOrgPolicyReference());
                respondent2Policy.setOrgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName());
                caseData.setRespondent2OrganisationPolicy(respondent2Policy);
            }
        }
    }

    private void prepareAddress(YesOrNo requiredFromForm,
                                Address oldAddress,
                                Consumer<YesOrNo> setterYesNo,
                                Consumer<Address> setterAddress) {
        // if requiredFromForm = Yes assume serviceAddress was populated in the form
        if (requiredFromForm == NO) {
            Optional.ofNullable(oldAddress)
                .map(Address::getPostCode).filter(StringUtils::isNotBlank)
                .ifPresent(a -> {
                    setterYesNo.accept(YES);
                    setterAddress.accept(oldAddress);
                });
        }
    }

    /**
     * If spec, copy modified correspondence address info back to spec's fields.
     *
     * @param caseData        original case data
     * @param caseData updated case data
     */
    private void updateSpecCorrespondenceAddresses(CallbackParams callbackParams,
                                                   CaseData caseData) {
        if (caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM) {
            List<String> userRoles = getUserRoles(callbackParams);
            if (userRoles.contains(CaseRole.APPLICANTSOLICITORONE.getFormattedName())) {
                caseData.setSpecApplicantCorrespondenceAddressRequired(
                    caseData.getApplicantSolicitor1ServiceAddressRequired()
                );
                caseData.setSpecApplicantCorrespondenceAddressdetails(
                    caseData.getApplicantSolicitor1ServiceAddress()
                );
            } else if (userRoles.contains(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())) {
                caseData.setSpecRespondentCorrespondenceAddressRequired(
                    caseData.getRespondentSolicitor1ServiceAddressRequired()
                );
                caseData.setSpecRespondentCorrespondenceAddressdetails(
                    caseData.getRespondentSolicitor1ServiceAddress()
                );
                if (MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP == MultiPartyScenario.getMultiPartyScenario(caseData)) {
                    caseData.setSpecRespondent2CorrespondenceAddressdetails(
                        caseData.getRespondentSolicitor1ServiceAddress());
                    caseData.setSpecRespondent2CorrespondenceAddressRequired(
                        caseData.getRespondentSolicitor1ServiceAddressRequired());
                }
            } else if (userRoles.contains(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())) {
                caseData.setSpecRespondent2CorrespondenceAddressRequired(
                    caseData.getRespondentSolicitor2ServiceAddressRequired()
                );
                caseData.setSpecRespondent2CorrespondenceAddressdetails(
                    caseData.getRespondentSolicitor2ServiceAddress()
                );
            }

            caseData.setApplicantSolicitor1ServiceAddress(new Address());
            caseData.setRespondentSolicitor1ServiceAddress(new Address());
            caseData.setRespondentSolicitor2ServiceAddress(new Address());
        }
    }

    /**
     * Solicitor references appear twice with two different names (solicitorReferences,
     * XXXOrganisationPolicy_OrgPolicyReference, respondentSolicitor2Reference...).
     * At least two of them (solicitorReferences and XXXOrganisationPolicy) appear in the same journey, with no
     * guarantee that their values are going to match. To lessen the side effects of this event, we check if
     * the field was actually changed before updating the other field(s).
     *
     * @param caseData    original case data
     * @param caseData case data being updated
     */
    private void updateSolicitorReferences(CallbackParams callbackParams, CaseData caseData) {
        List<String> userRoles = getUserRoles(callbackParams);
        if (userRoles.contains(CaseRole.APPLICANTSOLICITORONE.getFormattedName())) {
            updateReference(
                Optional.ofNullable(caseData.getSolicitorReferencesCopy())
                    .map(SolicitorReferences::getApplicantSolicitor1Reference)
                    .orElse(null),
                caseData.getApplicant1OrganisationPolicy(),
                newReference -> {
                    SolicitorReferences references = Optional.ofNullable(caseData.getSolicitorReferences())
                        .orElse(new SolicitorReferences());
                    references.setApplicantSolicitor1Reference(newReference);
                    caseData.setSolicitorReferences(references);
                }
            );
        } else if (userRoles.contains(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())) {
            updateReference(
                Optional.ofNullable(caseData.getSolicitorReferencesCopy())
                    .map(SolicitorReferences::getRespondentSolicitor1Reference)
                    .orElse(null),
                caseData.getRespondent1OrganisationPolicy(),
                newReference -> {
                    SolicitorReferences references = Optional.ofNullable(caseData.getSolicitorReferences())
                        .orElse(new SolicitorReferences());
                    references.setRespondentSolicitor1Reference(newReference);
                    caseData.setSolicitorReferences(references);
                }
            );
        } else if (userRoles.contains(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())) {
            updateReference(
                Optional.ofNullable(caseData.getSolicitorReferencesCopy())
                    .map(SolicitorReferences::getRespondentSolicitor2Reference)
                    .orElse(null),
                caseData.getRespondent2OrganisationPolicy(),
                newReference -> {
                    SolicitorReferences references = Optional.ofNullable(caseData.getSolicitorReferences())
                        .orElse(new SolicitorReferences());
                    references.setRespondentSolicitor2Reference(newReference);
                    caseData.setSolicitorReferences(references);
                    caseData.setRespondentSolicitor2Reference(newReference);
                }
            );
        }
        caseData.setSolicitorReferencesCopy(new SolicitorReferences());
    }

    private static void updateReference(String oldReference, OrganisationPolicy policy, Consumer<String> ifDifferent) {
        String newReference = Optional.ofNullable(policy)
            .map(OrganisationPolicy::getOrgPolicyReference)
            .orElse("");
        if (!StringUtils.equals(newReference, oldReference)) {
            ifDifferent.accept(newReference);
        }
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        String partyType = getUserRoles(callbackParams).stream()
            .anyMatch(r -> r.equals(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                || r.equals(CaseRole.RESPONDENTSOLICITORONE.getFormattedName()))
            ? "defendant" : "claimant";

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(
                String.format("# You have updated a %s's legal representative's information", partyType))
            .confirmationBody("<br />")
            .build();
    }

    private List<String> validateAddress(Address address) {
        if (address != null) {
            String postCode = address.getPostCode();
            if (StringUtils.isBlank(postCode)) {
                // postcode validator is ok with null but not ""
                postCode = null;
            }
            return postcodeValidator.validate(postCode);
        } else {
            return Collections.emptyList();
        }
    }

    private CallbackResponse validateApplicant1SolicitorEmail(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>(validateEmailService.validate(
            callbackParams.getCaseData().getApplicantSolicitor1UserDetails().getEmail()));
        errors.addAll(validateAddress(callbackParams.getCaseData().getApplicantSolicitor1ServiceAddress()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors).build();
    }

    private CallbackResponse validateRespondent1SolicitorEmail(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>(validateEmailService.validate(
            callbackParams.getCaseData().getRespondentSolicitor1EmailAddress()));
        errors.addAll(validateAddress(callbackParams.getCaseData().getRespondentSolicitor1ServiceAddress()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors).build();
    }

    private CallbackResponse validateRespondent2SolicitorEmail(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>(validateEmailService.validate(
            callbackParams.getCaseData().getRespondentSolicitor2EmailAddress()));
        errors.addAll(validateAddress(callbackParams.getCaseData().getRespondentSolicitor2ServiceAddress()));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors).build();
    }

    private void clearTempInfo(CaseData caseData) {
        caseData.setIsApplicant1(null);
        caseData.setIsRespondent1(null);
        caseData.setIsRespondent2(null);
        if (caseData.getCaseAccessCategory() != CaseCategory.SPEC_CLAIM) {
            caseData.setSpecApplicantCorrespondenceAddressRequired(NO);
            caseData.setSpecApplicantCorrespondenceAddressdetails(new Address());
            caseData.setSpecRespondentCorrespondenceAddressRequired(NO);
            caseData.setSpecRespondentCorrespondenceAddressdetails(new Address());
            caseData.setSpecRespondent2CorrespondenceAddressRequired(NO);
            caseData.setSpecRespondent2CorrespondenceAddressdetails(new Address());
        }
    }

    private List<String> getUserRoles(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        return coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );
    }
}

