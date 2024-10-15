package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static java.lang.String.format;

@Component
public class ClaimantAttendsOrRepresentedTextBuilder {

    private static final String NOTICE_RECEIVED_CAN_PROCEED = "received notice of the trial and determined that it was reasonable to proceed in their absence.";
    private static final String NOTICE_RECEIVED_CANNOT_PROCEED = "received notice of the trial, the Judge was not satisfied that it was "
        + "reasonable to proceed in their absence.";
    private static final String NOTICE_NOT_RECEIVED_CANNOT_PROCEED = "The Judge was not satisfied that they had received notice of the hearing "
        + "and it was not reasonable to proceed in their absence.";

    public String claimantBuilder(CaseData caseData, Boolean isClaimant2) {
        String name = getClaimantName(caseData, isClaimant2);
        FinalOrdersClaimantRepresentationList type = getClaimantRepresentationType(caseData, isClaimant2);

        return type != null ? buildClaimantRepresentationText(caseData, name, type, isClaimant2) : "";
    }

    private String getClaimantName(CaseData caseData, Boolean isClaimant2) {
        return Boolean.TRUE.equals(isClaimant2) ? caseData.getApplicant2().getPartyName() : caseData.getApplicant1().getPartyName();
    }

    private FinalOrdersClaimantRepresentationList getClaimantRepresentationType(CaseData caseData, Boolean isClaimant2) {
        if (hasRepresentationType(caseData)) {
            return Boolean.TRUE.equals(isClaimant2)
                ? caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantListTwo()
                : caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationClaimantList();
        }
        return null;
    }

    private boolean hasRepresentationType(CaseData caseData) {
        return caseData.getFinalOrderRepresentation() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null;
    }

    public String buildClaimantRepresentationText(CaseData caseData, String name, FinalOrdersClaimantRepresentationList type, Boolean isClaimant2) {
        return switch (type) {
            case COUNSEL_FOR_CLAIMANT -> format("Counsel for %s, the claimant.", name);
            case SOLICITOR_FOR_CLAIMANT -> format("Solicitor for %s, the claimant.", name);
            case COST_DRAFTSMAN_FOR_THE_CLAIMANT -> format("Costs draftsman for %s, the claimant.", name);
            case THE_CLAIMANT_IN_PERSON -> format("%s, the claimant, in person.", name);
            case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT -> format("A lay representative for %s, the claimant.", name);
            case LEGAL_EXECUTIVE_FOR_THE_CLAIMANT -> format("Legal Executive for %s, the claimant.", name);
            case SOLICITORS_AGENT_FOR_THE_CLAIMANT -> format("Solicitor's Agent for %s, the claimant.", name);
            case CLAIMANT_NOT_ATTENDING -> getClaimantNotAttendingText(caseData, isClaimant2, name);
        };
    }

    private String getClaimantNotAttendingText(String name, FinalOrdersClaimantDefendantNotAttending notAttendingType) {
        return switch (notAttendingType) {
            case SATISFIED_REASONABLE_TO_PROCEED -> format(
                "%s, the claimant, did not attend the trial. The Judge was satisfied that they had %s",
                name, NOTICE_RECEIVED_CAN_PROCEED
            );
            case SATISFIED_NOTICE_OF_TRIAL -> format(
                "%s, the claimant, did not attend the trial and, whilst the Judge was satisfied that they had %s",
                name, NOTICE_RECEIVED_CANNOT_PROCEED
            );
            case NOT_SATISFIED_NOTICE_OF_TRIAL -> format(
                "%s, the claimant, did not attend the trial. %s",
                name, NOTICE_NOT_RECEIVED_CANNOT_PROCEED
            );
        };
    }

    public String getClaimantNotAttendingText(CaseData caseData, Boolean isClaimant2, String name) {
        FinalOrdersClaimantDefendantNotAttending notAttendingType = getNotAttendingType(caseData, isClaimant2);

        return notAttendingType != null ? getClaimantNotAttendingText(name, notAttendingType) : "";
    }

    private FinalOrdersClaimantDefendantNotAttending getNotAttendingType(CaseData caseData, Boolean isClaimant2) {
        if (hasRepresentationType(caseData)) {

            if (Boolean.FALSE.equals(isClaimant2)
                && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex() != null) {
                return caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureClaimantComplex().getList();
            }

            if (caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedClaimTwoComplex() != null) {
                return caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedClaimTwoComplex().getListClaimTwo();
            }
        }
        return null;
    }
}
