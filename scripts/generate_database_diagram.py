#!/usr/bin/env python3

import argparse
import re
from collections import OrderedDict
from dataclasses import dataclass, field
from pathlib import Path


DEFAULT_MIGRATION_DIR = "src/main/resources/db/migration"
DEFAULT_OUTPUT = "docs/database_diagram.mmd"
SCHEMA_CHANGE_KEYWORDS = ("CREATE TABLE", "ALTER TABLE", "DROP TABLE")
TYPE_TERMINATORS = {
    "COLLATE",
    "CONSTRAINT",
    "DEFAULT",
    "GENERATED",
    "NOT",
    "NULL",
    "PRIMARY",
    "REFERENCES",
    "UNIQUE",
}


@dataclass
class Column:
    name: str
    column_type: str
    nullable: bool = True
    primary_key: bool = False
    foreign_key: bool = False


@dataclass
class ForeignKey:
    name: str
    source_table: str
    source_columns: tuple[str, ...]
    target_table: str
    target_columns: tuple[str, ...]


@dataclass
class Table:
    name: str
    columns: OrderedDict[str, Column] = field(default_factory=OrderedDict)


def parse_args():
    parser = argparse.ArgumentParser(
        description="Generate a Mermaid ER diagram by statically analysing Flyway SQL migrations."
    )
    parser.add_argument(
        "--migration-dir",
        default=DEFAULT_MIGRATION_DIR,
        help="Directory containing Flyway migration SQL files.",
    )
    parser.add_argument(
        "--schemas",
        default="dbs",
        help="Comma-separated schemas to include in the diagram.",
    )
    parser.add_argument(
        "--output",
        default=DEFAULT_OUTPUT,
        help="Path to write the generated Mermaid diagram.",
    )
    return parser.parse_args()


def normalise_identifier(value):
    value = value.strip().strip('"')
    return value.lower()


def normalise_table_name(value, default_schema="public"):
    value = value.strip().rstrip(";")
    value = re.sub(r"\s+", "", value)
    parts = [normalise_identifier(part) for part in value.split(".")]
    if len(parts) == 1:
        return f"{default_schema}.{parts[0]}"
    return f"{parts[-2]}.{parts[-1]}"


def mermaid_identifier(value):
    return re.sub(r"[^0-9A-Za-z_]", "_", value)


def mermaid_type(value):
    value = value.lower().strip()
    is_array = value.endswith("[]")
    value = value.removesuffix("[]")
    value = re.sub(r"\([^)]*\)", "", value)
    value = re.sub(r"\s+", "_", value)
    value = re.sub(r"[^0-9A-Za-z_]", "_", value).strip("_")
    return f"{value}_array" if is_array else value


def relationship_label(value):
    return re.sub(r"[^0-9A-Za-z_ -]", "_", value)


def strip_sql_comments(sql):
    sql = re.sub(r"/\*.*?\*/", "", sql, flags=re.DOTALL)
    lines = []
    for line in sql.splitlines():
        in_single_quote = False
        index = 0
        while index < len(line):
            char = line[index]
            if char == "'":
                in_single_quote = not in_single_quote
            if not in_single_quote and line[index:index + 2] == "--":
                line = line[:index]
                break
            index += 1
        lines.append(line)
    return "\n".join(lines)


def split_sql_statements(sql):
    statements = []
    current = []
    in_single_quote = False

    index = 0
    while index < len(sql):
        char = sql[index]
        current.append(char)
        if char == "'":
            if index + 1 < len(sql) and sql[index + 1] == "'":
                current.append(sql[index + 1])
                index += 2
                continue
            in_single_quote = not in_single_quote
        elif char == ";" and not in_single_quote:
            statement = "".join(current).strip()
            if statement:
                statements.append(statement)
            current = []
        index += 1

    remainder = "".join(current).strip()
    if remainder:
        statements.append(remainder)
    return statements


def split_top_level_csv(value):
    items = []
    current = []
    depth = 0
    in_single_quote = False

    for char in value:
        if char == "'":
            in_single_quote = not in_single_quote
        elif not in_single_quote and char == "(":
            depth += 1
        elif not in_single_quote and char == ")":
            depth -= 1
        elif not in_single_quote and char == "," and depth == 0:
            items.append("".join(current).strip())
            current = []
            continue
        current.append(char)

    item = "".join(current).strip()
    if item:
        items.append(item)
    return items


def parse_column_list(value):
    return tuple(
        normalise_identifier(column)
        for column in split_top_level_csv(value)
        if normalise_identifier(column)
    )


