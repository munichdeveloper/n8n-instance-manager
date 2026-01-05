package de.atstck.controla.passwordreset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findByUserId(Long userId);
    void deleteByExpiryDateBefore(LocalDateTime now);
    void deleteByUserId(Long userId);
}

