"""
WinGo — API IA Flask

Contient :
1) Modèle ML de prédiction d'abandon de panier
2) Smart Product Assistant avec Gemini pour générer une fiche produit

Dataset : wingo_v3.csv
Lancer : python ai_model/app.py
"""

import os
import json
import re
import pickle
from pathlib import Path

import numpy as np
import pandas as pd
from flask import Flask, request, jsonify
from flask_cors import CORS
from dotenv import load_dotenv

import google.generativeai as genai

from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import accuracy_score, classification_report


# ─── CONFIG FLASK ─────────────────────────────────────────────────────────────

app = Flask(__name__)
CORS(app)


# ─── CHARGER .env.local SYMFONY ──────────────────────────────────────────────
# ai_model/app.py -> parent = ai_model -> parent.parent = racine WEB/

ROOT_DIR = Path(__file__).resolve().parent.parent
ENV_LOCAL_PATH = ROOT_DIR / ".env.local"

load_dotenv(dotenv_path=ENV_LOCAL_PATH)


# ─── CONFIG GEMINI ───────────────────────────────────────────────────────────

GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
print("[DEBUG] GEMINI_API_KEY =", GEMINI_API_KEY[:8] if GEMINI_API_KEY else "AUCUNE")
if GEMINI_API_KEY:
    genai.configure(api_key=GEMINI_API_KEY)
    gemini_model = genai.GenerativeModel("gemini-2.5-flash-lite")
else:
    gemini_model = None
    print("[WinGo AI] Attention : GEMINI_API_KEY introuvable dans .env.local")


# ─── CONFIG MODELE PREDICTION PANIER ─────────────────────────────────────────

BASE_DIR = os.path.dirname(__file__)

MODEL_PATH = os.path.join(BASE_DIR, "model.pkl")
SCALER_PATH = os.path.join(BASE_DIR, "scaler.pkl")
DATASET_PATH = os.path.join(BASE_DIR, "wingo_v3.csv")

FEATURES = ["pages_visited", "time_on_site", "cart_value"]
TARGET = "abandoned"

# Plages du dataset wingo_v3.csv
PAGES_MIN = 1
PAGES_MAX = 10
TIME_MIN = 30
TIME_MAX = 600
CART_MIN = 5.0
CART_MAX = 500.0
TIME_DEFAULT = 343


# ─── UTILITAIRE GEMINI ───────────────────────────────────────────────────────

def clean_gemini_json(text: str) -> str:
    """
    Nettoie la réponse Gemini.
    Gemini peut parfois retourner :
    ```json
    {...}
    ```
    Cette fonction récupère seulement l'objet JSON.
    """
    text = text.strip()

    text = re.sub(r"^```json\s*", "", text)
    text = re.sub(r"^```\s*", "", text)
    text = re.sub(r"\s*```$", "", text)

    start = text.find("{")
    end = text.rfind("}")

    if start != -1 and end != -1:
        text = text[start:end + 1]

    return text.strip()


# ─── ENTRAÎNEMENT MODELE PANIER ──────────────────────────────────────────────

