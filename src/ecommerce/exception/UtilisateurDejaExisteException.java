package ecommerce.exception;

public class UtilisateurDejaExisteException extends RuntimeException {
    public UtilisateurDejaExisteException(String email) {
        super("Un compte existe déjà avec l'adresse : " + email);
    }
}