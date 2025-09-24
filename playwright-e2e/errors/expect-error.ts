import CustomError from './custom-error';

export default class ExpectError extends CustomError {
  constructor(message: string) {
    super('ExpectError', message);
  }
}
