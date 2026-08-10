const { I } = inject();

module.exports = {
  fields: {
    label1: '[id="downloadTemplateLabel1"] h2',
    label2: '[id="downloadTemplateLabel1"] p',
    label3: '[id="downloadTemplateLabel2"] p',
    downloadLink: '//button[contains(text(), ".docx")]',
  },

  async verifyLabelsAndDownload() {
    I.seeElement(this.fields.label1);
    I.see('You must now download the template', this.fields.label1);

    I.seeElement(this.fields.label2);
    I.see('Open the selected template and download it to your computer to complete the order.', this.fields.label2);

    I.seeElement(this.fields.label3);
    I.see('Once you\'ve completed the order, you can save and upload it on the next screen.', this.fields.label3);

    // Verify the hyperlink is present and contains today's date
    const today = new Date().toISOString().split('T')[0].split('-').reverse().join('-'); // Get today's date in DD-MM-YYYY format

    I.seeElement(this.fields.downloadLink);

    const linkText = await I.grabTextFrom(this.fields.downloadLink);

    if (!linkText.includes(today)) {
      throw new Error(`The link does not contain today's date. Found: ${linkText}`);
    }
    await I.clickContinue();
  }
};
