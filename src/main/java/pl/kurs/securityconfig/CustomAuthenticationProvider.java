package pl.kurs.securityconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final DbUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();

        if (loginAttemptService.isBlocked(username))
            throw new LockedException("Account is temporarily locked due to failed login attempts.");

        try {
            UserDetails user = userDetailsService.loadUserByUsername(username);

            String rawPassword = authentication.getCredentials().toString();
            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                loginAttemptService.loginFailed(username);
                throw new BadCredentialsException("Bad credentials");
            }

            loginAttemptService.loginSucceeded(username);
            return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());

        } catch (UsernameNotFoundException e) {
            loginAttemptService.loginFailed(username);
            throw e;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
