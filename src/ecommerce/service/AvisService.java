package ecommerce.service;

import ecommerce.dao.impl.AvisDAO;
import ecommerce.dao.impl.ProduitDAO;
import ecommerce.Models.AvisClient;

import java.util.List;

public class AvisService {

    private final AvisDAO   avisDAO;
    private final ProduitDAO produitDAO;

    public AvisService(AvisDAO avisDAO, ProduitDAO produitDAO) {
        this.avisDAO    = avisDAO;
        this.produitDAO = produitDAO;
    }

    public AvisClient laisserUnAvis(String produitId, String clientId, String nomClient,
                                    int note, String commentaire) {
        if (avisDAO.aDejaCommente(clientId, produitId))
            throw new RuntimeException("Vous avez déjà laissé un avis pour ce produit.");

        if (note < 1 || note > 5)
            throw new IllegalArgumentException("La note doit être entre 1 et 5.");

        AvisClient avis = new AvisClient(produitId, clientId, nomClient, note, commentaire);
        avisDAO.ajouter(avis);

        // Mettre à jour la note moyenne du produit
        produitDAO.trouverParId(produitId).ifPresent(p -> {
            p.mettreAJourNote(note);
            produitDAO.modifier(p);
        });

        return avis;
    }

    public List<AvisClient> avisParProduit(String produitId) {
        return avisDAO.trouverParProduit(produitId);
    }

    public void supprimerAvis(String avisId) {
        avisDAO.supprimer(avisId);
    }
}