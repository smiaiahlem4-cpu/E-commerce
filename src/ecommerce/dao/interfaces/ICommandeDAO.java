package ecommerce.dao.interfaces;

import ecommerce.Models.Commande;
import ecommerce.Models.StatutCommande;
import java.util.List;

public interface ICommandeDAO extends IDao<Commande> {
    List<Commande> trouverParClient(String clientId);
    List<Commande> trouverParStatut(StatutCommande statut);
}