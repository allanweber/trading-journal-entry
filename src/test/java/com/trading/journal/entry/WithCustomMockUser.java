package com.trading.journal.entry;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockSecurityContextFactory.class)
public @interface WithCustomMockUser {

    String username() default "user";

    String[] authorities() default {"ROLE_USER"};

    long tenancyId() default 1L;

    String tenancyName() default "tenancy";
}
