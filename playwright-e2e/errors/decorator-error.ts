import CustomError from './custom-error';

export default class DecoratorError extends CustomError {
  constructor(message: string) {
    super('DecoratorError', message);
  }
}
