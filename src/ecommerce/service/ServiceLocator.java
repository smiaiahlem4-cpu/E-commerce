package ecommerce.service;

import ecommerce.dao.impl.*;

/**
 * Conteneur de services — crée et partage toutes les instances.
 * Utilisé par toutes les fenêtres GUI.
 */
public class ServiceLocator {

    private static ServiceLocator instance;

    private final UtilisateurDAO utilisateurDAO;
    private final ProduitDAO     produitDAO;
    private final CommandeDAO    commandeDAO;
    private final PaiementDAO    paiementDAO;
    private final CategorieDAO   categorieDAO;
    private final AvisDAO        avisDAO;
    private final PromotionDAO   promotionDAO;

    public final AuthService      authService;
    public final ProduitService   produitService;
    public final CommandeService  commandeService;
    public final PaiementService  paiementService;
    public final AvisService      avisService;
    public final StatsService     statsService;

    private ServiceLocator() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.produitDAO     = new ProduitDAO();
        this.commandeDAO    = new CommandeDAO();
        this.paiementDAO    = new PaiementDAO();
        this.categorieDAO   = new CategorieDAO();
        this.avisDAO        = new AvisDAO();
        this.promotionDAO   = new PromotionDAO();

        this.authService     = new AuthService(utilisateurDAO);
        this.produitService  = new ProduitService(produitDAO, categorieDAO);
        this.commandeService = new CommandeService(commandeDAO, produitDAO, promotionDAO);
        this.paiementService = new PaiementService(paiementDAO, commandeDAO);
        this.avisService     = new AvisService(avisDAO, produitDAO);
        this.statsService    = new StatsService(commandeDAO, produitDAO, utilisateurDAO);
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) instance = new ServiceLocator();
        return instance;
    }
}