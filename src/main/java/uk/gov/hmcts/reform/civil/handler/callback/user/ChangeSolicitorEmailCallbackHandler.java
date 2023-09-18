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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        boolean isApplicant1 = userRoles.contains(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
        boolean isRespondent1 = userRoles.contains(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
        boolean isRespondent2 = userRoles.contains(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
        caseDataBuilder.isApplicant1(isApplicant1 ? YES : NO)
            .isRespondent1(isRespondent1 ? YES : NO)
            .isRespondent2(isRespondent2 ? YES : NO);

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
        caseDataBuilder.solicitorReferencesCopy(
            SolicitorReferences.builder()
                .applicantSolicitor1Reference(applicantReference)
                .respondentSolicitor1Reference(respondent1Reference)
                .respondentSolicitor2Reference(respondent2Reference)
                .build()
        );
        Optional.ofNullable(caseData.getApplicant1OrganisationPolicy())
            .map(op -> op.toBuilder().orgPolicyReference(applicantReference).build())
            .ifPresent(caseDataBuilder::applicant1OrganisationPolicy);
        Optional.ofNullable(caseData.getRespondent1OrganisationPolicy())
            .map(op -> op.toBuilder().orgPolicyReference(respondent1Reference).build())
            .ifPresent(caseDataBuilder::respondent1OrganisationPolicy);
        Optional.ofNullable(caseData.getRespondent2OrganisationPolicy())
            .map(op -> op.toBuilder().orgPolicyReference(respondent2Reference).build())
            .ifPresent(caseDataBuilder::respondent2OrganisationPolicy);

        prepareCorrespondenceAddresses(caseData, caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    /**
     * We want to send only serviceAddress, but if user says "no" to correspondence address, instead of removing
     * what we have, we have to keep it. However, in that case, serviceAddress come as null. So, in spec cases,
     * we copy spec's addresses over to serviceAddress, and in unspec we use the spec fields as a backup holder.
     *
     * @param caseData        original case data
     * @param caseDataBuilder updated case data
     */
    private static void prepareCorrespondenceAddresses(CaseData caseData, CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        if (caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM) {
            caseDataBuilder
                .applicantSolicitor1ServiceAddressRequired(
                    caseData.getSpecApplicantCorrespondenceAddressRequired()
                )
                .applicantSolicitor1ServiceAddress(
                    caseData.getSpecApplicantCorrespondenceAddressdetails()
                )
                .respondentSolicitor1ServiceAddressRequired(
                    caseData.getSpecRespondentCorrespondenceAddressRequired()
                )
                .respondentSolicitor1ServiceAddress(
                    caseData.getSpecRespondentCorrespondenceAddressdetails()
                )
                .respondentSolicitor2ServiceAddressRequired(
                    caseData.getSpecRespondent2CorrespondenceAddressRequired()
                )
                .respondentSolicitor2ServiceAddress(
                    caseData.getSpecRespondent2CorrespondenceAddressdetails()
                );
        } else {
            caseDataBuilder
                .specApplicantCorrespondenceAddressRequired(
                    caseData.getApplicantSolicitor1ServiceAddressRequired()
                )
                .specApplicantCorrespondenceAddressdetails(
                    caseData.getApplicantSolicitor1ServiceAddress()
                )
                .specRespondentCorrespondenceAddressRequired(
                    caseData.getRespondentSolicitor1ServiceAddressRequired()
                )
                .specRespondentCorrespondenceAddressdetails(
                    caseData.getRespondentSolicitor1ServiceAddress()
                )
                .specRespondent2CorrespondenceAddressRequired(
                    caseData.getRespondentSolicitor2ServiceAddressRequired()
                )
                .specRespondent2CorrespondenceAddressdetails(
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
        CaseData.CaseDataBuilder<?, ?> caseBuilder = prepareForSubmit(caseData);
        // because we'll use the fields above
        caseData = caseBuilder.build();

        updateSolicitorReferences(callbackParams, caseData, caseBuilder);
        updateSpecCorrespondenceAddresses(callbackParams, caseData, caseBuilder);
        clearTempInfo(caseBuilder, caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseBuilder.build().toMap(objectMapper))
            .build();
    }

    private CaseData.CaseDataBuilder<?, ?> prepareForSubmit(CaseData caseData) {
        CaseData.CaseDataBuilder<?, ?> caseBuilder = caseData.toBuilder();
        prepareAddress(
            caseData.getApplicantSolicitor1ServiceAddressRequired(),
            caseData.getSpecApplicantCorrespondenceAddressdetails(),
            caseBuilder::applicantSolicitor1ServiceAddressRequired,
            caseBuilder::applicantSolicitor1ServiceAddress
        );
        prepareAddress(
            caseData.getRespondentSolicitor1ServiceAddressRequired(),
            caseData.getSpecRespondentCorrespondenceAddressdetails(),
            caseBuilder::respondentSolicitor1ServiceAddressRequired,
            caseBuilder::respondentSolicitor1ServiceAddress
        );
        prepareAddress(
            caseData.getRespondentSolicitor2ServiceAddressRequired(),
            caseData.getSpecRespondent2CorrespondenceAddressdetails(),
            caseBuilder::respondentSolicitor2ServiceAddressRequired,
            caseBuilder::respondentSolicitor2ServiceAddress
        );
        if (MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP == MultiPartyScenario.getMultiPartyScenario(caseData)) {
            // copy 1 into 2
            CaseData temp = caseBuilder.build();
            caseBuilder
                .respondentSolicitor2ServiceAddressRequired(temp.getRespondentSolicitor1ServiceAddressRequired())
                .respondentSolicitor2ServiceAddress(temp.getRespondentSolicitor1ServiceAddress())
                .respondentSolicitor2EmailAddress(temp.getRespondentSolicitor1EmailAddress())
                .respondent2OrganisationPolicy(temp.getRespondent1OrganisationPolicy());
        }
        return caseBuilder;
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
     * @param caseDataBuilder updated case data
     */
    private void updateSpecCorrespondenceAddresses(CallbackParams callbackParams,
                                                   CaseData caseData,
                                                   CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        if (caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM) {
            List<String> userRoles = getUserRoles(callbackParams);
            if (userRoles.contains(CaseRole.APPLICANTSOLICITORONE.getFormattedName())) {
                caseDataBuilder
                    .specApplicantCorrespondenceAddressRequired(
                        caseData.getApplicantSolicitor1ServiceAddressRequired()
                    )
                    .specApplicantCorrespondenceAddressdetails(
                        caseData.getApplicantSolicitor1ServiceAddress()
                    );
            } else if (userRoles.contains(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())) {
                caseDataBuilder.specRespondentCorrespondenceAddressRequired(
                        caseData.getRespondentSolicitor1ServiceAddressRequired()
                    )
                    .specRespondentCorrespondenceAddressdetails(
                        caseData.getRespondentSolicitor1ServiceAddress()
                    );
                if (MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP == MultiPartyScenario.getMultiPartyScenario(caseData)) {
                    caseDataBuilder
                        .specRespondent2CorrespondenceAddressdetails(
                            caseData.getRespondentSolicitor1ServiceAddress())
                        .specRespondent2CorrespondenceAddressRequired(
                            caseData.getRespondentSolicitor1ServiceAddressRequired());
                }
            } else if (userRoles.contains(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())) {
                caseDataBuilder.specRespondent2CorrespondenceAddressRequired(
                        caseData.getRespondentSolicitor2ServiceAddressRequired()
                    )
                    .specRespondent2CorrespondenceAddressdetails(
                        caseData.getRespondentSolicitor2ServiceAddress()
                    );
            }

            caseDataBuilder
                .applicantSolicitor1ServiceAddress(Address.builder().build())
                .respondentSolicitor1ServiceAddress(Address.builder().build())
                .respondentSolicitor2ServiceAddress(Address.builder().build());
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
     * @param caseBuilder case data being updated
     */
    private void updateSolicitorReferences(CallbackParams callbackParams, CaseData caseData,
                                           CaseData.CaseDataBuilder<?, ?> caseBuilder) {
        List<String> userRoles = getUserRoles(callbackParams);
        if (userRoles.contains(CaseRole.APPLICANTSOLICITORONE.getFormattedName())) {
            updateReference(
                Optional.ofNullable(caseData.getSolicitorReferencesCopy())
                    .map(SolicitorReferences::getApplicantSolicitor1Reference)
                    .orElse(null),
                caseData.getApplicant1OrganisationPolicy(),
                newReference -> {
                    SolicitorReferences references = Optional.ofNullable(caseData.getSolicitorReferences())
                        .orElse(SolicitorReferences.builder().build());
                    references.setApplicantSolicitor1Reference(newReference);
                    caseBuilder.solicitorReferences(references);
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
                        .orElse(SolicitorReferences.builder().build());
                    references.setRespondentSolicitor1Reference(newReference);
                    caseBuilder.solicitorReferences(references);
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
                        .orElse(SolicitorReferences.builder().build());
                    references.setRespondentSolicitor2Reference(newReference);
                    caseBuilder.solicitorReferences(references)
                        .respondentSolicitor2Reference(newReference);
                }
            );
        }
        caseBuilder.solicitorReferencesCopy(SolicitorReferences.builder().build());
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
        String postCode = Optional.ofNullable(address)
            .map(Address::getPostCode)
            .orElse(null);
        return postcodeValidator.validate(postCode);
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

    private void clearTempInfo(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, CaseData caseData) {
        caseDataBuilder.isApplicant1(null)
            .isRespondent1(null)
            .isRespondent2(null);
        if (caseData.getCaseAccessCategory() != CaseCategory.SPEC_CLAIM) {
            caseDataBuilder
                .specApplicantCorrespondenceAddressRequired(NO)
                .specApplicantCorrespondenceAddressdetails(Address.builder().build())
                .specRespondentCorrespondenceAddressRequired(NO)
                .specRespondentCorrespondenceAddressdetails(Address.builder().build())
                .specRespondent2CorrespondenceAddressRequired(NO)
                .specRespondent2CorrespondenceAddressdetails(Address.builder().build());
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

