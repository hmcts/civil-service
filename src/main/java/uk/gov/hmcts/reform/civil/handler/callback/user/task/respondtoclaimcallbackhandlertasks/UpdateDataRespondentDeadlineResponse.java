package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
public class UpdateDataRespondentDeadlineResponse {

    private final LocationReferenceDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;

    public UpdateDataRespondentDeadlineResponse(LocationReferenceDataService locationRefDataService,
                                                CourtLocationUtils courtLocationUtils) {

        this.locationRefDataService = locationRefDataService;
        this.courtLocationUtils = courtLocationUtils;
    }

    void updateBothRespondentsResponseSameLegalRep(CallbackParams callbackParams,
                                                   CaseData caseData,
                                                   LocalDateTime responseDate,
                                                   LocalDateTime applicant1Deadline) {
        if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
            responseHasSameUpdateValues(callbackParams, caseData, responseDate, applicant1Deadline);
        } else if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
            responseDoesNotHaveSameUpdateValues(
                callbackParams,
                responseDate,
                applicant1Deadline,
                caseData
            );
        }
    }

    void updateResponseDataForSecondRespondent(CallbackParams callbackParams,
                                               CaseData updatedData,
                                               CaseData caseData,
                                               LocalDateTime applicant1Deadline) {
        updatedData.setBusinessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));
        updateRespondent2StatementOfTruth(callbackParams, caseData);
        setApplicantDeadLineIfRespondent1DateExist(caseData, applicant1Deadline);
    }

    void updateResponseDataForBothRespondent(CallbackParams callbackParams,
                                             LocalDateTime responseDate,
                                             CaseData caseData,
                                             LocalDateTime applicant1Deadline) {
        caseData.setRespondent1ResponseDate(responseDate);
        caseData.setBusinessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));

        setApplicantDeadlineIfRequired(caseData, applicant1Deadline);
        updateRespondent2AdressesAndSetDeadline(caseData);
        updateRespondent2Date(caseData, responseDate);
        updateRespondent1StatementOfTruth(callbackParams, caseData);
    }

    private void updateRespondent2StatementOfTruth(CallbackParams callbackParams, CaseData caseData) {

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent2DQ.Respondent2DQBuilder dq = caseData.getRespondent2DQ().toBuilder()
            .respondent2DQStatementOfTruth(statementOfTruth);
        handleCourtLocationForRespondent2DQ(caseData, dq, callbackParams);
        caseData.setRespondent2DQ(dq.build());

        caseData.setUiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private Optional<CaseLocationCivil> buildWithMatching(LocationRefData courtLocation) {
        return Optional.ofNullable(courtLocation).map(LocationHelper::buildCaseLocation);
    }

    private void setApplicantDeadLineIfRespondent1DateExist(CaseData caseData, LocalDateTime applicant1Deadline) {
        if (caseData.getRespondent1ResponseDate() != null) {
            caseData
                .setNextDeadline(applicant1Deadline.toLocalDate());
            caseData.setApplicant1ResponseDeadline(applicant1Deadline);
        } else {
            caseData.setNextDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate());
        }
    }

    private void setApplicantDeadlineIfRequired(CaseData caseData, LocalDateTime applicant1Deadline) {
        if (isRespondent2NotPresent(caseData)
            || isApplicant2Present(caseData)
            || caseData.getRespondent2ResponseDate() != null) {
            caseData.setApplicant1ResponseDeadline(applicant1Deadline);
            caseData.setNextDeadline(applicant1Deadline.toLocalDate());
        }
    }

    private boolean isApplicant2Present(CaseData caseData) {
        return caseData.getAddApplicant2() != null && caseData.getAddApplicant2() == YES;
    }

    private void updateRespondent2AdressesAndSetDeadline(CaseData caseData) {
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .build();

            caseData
                .setRespondent2(updatedRespondent2);
            caseData.setRespondent2Copy(null);
            caseData.setRespondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());

            if (caseData.getRespondent2ResponseDate() == null) {
                caseData.setNextDeadline(caseData.getRespondent2ResponseDeadline().toLocalDate());
            }
        }
    }

    private void updateRespondent2Date(CaseData caseData, LocalDateTime responseDate) {
        if (isRespondent2SameLegalRep(caseData)) {
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
                caseData.setRespondent2ClaimResponseType(caseData.getRespondent1ClaimResponseType());
            }

            caseData.setRespondent2ResponseDate(responseDate);
        }
    }

    private void updateRespondent1StatementOfTruth(CallbackParams callbackParams, CaseData caseData) {
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
            .respondent1DQStatementOfTruth(statementOfTruth);
        handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
        caseData.setRespondent1DQ(dq.build());
        caseData.setUiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private void handleCourtLocationForRespondent1DQ(CaseData caseData, Respondent1DQ.Respondent1DQBuilder dq,
                                                     CallbackParams callbackParams) {

        if (Optional.ofNullable(caseData.getRespondent1DQ())
            .map(Respondent1DQ::getRespondent1DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations)
            .map(DynamicList::getValue).isPresent()) {
            DynamicList courtLocations = caseData
                .getRespondent1DQ().getRespondent1DQRequestedCourt().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                getLocationData(callbackParams), courtLocations);
            RequestedCourt.RequestedCourtBuilder dqBuilder = caseData.getRespondent1DQ()
                .getRespondent1DQRequestedCourt().toBuilder()
                .responseCourtLocations(null)
                .responseCourtCode(Optional.ofNullable(courtLocation)
                                       .map(LocationRefData::getCourtLocationCode)
                                       .orElse(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                                                   .getResponseCourtCode()));
            buildWithMatching(courtLocation).ifPresent(dqBuilder::caseLocation);
            dq.respondent1DQRequestedCourt(dqBuilder.build());
        } else if (Optional.ofNullable(caseData.getRespondent1DQ())
            .map(Respondent1DQ::getRespondent1DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations).isPresent()) {
            dq.respondent1DQRequestedCourt(caseData.getRespondent1DQ()
                                               .getRespondent1DQRequestedCourt()
                                               .toBuilder().responseCourtLocations(null).build());
        }
    }

    private void handleCourtLocationForRespondent2DQ(CaseData caseData, Respondent2DQ.Respondent2DQBuilder dq,
                                                     CallbackParams callbackParams) {

        if (Optional.ofNullable(caseData.getRespondent2DQ())
            .map(Respondent2DQ::getRespondent2DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations)
            .map(DynamicList::getValue).isPresent()) {
            DynamicList courtLocations = caseData
                .getRespondent2DQ().getRespondent2DQRequestedCourt().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                getLocationData(callbackParams), courtLocations);
            RequestedCourt.RequestedCourtBuilder dqBuilder = caseData.getRespondent2DQ().getRequestedCourt().toBuilder()
                .responseCourtLocations(null)
                .responseCourtCode(Optional.ofNullable(courtLocation)
                                       .map(LocationRefData::getCourtLocationCode)
                                       .orElse(caseData.getRespondent2DQ().getRespondent2DQRequestedCourt()
                                                   .getResponseCourtCode()));
            buildWithMatching(courtLocation).ifPresent(dqBuilder::caseLocation);
            dq.respondent2DQRequestedCourt(dqBuilder.build());
        } else if (Optional.ofNullable(caseData.getRespondent2DQ())
            .map(Respondent2DQ::getRespondent2DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations).isPresent()) {
            dq.respondent2DQRequestedCourt(caseData.getRespondent2DQ()
                                               .getRespondent2DQRequestedCourt()
                                               .toBuilder().responseCourtLocations(null).build());
        }

    }

    private boolean isRespondent2SameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private boolean isRespondent2NotPresent(CaseData caseData) {
        return caseData.getAddRespondent2() != null
            && caseData.getAddRespondent2() == NO;
    }

    private void responseHasSameUpdateValues(CallbackParams callbackParams,
                                             CaseData caseData,
                                             LocalDateTime responseDate,
                                             LocalDateTime applicant1Deadline) {
        caseData.setRespondent2ClaimResponseType(caseData.getRespondent1ClaimResponseType());
        caseData
            .setBusinessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));
        caseData.setRespondent1ResponseDate(responseDate);
        caseData.setRespondent2ResponseDate(responseDate);
        caseData.setNextDeadline(applicant1Deadline.toLocalDate());
        caseData.setApplicant1ResponseDeadline(applicant1Deadline);

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
            .respondent1DQStatementOfTruth(statementOfTruth);

        handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);

        caseData.setRespondent1DQ(dq.build());

        caseData.setUiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private void responseDoesNotHaveSameUpdateValues(CallbackParams callbackParams,
                                                     LocalDateTime responseDate,
                                                     LocalDateTime applicant1Deadline,
                                                     CaseData caseData) {
        caseData
            .setBusinessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));
        caseData.setRespondent1ResponseDate(responseDate);
        caseData.setRespondent2ResponseDate(responseDate);
        caseData.setNextDeadline(applicant1Deadline.toLocalDate());
        caseData.setApplicant1ResponseDeadline(applicant1Deadline);

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())) {
            Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
                .respondent1DQStatementOfTruth(statementOfTruth);
            handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
            caseData.setRespondent1DQ(dq.build());

        } else {
            caseData.setRespondent1DQ(null);
        }

        if (FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType())) {

            Respondent2DQ.Respondent2DQBuilder dq2 = caseData.getRespondent2DQ().toBuilder()
                .respondent2DQStatementOfTruth(statementOfTruth);
            handleCourtLocationForRespondent2DQ(caseData, dq2, callbackParams);
            caseData.setRespondent2DQ(dq2.build());

        } else {
            caseData.setRespondent2DQ(null);
        }

        caseData.setUiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private List<LocationRefData> getLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }
}
