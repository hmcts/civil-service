import CustomError from './custom-error';

export default class PromiseError extends CustomError {
  constructor(message: string) {
    super('PromiseError', message);
  }
}
