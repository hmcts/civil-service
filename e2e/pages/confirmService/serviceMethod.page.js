const {I} = inject();

module.exports = {

  fields: {
    serviceMethod: {
      id: '#serviceMethodToRespondentSolicitor1_serviceMethodToRespondentSolicitor1',
      options: {
        post: 'First class post',
        dx: 'Document exchange',
        fax: 'Fax',
        email: 'Email',
        other: 'Other'
      }
    }
  },

  async selectPostMethod() {
    I.waitForElement(this.fields.serviceMethod.id);
    await within(this.fields.serviceMethod.id, () => {
      I.click(this.fields.serviceMethod.options.post);
    });

    await I.clickContinue();
  }
};

