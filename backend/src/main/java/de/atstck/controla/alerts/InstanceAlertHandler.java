package de.atstck.controla.alerts;

import de.atstck.controla.instance.Instance;

public interface InstanceAlertHandler {
    void handleInvalidApiKey(Instance instance);
    void handleInstanceOffline(Instance instance);
    void handleInstanceOnline(Instance instance);
    void handleWorkflowError(Instance instance, String workflowName, String errorMessage);
}
