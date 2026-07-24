function unsafe(logger: Logger, claim: Claim, telemetryClient: TelemetryClient): void {
  // ruleid: civil.typescript.pii-field-to-log
  logger.info(`Email: ${claim.emailAddress}`);

  // ruleid: civil.typescript.pii-field-to-log
  logger.warn('Amount', claim.claimAmount);

  // ruleid: civil.typescript.sensitive-object-to-log
  logger.info('Claim', claim);

  const paymentInfo = claim.paymentInfo;
  // ruleid: civil.typescript.sensitive-object-to-log
  telemetryClient.trackEvent(paymentInfo);

  // ruleid: civil.typescript.pii-field-to-log
  telemetryClient.trackTrace({message: claim.partyName});

  // ruleid: civil.typescript.raw-console
  console.log(claim.id);
}

function safe(logger: Logger, claim: Claim, caseId: string, userId: string): void {
  // ok: civil.typescript.pii-field-to-log
  logger.info(`Processing case ${claim.id}`);

  // ok: civil.typescript.sensitive-object-to-log
  logger.info('Processing case and user', caseId, userId);

  const paymentStatus = getStatus(claim.paymentReference);
  // ok: civil.typescript.pii-field-to-log
  logger.info('Payment state', paymentStatus.status);

  // ok: civil.typescript.pii-field-to-log
  logger.debug('Interest option', claim.interest.interestClaimOptions);
}
