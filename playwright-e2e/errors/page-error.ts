import CustomError from './custom-error';

export default class PageError extends CustomError {
  constructor(message: string) {
    super('PageError', message);
  }
}
