package com.xtensus.hrmanagementapi.conge.solde.exception;
public class CongeSoldeExisteDejaException extends RuntimeException { public CongeSoldeExisteDejaException() { super("Un solde existe deja pour cet employe, ce type de conge et cette annee"); } }
