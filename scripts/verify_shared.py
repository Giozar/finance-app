#!/usr/bin/env python3
"""Compile each module against this checkout and check shared's wire contract (JDK 17)."""
import argparse
import json
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
MODULES = ("shared/java-shared", "backend/java-server", "client/java-client")
FIXTURE = ROOT / MODULES[0] / "src/test/resources/contracts.json"


def run(*args):
    return subprocess.run(args, cwd=ROOT, check=True, text=True, capture_output=True).stdout


def verify(capture=False):
    with tempfile.TemporaryDirectory(prefix="finance-shared-") as directory:
        output = Path(directory)
        shared = output / "shared"
        for index, module in enumerate(MODULES):
            sources = sorted((ROOT / module / "src/main/java").rglob("*.java"))
            destination = shared if index == 0 else output / Path(module).name
            destination.mkdir()
            arguments = output / f"sources-{index}.txt"
            arguments.write_text("\n".join('"' + str(p) + '"' for p in sources))
            run("javac", "--release", "17", "-encoding", "UTF-8", "-classpath", str(shared),
                "-d", str(destination), "@" + str(arguments))
            print(f"Compilado: {module} ({len(sources)} archivos)", flush=True)
        probe = ROOT / MODULES[0] / "src/test/java/com/giozar04/contracts/ContractProbe.java"
        run("javac", "--release", "17", "-encoding", "UTF-8", "-classpath", str(shared),
            "-d", str(shared), str(probe))
        probe_result = json.loads(run("java", "-ea", "-classpath", str(shared),
                                     "com.giozar04.contracts.ContractProbe"))
        actual = {key: json.loads(value) for key, value in probe_result["data"].items()}
        backend_probe = ROOT / MODULES[1] / "src/test/java/com/giozar04/tags/TagUseCaseProbe.java"
        if backend_probe.exists():
            destination = output / "java-server"
            run("javac", "--release", "17", "-encoding", "UTF-8",
                "-classpath", str(shared) + ":" + str(destination),
                "-d", str(destination), str(backend_probe))
            run("java", "-ea", "-classpath", str(shared) + ":" + str(destination),
                "com.giozar04.tags.TagUseCaseProbe")
            print("Caso de uso tags: operaciones y validaciones correctas.")
        if capture:
            FIXTURE.parent.mkdir(parents=True, exist_ok=True)
            FIXTURE.write_text(json.dumps(actual, ensure_ascii=False, indent=2, sort_keys=True) + "\n")
            print("Referencia de contratos capturada; revisar antes de hacer commit.")
        else:
            expected = json.loads(FIXTURE.read_text())
            if actual != expected:
                changed = [key for key in actual.keys() | expected.keys()
                           if actual.get(key) != expected.get(key)]
                raise AssertionError(f"Cambió el contrato: {changed}")
            print(f"Contratos compatibles: {len(actual)} escenarios.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--capture-baseline", action="store_true",
                        help="Capturar una referencia nueva SOLO al aceptar un cambio de contrato.")
    try:
        verify(parser.parse_args().capture_baseline)
    except subprocess.CalledProcessError as error:
        print(error.stdout or "")
        print(error.stderr or "")
        raise SystemExit(error.returncode)
