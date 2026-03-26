package ecommerce.Models;

public enum ModePaiement {
    CARTE_BANCAIRE("Carte bancaire"),
    VIREMENT("Virement bancaire"),
    COUPON("Coupon / Bon de réduction"),
    PAIEMENT_A_LA_LIVRAISON("Paiement à la livraison");

    private final String libelle;

    ModePaiement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() { return libelle; }

    @Override
    public String toString() { return libelle; }
}
