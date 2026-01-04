package de.dgtlschmd.controla.passwordreset;

/**
 * Interface für Password-Reset-Services
 * Ermöglicht verschiedene Implementierungen (z.B. Standard und Premium)
 */
public interface PasswordResetService {

    /**
     * Erstellt einen Password-Reset-Token und sendet eine E-Mail
     * Wichtig: Gibt immer eine erfolgreiche Antwort zurück (Timing-Attack-Schutz)
     *
     * @param email Die E-Mail-Adresse des Benutzers
     */
    void requestPasswordReset(String email);

    /**
     * Setzt das Passwort mit einem Token zurück
     *
     * @param token       Das Reset-Token
     * @param newPassword Das neue Passwort
     * @throws IllegalArgumentException wenn der Token ungültig ist
     */
    void resetPassword(String token, String newPassword);

    /**
     * Validiert einen Token (für Frontend-Check)
     *
     * @param token Das zu validierende Token
     * @return true wenn der Token gültig ist, false sonst
     */
    boolean isTokenValid(String token);
}

