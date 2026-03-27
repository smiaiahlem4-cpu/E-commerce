package ecommerce.service;

import ecommerce.dao.impl.CommandeDAO;
import ecommerce.dao.impl.ProduitDAO;
import ecommerce.dao.impl.UtilisateurDAO;
import ecommerce.Models.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StatsService {

    private final CommandeDAO    commandeDAO;
    private final ProduitDAO     produitDAO;
    private final UtilisateurDAO utilisateurDAO;

    public StatsService(CommandeDAO commandeDAO, ProduitDAO produitDAO, UtilisateurDAO utilisateurDAO) {
        this.commandeDAO    = commandeDAO;
        this.produitDAO     = produitDAO;
        this.utilisateurDAO = utilisateurDAO;
    }

    public double chiffreAffairesTotal() {
        return commandeDAO.trouverTous().stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE && c.getStatut() != StatutCommande.REMBOURSEE)
                .mapToDouble(Commande::getMontantAPayer).sum();
    }

    public double chiffreAffairesAujourdhui() {
        LocalDate today = LocalDate.now();
        return commandeDAO.trouverTous().stream()
                .filter(c -> c.getDateCommande().toLocalDate().equals(today))
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .mapToDouble(Commande::getMontantAPayer).sum();
    }

    public long nombreCommandesAujourdhui() {
        LocalDate today = LocalDate.now();
        return commandeDAO.trouverTous().stream()
                .filter(c -> c.getDateCommande().toLocalDate().equals(today))
                .count();
    }

    public long nombreClientsTotal() {
        return utilisateurDAO.trouverTous().stream()
                .filter(u -> u.getRole() == Role.CLIENT).count();
    }

    public List<Produit> produitsStockFaible(int seuil) {
        return produitDAO.trouverTous().stream()
                .filter(p -> p.getQuantiteStock() <= seuil && p.isDisponible())
                .sorted(Comparator.comparingInt(Produit::getQuantiteStock))
                .collect(Collectors.toList());
    }

    public List<Map.Entry<String, Long>> topProduitsVendus(int top) {
        Map<String, Long> compteur = new HashMap<>();
        commandeDAO.trouverTous().stream()
                .filter(c -> c.getStatut() != StatutCommande.ANNULEE)
                .flatMap(c -> c.getLignes().stream())
                .forEach(l -> compteur.merge(l.getNomProduit(), (long) l.getQuantite(), Long::sum));
        return compteur.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(top)
                .collect(Collectors.toList());
    }

    public Map<StatutCommande, Long> repartitionStatuts() {
        Map<StatutCommande, Long> map = new LinkedHashMap<>();
        for (StatutCommande s : StatutCommande.values()) map.put(s, 0L);
        commandeDAO.trouverTous()
                .forEach(c -> map.merge(c.getStatut(), 1L, Long::sum));
        return map;
    }
}