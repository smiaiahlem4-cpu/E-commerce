package ecommerce.service;

import ecommerce.dao.impl.CommandeDAO;
import ecommerce.dao.impl.PaiementDAO;
import ecommerce.Models.*;
import ecommerce.util.AppLogger;

public class PaiementService {

    private final PaiementDAO  paiementDAO;
    private final CommandeDAO  commandeDAO;

    public PaiementService(PaiementDAO paiementDAO, CommandeDAO commandeDAO) {
        this.paiementDAO = paiementDAO;
        this.commandeDAO = commandeDAO;
    }

    public Paiement effectuerPaiement(String commandeId, ModePaiement mode) {
        Commande c = commandeDAO.trouverParId(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + commandeId));

        if (c.getStatut() != StatutCommande.EN_ATTENTE)
            throw new RuntimeException("Cette commande n'est pas en attente de paiement.");

        Paiement p = new Paiement(commandeId, c.getMontantAPayer(), mode);
        paiementDAO.ajouter(p);

        // Confirmer la commande après paiement
        c.setStatut(StatutCommande.CONFIRMEE);
        commandeDAO.modifier(c);

        AppLogger.info("Paiement effectué : " + p.getReference() + " — " + p.getMontant() + " DT via " + mode);
        return p;
    }

    public java.util.List<Paiement> paiementsParCommande(String commandeId) {
        return paiementDAO.trouverParCommande(commandeId);
    }
}