package ecommerce.exception;

public class StockInsuffisantException extends RuntimeException {
    private final int stockDisponible;
    private final int quantiteDemandee;

    public StockInsuffisantException(String nomProduit, int stockDisponible, int quantiteDemandee) {
        super(String.format("Stock insuffisant pour '%s' : %d disponible(s), %d demandé(s)",
                nomProduit, stockDisponible, quantiteDemandee));
        this.stockDisponible = stockDisponible;
        this.quantiteDemandee = quantiteDemandee;
    }

    public int getStockDisponible() { return stockDisponible; }
    public int getQuantiteDemandee() { return quantiteDemandee; }
}