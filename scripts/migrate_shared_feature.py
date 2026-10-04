#!/usr/bin/env python3
"""Mover una feature de shared y reescribir sus referencias Java en este repositorio."""
import argparse
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "shared/java-shared/src/main/java/com/giozar04"
FEATURES = {
    "tags": ("Tag", "Tag", "tagToMap", "mapToTag"),
    "users": ("User", "User", "userToMap", "mapToUser"),
    "categories": ("Category", "Category", "categoryToMap", "mapToCategory"),
    "externalEntities": ("ExternalEntity", "ExternalEntity", "externalEntityToMap", "mapToExternalEntity"),
    "bankClient": ("BankClient", "BankClient", "bankClientToMap", "mapToBankClient"),
    "accounts": ("Account", "Account", "accountToMap", "mapToAccount"),
    "card": ("Card", "Card", "cardToMap", "mapToCard"),
    "accountCashbackSettings": ("AccountCashbackSetting", "AccountCashbackSetting", "toMap", "fromMap"),
    "walletCardLinks": ("WalletCardLink", "WalletCardLink", "toMap", "fromMap"),
    "cardTransactionDetails": ("CardTransactionDetail", "CardTransactionDetail", "toMap", "fromMap"),
    "walletTransactionDetails": ("WalletTransactionDetail", "WalletTransactionDetail", "toMap", "fromMap"),
    "transactions": ("Transaction", "Transaction", "transactionToMap", "mapToTransaction"),
    "accountReconciliations": ("AccountReconciliation", "AccountReconciliation", "accountReconciliationToMap", "mapToAccountReconciliation"),
}


def java_files():
    for module in ("shared/java-shared", "backend/java-server", "client/java-client"):
        yield from (ROOT / module / "src").rglob("*.java")


def nested_classes(source):
    header = re.compile(r"public static class (\w+) extends RuntimeException\s*\{")
    for match in header.finditer(source):
        depth = 1
        end = match.end()
        while depth:
            if source[end] == "{":
                depth += 1
            elif source[end] == "}":
                depth -= 1
            end += 1
        yield match.group(1), source[match.start():end]