def extract_column_type(definition):
    tokens = definition.strip().split()
    column_type = []
    for token in tokens:
        if token.upper() in TYPE_TERMINATORS:
            break
        column_type.append(token)
    return " ".join(column_type).strip()


def parse_column_definition(definition):
    match = re.match(r'"?([A-Za-z_][A-Za-z0-9_]*)"?\s+(.+)$', definition.strip(), re.DOTALL)
    if not match:
        return None

    column_name = normalise_identifier(match.group(1))
    remainder = match.group(2).strip()
    upper_remainder = remainder.upper()
    column = Column(
        name=column_name,
        column_type=extract_column_type(remainder),
        nullable="NOT NULL" not in upper_remainder,
        primary_key="PRIMARY KEY" in upper_remainder,
    )
    if column.primary_key:
        column.nullable = False
    return column


def add_column(table, column):
    table.columns[column.name] = column


def mark_primary_key(table, columns):
    for column_name in columns:
        if column_name in table.columns:
            table.columns[column_name].primary_key = True
            table.columns[column_name].nullable = False


def add_foreign_key(foreign_keys, table, name, source_columns, target_table, target_columns):
    for column_name in source_columns:
        if column_name in table.columns:
            table.columns[column_name].foreign_key = True
    foreign_keys.append(
        ForeignKey(
            name=name,
            source_table=table.name,
            source_columns=source_columns,
            target_table=target_table,
            target_columns=target_columns,
        )
    )


def parse_table_constraint(item, table, foreign_keys):
    primary_match = re.search(r"\bPRIMARY\s+KEY\s*\(([^)]+)\)", item, flags=re.IGNORECASE)
    if primary_match:
        mark_primary_key(table, parse_column_list(primary_match.group(1)))

    fk_match = re.search(
        r"(?:CONSTRAINT\s+\"?([A-Za-z_][A-Za-z0-9_]*)\"?\s+)?"
        r"FOREIGN\s+KEY\s*\(([^)]+)\)\s+"
        r"REFERENCES\s+([A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)?)\s*\(([^)]+)\)",
        item,
        flags=re.IGNORECASE | re.DOTALL,
    )
    if fk_match:
        name = fk_match.group(1) or f"fk_{table.name.replace('.', '_')}_{len(foreign_keys) + 1}"
        add_foreign_key(
            foreign_keys,
            table,
            normalise_identifier(name),
            parse_column_list(fk_match.group(2)),
            normalise_table_name(fk_match.group(3)),
            parse_column_list(fk_match.group(4)),
        )


def parse_inline_foreign_key(definition, table, column, foreign_keys):
    fk_match = re.search(
        r"\bREFERENCES\s+([A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)?)\s*\(([^)]+)\)",
        definition,
        flags=re.IGNORECASE,
    )
    if not fk_match:
        return
    add_foreign_key(
        foreign_keys,
        table,
        f"fk_{table.name.replace('.', '_')}_{column.name}",
        (column.name,),
        normalise_table_name(fk_match.group(1)),
        parse_column_list(fk_match.group(2)),
    )


def parse_create_table(statement, tables, foreign_keys):
    match = re.match(
        r"CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?"
        r"([A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)?)\s*\((.*)\)\s*;?$",
        statement,
        flags=re.IGNORECASE | re.DOTALL,
    )
    if not match:
        return False

    table_name = normalise_table_name(match.group(1))
    table = tables.setdefault(table_name, Table(table_name))
    for item in split_top_level_csv(match.group(2)):
        first_token = item.strip().split(maxsplit=1)[0].upper()
        if first_token in {"CONSTRAINT", "FOREIGN", "PRIMARY", "UNIQUE", "CHECK"}:
            parse_table_constraint(item, table, foreign_keys)
            continue

        column = parse_column_definition(item)
        if column:
            add_column(table, column)
            parse_inline_foreign_key(item, table, column, foreign_keys)

    return True


