package org.example.nordicnestshop.security.impl;

import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.exception.ElementNotFoundException;
import org.example.nordicnestshop.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(username)
                .orElseThrow(() -> new ElementNotFoundException("Can't find user by email"));
    }
}
