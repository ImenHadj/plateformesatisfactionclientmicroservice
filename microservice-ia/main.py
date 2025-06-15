from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from llama_cpp import Llama
import re
import logging
from typing import List, Dict, Optional
from datetime import datetime

# Configuration du logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

# Middleware de gestion d'erreurs
@app.middleware("http")
async def error_handler(request: Request, call_next):
    try:
        start_time = datetime.now()
        response = await call_next(request)
        process_time = (datetime.now() - start_time).total_seconds()
        response.headers["X-Process-Time"] = str(process_time)
        return response
    except Exception as e:
        logger.error(f"Erreur non gérée: {str(e)}", exc_info=True)
        return JSONResponse(
            status_code=500,
            content={"detail": f"Erreur interne du serveur: {str(e)}"}
        )

# Initialisation du modèle LLM
try:
    llm = Llama(
        model_path="mistral.Q4_K_M.gguf",
        n_ctx=2048,
        n_threads=4,
        verbose=False
    )
    logger.info("Modèle LLM chargé avec succès")
except Exception as e:
    logger.error(f"Erreur lors du chargement du modèle: {str(e)}")
    raise RuntimeError("Impossible de charger le modèle LLM") from e

# Modèles Pydantic
class Question(BaseModel):
    question: str
    type: str
    choices: List[str]
    theme: str

class EnqueteResponse(BaseModel):
    questions: List[Question]
    raw_output: Optional[str] = None
    warnings: List[str] = []
    timestamp: str = Field(default_factory=lambda: datetime.now().isoformat())

class EnqueteRequest(BaseModel):
    titre: str
    description: str

# Constantes pour la validation
VALID_TYPES = {
    "question ouverte": "Question ouverte",
    "échelle de satisfaction": "Échelle de satisfaction",
    "choix simple": "Choix simple",
    "choix multiple": "Choix multiple",
    "notation": "Notation",
    "oui/non": "Oui/Non",
    "likert": "Échelle Likert"
}

TYPE_SYNONYMS = {
    "ouverte": "Question ouverte",
    "échelle": "Échelle de satisfaction",
    "oui non": "Oui/Non",
    "oui/non": "Oui/Non",
    "likert": "Échelle Likert"
}

THEME_KEYWORDS = {
    "accueil": "Accueil",
    "en ligne": "Expérience en ligne",
    "site": "Expérience en ligne",
    "délai": "Délais",
    "clarté": "Clarté des informations",
    "recommander": "Recommandation",
    "global": "Satisfaction globale",
    "service": "Qualité du service",
    "agence": "Expérience en agence",
    "traitement": "Traitement des demandes",
    "professionnel": "Professionalisme",
    "rapide": "Rapidité",
    "besoin": "Adéquation aux besoins"
}

DEFAULT_CHOICES = {
    "Échelle de satisfaction": ["1 - Très insatisfait", "2", "3", "4", "5 - Très satisfait"],
    "Notation": ["1 (Très mauvais)", "2", "3", "4", "5 (Excellent)"],
    "Oui/Non": ["Oui", "Non"],
    "Échelle Likert": ["Fortement en désaccord", "En désaccord", "Neutre", "D'accord", "Fortement d'accord"]
}

# Endpoint de santé
@app.get("/health")
async def health_check():
    return {
        "status": "OK",
        "model_loaded": llm is not None,
        "timestamp": datetime.now().isoformat()
    }

# Fonctions utilitaires
def normalize_type(type_raw: str) -> Optional[str]:
    t = type_raw.strip().lower()
    return VALID_TYPES.get(t) or TYPE_SYNONYMS.get(t)

def detect_theme(question: str) -> str:
    q = question.lower()
    for keyword, theme in THEME_KEYWORDS.items():
        if keyword in q:
            return theme
    return "Satisfaction globale"

def clean_choices(choices: List[str]) -> List[str]:
    return [c.strip() for c in choices
            if c.strip()
            and not c.strip().startswith('[')
            and not c.strip().endswith(']')
            and "option" not in c.lower()]

def get_default_questions(titre: str, count: int) -> List[Question]:
    """Retourne des questions par défaut basées sur le thème"""
    base_questions = [
        Question(
            question=f"Que pensez-vous de {titre.lower()}?",
            type="Question ouverte",
            choices=[],
            theme="Satisfaction globale"
        ),
        Question(
            question=f"Notez votre expérience avec {titre.lower()} (1-5)",
            type="Échelle de satisfaction",
            choices=DEFAULT_CHOICES["Échelle de satisfaction"],
            theme="Évaluation globale"
        ),
        Question(
            question=f"Quels aspects de {titre.lower()} avez-vous appréciés?",
            type="Choix multiple",
            choices=["Qualité", "Service", "Prix", "Facilité d'utilisation"],
            theme="Points positifs"
        ),
        Question(
            question=f"Recommanderiez-vous {titre.lower()} à d'autres?",
            type="Oui/Non",
            choices=DEFAULT_CHOICES["Oui/Non"],
            theme="Recommandation"
        ),
        Question(
            question=f"Comment pourrions-nous améliorer {titre.lower()}?",
            type="Question ouverte",
            choices=[],
            theme="Améliorations"
        )
    ]
    return base_questions[:count]

