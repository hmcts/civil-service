export const serviceUpdateDTO = (caseId: number, paymentStatus: string) => ({
  service_request_reference: '1324646546456',
  ccd_case_number: caseId,
  service_request_amount: '167.00',
  service_request_status: paymentStatus,
  payment: {
    payment_amount: 167.0,
    payment_reference: '13213223',
    payment_method: 'by account',
    case_reference: 'example of case ref',
  },
});
