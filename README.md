# ⚡ Meu Posto - Painel Web & Backend Multilíngue (TypeScript/Java)

Este repositório contém o código-fonte do **Meu Posto**, agora totalmente otimizado para modo web e equipado com duas soluções robustas de backend:
1. **API Serverless em TypeScript** pronta para deploy instantâneo na **Vercel** com suporte nativo do GitHub.
2. **Servidor Java de Alta Performance** nativo, autocontido e ultraveloz para execução local ou em servidores dedicados.

---

## ⚙️ Estrutura do Repositório

*   `web/`: Contém todo o código da SPA (Single Page Application) do Painel Administrativo (`index.html` e `reports.js`).
*   `server/`: Contém a implementação completa do backend em **Java** (`BackupServer.java`), configurável, multithread e sem dependências externas.
*   `api/backup.ts`: Ponto de entrada da API Serverless TypeScript na raiz, que encaminha de forma limpa as requisições para a lógica em TypeScript localizada em `web/api/backup.ts`.
*   `vercel.json`: Regras de roteamento de nível raiz com suporte nativo a CORS pré-configurado, permitindo que a aplicação Android e o cliente Web acessem a API sem qualquer bloqueio de segurança.
*   `package.json` & `tsconfig.json`: Gerenciamento de dependências e regras de compilação TypeScript para as funções da Vercel.

---

## ☕ Como Executar o Servidor Java

Para utilizar a solução 100% Java no backend:
1. Acesse a pasta `/server`:
   ```bash
   cd server
   ```
2. Inicie o servidor usando o script automatizado (Linux/macOS):
   ```bash
   chmod +x run.sh
   ./run.sh
   ```
   *Ou compile e execute manualmente em qualquer sistema:*
   ```bash
   javac -d bin BackupServer.java
   java -cp bin com.meuposto.BackupServer
   ```
3. O servidor rodará em `http://localhost:8080/api/backup`.

---

## 🚀 Como Integrar com GitHub e Implantar na Vercel (TypeScript)

Siga os passos abaixo para colocar o seu sistema no ar em menos de 2 minutos:

### Passo 1: Enviar para o GitHub
Se você já possui o repositório vinculado à sua conta, ignore este passo. Caso contrário:
1. No painel do **Google AI Studio**, clique no menu de configurações do projeto no canto superior direito e selecione **Push to GitHub** (ou exporte como arquivo ZIP).
2. Se exportou como ZIP, crie um novo repositório no seu GitHub, extraia os arquivos localmente e envie-os usando os comandos:
   ```bash
   git init
   git add .
   git commit -m "feat: integracao vercel zero-config"
   git branch -M main
   git remote add origin https://github.com/seu-usuario/seu-repositorio.git
   git push -u origin main
   ```

### Passo 2: Conectar à Vercel
1. Acesse o painel da [Vercel](https://vercel.com) e faça login com sua conta do GitHub.
2. Clique em **Add New...** > **Project**.
3. Importe o repositório que você acabou de criar.
4. **IMPORTANTE:** Deixe a configuração **Root Directory** vazia (como raiz `/`). Graças aos novos arquivos de roteamento na raiz, a Vercel detectará e configurará tudo de forma 100% automatizada.
5. Clique em **Deploy**. 

Pronto! Em alguns segundos, a Vercel gerará uma URL segura e permanente (ex: `https://seu-posto.vercel.app`) para você utilizar tanto no navegador quanto no app Android.

---

## 🗄️ Configuração do Banco de Dados na Vercel

O endpoint da API TypeScript gerencia dinamicamente onde os dados de backup do posto serão armazenados de forma persistente, dependendo de quais variáveis de ambiente você configurar na Vercel:

### Opção A: Vercel KV (Redis de Alta Performance) - RECOMENDADO ⭐
É a maneira mais rápida e robusta de salvar e carregar os dados chave-valor de cada CNPJ.
*   No painel do seu projeto na Vercel, vá até a aba **Storage** (Armazenamento).
*   Selecione **KV (Redis)** e clique em conectar para criar um novo banco de dados.
*   A Vercel injetará automaticamente as variáveis `KV_REST_API_URL` e `KV_REST_API_TOKEN` no seu projeto. A API começará a usá-lo na mesma hora!

### Opção B: Supabase (PostgreSQL)
Se preferir armazenar os dados em formato JSON em seu banco Supabase pré-existente:
*   Crie uma tabela chamada `posto_backups` com as colunas `id` (text, primary key), `data` (jsonb) e `updated_at` (timestamp).
*   Nas configurações de variáveis de ambiente do seu projeto Vercel (Settings > Environment Variables), adicione:
    *   `SUPABASE_URL` = *A URL do seu projeto Supabase*
    *   `SUPABASE_SERVICE_ROLE_KEY` (ou `SUPABASE_KEY`) = *Sua chave de acesso de serviço segura*

### Opção C: Modo Volátil (In-Memory)
Se nenhuma variável acima for configurada, a API armazenará os backups temporariamente na memória RAM do contêiner. Isso é ótimo para testes rápidos, mas lembre-se que os dados expiram quando a função da Vercel entra em standby (inatividade).

---

## ⚡ Funcionalidade de Auto-Sync
No Painel Web, as configurações de sincronização da Vercel agora possuem uma chave de **Sincronização Automática (Auto-Sync)**:
*   **Ativo (Luz Verde 🟢):** Envia todas as atualizações de dados em tempo real para a Vercel de forma automática e otimizada (com debouncing).
*   **Pausado (Luz Cinza ⚫):** Desativa o envio em tempo real. Você ainda poderá efetuar backups manuais usando os botões de Enviar/Baixar.
