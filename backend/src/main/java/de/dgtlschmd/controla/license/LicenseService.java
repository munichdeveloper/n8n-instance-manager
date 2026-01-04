package de.dgtlschmd.controla.license;

public interface LicenseService {

    int getMaxInstances();

    String getEditionName();

    boolean isFeatureEnabled(String featureKey);
}

