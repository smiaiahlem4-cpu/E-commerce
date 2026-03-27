package ecommerce.service;

import ecommerce.dao.impl.CommandeDAO;
import ecommerce.dao.impl.ProduitDAO;
import ecommerce.dao.impl.PromotionDAO;
import ecommerce.exception.StockInsuffisantException;
import ecommerce.Models.*;
import ecommerce.util.AppLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandeService {

    private final CommandeDAO  commandeDAO;
    private final ProduitDAO   produitDAO;
    private final PromotionDAO promotionDAO;

    // Un panier par utilisateur connecté (en mémoire)
    private final Map<String, Panier> paniers = new HashMap<>();

    public CommandeService(CommandeDAO commandeDAO, ProduitDAO produitDAO, PromotionDAO promotionDAO) {
        this.commandeDAO  = commandeDAO;
        this.produitDAO   = produitDAO;
        this.promotionDAO = promotionDAO;
    }

    // ── Panier ────────────────────────────────────────────────────────────────

    public Panier getPanier(String clientId) {
        return paniers.computeIfAbsent(clientId, Panier::new);
    }

    public void ajouterAuPanier(String clientId, String produitId, int quantite) {
        Produit p = produitDAO.trouverParId(produitId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable."));

        if (!p.estEnStock()) throw new RuntimeException("Ce produit n'est plus disponible.");

        int dejaDansPanier = getPanier(clientId).getLignes().stream()
                .filter(l -> l.getProduitId().equals(produitId))
                .mapToInt(LigneCommande::getQuantite).sum();

        if (p.getQuantiteStock() < dejaDansPanier + quantite) {
            throw new StockInsuffisantException(p.getNom(), p.getQuantiteStock(), dejaDansPanier + quantite);
        }

        getPanier(clientId).ajouterProduit(p, quantite);
        AppLogger.info("Panier[" + clientId + "] + " + quantite + "x " + p.getNom());
    }

    public void retirerDuPanier(String clientId, String produitId) {
        getPanier(clientId).retirerProduit(produitId);
    }

    public void modifierQuantitePanier(String clientId, String produitId, int nouvelleQte) {
        if (nouvelleQte > 0) {
            Produit p = produitDAO.trouverParId(produitId)
                    .orElseThrow(() -> new RuntimeException("Produit introuvable."));
            if (p.getQuantiteStock() < nouvelleQte)
                throw new StockInsuffisantException(p.getNom(), p.getQuantiteStock(), nouvelleQte);
        }
        getPanier(clientId).modifierQuantite(produitId, nouvelleQte);
    }

    public void viderPanier(String clientId) {
        getPanier(clientId).vider();
    }

    // ── Checkout ──────────────────────────────────────────────────────────────

    public Commande passerCommande(String clientId, Adresse adresseLivraison, String codePromo) {
        Panier panier = getPanier(clientId);
        if (panier.estVide()) throw new RuntimeException("Le panier est vide.");

        // Vérifier le stock pour chaque ligne
        for (LigneCommande ligne : panier.getLignes()) {
            Produit p = produitDAO.trouverParId(ligne.getProduitId())
                    .orElseThrow(() -> new RuntimeException("Produit '" + ligne.getNomProduit() + "' introuvable."));
            if (p.getQuantiteStock() < ligne.getQuantite()) {
                throw new StockInsuffisantException(p.getNom(), p.getQuantiteStock(), ligne.getQuantite());
            }
        }

        // Créer la commande
        Commande commande = new Commande(clientId, panier.getLignes(), adresseLivraison);

        // Appliquer une promotion si fournie
        if (codePromo != null && !codePromo.isBlank()) {
            promotionDAO.trouverParCode(codePromo).ifPresent(promo -> {
                if (promo.estValide(commande.getMontantTotal())) {
                    double remise = promo.calculerRemise(commande.getMontantTotal());
                    commande.setMontantRemise(remise);
                    commande.setCodePromo(codePromo.toUpperCase());
                    promo.incrementerUtilisations();
                    promotionDAO.modifier(promo);
                }
            });
        }

        // Décrémenter le stock
        for (LigneCommande ligne : panier.getLignes()) {
            produitDAO.trouverParId(ligne.getProduitId()).ifPresent(p -> {
                p.diminuerStock(ligne.getQuantite());
                produitDAO.modifier(p);
            });
        }

        commandeDAO.ajouter(commande);
        panier.vider();
        AppLogger.info("Commande passée : " + commande.getNumeroCommande());
        return commande;
    }

    // ── Suivi & Gestion ───────────────────────────────────────────────────────

    public List<Commande> historiqueClient(String clientId) {
        return commandeDAO.trouverParClient(clientId);
    }

    public List<Commande> toutesLesCommandes() {
        return commandeDAO.trouverTous();
    }

    public List<Commande> parStatut(StatutCommande statut) {
        return commandeDAO.trouverParStatut(statut);
    }

    public void changerStatut(String commandeId, StatutCommande nouveauStatut) {
        commandeDAO.trouverParId(commandeId).ifPresent(c -> {
            c.setStatut(nouveauStatut);
            if (nouveauStatut == StatutCommande.LIVREE) {
                c.setDateLivraison(java.time.LocalDateTime.now());
            }
            commandeDAO.modifier(c);
            AppLogger.info("Statut commande " + c.getNumeroCommande() + " → " + nouveauStatut);
        });
    }

    public void annulerCommande(String commandeId, String clientId) {
        Commande c = commandeDAO.trouverParId(commandeId)
                .orElseThrow(() -> new RuntimeException("Commande introuvable."));

        if (!c.getClientId().equals(clientId))
            throw new RuntimeException("Cette commande ne vous appartient pas.");

        if (c.getStatut() != StatutCommande.EN_ATTENTE)
            throw new RuntimeException("Impossible d'annuler une commande " + c.getStatut().getLibelle() + ".");

        // Remettre le stock
        for (LigneCommande ligne : c.getLignes()) {
            produitDAO.trouverParId(ligne.getProduitId()).ifPresent(p -> {
                p.augmenterStock(ligne.getQuantite());
                produitDAO.modifier(p);
            });
        }

        c.setStatut(StatutCommande.ANNULEE);
        commandeDAO.modifier(c);
        AppLogger.info("Commande annulée : " + c.getNumeroCommande());
    }

    public Commande trouverParId(String id) {
        return commandeDAO.trouverParId(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable : " + id));
    }
}