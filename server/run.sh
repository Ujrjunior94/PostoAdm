#!/bin/bash
# ⚡ Script para compilar e executar o Servidor Java do Meu Posto

# Obtém o diretório do script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$DIR"

echo "☕ Compilando o Servidor Java 'Meu Posto'..."
mkdir -p bin

javac -d bin BackupServer.java

if [ $? -eq 0 ]; then
    echo "✅ Compilado com sucesso!"
    echo "🚀 Iniciando Servidor Java..."
    java -cp bin com.meuposto.BackupServer
else
    echo "❌ Erro ao compilar o código Java."
    exit 1
fi
