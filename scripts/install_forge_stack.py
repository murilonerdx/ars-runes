#!/usr/bin/env python3
"""
Baixa e prepara um ambiente Forge 1.20.1 com:
- Ruina Arcana (jar local, se informado)
- Ars Nouveau (Modrinth)
- Occultism (Modrinth)

Uso rápido:
  python scripts/install_forge_stack.py --minecraft-dir ~/.minecraft --include-local-mod build/libs/ruinaarcana-0.3.0.jar
"""

from __future__ import annotations

import argparse
import json
import shutil
import sys
import urllib.parse
import urllib.request
from pathlib import Path

MODRINTH_API = "https://api.modrinth.com/v2"
FORGE_MAVEN = "https://maven.minecraftforge.net/net/minecraftforge/forge"
DEFAULT_MC = "1.20.1"
DEFAULT_FORGE = "47.4.18"


def fetch_json(url: str) -> dict | list:
    req = urllib.request.Request(url, headers={"User-Agent": "ruinaarcana-installer/1.0"})
    with urllib.request.urlopen(req, timeout=30) as response:
        return json.loads(response.read().decode("utf-8"))


def download_file(url: str, target: Path) -> None:
    target.parent.mkdir(parents=True, exist_ok=True)
    req = urllib.request.Request(url, headers={"User-Agent": "ruinaarcana-installer/1.0"})
    with urllib.request.urlopen(req, timeout=120) as response, target.open("wb") as output:
        shutil.copyfileobj(response, output)


def latest_modrinth_file(slug: str, game_version: str, loader: str = "forge") -> tuple[str, str]:
    params = urllib.parse.urlencode(
        {
            "game_versions": json.dumps([game_version]),
            "loaders": json.dumps([loader]),
            "featured": "true",
        }
    )
    versions_url = f"{MODRINTH_API}/project/{slug}/version?{params}"
    versions = fetch_json(versions_url)

    if not versions:
        params = urllib.parse.urlencode(
            {
                "game_versions": json.dumps([game_version]),
                "loaders": json.dumps([loader]),
            }
        )
        versions_url = f"{MODRINTH_API}/project/{slug}/version?{params}"
        versions = fetch_json(versions_url)

    if not versions:
        raise RuntimeError(f"Nenhuma versão encontrada para '{slug}' em {game_version}/{loader}.")

    chosen = versions[0]
    files = chosen.get("files", [])
    if not files:
        raise RuntimeError(f"Projeto '{slug}' não possui arquivos para download nesta versão.")

    primary = next((file for file in files if file.get("primary")), files[0])
    return primary["url"], primary["filename"]


def forge_installer_url(mc_version: str, forge_version: str) -> tuple[str, str]:
    full = f"{mc_version}-{forge_version}"
    filename = f"forge-{full}-installer.jar"
    return f"{FORGE_MAVEN}/{full}/{filename}", filename


def main() -> int:
    parser = argparse.ArgumentParser(description="Instalador Forge + RuinaArcana + integrações (Ars Nouveau e Occultism).")
    parser.add_argument("--minecraft-dir", default=str(Path.home() / ".minecraft"), help="Pasta do Minecraft.")
    parser.add_argument("--minecraft-version", default=DEFAULT_MC, help="Versão do Minecraft.")
    parser.add_argument("--forge-version", default=DEFAULT_FORGE, help="Versão do Forge.")
    parser.add_argument("--include-local-mod", default=None, help="Caminho para o jar local do Ruina Arcana.")
    parser.add_argument("--dry-run", action="store_true", help="Somente imprime ações sem baixar arquivos.")
    args = parser.parse_args()

    mc_dir = Path(args.minecraft_dir).expanduser().resolve()
    mods_dir = mc_dir / "mods"
    forge_dir = mc_dir / "forge-installers"

    tasks: list[tuple[str, str, Path]] = []

    forge_url, forge_file = forge_installer_url(args.minecraft_version, args.forge_version)
    tasks.append(("forge", forge_url, forge_dir / forge_file))

    for slug in ("ars-nouveau", "occultism"):
        if args.dry_run:
            tasks.append((
                    slug,
                    f"{MODRINTH_API}/project/{slug}/version?game_versions=[\"{args.minecraft_version}\"]&loaders=[\"forge\"]",
                    mods_dir / f"{slug}-{args.minecraft_version}-forge.jar"
            ))
        else:
            url, filename = latest_modrinth_file(slug, args.minecraft_version, "forge")
            tasks.append((slug, url, mods_dir / filename))

    if args.include_local_mod:
        local_mod = Path(args.include_local_mod).expanduser().resolve()
        if not local_mod.exists():
            raise FileNotFoundError(f"Jar local não encontrado: {local_mod}")
        tasks.append(("ruinaarcana-local", f"file://{local_mod}", mods_dir / local_mod.name))

    print("\nPlano de instalação:")
    for name, source, target in tasks:
        print(f"- {name}: {source} -> {target}")

    if args.dry_run:
        print("\nDry-run concluído. Nenhum arquivo foi baixado/copiado.")
        return 0

    for name, source, target in tasks:
        print(f"\n[{name}] preparando {target.name}...")
        if source.startswith("file://"):
            source_path = Path(source.replace("file://", "", 1))
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source_path, target)
        else:
            download_file(source, target)

    print("\nInstalação concluída.")
    print(f"- Mods em: {mods_dir}")
    print(f"- Installer Forge em: {forge_dir}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"Erro: {exc}", file=sys.stderr)
        raise SystemExit(1)
