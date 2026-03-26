package ecommerce.exception;

public class ProduitNotFoundException extends RuntimeException {
    public ProduitNotFoundException(String id) {
        super("Produit introuvable avec l'id : " + id);
    }
}