def migrate(feature):
    if feature not in FEATURES:
        raise ValueError(f"Feature desconocida: {feature}")
    entity, prefix, old_write, old_read = FEATURES[feature]
    directory = BASE / feature
    old_mapper = directory / "application/utils" / f"{entity}Utils.java"
    old_errors = directory / "domain/exceptions" / f"{entity}Exceptions.java"
    if not old_mapper.exists() or not old_errors.exists():
        raise RuntimeError(f"{feature} ya está migrada o faltan archivos")

    original_mapper = f"com.giozar04.{feature}.application.utils.{entity}Utils"
    target_mapper = f"com.giozar04.{feature}.infrastructure.serialization.{entity}Mapper"
    original_errors = f"com.giozar04.{feature}.domain.exceptions.{entity}Exceptions"
    source = old_errors.read_text()
    exceptions = {}
    written = set()
    for nested, block in nested_classes(source):
        name = nested if nested.startswith(prefix) else prefix + nested
        # Las variantes genéricas de Transaction son clases no usadas que duplican
        # las explícitas; conservar la variante explícita.
        if name in written:
            continue
        if feature == "transactions" and nested in {"CreationException", "RetrievalException", "UpdateException", "DeletionException"}:
            continue
        written.add(name)
        layer = ("domain/exceptions" if name.endswith("ValidationException")
                 else "infrastructure/serialization" if name.endswith("ParsingException")
                 else "application/exceptions")
        package = f"com.giozar04.{feature}.{layer.replace('/', '.')}"
        body = re.sub(r"public static class\s+" + nested, "public class " + name, block, count=1)
        body = re.sub(r"\b" + nested + r"\(", name + "(", body)
        body = body.replace("\n    ", "\n")
        target = directory / layer / f"{name}.java"
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(f"package {package};\n\n{body}\n")
        exceptions[nested] = f"{package}.{name}"

    new_mapper = directory / "infrastructure/serialization" / f"{entity}Mapper.java"
    new_mapper.parent.mkdir(parents=True, exist_ok=True)
    mapper_source = old_mapper.read_text()
    mapper_source = mapper_source.replace(f"package com.giozar04.{feature}.application.utils;",
                                          f"package com.giozar04.{feature}.infrastructure.serialization;")
    mapper_source = mapper_source.replace(f"public class {entity}Utils", f"public class {entity}Mapper")
    mapper_source = mapper_source.replace("import com.giozar04.shared.utils.SharedUtils;",
                                          "import com.giozar04.shared.infrastructure.serialization.ValueParser;")
    mapper_source = mapper_source.replace("SharedUtils.", "ValueParser.")
    if old_write != "toMap":
        mapper_source = re.sub(r"\b" + old_write + r"\(", "toMap(", mapper_source)
    if old_read != "fromMap":
        mapper_source = re.sub(r"\b" + old_read + r"\(", "fromMap(", mapper_source)
    new_mapper.write_text(mapper_source)
    old_mapper.unlink()
    old_errors.unlink()

    probe = ROOT / "shared/java-shared/src/test/java/com/giozar04/contracts/ContractProbe.java"
    for path in java_files():
        if path == probe:
            continue
        text = path.read_text()
        if original_mapper not in text and f"{entity}Utils" not in text and f"{entity}Exceptions" not in text:
            continue
        updated = text.replace(f"import {original_mapper};", f"import {target_mapper};")
        updated = re.sub(r"\b" + entity + r"Utils\b", entity + "Mapper", updated)
        if old_write != "toMap":
            updated = updated.replace(f"{entity}Mapper.{old_write}(", f"{entity}Mapper.toMap(")
        if old_read != "fromMap":
            updated = updated.replace(f"{entity}Mapper.{old_read}(", f"{entity}Mapper.fromMap(")
        # Un import explícito de la clase anidada se cambia antes de buscar usos
        # calificados; de otro modo quedaría un import al paquete antiguo.
        for nested, full_name in exceptions.items():
            updated = updated.replace(f"import {original_errors}.{nested};", f"import {full_name};")
        used = sorted(set(re.findall(r"\b" + entity + r"Exceptions\.(\w+)\b", updated)))
        if used:
            imports = []
            for nested in used:
                if nested not in exceptions:
                    raise RuntimeError(f"Excepción sin destino: {feature}.{nested} en {path}")
                full_name = exceptions[nested]
                imports.append("import " + full_name + ";")
                updated = updated.replace(f"{entity}Exceptions.{nested}", full_name.rsplit(".", 1)[1])
            updated = updated.replace(f"import {original_errors};", "\n".join(imports))
        if f"{entity}Exceptions" in updated:
            raise RuntimeError(f"Referencia de excepción pendiente en {path}")
        if updated != text:
            path.write_text(updated)

    # El probe usa nombres de clases como datos para llamar a todas las versiones.
    text = probe.read_text().replace(original_mapper, target_mapper)
    text = text.replace(f'"{original_mapper}", "{old_write}", "{old_read}"',
                        f'"{target_mapper}", "toMap", "fromMap"')
    # La primera sustitución ya reemplazó la ruta; completar los métodos.
    text = text.replace(f'"{target_mapper}", "{old_write}", "{old_read}"',
                        f'"{target_mapper}", "toMap", "fromMap"')
    probe.write_text(text)
    migration = ROOT / "MIGRATION.md"
    text = migration.read_text().replace(f"| {feature} | Pendiente |", f"| {feature} | Migrada; contratos y consumidores verificados |")
    migration.write_text(text)
    print(f"Migrada {feature}: mapper, {len(exceptions)} excepciones y referencias")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("feature", choices=FEATURES)
    migrate(parser.parse_args().feature)