def train_model() -> tuple:
    print("[WinGo AI] Chargement du dataset wingo_v3.csv...")

    if not os.path.exists(DATASET_PATH):
        raise FileNotFoundError(
            f"Dataset introuvable : {DATASET_PATH}\n"
            "Place wingo_v3.csv dans le dossier ai_model/"
        )

    df = pd.read_csv(DATASET_PATH)
    print(f"[WinGo AI] Dataset : {df.shape[0]} lignes")

    missing = [c for c in FEATURES + [TARGET] if c not in df.columns]
    if missing:
        raise ValueError(f"Colonnes manquantes : {missing}")

    X = df[FEATURES].copy()
    y = df[TARGET].copy()

    print(f"[WinGo AI] Distribution : {y.value_counts().to_dict()}")
    print(
        f"[WinGo AI] Achat   → pages={X[y == 0]['pages_visited'].mean():.1f}, "
        f"time={X[y == 0]['time_on_site'].mean():.0f}s, "
        f"cart={X[y == 0]['cart_value'].mean():.0f} TND/ligne"
    )
    print(
        f"[WinGo AI] Abandon → pages={X[y == 1]['pages_visited'].mean():.1f}, "
        f"time={X[y == 1]['time_on_site'].mean():.0f}s, "
        f"cart={X[y == 1]['cart_value'].mean():.0f} TND/ligne"
    )

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    X_train, X_test, y_train, y_test = train_test_split(
        X_scaled,
        y,
        test_size=0.2,
        random_state=42,
        stratify=y
    )

    print("[WinGo AI] Entraînement RandomForestClassifier...")

    model = RandomForestClassifier(
        n_estimators=100,
        max_depth=8,
        random_state=42,
        n_jobs=-1
    )

    model.fit(X_train, y_train)

    y_pred = model.predict(X_test)
    acc = accuracy_score(y_test, y_pred)

    print(f"[WinGo AI] Accuracy : {acc * 100:.1f}%")
    print(classification_report(y_test, y_pred, target_names=["Achat", "Abandon"]))

    with open(MODEL_PATH, "wb") as f:
        pickle.dump(model, f)

    with open(SCALER_PATH, "wb") as f:
        pickle.dump(scaler, f)

    print("[WinGo AI] model.pkl et scaler.pkl sauvegardés.")

    return model, scaler


# ─── CHARGEMENT MODELE PANIER ────────────────────────────────────────────────

if os.path.exists(MODEL_PATH) and os.path.exists(SCALER_PATH):
    with open(MODEL_PATH, "rb") as f:
        model = pickle.load(f)

    with open(SCALER_PATH, "rb") as f:
        scaler = pickle.load(f)

    print("[WinGo AI] Modèle chargé depuis le disque.")
else:
    model, scaler = train_model()


# ─── ENDPOINT HEALTH ─────────────────────────────────────────────────────────

@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "ok",
        "cart_model": "RandomForestClassifier",
        "dataset": "wingo_v3.csv",
        "features": FEATURES,
        "gemini_configured": GEMINI_API_KEY is not None,
    })


# ─── ENDPOINT PREDICTION PANIER ──────────────────────────────────────────────

@app.route("/predict", methods=["POST"])
def predict():
    """
    Reçoit les features du panier WinGo et prédit l'intention d'achat.

    Body JSON attendu :
    {
        "nb_produits": int,
        "total_panier": float,
        "time_on_site": int
    }

    Réponse :
    {
        "prediction": 1=va acheter, 0=risque d'abandon,
        "probabilite": % probabilité d'achat,
        "label": texte lisible
    }
    """
    data = request.get_json(silent=True)

    if not data:
        return jsonify({"error": "Body JSON manquant."}), 400

    try:
        pages_visited = int(data.get("nb_produits", 1))
        cart_value = float(data.get("total_panier", 0.0))
        time_on_site = int(data.get("time_on_site", TIME_DEFAULT))

        # Clamping dans les plages du dataset
        pages_visited = max(PAGES_MIN, min(pages_visited, PAGES_MAX))
        time_on_site = max(TIME_MIN, min(time_on_site, TIME_MAX))
        cart_value = max(CART_MIN, min(cart_value, CART_MAX))

        features = [[pages_visited, time_on_site, cart_value]]
        features_scaled = scaler.transform(features)

        # Dataset : 0 = achat, 1 = abandon
        prediction_brute = int(model.predict(features_scaled)[0])
        proba = model.predict_proba(features_scaled)[0]

        proba_achat = round(float(proba[0]) * 100, 1)
        va_acheter = 1 if prediction_brute == 0 else 0

        return jsonify({
            "prediction": va_acheter,
            "probabilite": proba_achat,
            "label": "Va acheter" if va_acheter == 1 else "Risque d'abandon",
        })

    except (ValueError, TypeError) as e:
        return jsonify({"error": f"Données invalides : {str(e)}"}), 422

    except Exception as e:
        return jsonify({"error": f"Erreur serveur : {str(e)}"}), 500


