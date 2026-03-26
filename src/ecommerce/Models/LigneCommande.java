package ecommerce.Models;

public class LigneCommande {
    private String produitId;
    private String nomProduit;
    private double prixUnitaire;
    private int quantite;

    public LigneCommande() {}

    public LigneCommande(String produitId, String nomProduit, double prixUnitaire, int quantite) {
        this.produitId = produitId;
        this.nomProduit = nomProduit;
        this.prixUnitaire = prixUnitaire;
        this.quantite = quantite;
    }

    public double getSousTotal() {
        return prixUnitaire * quantite;
    }

    // Getters & Setters
    public String getProduitId() { return produitId; }
    public void setProduitId(String produitId) { this.produitId = produitId; }
    public String getNomProduit() { return nomProduit; }
    public void setNomProduit(String nomProduit) { this.nomProduit = nomProduit; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    @Override
    public String toString() {
        return String.format("  %-30s x%d  @ %.2f DT  = %.2f DT", nomProduit, quantite, prixUnitaire, getSousTotal());
    }
}
