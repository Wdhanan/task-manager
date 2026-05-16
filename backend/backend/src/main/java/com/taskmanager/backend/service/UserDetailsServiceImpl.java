// src/main/java/com/taskmanager/backend/security/UserDetailsServiceImpl.java

package com.taskmanager.backend.service;

import com.taskmanager.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Spring Security ruft das auf wenn es einen User laden will
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User nicht gefunden: " + username)
                );
        // User implementiert UserDetails → kann direkt zurückgegeben werden
    }
}