# ☕ Meu Posto - Servidor Java de Alta Performance

Este diretório contém a implementação completa em **Java** do backend de sincronização do **Meu Posto**.

---

## 🚀 Como Executar o Servidor Java

Você pode compilar e rodar o servidor em qualquer ambiente com o Java Development Kit (JDK 8 ou superior instalado):

### Opção 1: Usando o Script Automatizado (Linux/macOS)
1. Dê permissão de execução ao script:
   ```bash
   chmod +x run.sh
   ```
2. Execute o script:
   ```bash
   ./run.sh
   ```

### Opção 2: Manualmente via Terminal (Qualquer Sistema)
1. Compile o arquivo Java:
   ```bash
   javac -d bin BackupServer.java
   ```
2. Inicie o servidor:
   ```bash
   java -cp bin com.meuposto.BackupServer
   ```

Por padrão, o servidor iniciará no endereço `http://localhost:8080`.

---

## ⚡ Integração com o Painel Web (HTML/JS)

Para conectar o painel administrativo da Web ao seu novo servidor Java local:
1. No Painel Web do Meu Posto, clique em **Configurar Integração** no banner da Vercel/Cloud.
2. No campo **URL da API Vercel**, insira a URL do seu servidor Java local:
   ```text
   http://localhost:8080/api/backup
   ```
3. Opcionalmente, ative a chave de **Auto-Sync** para salvar todas as alterações em tempo real de forma 100% automatizada.
4. Clique em **Salvar e Conectar**.

---

## 📂 Persistência de Dados
Os dados de backup de cada posto serão gravados automaticamente em arquivos JSON individuais dentro da pasta `./backups/` com base no CNPJ do posto (ex: `backups/12_345_678_0001-99.json`). Isso garante uma persistência rápida, offline-first e independente de bancos de dados externos pesados.
