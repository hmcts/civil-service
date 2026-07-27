import { z } from 'zod';
import type { JsonToZodSchemaOptions } from '../models/zod';

export default class ZodHelper {
  static safeParse(schema: z.ZodType, data: unknown): unknown {
    const result = schema.safeParse(data);

    if (result.success) {
      return result.data;
    }

    throw new Error(`Zod schema validation failed:\n${this.formatZodIssues(result.error.issues)}`);
  }

  static formatZodIssues(issues: z.core.$ZodIssue[]) {
    return issues
      .map((issue) => {
        const path = issue.path.length ? issue.path.join('.') : '<root>';
        return `- ${path}: ${issue.message}`;
      })
      .join('\n');
  }

  static createSchemaFromJson(json: unknown, options: JsonToZodSchemaOptions = {}): z.ZodType {
    return this.createSchemaFromJsonValue(json, options, 0);
  }

  private static createSchemaFromJsonValue(
    json: unknown,
    options: JsonToZodSchemaOptions,
    depth: number,
  ): z.ZodType {
    if (json === null) {
      return z.null();
    }

    if (json === undefined) {
      return z.undefined();
    }

    if (Array.isArray(json)) {
      return this.createArraySchemaFromJson(json, options, depth);
    }

    if (typeof json === 'object') {
      return this.createObjectSchemaFromJson(json as Record<string, unknown>, options, depth);
    }

    if (options.literalValues) {
      return z.literal(json as string | number | boolean);
    }

    if (typeof json === 'string') {
      return z.string();
    }

    if (typeof json === 'number') {
      return z.number();
    }

    if (typeof json === 'boolean') {
      return z.boolean();
    }

    return z.unknown();
  }

  private static createObjectSchemaFromJson(
    json: Record<string, unknown>,
    options: JsonToZodSchemaOptions,
    depth: number,
  ): z.ZodType {
    const shape = Object.entries(json).reduce<Record<string, z.ZodType>>(
      (schemaShape, [key, value]) => {
        schemaShape[key] = this.createSchemaFromJsonValue(value, options, depth + 1);
        return schemaShape;
      },
      {},
    );

    return options.strictObjects && depth > 0 ? z.strictObject(shape) : z.looseObject(shape);
  }

  private static createArraySchemaFromJson(
    json: unknown[],
    options: JsonToZodSchemaOptions,
    depth: number,
  ): z.ZodType {
    const itemSchemas = json.map((item) =>
      this.createSchemaFromJsonValue(item, options, depth + 1),
    );

    if (options.tupleArrays) {
      return itemSchemas.length ? z.tuple(itemSchemas as [z.ZodType, ...z.ZodType[]]) : z.tuple([]);
    }

    if (itemSchemas.length === 0) {
      return z.array(z.unknown());
    }

    if (itemSchemas.length === 1) {
      return z.array(itemSchemas[0]);
    }

    return z.array(z.union(itemSchemas as [z.ZodType, z.ZodType, ...z.ZodType[]]));
  }
}
