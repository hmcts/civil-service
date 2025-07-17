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
                                                   CaseData.CaseDataBuilder<?, ?> updatedData,
                                                   LocalDateTime responseDate,
                                                   LocalDateTime applicant1Deadline) {
        if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
            responseHasSameUpdateValues(callbackParams, updatedData, caseData, responseDate, applicant1Deadline);
        } else if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
            responseDoesNotHaveSameUpdateValues(
                callbackParams,
                updatedData,
                responseDate,
                applicant1Deadline,
                caseData
            );
        }
    }

    void updateResponseDataForSecondRespondent(CallbackParams callbackParams,
                                               CaseData.CaseDataBuilder<?, ?> updatedData,
                                               LocalDateTime responseDate,
                                               CaseData caseData,
                                               LocalDateTime applicant1Deadline) {
        updatedData.respondent2ResponseDate(responseDate)
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));
        updateRespondent2StatementOfTruth(callbackParams, updatedData, caseData);
        setApplicantDeadLineIfRespondent1DateExist(caseData, updatedData, applicant1Deadline);
    }

    void updateResponseDataForBothRespondent(CallbackParams callbackParams,
                                             CaseData.CaseDataBuilder<?, ?> updatedData,
                                             LocalDateTime responseDate,
                                             CaseData caseData,
                                             LocalDateTime applicant1Deadline) {
        updatedData.respondent1ResponseDate(responseDate)
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));

        setApplicantDeadlineIfRequired(caseData, updatedData, applicant1Deadline);
        updateRespondent2AdressesAndSetDeadline(caseData, updatedData);
        updateRespondent2Date(caseData, updatedData, responseDate);
        updateRespondent1StatementOfTruth(callbackParams, caseData, updatedData);
    }

    private void updateRespondent2StatementOfTruth(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> updatedData, CaseData caseData) {

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent2DQ.Respondent2DQBuilder dq = caseData.getRespondent2DQ().toBuilder()
            .respondent2DQStatementOfTruth(statementOfTruth);
        handleCourtLocationForRespondent2DQ(caseData, dq, callbackParams);
        updatedData.respondent2DQ(dq.build());

        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private Optional<CaseLocationCivil> buildWithMatching(LocationRefData courtLocation) {
        return Optional.ofNullable(courtLocation).map(LocationHelper::buildCaseLocation);
    }

    private void setApplicantDeadLineIfRespondent1DateExist(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, LocalDateTime applicant1Deadline) {
        if (caseData.getRespondent1ResponseDate() != null) {
            updatedData
                .nextDeadline(applicant1Deadline.toLocalDate())
                .applicant1ResponseDeadline(applicant1Deadline);
        } else {
            updatedData.nextDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate());
        }
    }

    private void setApplicantDeadlineIfRequired(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, LocalDateTime applicant1Deadline) {
        if (isRespondent2NotPresent(caseData)
            || isApplicant2Present(caseData)
            || caseData.getRespondent2ResponseDate() != null) {
            updatedData
                .applicant1ResponseDeadline(applicant1Deadline)
                .nextDeadline(applicant1Deadline.toLocalDate());
        }
    }

    private boolean isApplicant2Present(CaseData caseData) {
        return caseData.getAddApplicant2() != null && caseData.getAddApplicant2() == YES;
    }

    private void updateRespondent2AdressesAndSetDeadline(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .build();

            updatedData
                .respondent2(updatedRespondent2)
                .respondent2Copy(null)
                .respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());

            if (caseData.getRespondent2ResponseDate() == null) {
                updatedData.nextDeadline(caseData.getRespondent2ResponseDeadline().toLocalDate());
            }
        }
    }

    private void updateRespondent2Date(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData, LocalDateTime responseDate) {
        if (isRespondent2SameLegalRep(caseData)) {
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
                updatedData.respondent2ClaimResponseType(caseData.getRespondent1ClaimResponseType());
            }

            updatedData.respondent2ResponseDate(responseDate);
        }
    }

    private void updateRespondent1StatementOfTruth(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
            .respondent1DQStatementOfTruth(statementOfTruth);
        handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
        updatedData.respondent1DQ(dq.build());
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
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
                                             CaseData.CaseDataBuilder<?, ?> updatedData,
                                             CaseData caseData,
                                             LocalDateTime responseDate,
                                             LocalDateTime applicant1Deadline) {
        updatedData.respondent2ClaimResponseType(caseData.getRespondent1ClaimResponseType());
        updatedData
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE))
            .respondent1ResponseDate(responseDate)
            .respondent2ResponseDate(responseDate)
            .nextDeadline(applicant1Deadline.toLocalDate())
            .applicant1ResponseDeadline(applicant1Deadline);

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
            .respondent1DQStatementOfTruth(statementOfTruth);

        handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);

        updatedData.respondent1DQ(dq.build());

        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private void responseDoesNotHaveSameUpdateValues(CallbackParams callbackParams,
                                                     CaseData.CaseDataBuilder<?, ?> updatedData,
                                                     LocalDateTime responseDate,
                                                     LocalDateTime applicant1Deadline,
                                                     CaseData caseData) {
        updatedData
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE))
            .respondent1ResponseDate(responseDate)
            .respondent2ResponseDate(responseDate)
            .nextDeadline(applicant1Deadline.toLocalDate())
            .applicant1ResponseDeadline(applicant1Deadline);

        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        if (FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())) {
            Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
                .respondent1DQStatementOfTruth(statementOfTruth);
            handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
            updatedData.respondent1DQ(dq.build());

        } else {
            updatedData.respondent1DQ(null);
        }

        if (FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType())) {

            Respondent2DQ.Respondent2DQBuilder dq2 = caseData.getRespondent2DQ().toBuilder()
                .respondent2DQStatementOfTruth(statementOfTruth);
            handleCourtLocationForRespondent2DQ(caseData, dq2, callbackParams);
            updatedData.respondent2DQ(dq2.build());

        } else {
            updatedData.respondent2DQ(null);
        }

        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
    }

    private List<LocationRefData> getLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }
}
