export default class Timer {
  private _endTime: number;

  constructor(duration: number) {
    this._endTime = Date.now() + duration;
  }

  get remainingTime() {
    const remainingTime = Math.round((this._endTime - Date.now()) / 1000);
    if (remainingTime < 0) return 0;
    return remainingTime;
  }

  get endTime() {
    return this.endTime;
  }
}
