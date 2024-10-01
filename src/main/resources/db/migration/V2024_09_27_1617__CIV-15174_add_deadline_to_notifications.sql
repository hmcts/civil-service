/**
 * Update tables
 */
ALTER TABLE dbs.dashboard_notifications_templates
  ADD COLUMN deadline_param VARCHAR(256);

ALTER TABLE dbs.dashboard_notifications
  ADD COLUMN deadline TIMESTAMP;

UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'respondent1ResponseDeadline' WHERE template_name = 'Notice.AAA6.ClaimIssue.Response.Required';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'claimSettledObjectionsDeadline' WHERE template_name = 'Notice.AAA6.ClaimantIntent.ClaimSettled.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'respondent1SettlementAgreementDeadline' WHERE template_name = 'Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithClaimant.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'respondent1SettlementAgreementDeadline' WHERE template_name = 'Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantAcceptsPlan.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'respondent1AdmittedAmountPaymentDeadline' WHERE template_name = 'Notice.AAA6.ClaimantIntent.PartAdmit.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'respondent1SettlementAgreementDeadline' WHERE template_name = 'Notice.AAA6.ClaimantIntent.SettlementAgreement.ClaimantRejectsPlan.CourtAgreesWithDefendant.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'respondent1AdmittedAmountPaymentDeadline' WHERE template_name = 'Notice.AAA6.DefResponse.FullOrPartAdmit.PayImmediately.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'respondent1ResponseDeadline' WHERE template_name = 'Notice.AAA6.DefResponse.MoreTimeRequested.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'respondent1AdmittedAmountPaymentDeadline' WHERE template_name = 'Notice.AAA6.DefResponse.OrgOrLtdCompany.FullOrPartAdmit.PayBySetDate.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'applicant1ClaimSettledObjectionsDeadline' WHERE template_name = 'Notice.AAA6.ClaimantIntent.ClaimSettleEvent.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'applicant1ResponseDeadline' WHERE template_name = 'Notice.AAA6.DefResponse.FullDefence.AlreadyPaid.Claimant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'applicant1ResponseDeadline' WHERE template_name = 'Notice.AAA6.DefResponse.FullDefence.FullDispute.RefusedMediation.Claimant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'applicant1ResponseDeadline' WHERE template_name = 'Notice.AAA6.DefResponse.FullDefence.FullDispute.SuggestedMediation.Claimant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'applicant1ResponseDeadline' WHERE template_name = 'Notice.AAA6.DefResponse.FullDefence.FullDispute.Multi.Int.Fast.Claimant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'trialArrangementDeadline' WHERE template_name = 'Notice.AAA6.CP.Trial.Arrangements.Required.Claimant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'trialArrangementDeadline' WHERE template_name = 'Notice.AAA6.CP.Trial.Arrangements.Required.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'hearingDueDate' WHERE template_name = 'Notice.AAA6.CP.HearingFee.Required.Claimant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'hearingDueDate' WHERE template_name = 'Notice.AAA6.CP.HearingFee.HWF.Rejected';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'hearingDueDate' WHERE template_name = 'Notice.AAA6.CP.HearingFee.HWF.PartRemission';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'applicant1ResponseDeadline' WHERE template_name = 'Notice.AAA6.DefResponse.FullDefence.FullDispute.CARM.Claimant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'requestForReconsiderationDeadline' WHERE template_name = 'Notice.AAA6.CP.SDOMadebyLA.Claimant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'requestForReconsiderationDeadline' WHERE template_name = 'Notice.AAA6.CP.SDOMadebyLA.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'requestForReconsiderationDeadline' WHERE template_name = 'Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Claimant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'requestForReconsiderationDeadline' WHERE template_name = 'Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'judgeRequestMoreInfoByDate' WHERE template_name = 'Notice.AAA6.GeneralApps.MoreInfoRequired.Applicant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'judgeRequestMoreInfoByDate' WHERE template_name = 'Notice.AAA6.GeneralApps.MoreInfoRequired.Respondent';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'generalAppNotificationDeadlineDate' WHERE template_name = 'Notice.AAA6.GeneralApps.UrgentApplicationMade.Respondent';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'judgeRequestMoreInfoByDate' WHERE template_name = 'Notice.AAA6.GeneralApps.UrgentApplicationUncloaked.Respondent';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'generalAppNotificationDeadlineDate' WHERE template_name = 'Notice.AAA6.GeneralApps.NonUrgentApplicationMade.Respondent';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'judgeRequestMoreInfoByDate' WHERE template_name = 'Notice.AAA6.GeneralApps.NonUrgentApplicationUncloaked.Respondent';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'writtenRepApplicantDeadline' WHERE template_name = 'Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Applicant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'writtenRepRespondentDeadline' WHERE template_name = 'Notice.AAA6.GeneralApps.WrittenRepresentationRequired.Respondent';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'priorityNotificationDeadline' WHERE template_name = 'Notice.AAA6.CP.HearingDocuments.Upload.Claimant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'priorityNotificationDeadline' WHERE template_name = 'Notice.AAA6.CP.HearingDocuments.Upload.Defendant';
UPDATE dbs.dashboard_notifications_templates SET deadline_param = 'priorityNotificationDeadline' WHERE template_name = 'Notice.AAA6.JudgmentsOnline.DefaultJudgmentIssued.Defendant';
