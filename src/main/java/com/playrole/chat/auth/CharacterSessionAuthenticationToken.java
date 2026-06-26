package com.playrole.chat.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public class CharacterSessionAuthenticationToken extends AbstractAuthenticationToken {

    private final CharacterSessionPrincipal principal;

    public CharacterSessionAuthenticationToken(
            CharacterSessionPrincipal principal,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public CharacterSessionPrincipal getPrincipal() {
        return principal;
    }
}