# ─── ENDPOINT SMART PRODUCT ASSISTANT ────────────────────────────────────────

@app.route("/api/product/generate", methods=["POST"])
def generate_product():
    """
    Génère une fiche produit professionnelle avec Gemini.

    Body JSON attendu :
    {
        "nom": "Miel naturel",
        "categorie": "Gastronomie",
        "region": "Béja",
        "prix": 25,
        "stock": 30
    }
    """
    if gemini_model is None:
        return jsonify({
            "success": False,
            "error": "GEMINI_API_KEY manquante dans .env.local."
        }), 500

    data = request.get_json(silent=True) or {}

    nom = str(data.get("nom", "")).strip()
    categorie = str(data.get("categorie", "")).strip()
    region = str(data.get("region", "")).strip()
    prix = str(data.get("prix", "")).strip()
    stock = str(data.get("stock", "")).strip()

    if not nom:
        return jsonify({
            "success": False,
            "error": "Le nom du produit est obligatoire."
        }), 400

    prompt = f"""
Tu es un assistant marketing spécialisé dans le commerce local tunisien.
À partir des informations suivantes d'un produit, génère une fiche produit professionnelle.

Informations du produit :
- Nom        : {nom}
- Catégorie  : {categorie if categorie else "non précisée"}
- Région     : {region if region else "non précisée"}
- Prix       : {prix if prix else "non précisé"} TND
- Stock      : {stock if stock else "non précisé"} unités

Retourne UNIQUEMENT un objet JSON valide, sans markdown, sans explication, avec exactement cette structure :
{{
  "titre_ameliore": "...",
  "description": "...",
  "tags": ["...", "...", "...", "..."],
  "score_qualite": 0,
  "conseils": ["...", "..."]
}}

Règles :
- description : 2 à 3 phrases marketing, chaleureuses et professionnelles.
- Si la région est précisée, mets en valeur l'origine locale.
- tags : 4 à 6 mots-clés pertinents.
- score_qualite : évalue la complétude des informations fournies entre 0 et 100.
- conseils : 2 à 4 suggestions concrètes pour améliorer la fiche.
- Ne pas inventer des informations comme bio, naturel, fait main, artisanal ou premium si elles ne sont pas indiquées dans le nom ou la catégorie.
"""

    try:
        response = gemini_model.generate_content(prompt)

        if not response.text:
            return jsonify({
                "success": False,
                "error": "Réponse vide depuis Gemini."
            }), 502

        raw = clean_gemini_json(response.text)
        result = json.loads(raw)

        return jsonify({
            "success": True,
            "titre_ameliore": result.get("titre_ameliore", ""),
            "description": result.get("description", ""),
            "tags": result.get("tags", []),
            "score_qualite": result.get("score_qualite", 0),
            "conseils": result.get("conseils", []),
        })

    except json.JSONDecodeError as e:
        return jsonify({
            "success": False,
            "error": f"JSON invalide retourné par Gemini : {str(e)}",
            "raw_response": response.text if "response" in locals() else ""
        }), 500

    except Exception as e:
        return jsonify({
            "success": False,
            "error": f"Erreur serveur pendant la génération IA : {str(e)}"
        }), 500


# ─── ENDPOINT RETRAIN PANIER ─────────────────────────────────────────────────

@app.route("/retrain", methods=["POST"])
def retrain():
    """Réentraîne le modèle panier à la demande."""
    global model, scaler

    try:
        model, scaler = train_model()

        return jsonify({
            "status": "ok",
            "message": "Modèle réentraîné avec succès."
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ─── LANCEMENT ───────────────────────────────────────────────────────────────

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5001))
    app.run(host="0.0.0.0", port=port)