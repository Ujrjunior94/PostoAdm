package com.meuposto;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ⚡ Meu Posto - Servidor Java Backend de Alta Performance
 * 
 * Implementação nativa e ultraleve que gerencia a sincronização e backup
 * de dados usando o servidor HTTP embutido do Java SE.
 * 
 * Funcionalidades:
 * - Porta padrão: 8080 (configurável por variável de ambiente PORT)
 * - Suporte completo a CORS (GET, POST, OPTIONS)
 * - Persistência direta em arquivos JSON locais (pasta ./backups)
 * - Sem dependências externas - compila e roda em qualquer ambiente Java (JDK 8+)
 */
public class BackupServer {

    private static final int DEFAULT_PORT = 8080;
    private static final String BACKUP_DIR = "backups";

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Permite definir a porta via variável de ambiente (ex: Railway, Render, Heroku)
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida especificada na variável PORT, usando padrão: " + DEFAULT_PORT);
            }
        }

        try {
            // Cria o diretório de backups se não existir
            Files.createDirectories(Paths.get(BACKUP_DIR));

            // Inicializa o servidor HTTP
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // Define a rota principal para backup
            server.createContext("/api/backup", new BackupHandler());
            
            // Define o executor multithread para alta performance
            server.setExecutor(Executors.newCachedThreadPool());
            
            System.out.println("=================================================");
            System.out.println("⚡ Servidor Java 'Meu Posto' Iniciado com Sucesso!");
            System.out.println("📍 Endereço local: http://localhost:" + port);
            System.out.println("📂 Diretório de armazenamento: ./" + BACKUP_DIR);
            System.out.println("=================================================");
            
            server.start();

        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Manipulador das requisições de API (GET, POST, OPTIONS)
     */
    static class BackupHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Configura os cabeçalhos de CORS (Cross-Origin Resource Sharing)
            setCorsHeaders(exchange);

            String requestMethod = exchange.getRequestMethod();

            // Trata requisição de preflight (OPTIONS)
            if ("OPTIONS".equalsIgnoreCase(requestMethod)) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            try {
                if ("GET".equalsIgnoreCase(requestMethod)) {
                    handleGet(exchange);
                } else if ("POST".equalsIgnoreCase(requestMethod)) {
                    handlePost(exchange);
                } else {
                    // Método não permitido
                    sendResponse(exchange, 405, "{\"error\": \"Método não permitido. Utilize GET ou POST.\"}");
                }
            } catch (Exception e) {
                System.err.println("Erro ao processar requisição: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\": \"Erro interno do servidor\", \"details\": \"" + e.getMessage() + "\"}");
            }
        }

        /**
         * Trata requisições GET para recuperar o backup de um posto por CNPJ
         */
        private void handleGet(HttpExchange exchange) throws IOException {
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String cnpj = queryParams.get("cnpj");
            if (cnpj == null || cnpj.trim().isEmpty()) {
                cnpj = queryParams.get("id"); // Fallback
            }

            if (cnpj == null || cnpj.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"CNPJ ou ID do posto é obrigatório na query (?cnpj=...)\"}");
                return;
            }

            cnpj = sanitizeFileName(cnpj);
            Path filePath = Paths.get(BACKUP_DIR, cnpj + ".json");

            if (Files.exists(filePath)) {
                System.out.println("[Java Server] [GET] Enviando backup recuperado para o CNPJ: " + cnpj);
                byte[] fileBytes = Files.readAllBytes(filePath);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, fileBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(fileBytes);
                os.close();
            } else {
                System.out.println("[Java Server] [GET] Backup não encontrado para o CNPJ: " + cnpj);
                sendResponse(exchange, 200, "{\"message\": \"Nenhum backup encontrado na nuvem para este CNPJ\", \"cnpj\": \"" + cnpj + "\", \"data\": null}");
            }
        }

        /**
         * Trata requisições POST para salvar o backup enviado pelo cliente
         */
        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            String body = bos.toString(StandardCharsets.UTF_8.name());

            // Extrai o CNPJ/ID do corpo do JSON de forma manual e segura (sem libs externas)
            String cnpj = extractJsonValue(body, "cnpj");
            if (cnpj == null || cnpj.trim().isEmpty()) {
                cnpj = extractJsonValue(body, "id"); // Fallback
            }

            if (cnpj == null || cnpj.trim().isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"CNPJ ou ID do posto é obrigatório no corpo do JSON (campo 'cnpj' ou 'id').\"}");
                return;
            }

            cnpj = sanitizeFileName(cnpj);
            Path filePath = Paths.get(BACKUP_DIR, cnpj + ".json");

            // Grava o arquivo JSON localmente
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                writer.write(body);
            }

            System.out.println("[Java Server] [POST] Backup salvo com sucesso para o CNPJ: " + cnpj);

            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());
            String jsonResponse = String.format(
                "{\"success\": true, \"message\": \"Backup sincronizado com sucesso no Servidor Java!\", \"cnpj\": \"%s\", \"timestamp\": \"%s\", \"engine\": \"Java Local File System\"}",
                cnpj, timestamp
            );

            sendResponse(exchange, 200, jsonResponse);
        }

        /**
         * Envia uma resposta JSON padronizada
         */
        private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
            byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }

        /**
         * Adiciona cabeçalhos de CORS necessários
         */
        private void setCorsHeaders(HttpExchange exchange) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization");
            exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        }

        /**
         * Analisa parâmetros da Query String do URI
         */
        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query == null || query.trim().isEmpty()) {
                return params;
            }
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                    String value = (idx > 0 && pair.length() > idx + 1) ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : "";
                    params.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    // Ignora silenciosamente
                }
            }
            return params;
        }

        /**
         * Extrai valor de um campo de texto em uma string JSON usando expressões regulares
         */
        private String extractJsonValue(String json, String key) {
            Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }

        /**
         * Limpa caracteres indesejados para salvar arquivos com segurança
         */
        private String sanitizeFileName(String name) {
            return name.replaceAll("[\\\\/:*?\"<>|\\s]", "_");
        }
    }
}
