package ecommerce.dao.interfaces;

import ecommerce.Models.Utilisateur;
import java.util.Optional;

public interface IUtilisateurDAO extends IDao<Utilisateur> {
    Optional<Utilisateur> trouverParEmail(String email);
    boolean emailExiste(String email);
}