# Endpoint principal
@app.post("/generate-questions", response_model=EnqueteResponse)
async def generate_questions(input: EnqueteRequest):
    logger.info(f"Reçu une requête pour générer des questions: {input.titre}")

    prompt = f"""Tu es un expert en création de questionnaires de satisfaction. Génère 5 questions variées en français.

Thème: {input.titre}
Contexte: {input.description}

FORMAT REQUIS POUR CHAQUE QUESTION:
=== DEBUT QUESTION ===
Question: [texte]
Type: [Question ouverte|Échelle de satisfaction|Choix simple|Choix multiple|Notation|Oui/Non|Échelle Likert]
Thème: [sous-thème]
Choix:
- option1
- option2
=== FIN QUESTION ===

Règles STRICTES:
1. Une question par type maximum
2. Pour les échelles, utiliser les options prédéfinies
3. Pour Oui/Non: options "Oui" et "Non" uniquement
4. Ne pas inclure de texte supplémentaire

Exemple valide:
=== DEBUT QUESTION ===
Question: Comment évaluez-vous notre service client?
Type: Échelle de satisfaction
Thème: Qualité du service
Choix:
- 1 - Très insatisfait
- 2
- 3
- 4
- 5 - Très satisfait
=== FIN QUESTION ===

Génère maintenant 5 questions distinctes:"""

    try:
        output = llm(
            prompt,
            max_tokens=2000,
            temperature=0.5,
            stop=["=== FIN QUESTION ==="],
            repeat_penalty=1.1
        )
        text = output["choices"][0]["text"].strip()
        logger.debug(f"Réponse brute:\n{text}")
    except Exception as e:
        logger.error(f"Erreur LLM: {str(e)}")
        # Retourner des questions par défaut en cas d'erreur
        return EnqueteResponse(
            questions=get_default_questions(input.titre, 5),
            warnings=["Erreur de génération - Questions par défaut utilisées"]
        )

    questions = []
    warnings = []
    used_types = set()

    # Nouveau parsing plus robuste
    question_blocks = [b.strip() for b in text.split("=== DEBUT QUESTION ===")[1:] if b.strip()]

    for block in question_blocks:
        try:
            block_content = block.split("=== FIN QUESTION ===")[0].strip()
            if not block_content:
                continue

            # Extraction avec regex plus permissive
            question_match = re.search(r"Question:\s*(.+?)(?:\n|$)", block_content, re.IGNORECASE)
            type_match = re.search(r"Type:\s*(.+?)(?:\n|$)", block_content, re.IGNORECASE)
            theme_match = re.search(r"Thème:\s*(.+?)(?:\n|$)", block_content, re.IGNORECASE)
            choices_match = re.search(r"Choix:\s*([\s\S]+?)(?:\n===|$)", block_content, re.IGNORECASE)

            if not all([question_match, type_match]):
                warnings.append("Format de question incomplet - question ignorée")
                continue

            question_text = question_match.group(1).strip()
            question_type = normalize_type(type_match.group(1).strip())
            theme = theme_match.group(1).strip() if theme_match else detect_theme(question_text)

            if not question_type:
                warnings.append(f"Type invalide: {type_match.group(1)} - question ignorée")
                continue

            if question_type in used_types:
                warnings.append(f"Type en doublon: {question_type} - question ignorée")
                continue

            # Gestion des choix
            choices = []
            if choices_match:
                raw_choices = [c.strip() for c in choices_match.group(1).split("\n") if c.strip()]
                choices = [c[2:] if c.startswith("- ") else c for c in raw_choices]
                choices = clean_choices(choices)

            # Appliquer les options par défaut si nécessaire
            if not choices and question_type in DEFAULT_CHOICES:
                choices = DEFAULT_CHOICES[question_type]

            questions.append(Question(
                question=question_text,
                type=question_type,
                choices=choices,
                theme=theme
            ))
            used_types.add(question_type)

        except Exception as e:
            warnings.append(f"Erreur parsing question: {str(e)}")
            continue

    # Compléter avec des questions par défaut si nécessaire
    if len(questions) < 5:
        needed = 5 - len(questions)
        default_questions = get_default_questions(input.titre, needed)
        questions.extend(default_questions)
        warnings.append(f"Complété avec {needed} questions par défaut")

    return EnqueteResponse(
        questions=questions[:5],
        raw_output=text,
        warnings=warnings
    )