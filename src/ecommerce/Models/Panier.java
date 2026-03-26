package ecommerce.Models;
import java.util.*;

public class Panier {
    private String clientId;
    private Map<String, LigneCommande> lignes; // produitId -> ligne

    public Panier(String clientId) {
        this.clientId = clientId;
        this.lignes = new LinkedHashMap<>();
    }

    public void ajouterProduit(Produit produit, int quantite) {
        if (lignes.containsKey(produit.getId())) {
            LigneCommande ligne = lignes.get(produit.getId());
            ligne.setQuantite(ligne.getQuantite() + quantite);
        } else {
            lignes.put(produit.getId(), new LigneCommande(
                    produit.getId(), produit.getNom(), produit.getPrix(), quantite
            ));
        }
    }

    public void retirerProduit(String produitId) {
        lignes.remove(produitId);
    }

    public void modifierQuantite(String produitId, int nouvelleQte) {
        if (lignes.containsKey(produitId)) {
            if (nouvelleQte <= 0) {
                lignes.remove(produitId);
            } else {
                lignes.get(produitId).setQuantite(nouvelleQte);
            }
        }
    }

    public void vider() {
        lignes.clear();
    }

    public boolean estVide() {
        return lignes.isEmpty();
    }

    public double getTotal() {
        return lignes.values().stream().mapToDouble(LigneCommande::getSousTotal).sum();
    }

    public int getNombreArticles() {
        return lignes.values().stream().mapToInt(LigneCommande::getQuantite).sum();
    }

    public List<LigneCommande> getLignes() {
        return new ArrayList<>(lignes.values());
    }

    public String getClientId() { return clientId; }
}