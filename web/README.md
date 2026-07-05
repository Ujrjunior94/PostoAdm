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
