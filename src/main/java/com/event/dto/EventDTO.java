package com.event.dto;

import com.event.model.entities.Event;
import com.event.model.enums.EventCategory;
import com.event.model.enums.EventStatus;

import java.time.LocalDateTime;

public class EventDTO {

    private Long id;
    private String titre;
    private String description;
    private EventCategory categorie;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String lieu;
    private String ville;
    private Integer capaciteMax;
    private Double prixUnitaire;
    private String imageUrl;
    private String organisateurNom;
    private Long organisateurId;
    private EventStatus statut;
    private Integer placesReservees;
    private Integer placesDisponibles;
    private Double tauxRemplissage;

    // Constructors
    public EventDTO() {
    }

    public EventDTO(Event event, Integer placesReservees) {
        this.id = event.getId();
        this.titre = event.getTitre();
        this.description = event.getDescription();
        this.categorie = event.getCategorie();
        this.dateDebut = event.getDateDebut();
        this.dateFin = event.getDateFin();
        this.lieu = event.getLieu();
        this.ville = event.getVille();
        this.capaciteMax = event.getCapaciteMax();
        this.prixUnitaire = event.getPrixUnitaire();
        this.imageUrl = event.getImageUrl();
        this.organisateurNom = event.getOrganisateur().getFullName();
        this.organisateurId = event.getOrganisateur().getId();
        this.statut = event.getStatut();
        this.placesReservees = placesReservees;
        this.placesDisponibles = event.getCapaciteMax() - placesReservees;
        this.tauxRemplissage = event.getCapaciteMax() > 0
                ? (double) placesReservees / event.getCapaciteMax() * 100
                : 0.0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventCategory getCategorie() {
        return categorie;
    }

    public void setCategorie(EventCategory categorie) {
        this.categorie = categorie;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Integer getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(Integer capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public Double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(Double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOrganisateurNom() {
        return organisateurNom;
    }

    public void setOrganisateurNom(String organisateurNom) {
        this.organisateurNom = organisateurNom;
    }

    public Long getOrganisateurId() {
        return organisateurId;
    }

    public void setOrganisateurId(Long organisateurId) {
        this.organisateurId = organisateurId;
    }

    public EventStatus getStatut() {
        return statut;
    }

    public void setStatut(EventStatus statut) {
        this.statut = statut;
    }

    public Integer getPlacesReservees() {
        return placesReservees;
    }

    public void setPlacesReservees(Integer placesReservees) {
        this.placesReservees = placesReservees;
    }

    public Integer getPlacesDisponibles() {
        return placesDisponibles;
    }

    public void setPlacesDisponibles(Integer placesDisponibles) {
        this.placesDisponibles = placesDisponibles;
    }

    public Double getTauxRemplissage() {
        return tauxRemplissage;
    }

    public void setTauxRemplissage(Double tauxRemplissage) {
        this.tauxRemplissage = tauxRemplissage;
    }

    public boolean isAvailable() {
        return statut == EventStatus.PUBLIE &&
                LocalDateTime.now().isBefore(dateDebut) &&
                placesDisponibles > 0;
    }
}