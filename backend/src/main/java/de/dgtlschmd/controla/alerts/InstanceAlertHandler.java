package de.dgtlschmd.controla.alerts;

import de.dgtlschmd.controla.instance.Instance;

public interface InstanceAlertHandler {
    void handleInvalidApiKey(Instance instance);
    void handleInstanceOffline(Instance instance);
    void handleInstanceOnline(Instance instance);
    void handleWorkflowError(Instance instance, String workflowName, String errorMessage);
}
