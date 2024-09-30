package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static java.lang.String.format;

@Component
public class DefendantAttendsOrRepresentedTextBuilder {

    private static final String NOTICE_RECEIVED_CAN_PROCEED = "received notice of the trial and determined that it was reasonable to proceed in their absence.";
    private static final String NOTICE_RECEIVED_CANNOT_PROCEED = "received notice of the trial, the Judge was not satisfied that it was "
        + "reasonable to proceed in their absence.";
    private static final String NOTICE_NOT_RECEIVED_CANNOT_PROCEED = "The Judge was not satisfied that they had received notice of the hearing "
        + "and it was not reasonable to proceed in their absence.";

    public String defendantBuilder(CaseData caseData, Boolean isDefendant2) {
        String name = getDefendantName(caseData, isDefendant2);
        FinalOrdersDefendantRepresentationList type = getDefendantRepresentationType(caseData, isDefendant2);

        return type != null ? buildRespondentRepresentationText(caseData, name, type, isDefendant2) : "";
    }

    private String getDefendantName(CaseData caseData, Boolean isDefendant2) {
        return Boolean.TRUE.equals(isDefendant2) ? caseData.getRespondent2().getPartyName() : caseData.getRespondent1().getPartyName();
    }

    private FinalOrdersDefendantRepresentationList getDefendantRepresentationType(CaseData caseData, Boolean isDefendant2) {
        if (hasRepresentationType(caseData)) {
            return Boolean.TRUE.equals(isDefendant2)
                ? caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantTwoList()
                : caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTypeRepresentationDefendantList();
        }
        return null;
    }

    private boolean hasRepresentationType(CaseData caseData) {
        return caseData.getFinalOrderRepresentation() != null
            && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex() != null;
    }

    public String buildRespondentRepresentationText(CaseData caseData, String name, FinalOrdersDefendantRepresentationList type, Boolean isDefendant2) {
        return switch (type) {
            case COUNSEL_FOR_DEFENDANT -> format("Counsel for %s, the defendant.", name);
            case SOLICITOR_FOR_DEFENDANT -> format("Solicitor for %s, the defendant.", name);
            case COST_DRAFTSMAN_FOR_THE_DEFENDANT -> format("Costs draftsman for %s, the defendant.", name);
            case THE_DEFENDANT_IN_PERSON -> format("%s, the defendant, in person.", name);
            case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT -> format("A lay representative for %s, the defendant.", name);
            case LEGAL_EXECUTIVE_FOR_THE_DEFENDANT -> format("Legal Executive for %s, the defendant.", name);
            case SOLICITORS_AGENT_FOR_THE_DEFENDANT -> format("Solicitor's Agent for %s, the defendant.", name);
            case DEFENDANT_NOT_ATTENDING -> buildDefendantNotAttendingText(caseData, isDefendant2, name);
        };
    }

    public String buildDefendantNotAttendingText(CaseData caseData, Boolean isDefendant2, String name) {
        FinalOrdersClaimantDefendantNotAttending notAttendingType = getNotAttendingType(caseData, isDefendant2);
        return notAttendingType != null ? getDefendantNotAttendingText(notAttendingType, name) : "";
    }

    private FinalOrdersClaimantDefendantNotAttending getNotAttendingType(CaseData caseData, Boolean isDefendant2) {
        if (hasRepresentationType(caseData)) {
            return Boolean.FALSE.equals(isDefendant2) && caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex() != null
                ? caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureComplex().getListDef()
                : caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureDefTwoComplex() != null
                ? caseData.getFinalOrderRepresentation().getTypeRepresentationComplex().getTrialProcedureDefTwoComplex().getListDefTwo()
                : null;
        }
        return null;
    }

    private String getDefendantNotAttendingText(FinalOrdersClaimantDefendantNotAttending notAttendingType, String name) {
        return switch (notAttendingType) {
            case SATISFIED_REASONABLE_TO_PROCEED -> format("%s, the defendant, did not attend the trial. The Judge was satisfied that they had %s",
                                                           name, NOTICE_RECEIVED_CAN_PROCEED);
            case SATISFIED_NOTICE_OF_TRIAL -> format("%s, the defendant, did not attend the trial and, whilst the Judge was satisfied that they had %s",
                                                     name, NOTICE_RECEIVED_CANNOT_PROCEED);
            case NOT_SATISFIED_NOTICE_OF_TRIAL -> format("%s, the defendant, did not attend the trial. %s",
                                                         name, NOTICE_NOT_RECEIVED_CANNOT_PROCEED);
        };
    }
}
