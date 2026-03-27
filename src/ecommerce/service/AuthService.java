package ecommerce.service;

import ecommerce.dao.impl.UtilisateurDAO;
import ecommerce.exception.AuthException;
import ecommerce.exception.UtilisateurDejaExisteException;
import ecommerce.Models.Role;
import ecommerce.Models.Utilisateur;
import ecommerce.util.AppLogger;
import ecommerce.util.PasswordUtil;
import ecommerce.util.Validator;

import java.util.Optional;

public class AuthService {

    private final UtilisateurDAO utilisateurDAO;
    private Utilisateur utilisateurConnecte;

    public AuthService(UtilisateurDAO utilisateurDAO) {
        this.utilisateurDAO = utilisateurDAO;
    }

    /**
     * Connexion d'un utilisateur.
     * @throws AuthException si les identifiants sont incorrects ou le compte inactif.
     */
    public Utilisateur connecter(String email, String motDePasse) {
        Validator.validerEmail(email);

        Optional<Utilisateur> opt = utilisateurDAO.trouverParEmail(email);
        if (opt.isEmpty()) {
            AppLogger.warn("Tentative de connexion avec email inconnu : " + email);
            throw new AuthException("Email ou mot de passe incorrect.");
        }

        Utilisateur u = opt.get();

        if (!u.isActif()) {
            throw new AuthException("Ce compte a été désactivé. Contactez l'administrateur.");
        }

        if (!PasswordUtil.verifier(motDePasse, u.getMotDePasseHash())) {
            AppLogger.warn("Mot de passe incorrect pour : " + email);
            throw new AuthException("Email ou mot de passe incorrect.");
        }

        this.utilisateurConnecte = u;
        AppLogger.info("Connexion réussie : " + u.getNomComplet() + " [" + u.getRole() + "]");
        return u;
    }

    /**
     * Inscription d'un nouveau client.
     */
    public Utilisateur inscrire(String nom, String prenom, String email,
                                String motDePasse, String telephone) {
        Validator.validerChamp(nom,    "Nom");
        Validator.validerChamp(prenom, "Prénom");
        Validator.validerEmail(email);
        Validator.validerChamp(motDePasse, "Mot de passe");

        if (!PasswordUtil.estAssezSolide(motDePasse)) {
            throw new AuthException(
                    "Mot de passe trop faible. Il doit contenir au moins 8 caractères, une majuscule et un chiffre.");
        }

        if (utilisateurDAO.emailExiste(email)) {
            throw new UtilisateurDejaExisteException(email);
        }

        Utilisateur u = new Utilisateur(nom, prenom, email, PasswordUtil.hacher(motDePasse));
        u.setRole(Role.CLIENT);
        u.setTelephone(telephone);
        utilisateurDAO.ajouter(u);

        AppLogger.info("Nouveau compte créé : " + email);
        return u;
    }

    /**
     * Crée un compte avec un rôle spécifique (réservé à l'admin).
     */
    public Utilisateur creerCompte(String nom, String prenom, String email,
                                   String motDePasse, Role role) {
        Validator.validerChamp(nom,    "Nom");
        Validator.validerChamp(prenom, "Prénom");
        Validator.validerEmail(email);

        if (utilisateurDAO.emailExiste(email)) {
            throw new UtilisateurDejaExisteException(email);
        }

        String hash = PasswordUtil.hacher(
                motDePasse.isBlank() ? "Azerty1234" : motDePasse
        );
        Utilisateur u = new Utilisateur(nom, prenom, email, hash);
        u.setRole(role);
        utilisateurDAO.ajouter(u);
        AppLogger.info("Compte créé par admin : " + email + " [" + role + "]");
        return u;
    }

    public void deconnecter() {
        if (utilisateurConnecte != null) {
            AppLogger.info("Déconnexion : " + utilisateurConnecte.getEmail());
            this.utilisateurConnecte = null;
        }
    }

    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public boolean estAdmin() {
        return estConnecte() && utilisateurConnecte.getRole() == Role.ADMIN;
    }

    public boolean estVendeur() {
        return estConnecte() && utilisateurConnecte.getRole() == Role.VENDEUR;
    }

    public boolean estClient() {
        return estConnecte() && utilisateurConnecte.getRole() == Role.CLIENT;
    }

    public void changerMotDePasse(String ancien, String nouveau) {
        if (!estConnecte()) throw new AuthException("Non connecté.");
        if (!PasswordUtil.verifier(ancien, utilisateurConnecte.getMotDePasseHash()))
            throw new AuthException("Ancien mot de passe incorrect.");
        if (!PasswordUtil.estAssezSolide(nouveau))
            throw new AuthException("Nouveau mot de passe trop faible.");
        utilisateurConnecte.setMotDePasseHash(PasswordUtil.hacher(nouveau));
        utilisateurDAO.modifier(utilisateurConnecte);
        AppLogger.info("Mot de passe changé pour : " + utilisateurConnecte.getEmail());
    }
}