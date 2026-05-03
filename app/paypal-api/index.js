import express from "express";
import fetch from "node-fetch";

const app = express();
app.use(express.json());

// ================== CONFIG ==================
const PORT = 3000;

// ⚠️ À déplacer plus tard dans .env
const PAYPAL_CLIENT =
  "AQaeJzVV8h0wrArn8a-kQht556YzHeNGASc3JfrLOK9H-vwCNTgMZA2QMp75jdospzlx6HLbS7O73rr5";
const PAYPAL_SECRET =
  "EJ5mipVBYBAIhFjdooFBar8wkzrvSY1aJQWKdqhBXibcgNlXp9Zycyd2IkVq81vl9zC3Jo_RQWw3e2Wf";

// ================== ROUTE TEST ==================
app.get("/", (req, res) => {
  res.send("Backend PayPal OK");
});

// ================== PAYPAL TOKEN ==================
async function getAccessToken() {
  const auth = Buffer.from(
    PAYPAL_CLIENT + ":" + PAYPAL_SECRET
  ).toString("base64");

  const response = await fetch(
    "https://api-m.sandbox.paypal.com/v1/oauth2/token",
    {
      method: "POST",
      headers: {
        Authorization: `Basic ${auth}`,
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: "grant_type=client_credentials",
    }
  );

  const data = await response.json();

  if (!data.access_token) {
    throw new Error("Impossible d'obtenir le token PayPal");
  }

  return data.access_token;
}

// ================== CREATE ORDER ==================
app.post("/create-order", async (req, res) => {
  try {
    const { amount } = req.body;

    if (!amount) {
      return res.status(400).json({ error: "Amount manquant" });
    }

    const token = await getAccessToken();

    const response = await fetch(
      "https://api-m.sandbox.paypal.com/v2/checkout/orders",
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          intent: "CAPTURE",
          purchase_units: [
            {
              amount: {
                currency_code: "USD",
                value: parseFloat(amount).toFixed(2),
              },
              description: "Commande - Restaurant Marrakech",
            },
          ],
          application_context: {
            brand_name: "Restaurant Marrakech",
            landing_page: "LOGIN",
            user_action: "PAY_NOW",
            shipping_preference: "NO_SHIPPING",
            return_url: "marrakechapp://payment-success",
            cancel_url: "marrakechapp://payment-cancel",
          },
        }),
      }
    );

    const data = await response.json();

    if (!data.links) {
      console.error(data);
      return res.status(500).json({ error: "Erreur PayPal" });
    }

    const approveUrl = data.links.find(
      (link) => link.rel === "approve"
    )?.href;

    res.json({
      id: data.id,
      approveUrl: approveUrl,
    });
  } catch (error) {
    console.error("PAYPAL ERROR:", error);
    res.status(500).json({ error: "PayPal server error" });
  }
});

// ================== START SERVER ==================
app.listen(PORT, "0.0.0.0", () => {
  console.log(`✅ PayPal API running on http://0.0.0.0:${PORT}`);
});
