package com.xtensus.hrmanagementapi.conge.solde.exception;
public class CongeSoldeIntrouvableException extends RuntimeException { public CongeSoldeIntrouvableException(Long id) { super("Solde de conge introuvable avec l'identifiant " + id); } }
