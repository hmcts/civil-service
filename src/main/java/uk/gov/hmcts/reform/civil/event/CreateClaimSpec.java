package uk.gov.hmcts.reform.civil.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;

import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.enums.State;
import uk.gov.hmcts.reform.civil.model.CaseData;

import uk.gov.hmcts.reform.civil.enums.UserRole;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.api.Permission.CRU;
import static uk.gov.hmcts.reform.civil.enums.State.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.UserRole.CASE_WORKER;
import static uk.gov.hmcts.reform.civil.enums.UserRole.SOLICITOR;

@Component
public class CreateClaimSpec implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event("CREATE_CLAIM_SPEC")
            .initialState(PENDING_CASE_ISSUED)
            .name("Create claim - Specified")
            .description("Case created, post actions triggered")
            .showSummary()
            .endButtonLabel("Submit")
            .grant(CRU, SOLICITOR)
            .grantHistoryOnly(CASE_WORKER)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .fields()
            .page("CheckList")
            .readonly(CaseData::getSpecCheckList)
            .readonly(CaseData::getCheckListText)

            .page("References")
            .label("references", "## Your File Reference")
            .complex(CaseData::getSolicitorReferences)
            .optionalWithLabel(SolicitorReferences::getApplicantSolicitor1Reference,
                               "Claimant's legal representative's reference")
            .optionalWithLabel(
                SolicitorReferences::getRespondentSolicitor1Reference,
                "Defendant's legal representative's reference"
            )
            .done()

            .page("Claimant")
            .label("claimantsDetails", "## Claimant's details")
            .complex(CaseData::getApplicant1Spec)
            .mandatoryWithLabel(Party::getType, "Claimant type")
            .optionalWithoutDefaultValue(Party::getIndividualTitle, "type=\"INDIVIDUAL\"", "Title")
            .mandatoryWithoutDefaultValue(Party::getIndividualFirstName, "type=\"INDIVIDUAL\"", "First Name")
            .mandatoryWithoutDefaultValue(Party::getIndividualLastName, "type=\"INDIVIDUAL\"", "Last Name")
            .mandatoryWithoutDefaultValue(Party::getIndividualDateOfBirth, "type=\"INDIVIDUAL\"", "Date of birth")
            .mandatoryWithoutDefaultValue(Party::getCompanyName, "type=\"COMPANY\"", "Company Name")
            .mandatoryWithoutDefaultValue(Party::getOrganisationName, "type=\"ORGANISATION\"", "Organisation Name")
            .mandatoryWithoutDefaultValue(Party::getSoleTraderName, "type=\"SOLE_TRADER\"", "Soletrader Name")
            .done()
            .done();
    }

   /* public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        List<String> errors = new ArrayList<>();
        if (caseData.getApplicant1Spec().getIndividualDateOfBirth() != null) {
            if (caseData.getApplicant1Spec().getIndividualDateOfBirth().isAfter(LocalDate.now())) {
                errors.add("Correct the date. You can’t use a future date.");
            }
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .data(caseData)
            .build();
    }*/

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();
        details.setState(PENDING_CASE_ISSUED);


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        return SubmittedCallbackResponse.builder().build();
    }
}
