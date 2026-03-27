package ecommerce.service;

import ecommerce.dao.impl.CategorieDAO;
import ecommerce.dao.impl.ProduitDAO;
import ecommerce.exception.ProduitNotFoundException;
import ecommerce.Models.Categorie;
import ecommerce.Models.Produit;
import ecommerce.util.AppLogger;
import ecommerce.util.Validator;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProduitService {

    private final ProduitDAO    produitDAO;
    private final CategorieDAO  categorieDAO;

    public ProduitService(ProduitDAO produitDAO, CategorieDAO categorieDAO) {
        this.produitDAO   = produitDAO;
        this.categorieDAO = categorieDAO;
    }

    // ── CRUD Produits ─────────────────────────────────────────────────────────

    public Produit ajouterProduit(String nom, String description, double prix,
                                  int stock, String categorieId, String vendeurId) {
        Validator.validerChamp(nom, "Nom du produit");
        Validator.validerPrix(prix);
        Validator.validerQuantite(stock);

        Produit p = new Produit(nom, description, prix, stock, categorieId);
        p.setVendeurId(vendeurId);
        produitDAO.ajouter(p);
        AppLogger.info("Produit ajouté : " + nom);
        return p;
    }

    public void modifierProduit(String id, String nom, String description,
                                double prix, int stock, boolean disponible) {
        Produit p = trouverOuEchouer(id);
        Validator.validerChamp(nom, "Nom");
        Validator.validerPrix(prix);

        p.setNom(nom);
        p.setDescription(description);
        p.setPrix(prix);
        p.setQuantiteStock(stock);
        p.setDisponible(disponible);
        produitDAO.modifier(p);
        AppLogger.info("Produit modifié : " + id);
    }

    public void supprimerProduit(String id) {
        trouverOuEchouer(id);
        produitDAO.supprimer(id);
        AppLogger.info("Produit supprimé : " + id);
    }

    public void ajusterStock(String produitId, int delta) {
        Produit p = trouverOuEchouer(produitId);
        p.setQuantiteStock(Math.max(0, p.getQuantiteStock() + delta));
        produitDAO.modifier(p);
    }

    // ── Recherche & Filtres ───────────────────────────────────────────────────

    public List<Produit> tousLesProduits() {
        return produitDAO.trouverTous();
    }

    public List<Produit> produitsEnStock() {
        return produitDAO.trouverEnStock();
    }

    public List<Produit> rechercherParNom(String motCle) {
        if (motCle == null || motCle.isBlank()) return produitsEnStock();
        return produitDAO.rechercherParNom(motCle);
    }

    public List<Produit> parCategorie(String categorieId) {
        return produitDAO.trouverParCategorie(categorieId)
                .stream().filter(Produit::estEnStock).collect(Collectors.toList());
    }

    public List<Produit> parVendeur(String vendeurId) {
        return produitDAO.trouverParVendeur(vendeurId);
    }

    public List<Produit> filtrerEtTrier(List<Produit> source,
                                        Double prixMin, Double prixMax,
                                        Double noteMin, String tri) {
        var stream = source.stream();
        if (prixMin != null) stream = stream.filter(p -> p.getPrix() >= prixMin);
        if (prixMax != null) stream = stream.filter(p -> p.getPrix() <= prixMax);
        if (noteMin != null) stream = stream.filter(p -> p.getNoteMoyenne() >= noteMin);

        Comparator<Produit> comp = switch (tri == null ? "nom" : tri.toLowerCase()) {
            case "prix_asc"  -> Comparator.comparingDouble(Produit::getPrix);
            case "prix_desc" -> Comparator.comparingDouble(Produit::getPrix).reversed();
            case "note"      -> Comparator.comparingDouble(Produit::getNoteMoyenne).reversed();
            case "stock"     -> Comparator.comparingInt(Produit::getQuantiteStock).reversed();
            default          -> Comparator.comparing(Produit::getNom);
        };

        return stream.sorted(comp).collect(Collectors.toList());
    }

    public Produit trouverParId(String id) {
        return trouverOuEchouer(id);
    }

    // ── Catégories ────────────────────────────────────────────────────────────

    public List<Categorie> toutesLesCategories() {
        return categorieDAO.trouverTous();
    }

    public Categorie ajouterCategorie(String nom, String description) {
        Validator.validerChamp(nom, "Nom catégorie");
        Categorie c = new Categorie(nom, description);
        categorieDAO.ajouter(c);
        return c;
    }

    public void supprimerCategorie(String id) {
        categorieDAO.supprimer(id);
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private Produit trouverOuEchouer(String id) {
        return produitDAO.trouverParId(id)
                .orElseThrow(() -> new ProduitNotFoundException(id));
    }
}