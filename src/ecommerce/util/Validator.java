package ecommerce.util;

public class Validator {

    private Validator() {}

    public static boolean emailValide(String email) {
        if (email == null || email.isBlank()) return false;
        return email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean telephoneValide(String tel) {
        if (tel == null || tel.isBlank()) return false;
        return tel.matches("^[+]?[0-9]{8,15}$");
    }

    public static boolean prixValide(double prix) {
        return prix > 0;
    }

    public static boolean quantiteValide(int qte) {
        return qte > 0;
    }

    public static boolean nonVide(String valeur) {
        return valeur != null && !valeur.isBlank();
    }

    public static void validerEmail(String email) {
        if (!emailValide(email))
            throw new IllegalArgumentException("Adresse email invalide : " + email);
    }

    public static void validerPrix(double prix) {
        if (!prixValide(prix))
            throw new IllegalArgumentException("Le prix doit être supérieur à 0.");
    }

    public static void validerQuantite(int qte) {
        if (!quantiteValide(qte))
            throw new IllegalArgumentException("La quantité doit être supérieure à 0.");
    }

    public static void validerChamp(String valeur, String nomChamp) {
        if (!nonVide(valeur))
            throw new IllegalArgumentException("Le champ '" + nomChamp + "' est obligatoire.");
    }
}