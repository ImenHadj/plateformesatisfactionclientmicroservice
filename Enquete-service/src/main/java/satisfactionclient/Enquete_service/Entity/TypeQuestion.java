package satisfactionclient.Enquete_service.Entity;

public enum TypeQuestion {
    OUVERT,             // Réponse texte libre
    TEXTE_LONG,         // Réponse texte avec plusieurs lignes
    CHOIX_SIMPLE,       // Un seul choix parmi plusieurs options
    CHOIX_MULTIPLE,     // Plusieurs choix possibles
    OUI_NON,            // Question binaire Oui / Non
    NOTE,               // Notation sur une échelle (ex: 1 à 5, étoiles)
    LIKERT,             // Échelle de satisfaction (ex: Pas d'accord → Tout à fait d'accord)
    CLASSEMENT,         // L'utilisateur classe les options dans un ordre de préférence
    DATE,               // Sélection d'une date
    HEURE,              // Sélection d'une heure
    DATE_HEURE,         // Sélection d'une date et heure combinée
    NUMERIQUE,          // Réponse sous forme de nombre
    POURCENTAGE,        // Réponse en pourcentage (ex: taux de satisfaction)
    DEVISE,             // Montant en argent (ex: euros, dollars)
    EMAIL,              // Validation d’un email
    TELEPHONE,          // Numéro de téléphone avec validation
    FICHIER,            // Téléversement de fichier (ex: PDF, image)
    LOCALISATION,       // Capture d’une position GPS
    SIGNATURE,          // Saisie d’une signature numérique (via un canvas interactif)
    SLIDER,             // Curseur pour sélectionner une valeur entre deux bornes
    MATRICE,            // Réponses sous forme de tableau (ex: notation de plusieurs critères)
    IMAGE,              // Réponse sous forme de photo prise avec la caméra
    DESSIN,             // Réponse sous forme de dessin libre (ex: signature électronique)
    CODE_PIN,           // Saisie sécurisée d’un code à 4-6 chiffres (ex: validation bancaire)
    CHOIX_COULEUR ,      // Sélection d’une couleur dans une palette
    CAPTCHA,
    QR_CODE
}
