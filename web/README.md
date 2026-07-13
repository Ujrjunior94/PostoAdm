# Painel Administrativo Web - Meu Posto ⛽

Este diretório contém a versão web do **Meu Posto (PostoAdmin)**. O painel foi desenvolvido para permitir que gerentes e supervisores controlem as operações de qualquer computador ou celular através do navegador, sincronizando todos os dados em tempo real com o aplicativo Android utilizando o **Firebase Firestore** e **Firebase Authentication**.

---

## 🚀 Como Executar Localmente

Como o painel foi construído de forma totalmente autocontida em uma **Single Page Application (SPA)**, você pode abri-lo e usá-lo instantaneamente:

1. **Abrir o Arquivo diretamente**:
   - Basta dar um clique duplo no arquivo `index.html` para abri-lo no Chrome, Edge, Firefox ou Safari.
2. **Servidor Local Simples (Opcional)**:
   - Se preferir rodar com um servidor web local, você pode usar o terminal na raiz deste projeto:
     ```bash
     # Usando Python (qualquer versão)
     python -m http.server 8000
     ```
     Depois, abra no seu navegador: `http://localhost:8000/web/`

---

## 🔒 Conexão em Tempo Real com o Firebase

Para que o Painel Web se conecte ao **mesmo banco de dados do seu aplicativo Android**, siga estas etapas:

1. Obtenha as credenciais do seu projeto Web no **Console do Firebase**:
   - Vá em *Configurações do Projeto* ⚙️ -> *Geral*.
   - Em *Seus aplicativos*, clique em adicionar aplicativo Web (`</>`).
   - Copie o objeto `firebaseConfig` gerado.
2. No Painel Web do Meu Posto:
   - Clique no botão amarelo no topo superior direito (**"Modo Off-line / Local"**).
   - Cole o código de configuração copiado no modal de texto.
   - Clique em **"Salvar e Conectar"**.
3. O painel recarregará automaticamente no modo **Nuvem Firebase Ativa**, e todos os tanques, fechamentos de caixa, escalas e auditorias criados na Web aparecerão na hora no aplicativo Android e vice-versa!

---

## 💻 Modo de Demonstração Off-line (Local)

Se você não tiver configurado o Firebase ou quiser testar o painel imediatamente de forma isolada:
- O painel possui um simulador local integrado que salva todos os dados diretamente no `localStorage` do seu navegador.
- **Credenciais padrão para teste off-line**:
  - **Login / E-mail**: `admin@posto.com`
  - **Senha**: `admin123`
- Você pode criar novos tanques, funcionários, bicos e fechar relatórios diários de forma 100% funcional sem precisar de internet!

---

## 📦 Como Exportar e Hospedar (Hospedagem Gratuita)

Você pode hospedar este painel gratuitamente no **Firebase Hosting** do seu projeto:

1. Instale as ferramentas do Firebase no seu computador:
   ```bash
   npm install -g firebase-tools
   ```
2. Inicie o projeto de hospedagem na pasta:
   ```bash
   firebase login
   firebase init hosting
   ```
   *Dica: Selecione a pasta `web` como o diretório público do seu site.*
3. Faça o deploy em segundos:
   ```bash
   firebase deploy
   ```
   O Firebase fornecerá um link seguro do tipo `https://seu-projeto.web.app` para você acessar de qualquer lugar!

---

## ⚡ Hospedagem na Vercel (Front-end SPA + API Backend em TypeScript)

O Meu Posto agora está totalmente preparado para ser implantado na **Vercel** com suporte nativo a **Funções Serverless (em TypeScript)** que gerenciam a sincronização de dados de forma segura e veloz!

### ⚙️ Estrutura do Projeto Vercel
A pasta `/web` foi estruturada de forma modular contendo:
*   `index.html`: Interface do usuário do Painel Administrativo.
*   `vercel.json`: Arquivo de configuração de roteamento e habilitação nativa de CORS para que o aplicativo Android se comunique sem bloqueios.
*   `package.json` & `tsconfig.json`: Definição de dependências e regras de compilação TypeScript para a API.
*   `api/backup.ts`: O endpoint serverless principal (`POST /api/backup` para salvar e `GET /api/backup` para ler) que atende tanto a Web quanto o Aplicativo Android.

### 🚀 Passos para implantar na Vercel

Você pode fazer o deploy em minutos usando a CLI da Vercel ou vinculando ao seu repositório no GitHub:

#### Opção A: Usando a CLI da Vercel
1. Instale a ferramenta CLI da Vercel globalmente:
   ```bash
   npm install -g vercel
   ```
2. Navegue até a pasta `web/` do seu projeto:
   ```bash
   cd web
   ```
3. Execute o comando de deploy e siga as instruções na tela:
   ```bash
   vercel
   ```
4. Para enviar a versão de produção definitiva:
   ```bash
   vercel --prod
   ```

#### Opção B: Pelo Painel Web da Vercel (GitHub)
1. Crie ou envie este repositório para o seu **GitHub/GitLab/Bitbucket**.
2. Acesse [vercel.com](https://vercel.com) e crie um novo projeto.
3. Importe o repositório.
4. **Importante:** Nas configurações do projeto, defina o **Root Directory** (Diretório Raiz) como `web`.
5. Clique em **Deploy**. A Vercel criará automaticamente o link de produção (ex: `https://meu-posto.vercel.app`)!

---

## 🗄️ Integração com Bancos de Dados na Vercel

O arquivo `/web/api/backup.ts` em TypeScript gerencia automaticamente múltiplos motores de armazenamento na nuvem baseado nas variáveis de ambiente que você definir nas configurações da Vercel:

### 1. Vercel KV (Redis de Alta Performance) - RECOMENDADO ⭐
É a forma mais rápida e simples de armazenar os backups chave-valor dos postos.
*   **Como Configurar:** No painel do seu projeto na Vercel, vá em *Storage* (Armazenamento), selecione **KV (Redis)** e clique em conectar.
*   As variáveis `KV_REST_API_URL` e `KV_REST_API_TOKEN` serão criadas e vinculadas automaticamente. A API começará a utilizá-lo na hora!

### 2. Supabase (Banco de Dados Relacional PostgreSQL)
Se preferir armazenar os dados em formato JSON em seu banco Supabase pré-existente:
*   **Como Configurar:** Crie uma tabela chamada `posto_backups` com as colunas `id` (text, primary key), `data` (jsonb) e `updated_at` (timestamp).
*   Nas configurações de variáveis de ambiente do seu projeto Vercel, adicione:
    *   `SUPABASE_URL` = *A URL do seu projeto Supabase*
    *   `SUPABASE_SERVICE_ROLE_KEY` (ou `SUPABASE_KEY`) = *Sua chave de acesso de serviço segura*

### 3. Modo Volátil (In-Memory)
Se nenhuma variável acima estiver configurada, a API funcionará em modo temporário na memória RAM do contêiner. Isso é ideal para testes iniciais rápidos, mas lembre-se que os dados expiram quando a função da Vercel entra em standby (inatividade).

