package ecommerce.dao.interfaces;

import ecommerce.Models.Produit;
import java.util.List;

public interface IProduitDAO extends IDao<Produit> {
    List<Produit> trouverParCategorie(String categorieId);
    List<Produit> rechercherParNom(String motCle);
    List<Produit> trouverParVendeur(String vendeurId);
    List<Produit> trouverEnStock();
}