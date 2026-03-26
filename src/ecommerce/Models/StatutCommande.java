package ecommerce.Models;

public enum StatutCommande {
    EN_ATTENTE("En attente"),
    CONFIRMEE("Confirmée"),
    EN_COURS_LIVRAISON("En cours de livraison"),
    LIVREE("Livrée"),
    ANNULEE("Annulée"),
    REMBOURSEE("Remboursée");

    private final String libelle;

    StatutCommande(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}
