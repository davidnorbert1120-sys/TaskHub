package com.taskhub.security;

import com.taskhub.domain.User;
import com.taskhub.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final String frontendRedirectUrl;

    public OAuth2SuccessHandler(UserRepository userRepository,
                                JwtService jwtService,
                                @org.springframework.beans.factory.annotation.Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.frontendRedirectUrl = frontendUrl + "/oauth2/callback";
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        log.info("OAuth2 login success for email: {}", email);

        User user = findOrCreateUser(email, name);
        String token = jwtService.generateToken(user.getUsername());

        String redirectUrl = frontendRedirectUrl + "?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private User findOrCreateUser(String email, String name) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            log.info("OAuth2: existing user found: {}", existing.get().getUsername());
            return existing.get();
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(generateUniqueUsername(name, email));
            User saved = userRepository.save(newUser);
            log.info("OAuth2: new user registered: {}", saved.getUsername());
            return saved;
        }
    }

    private String generateUniqueUsername(String name, String email) {
        String base;
        if (name != null && !name.isBlank()) {
            base = name.toLowerCase().replaceAll("\\s+", "");
        } else {
            base = email.split("@")[0].toLowerCase();
        }
        base = base.replaceAll("[^a-z0-9]", "");

        String candidate = base;
        int counter = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + counter;
            counter++;
        }
        return candidate;
    }
}