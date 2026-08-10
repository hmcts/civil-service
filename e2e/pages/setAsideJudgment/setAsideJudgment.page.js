const {I} = inject();

module.exports = {
  fields: {
    joSetAsideReason: {
      id: '#joSetAsideReason',
      options: {
        judgeOrder: '#joSetAsideReason-JUDGE_ORDER',
        judgmentError: '#joSetAsideReason-JUDGMENT_ERROR',
      }
    },
    joSetAsideJudgmentErrorText: {
      id: '#joSetAsideJudgmentErrorText'
    }
  },

  async selectJudgeOrder() {
    await I.waitForText('Set aside judgment');
    await I.click(this.fields.joSetAsideReason.options.judgeOrder);
    await I.clickContinue();
  },

  async selectJudgmentError() {
    await I.waitForText('Set aside judgment');
    await I.click(this.fields.joSetAsideReason.options.judgmentError);
    await I.fillField(this.fields.joSetAsideJudgmentErrorText.id, 'A judgment has been made in error');
    await I.clickContinue();
  }
};