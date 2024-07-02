package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.dsl.DslPart;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArray;

public class CmcConsumerTestUtil {

    private CmcConsumerTestUtil() {
    }

    static DslPart buildBundleCreateResponseDsl() {
        return newJsonArray(response -> response
            .object(cmcClaim -> cmcClaim
                .stringType("submitterId", "123")
                .stringType("letterHolderId", "letterHolderId")
                .stringType("defendantId", "defendantId")
                .stringType("externalId", "externalId")
                .stringType("referenceNumber", "referenceNumber")
                .object("claim", claimData ->
                    claimData
                        .object("amount", amount ->
                            amount
                                .minArrayLike("rows", 1, rows ->
                                    rows
                                        .stringType("reason", "No reason")
                                        .numberType("amount", 20.0)))
                        .minArrayLike("claimants", 1, claimants ->
                            claimants
                                .stringType("name", "name")
                                .object("address", address ->
                                    address
                                        .stringType("line1", "line1")
                                        .stringType("line2", "line2")
                                        .stringType("line3", "line3")
                                        .stringType("city", "city")
                                        .stringType("county", "county")
                                        .stringType("postcode", "postcode"))
                                .object("correspondenceAddress", address ->
                                    address
                                        .stringType("line1", "line1")
                                        .stringType("line2", "line2")
                                        .stringType("line3", "line3")
                                        .stringType("city", "city")
                                        .stringType("county", "county")
                                        .stringType("postcode", "postcode"))
                                .object("breathingSpace", breathingSpace ->
                                    breathingSpace
                                        .stringType("bsReferenceNumber", "bsReferenceNumber")
                                        .date("bsEnteredDate", "yyyy-MM-dd")
                                        .date("bsLiftedDate", "yyyy-MM-dd")
                                        .date("bsEnteredDateByInsolvencyTeam", "yyyy-MM-dd")
                                        .date("bsLiftedDateByInsolvencyTeam", "yyyy-MM-dd")
                                        .date("bsExpectedEndDate", "yyyy-MM-dd")
                                        .stringType("bsLiftedFlag", "bsLiftedFlag")
                                )
                        )
                )
                .date("responseDeadline", "yyyy-MM-dd")
                .booleanType("moreTimeRequested")
                .stringType("submitterEmail", "submitterEmail")
                .object("response", response1 -> response1
                    .stringType("responseType", "FULL_DEFENCE")
                    .object("paymentIntention", paymentIntention ->
                        paymentIntention
                            .stringType("paymentOption", "IMMEDIATELY")
                            .date("paymentDate", "yyyy-MM-dd"))
                    .object("paymentDeclaration", paymentDeclaration ->
                        paymentDeclaration
                            .date("paidDate", "yyyy-MM-dd")
                            .numberType("paidAmount", 10.0))
                    .stringType("responseMethod", "OFFLINE"))
                .date("moneyReceivedOn", "yyyy-MM-dd")
                .date("countyCourtJudgmentRequestedAt", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                .date("createdAt", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                .date("reDeterminationRequestedAt", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                .date("intentionToProceedDeadline", "yyyy-MM-dd")
                .date("claimantRespondedAt", "yyyy-MM-dd'T'HH:mm:ss'Z'")
                .object("claimantResponse", claimantResponse ->
                    claimantResponse
                        .numberType("amountPaid", 50.0)
                        .stringType("paymentReceived", "paymentReceived")
                        .stringType("settleForAmount", "settleForAmount")
                        .object("courtDetermination", courtDetermination ->
                            courtDetermination
                                .object("courtDecision", courtDecision ->
                                    courtDecision
                                        .stringType("paymentOption", "IMMEDIATELY")
                                        .date("paymentDate", "yyyy-MM-dd"))
                                .object("courtPaymentIntention", courtDecision ->
                                    courtDecision
                                        .stringType("paymentOption", "IMMEDIATELY")
                                        .date("paymentDate", "yyyy-MM-dd"))
                                .stringType("rejectionReason", "rejectionReason")
                                .numberType("disposableIncome", 30.0))
                        .stringType("formaliseOption", "SETTLEMENT"))
                .stringType("state", "OPEN")
                .stringType("proceedOfflineReason", "OTHER")
                .object("settlement", settlement ->
                    settlement
                        .minArrayLike("partyStatements", 1, partyStatements ->
                            partyStatements
                                .stringType("type", "OFFER")
                                .stringType("madeBy", "CLAIMANT")
                                .object("offer", offer ->
                                    offer
                                        .stringType("content", "content")
                                        .date("completionDate", "yyyy-MM-dd")
                                        .object("paymentIntention", paymentIntention ->
                                            paymentIntention
                                                .stringType("paymentOption", "IMMEDIATELY")
                                                .date("paymentDate", "yyyy-MM-dd")))))
            )).build();
    }
}
