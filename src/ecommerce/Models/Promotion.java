package ecommerce.Models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Promotion {
    private String id;
    private String code;
    private double pourcentageRemise;
    private double montantMinimum;
    private int utilisationsMax;
    private int utilisationsActuelles;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private boolean active;

    public Promotion() {
        this.id = UUID.randomUUID().toString();
        this.active = true;
        this.utilisationsActuelles = 0;
    }

    public Promotion(String code, double pourcentageRemise, double montantMinimum,
                     int utilisationsMax, LocalDateTime dateFin) {
        this();
        this.code = code.toUpperCase();
        this.pourcentageRemise = pourcentageRemise;
        this.montantMinimum = montantMinimum;
        this.utilisationsMax = utilisationsMax;
        this.dateDebut = LocalDateTime.now();
        this.dateFin = dateFin;
    }

    public boolean estValide(double montantCommande) {
        LocalDateTime now = LocalDateTime.now();
        return active
                && now.isAfter(dateDebut)
                && now.isBefore(dateFin)
                && montantCommande >= montantMinimum
                && (utilisationsMax == 0 || utilisationsActuelles < utilisationsMax);
    }

    public double calculerRemise(double montant) {
        return montant * pourcentageRemise / 100.0;
    }

    public void incrementerUtilisations() {
        this.utilisationsActuelles++;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public double getPourcentageRemise() { return pourcentageRemise; }
    public void setPourcentageRemise(double pourcentageRemise) { this.pourcentageRemise = pourcentageRemise; }
    public double getMontantMinimum() { return montantMinimum; }
    public void setMontantMinimum(double montantMinimum) { this.montantMinimum = montantMinimum; }
    public int getUtilisationsMax() { return utilisationsMax; }
    public void setUtilisationsMax(int utilisationsMax) { this.utilisationsMax = utilisationsMax; }
    public int getUtilisationsActuelles() { return utilisationsActuelles; }
    public void setUtilisationsActuelles(int utilisationsActuelles) { this.utilisationsActuelles = utilisationsActuelles; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}