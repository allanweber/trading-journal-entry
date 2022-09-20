package com.trading.journal.entry.entries;

import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.Objects;

@NoArgsConstructor
public class EntryByTypeValidator implements ConstraintValidator<EntryByType, Entry> {

    @Override
    public boolean isValid(Entry entry, ConstraintValidatorContext context) {
        boolean isValid = true;
        if(EntryType.TRADE.equals(entry.getType())) {
            isValid = validateTrade(entry, context);
        }
        return isValid;
    }

    private boolean validateTrade(Entry entry, ConstraintValidatorContext context) {
        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        if(!StringUtils.hasText(entry.getSymbol())) {
            isValid = false;
            context.buildConstraintViolationWithTemplate("Symbol is required")
                    .addPropertyNode("symbol")
                    .addConstraintViolation();
        }

        if(Objects.isNull(entry.getDirection())) {
            isValid = false;
            context.buildConstraintViolationWithTemplate("Direction is required")
                    .addPropertyNode("direction")
                    .addConstraintViolation();
        }

        if(Objects.isNull(entry.getSize())) {
            isValid = false;
            context.buildConstraintViolationWithTemplate("Position size is required")
                    .addPropertyNode("size")
                    .addConstraintViolation();
        } else if(entry.getSize().compareTo(BigDecimal.ZERO) <= 0) {
            isValid = false;
            context.buildConstraintViolationWithTemplate("Position size must be positive")
                    .addPropertyNode("size")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
