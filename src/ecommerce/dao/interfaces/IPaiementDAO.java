package ecommerce.dao.interfaces;

import ecommerce.Models.Paiement;
import java.util.List;

public interface IPaiementDAO extends IDao<Paiement> {
    List<Paiement> trouverParCommande(String commandeId);
}