def parse_alter_table(statement, tables, foreign_keys):
    match = re.match(
        r"ALTER\s+TABLE\s+(?:ONLY\s+)?"
        r"([A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)?)\s+(.*)\s*;?$",
        statement,
        flags=re.IGNORECASE | re.DOTALL,
    )
    if not match:
        return False

    table_name = normalise_table_name(match.group(1))
    table = tables.setdefault(table_name, Table(table_name))
    action = match.group(2).strip()

    add_column_match = re.match(
        r"ADD\s+COLUMN\s+(?:IF\s+NOT\s+EXISTS\s+)?(.+)$",
        action,
        flags=re.IGNORECASE | re.DOTALL,
    )
    if add_column_match:
        column = parse_column_definition(add_column_match.group(1))
        if column:
            add_column(table, column)
            parse_inline_foreign_key(add_column_match.group(1), table, column, foreign_keys)
            return True

    drop_column_match = re.match(
        r"DROP\s+COLUMN\s+(?:IF\s+EXISTS\s+)?\"?([A-Za-z_][A-Za-z0-9_]*)\"?",
        action,
        flags=re.IGNORECASE,
    )
    if drop_column_match:
        table.columns.pop(normalise_identifier(drop_column_match.group(1)), None)
        return True

    if re.match(r"ADD\s+(?:CONSTRAINT|FOREIGN|PRIMARY)", action, flags=re.IGNORECASE):
        parse_table_constraint(action.removeprefix("ADD ").strip(), table, foreign_keys)
        return True

    return False


def parse_drop_table(statement, tables):
    match = re.match(
        r"DROP\s+TABLE\s+(?:IF\s+EXISTS\s+)?"
        r"([A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)?)",
        statement,
        flags=re.IGNORECASE,
    )
    if not match:
        return False
    tables.pop(normalise_table_name(match.group(1)), None)
    return True


def parse_migrations(migration_dir):
    tables = OrderedDict()
    foreign_keys = []
    unsupported = []

    for path in sorted(Path(migration_dir).glob("V*.sql")):
        sql = strip_sql_comments(path.read_text(encoding="utf-8"))
        for statement in split_sql_statements(sql):
            compact = re.sub(r"\s+", " ", statement).strip()
            keyword = compact.upper()
            parsed = True
            if keyword.startswith("CREATE TABLE"):
                parsed = parse_create_table(statement, tables, foreign_keys)
            elif keyword.startswith("ALTER TABLE"):
                parsed = parse_alter_table(statement, tables, foreign_keys)
            elif keyword.startswith("DROP TABLE"):
                parsed = parse_drop_table(statement, tables)
            elif keyword.startswith(SCHEMA_CHANGE_KEYWORDS):
                parsed = False

            if not parsed:
                unsupported.append(f"{path}: {compact[:240]}")

    if unsupported:
        formatted = "\n".join(f"  - {item}" for item in unsupported)
        raise SystemExit(f"Unsupported schema-changing SQL detected:\n{formatted}")

    return tables, foreign_keys


def render_mermaid(tables, foreign_keys, included_schemas):
    included_tables = {
        name: table
        for name, table in tables.items()
        if name.split(".", 1)[0] in included_schemas
    }
    table_names = {
        name: mermaid_identifier(name.replace(".", "_"))
        for name in sorted(included_tables)
    }

    lines = [
        "%% Auto-generated from Flyway migrations; edit src/main/resources/db/migration instead.",
        "erDiagram",
    ]

    for table_name in sorted(included_tables):
        table = included_tables[table_name]
        lines.append(f"  {table_names[table_name]} {{")
        for column in table.columns.values():
            markers = []
            if column.primary_key:
                markers.append("PK")
            if column.foreign_key:
                markers.append("FK")
            marker_text = f" {', '.join(markers)}" if markers else ""
            nullable_text = "nullable" if column.nullable else "not null"
            lines.append(
                f"    {mermaid_type(column.column_type)} {mermaid_identifier(column.name)}"
                f"{marker_text} \"{nullable_text}\""
            )
        lines.append("  }")
        lines.append("")

    rendered_relationships = set()
    for foreign_key in foreign_keys:
        if foreign_key.source_table not in table_names or foreign_key.target_table not in table_names:
            continue
        relationship = (foreign_key.name, foreign_key.source_table, foreign_key.target_table)
        if relationship in rendered_relationships:
            continue
        rendered_relationships.add(relationship)
        lines.append(
            f"  {table_names[foreign_key.target_table]} ||--o{{ "
            f"{table_names[foreign_key.source_table]} : \"{relationship_label(foreign_key.name)}\""
        )

    return "\n".join(lines).rstrip() + "\n"


def main():
    args = parse_args()
    included_schemas = {
        schema.strip().lower()
        for schema in args.schemas.split(",")
        if schema.strip()
    }

    tables, foreign_keys = parse_migrations(args.migration_dir)
    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(render_mermaid(tables, foreign_keys, included_schemas), encoding="utf-8")


if __name__ == "__main__":
    main()
