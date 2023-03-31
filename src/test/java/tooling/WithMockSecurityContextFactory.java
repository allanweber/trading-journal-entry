package tooling;

import com.allanweber.jwttoken.data.ContextUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WithMockSecurityContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        List<GrantedAuthority> authorities = Arrays.stream(user.authorities()).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        ContextUser principal = new ContextUser(user.username(), user.tenancyId(), user.tenancyName());
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        context.setAuthentication(authentication);
        return context;
    }
}
