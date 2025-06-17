update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant", "Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant"}'
where name = 'Scenario.AAA6.ClaimantIntent.GoToHearing.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant","Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant","Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant","Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant","Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant","Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant","Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant"}'
where name = 'Scenario.AAA6.ClaimantIntent.Mediation.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant",
                                "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant",
                                "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant",
                                "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant",
                                "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant",
                                "Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant",
                                "Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant",
                                "Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant",
                                "Notice.AAA6.ClaimantIntent.Mediation.Claimant",
                                "Notice.AAA6.ClaimantIntent.GoToHearing.Claimant"}'
where name = 'Scenario.AAA6.ClaimantIntent.Mediation.CARM.Claimant';

update dbs.scenario
set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.Response.Await", "Notice.AAA6.ClaimIssue.HWF.Requested", "Notice.AAA6.ClaimIssue.HWF.FullRemission", "Notice.AAA6.ClaimIssue.HWF.PhonePayment",
                                "Notice.AAA6.DefResponse.MoreTimeRequested.Claimant", "Notice.AAA6.DefResponse.FullAdmit.PayImmediately.Claimant", "Notice.AAA6.DefResponse.PartAdmit.PayImmediately.Claimant",
                                "Notice.AAA6.DefResponse.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.FullOrPartAdmit.PayByInstalments.Claimant",
                                "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Claimant", "Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayByInstallments.Claimant",
                                "Notice.AAA6.DefResponse.PartAdmit.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant", "Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant",
                                "Notice.AAA6.DefResponse.Full Defence. FullDispute.SuggestedMediation.Claimant", "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "Notice.AAA6.ClaimantIntent.PartAdmit.Claimant",
                                "Notice.AAA6.ClaimantIntent.FullAdmit.Claimant", "Notice.AAA6.ClaimantIntent.Mediation.Claimant", "Notice.AAA6.ClaimantIntent.GoToHearing.Claimant",
                                "Notice.AAA6.ClaimantIntent.SettlementAgreement.AcceptOrRejectDefPlan.Claimant", "Notice.AAA6.ClaimantIntent.SettlementNoResponse.Claimant",
                                "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantAccepted.Claimant", "Notice.AAA6.ClaimantIntent.SettlementAgreement.DefendantRejected.Claimant",
                                "Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant", "Notice.AAA6.ClaimantIntent.Mediation.CARM.Claimant", "Notice.AAA6.MediationSuccessful.CARM.Claimant",
                                "Notice.AAA6.MediationUnsuccessful.NOTClaimant1NonContactable.CARM.Claimant", "Notice.AAA6.MediationUnsuccessful.Claimant1NonAttendance.CARM.Claimant",
                                "Notice.AAA6.MediationUnsuccessful.TrackChange.CARM.Claimant",
                                "Notice.AAA6.ClaimantIntent.RequestedCCJ.Claimant", "Notice.AAA6.ClaimantIntent.Defendant.OrgLtdCo.Claimant", "Notice.AAA6.CP.Hearing.Scheduled", "Notice.AAA6.CP.Trial Arrangements.Required",
                                "Notice.AAA6.CP.Bundle.Ready", "Notice.AAA6.CP.OrderMade.Completed", "Notice.AAA6.CP.HearingDocuments.Upload", "Notice.AAA6.CP.HearingDocuments.OtherPartyUploaded", "Notice.AAA6.CP.HearingFee.Paid",
                                "Notice.AAA6.CP.HearingFee.Required", "Notice.AAA6.CP.HearingFee.HWF.AppliedFor", "Notice.AAA6.CP.HearingFee.HWF.Rejected", "Notice.AAA6.CP.HearingFee.HWF.PartRemission",
                                "Notice.AAA6.CP.HearingFee.HWF.FullRemission", "Notice.AAA6.CP.HearingFee.HWF.InfoRequired", "Notice.AAA6.CP.HearingFee.HWF.InvalidRef", "Notice.AAA6.CP.HearingFee.HWF.ReviewUpdate"}'
where name = 'Scenario.AAA6.ClaimantIntent.ClaimSettledEvent.Claimant';
