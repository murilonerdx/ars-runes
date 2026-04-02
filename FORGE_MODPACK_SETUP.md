# Instalação Forge + Ruina Arcana + integrações (1.20.1)

Este projeto inclui um instalador para preparar o ambiente com:
- Forge 1.20.1
- Ars Nouveau (Forge)
- Occultism (Forge)
- Jar local do Ruina Arcana (opcional)

## Script

Use `scripts/install_forge_stack.py`.

### 1) Ver plano sem baixar nada

```bash
python scripts/install_forge_stack.py --dry-run
```

### 2) Instalar dependências + incluir seu jar local

```bash
python scripts/install_forge_stack.py \
  --minecraft-dir ~/.minecraft \
  --include-local-mod build/libs/ruinaarcana-0.3.0.jar
```

## O que o script faz

- Baixa o instalador do Forge para `~/.minecraft/forge-installers/`.
- Busca no Modrinth a versão mais recente compatível com **Minecraft 1.20.1 + Forge** para:
  - `ars-nouveau`
  - `occultism`
- Copia o jar local do Ruina Arcana para `~/.minecraft/mods/` quando informado.

## Observações

- A seleção de versões de Ars Nouveau/Occultism é dinâmica (API Modrinth).
- Se você já tiver um profile Forge instalado, basta colocar os jars em `mods/`.
- O script não executa o instalador do Forge automaticamente; ele apenas baixa o jar do instalador para você rodar quando quiser.
- Com Ars Nouveau presente, a `Bateria Arcana` passa a transferir automaticamente energia para `Source Jar` próximos (integração por compat layer).
