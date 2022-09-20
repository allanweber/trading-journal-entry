package com.trading.journal.entry.entries;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EntryByTypeValidator.class)
public @interface EntryByType {

    String message() default "This entry is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
