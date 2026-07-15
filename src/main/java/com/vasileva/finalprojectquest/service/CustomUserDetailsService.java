package com.vasileva.finalprojectquest.service;

import com.vasileva.finalprojectquest.repository.UserRepository;
import com.vasileva.finalprojectquest.util.MessageHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final MessageHelper messageHelper;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        log.debug("Spring Security requests loading UserDetails for user: [{}]", username);
        return userRepository.findByLogin(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user [{}] not found", username);
                    return new UsernameNotFoundException(
                            messageHelper.getMessage("error.username.not_found", username));
                });
    }
}
