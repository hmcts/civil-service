export const subheadings = {
  describe: 'Describe your claim',
  uploadDocs: 'Upload supporting documents (optional)',
};

export const paragraphs = {
  timeline: "Do not give us a detailed timeline - we'll ask for that separately.",
};

export const inputs = {
  details: {
    label: 'Description of claim',
    selector: '#detailsOfClaim',
  },
  uploadFile: {
    label: 'Upload file (Optional)',
    hintText:
      'We accept documents in these formats: pdf, txt, doc, dot, docx, rtf, xlt, xlsx, jpg, jpeg or png. ' +
      'Please do not upload password protected documents as this will prevent the claim from being processed.',
    selector: '#specClaimDetailsDocumentFiles',
  },
};
