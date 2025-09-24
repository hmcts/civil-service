const {I} = inject();

module.exports = {

  fields: {
    username: '#username',
    password: '#password',
  },
  submitButton: 'input[value="Sign in"]',

  async signIn(user) {
    if (user.email && user.password) {
      await I.retry(5).waitForElement(this.fields.username);
      await I.fillField(this.fields.username, user.email);
      await I.fillField(this.fields.password, user.password);

      await I.retry(5).waitForElement(this.submitButton);
      await I.click(this.submitButton);
    } else {
      console.log('*******User details are empty. Cannot login to idam*******');
    }
  },
};
