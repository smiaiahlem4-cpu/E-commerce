package ecommerce.dao.interfaces;

import ecommerce.Models.AvisClient;
import java.util.List;

public interface IAvisDAO extends IDao<AvisClient> {
    List<AvisClient> trouverParProduit(String produitId);
    boolean aDejaCommente(String clientId, String produitId);
}