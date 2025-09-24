export default abstract class CustomError extends Error {
  constructor(name: string, message: string) {
    super(message);
    this.name = name;
    Error.captureStackTrace(this, CustomError);
  }
}
