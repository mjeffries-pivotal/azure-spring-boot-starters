/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.autoconfigure.aad;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AzureADJwtTokenFilterTest {

    @Before
    public void beforeEveryMethod() {
        Assume.assumeTrue(!Constants.CLIENT_ID.contains("real_client_id"));
        Assume.assumeTrue(!Constants.CLIENT_SECRET.contains("real_client_secret"));
        Assume.assumeTrue(!Constants.BEARER_TOKEN.contains("real_jtw_bearer_token"));
    }

    @Test
    public void doFilterInternal() throws Exception {
        System.setProperty(Constants.CLIENT_ID_PROPERTY, Constants.CLIENT_ID);
        System.setProperty(Constants.CLIENT_SECRET_PROPERTY, Constants.CLIENT_SECRET);
        System.setProperty(Constants.TARGETED_GROUPS_PROPERTY,
                Constants.TARGETED_GROUPS.toString().replace("[", "").replace("]", ""));

        final HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(Constants.TOKEN_HEADER)).thenReturn(Constants.BEARER_TOKEN);

        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain filterChain = mock(FilterChain.class);

        Authentication authentication = mock(Authentication.class);
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(AzureADJwtFilterAutoConfiguration.class);
            context.refresh();

            final AzureADJwtTokenFilter azureADJwtTokenFilter = context.getBean(AzureADJwtTokenFilter.class);
            assertThat(azureADJwtTokenFilter).isNotNull();
            assertThat(azureADJwtTokenFilter).isExactlyInstanceOf(AzureADJwtTokenFilter.class);

            azureADJwtTokenFilter.doFilterInternal(request, response, filterChain);

            authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication.getPrincipal()).isNotNull();
            assertThat(authentication.getPrincipal()).isExactlyInstanceOf(UserPrincipal.class);
            assertThat(authentication.getAuthorities()).isNotNull();
            assertThat(authentication.getAuthorities().size()).isEqualTo(2);
            assertThat(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_group1"))
                    && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_group2"))
            );

            final UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            assertThat(principal.getIssuer()).isNotNull().isNotEmpty();
            assertThat(principal.getKid()).isNotNull().isNotEmpty();
            assertThat(principal.getSubject()).isNotNull().isNotEmpty();

            assertThat(principal.getClaims()).isNotNull().isNotEmpty();
            final Map<String, Object> claims = principal.getClaims();
            assertThat(claims.get("iss")).isEqualTo(principal.getIssuer());
            assertThat(claims.get("sub")).isEqualTo(principal.getSubject());
        }

        System.clearProperty(Constants.CLIENT_ID_PROPERTY);
        System.clearProperty(Constants.CLIENT_SECRET_PROPERTY);
        System.clearProperty(Constants.TARGETED_GROUPS_PROPERTY);
    }

}
