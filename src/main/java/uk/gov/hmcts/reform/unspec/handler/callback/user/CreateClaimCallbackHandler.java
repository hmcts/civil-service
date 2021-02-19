package uk.gov.hmcts.reform.unspec.handler.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prd.model.Organisation;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.config.ClaimIssueConfiguration;
import uk.gov.hmcts.reform.unspec.enums.CaseState;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.CorrectEmail;
import uk.gov.hmcts.reform.unspec.model.IdamUserDetails;
import uk.gov.hmcts.reform.unspec.model.Party;
import uk.gov.hmcts.reform.unspec.model.SolicitorReferences;
import uk.gov.hmcts.reform.unspec.model.common.DynamicList;
import uk.gov.hmcts.reform.unspec.repositories.ReferenceNumberRepository;
import uk.gov.hmcts.reform.unspec.service.FeesService;
import uk.gov.hmcts.reform.unspec.service.OrganisationService;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowState;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.unspec.validation.DateOfBirthValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.unspec.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.getAllocatedTrack;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.Main.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.unspec.service.flowstate.FlowState.fromFullName;

@Service
@RequiredArgsConstructor
public class CreateClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_CLAIM);
    public static final String CONFIRMATION_SUMMARY = "<br />Follow these steps to serve a claim:"
        + "\n* [Download the sealed claim form](%s)"
        + "\n* Send the form, particulars of claim and "
        + "<a href=\"%s\" target=\"_blank\">a response pack</a> (PDF, 266 KB) to the defendant by %s"
        + "\n* Confirm service online within 21 days of sending the form, particulars and response pack, before"
        + " 4pm if you're doing this on the due day";

    public static final String LIP_CONFIRMATION_BODY = "<br />You do not need to do anything.\n\n"
        + "Your claim will be considered by the court and you will be informed of the outcome by post.";

    private final ClaimIssueConfiguration claimIssueConfiguration;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ReferenceNumberRepository referenceNumberRepository;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final FeesService feesService;
    private final OrganisationService organisationService;
    private final StateFlowEngine stateFlowEngine;
    private final IdamClient idamClient;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "applicant"), this::validateDateOfBirth,
            callbackKey(MID, "fee"), this::calculateFee,
            callbackKey(MID, "idam-email"), this::getIdamEmail,
            callbackKey(ABOUT_TO_SUBMIT), this::submitClaim,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
        Party applicant = callbackParams.getCaseData().getApplicant1();
        List<String> errors = dateOfBirthValidator.validate(applicant);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse calculateFee(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Optional<SolicitorReferences> references = ofNullable(caseData.getSolicitorReferences());
        String paymentReference = ofNullable(caseData.getPaymentReference())
            .orElse(references.map(SolicitorReferences::getApplicantSolicitor1Reference).orElse(""));

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> pbaNumbers = getPbaAccounts(authToken);

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder()
            .claimFee(feesService.getFeeDataByClaimValue(caseData.getClaimValue()))
            .applicantSolicitor1PbaAccounts(DynamicList.fromList(pbaNumbers))
            .applicantSolicitor1PbaAccountsIsEmpty(pbaNumbers.isEmpty() ? YesOrNo.YES : YesOrNo.NO)
            .paymentReference(paymentReference);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(caseDataBuilder.build()))
            .build();
    }

    private CallbackResponse getIdamEmail(CallbackParams callbackParams) {
        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());

        CaseData.CaseDataBuilder caseDataBuilder = callbackParams.getCaseData().toBuilder()
            .applicantSolicitor1CheckEmail(CorrectEmail.builder().email(userDetails.getEmail()).build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(caseDataBuilder.build()))
            .build();
    }

    private List<String> getPbaAccounts(String authToken) {
        return organisationService.findOrganisation(authToken)
            .map(Organisation::getPaymentAccount)
            .orElse(emptyList());
    }

    private CallbackResponse submitClaim(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        // second idam call is workaround for null pointer when hiding field in getIdamEmail callback
        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        IdamUserDetails.IdamUserDetailsBuilder idam = IdamUserDetails.builder().id(userDetails.getId());
        CorrectEmail applicantSolicitor1CheckEmail = caseData.getApplicantSolicitor1CheckEmail();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();

        if (applicantSolicitor1CheckEmail.isCorrect()) {
            dataBuilder.applicantSolicitor1UserDetails(idam.email(applicantSolicitor1CheckEmail.getEmail()).build());
        } else {
            IdamUserDetails applicantSolicitor1UserDetails = caseData.getApplicantSolicitor1UserDetails();
            dataBuilder.applicantSolicitor1UserDetails(idam.email(applicantSolicitor1UserDetails.getEmail()).build());
        }

        dataBuilder.legacyCaseReference(referenceNumberRepository.getReferenceNumber());
        dataBuilder.claimSubmittedDateTime(LocalDateTime.now());
        dataBuilder.allocatedTrack(getAllocatedTrack(caseData.getClaimValue().toPounds(), caseData.getClaimType()));
        dataBuilder.businessProcess(BusinessProcess.ready(CREATE_CLAIM));

        //set check email field to null for GDPR
        dataBuilder.applicantSolicitor1CheckEmail(CorrectEmail.builder().build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(dataBuilder.build()))
            .state(getState(dataBuilder.build()))
            .build();
    }

    private String getState(CaseData updatedCaseData) {
        FlowState flowState = fromFullName(stateFlowEngine.evaluate(updatedCaseData).getState().getName());
        return String.valueOf(
            flowState == PENDING_CASE_ISSUED ? CaseState.PENDING_CASE_ISSUED : CaseState.PROCEEDS_WITH_OFFLINE_JOURNEY
        );
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (caseData.getRespondent1Represented() == YesOrNo.NO) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader("# Your claim will now progress offline")
                .confirmationBody(LIP_CONFIRMATION_BODY)
                .build();
        }

        LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
        String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);
        String claimNumber = caseData.getLegacyCaseReference();

        String body = format(
            CONFIRMATION_SUMMARY,
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference()),
            claimIssueConfiguration.getResponsePackLink(),
            formattedServiceDeadline
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Your claim has been issued%n## Claim number: %s", claimNumber))
            .confirmationBody(body)
            .build();
    }
}
