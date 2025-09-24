/**
 * Update notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">You have accepted ${respondent1PartyName}''s offer to pay £{fullAdmitPayImmediatelyPaymentAmount} immediately. This amount includes interest if it has been claimed which may continue to accrue to the date of Judgment, settlement agreement or earlier payment. Funds must be received in your account by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p><p class="govuk-body">If you don''t receive the money by then, <a href="{COUNTY_COURT_JUDGEMENT_URL}"  rel="noopener noreferrer" class="govuk-link">you can request a County Court Judgment(CCJ)</a>.</p>',
    description_Cy = '<p class="govuk-body">Rydych wedi derbyn cynnig ${respondent1PartyName} i dalu £{fullAdmitPayImmediatelyPaymentAmount} ar unwaith. Mae’r swm hwn yn cynnwys y llog os hawliwyd a all barhau i gronni hyd at ddyddiad Dyfarniad, cytundeb setliad neu daliad cynharach. Rhaid i’r arian gyrraedd eich cyfrif erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}.</p><p class="govuk-body">Os na fyddwch wedi cael yr arian erbyn hynny, <a href="{COUNTY_COURT_JUDGEMENT_URL}"  rel="noopener noreferrer" class="govuk-link">gallwch wneud cais am Ddyfarniad Llys Sifil (CCJ)</a>.</p>'
WHERE template_name = 'Notice.AAA6.ClaimantIntent.FullAdmit.Claimant' AND notification_role = 'CLAIMANT';
