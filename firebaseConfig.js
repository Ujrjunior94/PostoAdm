/**
 * PostoAdmin - Firebase Web/Cross-Platform Configuration File
 * 
 * Este arquivo inicializa o Firebase SDK para aplicações Web, Painéis Administrativos,
 * ou serviços auxiliares em Node.js/JavaScript que se conectam ao mesmo banco de dados Firestore e Auth.
 * 
 * Substitua os valores abaixo com as credenciais do seu projeto obtidas no console do Firebase:
 * https://console.firebase.google.com/
 */

import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

// Configuração do Firebase obtida no Console do Firebase
const firebaseConfig = {
  apiKey: "SUA_API_KEY_AQUI",
  authDomain: "seu-projeto.firebaseapp.com",
  projectId: "seu-projeto",
  storageBucket: "seu-projeto.appspot.com",
  messagingSenderId: "SEU_MESSAGING_SENDER_ID",
  appId: "SEU_APP_ID",
  measurementId: "SEU_MEASUREMENT_ID" // Opcional
};

// Inicializa o Firebase
const app = initializeApp(firebaseConfig);

// Inicializa os Serviços
export const auth = getAuth(app);
export const db = getFirestore(app);

export default app;
