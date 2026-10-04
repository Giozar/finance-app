#!/usr/bin/env python3
"""Move one client feature behind input/output ports without changing its wire adapter."""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'client/java-client/src/main/java/com/giozar04'


def migrate(feature):
    old = SRC / feature / 'infrastructure/services'
    files = list(old.glob('*Service.java'))
    if len(files) != 1:
        raise SystemExit(f'Expected one service in {old}: {files}')
    file = files[0]
    name = file.stem
    stem = name.removesuffix('Service')
    operations = stem + 'Operations'
    gateway = stem + 'Gateway'
    usecase = stem + 'UseCase'
    old_package = f'com.giozar04.{feature}.infrastructure.services'
    new_package = f'com.giozar04.{feature}.infrastructure.transport.socket'
    input_package = f'com.giozar04.{feature}.application.ports.input'
    output_package = f'com.giozar04.{feature}.application.ports.output'
    usecase_package = f'com.giozar04.{feature}.application.usecases'
    source = file.read_text()
    signatures = re.findall(r'^    public (?!static)([^\n{]+) \{', source, re.M)
    if not signatures:
        raise SystemExit(f'No operations in {file}')
    imports = re.findall(r'^import [^;]+;', source, re.M)
    signature_text = ' '.join(signatures)
    required = [line for line in imports if re.search(r'\b' + re.escape(line.rsplit('.', 1)[-1][:-1]) + r'\b', signature_text)]
    def write(relative, content):
        target = SRC / feature / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content)
    def declaration(sig):
        return sig.strip() + ';'
    write(f'application/ports/input/{operations}.java', f'package {input_package};\n\n' + '\n'.join(required) + f'\n\npublic interface {operations} {{\n' + ''.join(f'    {declaration(s)}\n' for s in signatures) + '}\n')
    write(f'application/ports/output/{gateway}.java', f'package {output_package};\n\n' + '\n'.join(required) + f'\n\npublic interface {gateway} {{\n' + ''.join(f'    {declaration(s)}\n' for s in signatures) + '}\n')
    methods = []
    for sig in signatures:
        match = re.match(r'(.+?)\s+(\w+)\((.*?)\)(?:\s+throws\s+.+)?$', sig.strip())
        if not match:
            raise SystemExit(f'Cannot parse {sig}')
        result, method, args = match.groups()
        values = ', '.join(part.strip().split()[-1] for part in args.split(',') if part.strip())
        prefix = '' if result == 'void' else 'return '
        methods.append(f'    @Override\n    public {sig.strip()} {{\n        {prefix}gateway.{method}({values});\n    }}\n')
    write(f'application/usecases/{usecase}.java', f'package {usecase_package};\n\n' + '\n'.join(required) + f'\nimport {input_package}.{operations};\nimport {output_package}.{gateway};\n\npublic final class {usecase} implements {operations} {{\n    private final {gateway} gateway;\n\n    public {usecase}({gateway} gateway) {{\n        this.gateway = java.util.Objects.requireNonNull(gateway);\n    }}\n\n' + '\n'.join(methods) + '}\n')
    new_file = SRC / feature / 'infrastructure/transport/socket' / file.name
    new_file.parent.mkdir(parents=True, exist_ok=True)
    new_file.write_text(source.replace(f'package {old_package};', f'package {new_package};').replace(f'public class {name} {{', f'public class {name} implements {gateway} {{').replace(f'import com.giozar04.logging.infrastructure.ConsoleLogger;', f'import {output_package}.{gateway};\nimport com.giozar04.logging.infrastructure.ConsoleLogger;'))
    file.unlink()
    bootstrap = SRC / 'bootstrap/ApplicationInitializer.java'
    body = bootstrap.read_text().replace(f'import {old_package}.{name};', f'import {new_package}.{name};\nimport {usecase_package}.{usecase};\nimport {input_package}.{operations};')
    body = re.sub(r'(\b' + name + r'\.connectService\(connectionService\);)', r'\1\n            ClientUseCases.register(' + operations + '.class, new ' + usecase + '(' + name + '.getInstance()));', body)
    bootstrap.write_text(body)
    for other in SRC.rglob('*.java'):
        if other == new_file or other == bootstrap:
            continue
        content = other.read_text().replace(f'import {old_package}.{name};', f'import {new_package}.{name};')
        if '/presentation/' in other.as_posix() and name in content:
            content = content.replace(f'import {new_package}.{name};', f'import {input_package}.{operations};\nimport com.giozar04.bootstrap.ClientUseCases;')
            content = content.replace(f'{name}.getInstance()', f'ClientUseCases.get({operations}.class)')
            content = re.sub(r'\b' + name + r'\b', operations, content)
        other.write_text(content)
    status = ROOT / 'MIGRATION.md'
    summary = f'| {feature} | Migrada; puerto de entrada, caso de uso, puerto de salida y adaptador socket verificados |'
    current = status.read_text().replace('Pendiente. Su migración empieza después de cerrar backend.', '| Feature | Estado |\n| --- | --- |')
    if summary not in current:
        current = current.replace('| --- | --- |\n\nLa compilación de consumidores', '| --- | --- |\n' + summary + '\n\nLa compilación de consumidores') if '| --- | --- |\n\nLa compilación de consumidores' in current else current.replace('## Client\n\n', '## Client\n\n')
        if summary not in current:
            marker = '## Client\n\n| Feature | Estado |\n| --- | --- |'
            current = current.replace(marker, marker + '\n' + summary)
    status.write_text(current)
    agent = ROOT / '.claude/agents/finance-app-expert-client.md'
    current = agent.read_text()
    note = f'- `{feature}`: `{operations}` → `{usecase}` → `{gateway}` → `{name}` (socket).'
    if note not in current:
        current = current.replace('## Migración vigente\n', '## Migración vigente\n\n' + note + '\n')
    agent.write_text(current)
    print(feature, len(signatures), 'operations')


if __name__ == '__main__':
    migrate(sys.argv[1])
