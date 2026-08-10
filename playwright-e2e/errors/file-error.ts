import CustomError from './custom-error';

export default class FileError extends CustomError {
  constructor(message: string) {
    super('FileError', message);
  }
}
