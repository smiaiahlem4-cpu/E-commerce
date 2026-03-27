package ecommerce.dao.interfaces;

import ecommerce.Models.Categorie;
import java.util.Optional;

public interface ICategorieDAO extends IDao<Categorie> {
    Optional<Categorie> trouverParNom(String nom);
}