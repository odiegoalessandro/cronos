# Cronos

Cronos é um gerenciador pessoal de tempo diário baseado em blocos.

A ideia é simples: definir o que será feito no dia, atribuir uma duração para cada tarefa e usar um cronômetro para executar o plano. O estado do cronômetro e o tempo gasto permanecem salvos mesmo após atualizar ou fechar o navegador.

## Recursos

- Planejamento de até 24 horas por dia.
- Cronômetro com pausa, retomada e persistência no SQLite.
- Seleção da tarefa atual e reordenação por arrastar e soltar.
- Finalização manual de uma tarefa ou do dia.
- Histórico de dias, tempo planejado e tempo gasto.
- Tema claro/escuro e avisos sonoros.
- Backend inacessível externamente: apenas o frontend expõe uma porta no host.

## Stack

- Angular
- Spring Boot 3 / Java 21
- SQLite
- Docker Compose

## Pré-requisitos

- Git
- Docker com Docker Compose

## Executar

Clone o repositório:

```bash
git clone https://github.com/odiegoalessandro/cronos.git
cd cronos
```

Inicie os containers:

```bash
docker compose up --build -d
```

Acesse:

```text
http://localhost:48731
```

A porta `48731` é a única porta exposta no host. A API Spring Boot fica restrita à rede interna do Docker.

## Atualizar

```bash
git pull
docker compose up --build -d
```

## Parar

```bash
docker compose down
```

Os dados permanecem no volume Docker `cronos-data`.

Para apagar totalmente os dados locais, incluindo o banco SQLite:

```bash
docker compose down --volumes
```

> Esse comando é destrutivo.

## Windows: recomendação com WSL2

No Windows, prefira executar o projeto no **WSL2**. Evite clonar dentro de `C:\\` ou `/mnt/c`, pois operações de arquivo, Node e Docker ficam mais lentos.

1. Instale o WSL2 com Ubuntu:

   ```powershell
   wsl --install -d Ubuntu
   ```

2. Instale o Docker Desktop e habilite **Use the WSL 2 based engine**.

3. Em **Settings > Resources > WSL Integration**, habilite a distribuição Ubuntu.

4. No terminal Ubuntu, clone dentro do sistema de arquivos Linux:

   ```bash
   mkdir -p ~/projects
   cd ~/projects
   git clone https://github.com/odiegoalessandro/cronos.git
   cd cronos
   docker compose up --build -d
   ```

5. Abra `http://localhost:48731` no navegador do Windows.

O Docker Desktop disponibiliza essa porta do WSL automaticamente no `localhost` do Windows.
