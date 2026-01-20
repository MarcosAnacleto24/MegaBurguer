const express = require('express');
const admin = require('firebase-admin');
const app = express();
app.use(express.json());

app.get("/", (req, res) => {
  res.send("API online 🚀");
});

// Inicializa o Firebase
const serviceAccount = JSON.parse(process.env.FIREBASE_CREDENTIALS);
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://megaburguer-27edf-default-rtdb.firebaseio.com"
});
const rtdb = admin.database();


// Exemplo: Deletar usuário pelo ID do documento
app.post('/deleteUser', async (req, res) => {
  const { uid } = req.body;

  console.log("UID recebido:", uid);

  if (!uid) {
    console.log("UID vazio");
    return res.status(400).json({ error: "UID não informado" });
  }

  try {
    console.log("Deletando AUTH...");
    await admin.auth().deleteUser(uid);

    console.log("Deletando RTDB...");
    await admin.database().ref(`users/${uid}`).remove();

    console.log("Usuário deletado com sucesso");

    res.status(200).json({ message: "Usuário excluído com sucesso" });
  } catch (err) {
    console.error("ERRO AO DELETAR:", err);
    res.status(500).json({ error: err.message });
  }
});


const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Servidor rodando na porta ${PORT}`));