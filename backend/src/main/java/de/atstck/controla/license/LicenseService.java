package de.atstck.controla.license;

public interface LicenseService {

    int getMaxInstances();

    String getEditionName();

    boolean isFeatureEnabled(String featureKey);
}

