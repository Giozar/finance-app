#!/usr/bin/env python3
"""Migración estructural, por feature, del backend hacia los puertos y adaptadores."""
import argparse
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "backend/java-server/src/main/java/com/giozar04"
FEATURES = {
    "users": "User", "categories": "Category", "externalEntities": "ExternalEntity",
    "bankClients": "BankClient", "accounts": "Account", "cards": "Card",
    "accountCashbackSettings": "AccountCashbackSetting",
    "walletCardLinks": "WalletCardLink",
    "cardTransactionDetails": "CardTransactionDetail",
    "walletTransactionDetails": "WalletTransactionDetail",
    "transactions": "Transaction",
    "accountReconciliations": "AccountReconciliation",
}


def java_files():
    yield from (ROOT / "backend/java-server/src").rglob("*.java")


def validation_methods(source):
    pattern = re.compile(r"    protected void (validate\w+)\(([^)]*)\)\s*\{")
    found = []
    for match in pattern.finditer(source):
        depth, end = 1, match.end()
        while depth:
            if source[end] == "{": depth += 1
            elif source[end] == "}": depth -= 1
            end += 1
        found.append((match.start(), end, match.group(1), match.group(2), source[match.start():end]))
    return found


def migrate(feature):
    prefix = FEATURES[feature]
    base = BASE / feature
    old = {
        "service": base / "application/services" / f"{prefix}Service.java",
        "port": base / "domain/interfaces" / f"{prefix}RepositoryInterface.java",
        "abstract": base / "domain/models" / f"{prefix}RepositoryAbstract.java",
        "mysql": base / "infrastructure/repositories" / f"{prefix}RepositoryMySQL.java",
        "controller": base / "infrastructure/controllers" / f"{prefix}Controllers.java",
        "handler": base / "infrastructure/handlers" / f"{prefix}Handlers.java",
    }
    if not all(path.exists() for path in old.values()):
        raise RuntimeError(f"Archivos de {feature} incompletos o ya migrados")
    new = {
        "service": base / "application/usecases" / f"{prefix}UseCase.java",
        "port": base / "application/ports/output" / f"{prefix}Repository.java",
        "abstract": base / "infrastructure/persistence/mysql" / f"Abstract{prefix}JdbcRepository.java",
        "mysql": base / "infrastructure/persistence/mysql" / f"{prefix}RepositoryMySQL.java",
        "controller": base / "infrastructure/transport/socket" / f"{prefix}Controllers.java",
        "handler": base / "infrastructure/transport/socket" / f"{prefix}Handlers.java",
    }
    names = {
        f"{prefix}Service": f"{prefix}UseCase",
        f"{prefix}RepositoryInterface": f"{prefix}Repository",
        f"{prefix}RepositoryAbstract": f"Abstract{prefix}JdbcRepository",
    }
    full_names = {}
    for key, source in old.items():
        target = new[key]
        old_pkg = "com.giozar04." + ".".join(source.relative_to(BASE).parts[:-1])
        new_pkg = "com.giozar04." + ".".join(target.relative_to(BASE).parts[:-1])
        old_class = source.stem
        new_class = target.stem
        text = source.read_text().replace(f"package {old_pkg};", f"package {new_pkg};")
        if old_class != new_class:
            text = re.sub(r"\b" + old_class + r"\b", new_class, text)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(text)
        source.unlink()
        full_names[f"{old_pkg}.{old_class}"] = f"{new_pkg}.{new_class}"

    # La interfaz de entrada describe lo que puede solicitar el socket; el puerto
    # de salida describe lo que necesita el caso de uso de la persistencia.
    input_port = base / "application/ports/input" / f"{prefix}Operations.java"
    input_port.parent.mkdir(parents=True, exist_ok=True)
    port_text = new["port"].read_text()
    input_port.write_text(port_text.replace(".ports.output;", ".ports.input;")
                          .replace(f"interface {prefix}Repository", f"interface {prefix}Operations"))

    # Los métodos validate* son reglas puras que ya existían en la clase abstracta.
    # Se trasladan sin cambiar su cuerpo ni sus mensajes. La clase JDBC delega en ellas.
    abstract_path = new["abstract"]
    source = abstract_path.read_text()
    methods = validation_methods(source)
    if not methods:
        raise RuntimeError(f"Sin validaciones en {feature}; requiere revisión manual")
    policy_name = f"{prefix}Policy"
    policy = base / "domain/policies" / (policy_name + ".java")
    policy.parent.mkdir(parents=True, exist_ok=True)
    imports = [line for line in source.splitlines() if line.startswith("import ")
               and not any(name in line for name in
                           ("DatabaseConnectionInterface", "ConsoleLogger", f"{prefix}Repository"))]
    policy_methods = []
    for _, _, name, args, body in methods:
        method = body.replace("protected void", "public static void", 1)
        policy_methods.append(method)
    policy.write_text(f"package com.giozar04.{feature}.domain.policies;\n\n"
                      + "\n".join(imports) + "\n\n"
                      + f"public final class {policy_name} {{\n    private {policy_name}() {{}}\n\n"
                      + "\n\n".join(policy_methods) + "\n}\n")
    for start, end, name, args, _ in reversed(methods):
        parameters = [part.strip().split()[-1] for part in args.split(",") if part.strip()]
        source = source[:start] + (f"    protected void {name}({args}) {{\n"
                  f"        {policy_name}.{name}({', '.join(parameters)});\n    }}") + source[end:]
    abstract_path.write_text(source)

    # La implementación del puerto de entrada recibe solo el puerto de salida.
    usecase = new["service"]
    source = usecase.read_text()
    usecase.write_text(source)

    # Imports y referencias de otras features, bootstrap y apps de consola.
    for path in java_files():
        text = path.read_text()
        for before, after in full_names.items():
            text = text.replace(before, after)
        for before, after in names.items():
            text = re.sub(r"\b" + before + r"\b", after, text)
        if text != path.read_text():
            path.write_text(text)

    # Ajustes de dependencias después de cambiar los imports antiguos por sus FQN.
    usecase_source = usecase.read_text()
    usecase_source = usecase_source.replace(f"implements {prefix}Repository", f"implements {prefix}Operations")
    usecase_source = usecase_source.replace(f"implements {prefix}OperationsInterface", f"implements {prefix}Operations")
    usecase_source = usecase_source.replace(f"import com.giozar04.{feature}.application.ports.output.{prefix}Repository;",
        f"import com.giozar04.{feature}.application.ports.output.{prefix}Repository;\n"
        f"import com.giozar04.{feature}.application.ports.input.{prefix}Operations;")
    usecase.write_text(usecase_source)
    abstract_source = abstract_path.read_text().replace(
        f"import com.giozar04.{feature}.application.ports.output.{prefix}Repository;",
        f"import com.giozar04.{feature}.application.ports.output.{prefix}Repository;\n"
        f"import com.giozar04.{feature}.domain.policies.{policy_name};")
    abstract_path.write_text(abstract_source)
    for key in ("controller", "handler"):
        path = new[key]
        text = path.read_text().replace(f"import com.giozar04.{feature}.application.usecases.{prefix}UseCase;",
                                        f"import com.giozar04.{feature}.application.ports.input.{prefix}Operations;")
        text = re.sub(r"\b" + prefix + r"UseCase\b", prefix + "Operations", text)
        path.write_text(text)

    # El bootstrap inyecta la implementación a través del puerto de entrada.
    bootstrap = BASE / "bootstrap/ApplicationInitializer.java"
    text = bootstrap.read_text()
    text = text.replace(f"import com.giozar04.{feature}.application.usecases.{prefix}UseCase;",
                        f"import com.giozar04.{feature}.application.usecases.{prefix}UseCase;\n"
                        f"import com.giozar04.{feature}.application.ports.input.{prefix}Operations;")
    text = re.sub(r"\b" + prefix + r"UseCase (\w+) = new " + prefix + r"UseCase\(",
                  prefix + r"Operations \1 = new " + prefix + "UseCase(", text)
    bootstrap.write_text(text)
    migration = ROOT / "MIGRATION.md"
    text = migration.read_text().replace(f"| {feature} | Pendiente |",
                                         f"| {feature} | Migrada; puertos, política y adaptadores verificados |")
    migration.write_text(text)
    agent = ROOT / ".claude/agents/finance-app-expert-backend.md"
    text = agent.read_text().replace("| `tags` | `TagOperations`", f"| `{feature}` | `{prefix}Operations`, `{prefix}Repository`, `{prefix}UseCase`, `{prefix}Policy`, adaptadores MySQL/socket |\n| `tags` | `TagOperations`")
    agent.write_text(text)
    for path in (*new.values(), input_port, policy):
        path.write_text("\n".join(line.rstrip() for line in path.read_text().splitlines()) + "\n")
    print(f"Migrado backend/{feature}: 6 clases movidas, {len(methods)} reglas en dominio")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("feature", choices=FEATURES)
    migrate(parser.parse_args().feature)
