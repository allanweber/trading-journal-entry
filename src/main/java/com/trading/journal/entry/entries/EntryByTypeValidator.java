package com.trading.journal.entry.entries;

import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@NoArgsConstructor
public class EntryByTypeValidator implements ConstraintValidator<EntryByType, Entry> {

    @Override
    public boolean isValid(Entry entry, ConstraintValidatorContext context) {
        boolean isValid = true;
        if (EntryType.TRADE.equals(entry.getType())) {
            isValid = validateTrade(entry, context);
        }
        return isValid;
    }

    private boolean validateTrade(Entry entry, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        boolean isValid = validateExitTrading(entry, context);

        if (!StringUtils.hasText(entry.getSymbol())) {
            isValid = false;
            context.buildConstraintViolationWithTemplate("Symbol is required")
                    .addPropertyNode("symbol")
                    .addConstraintViolation();
        }

        if (Objects.isNull(entry.getDirection())) {
            isValid = false;
            context.buildConstraintViolationWithTemplate("Direction is required")
                    .addPropertyNode("direction")
                    .addConstraintViolation();
        }

        if (Objects.isNull(entry.getSize())) {
            isValid = false;
            context.buildConstraintViolationWithTemplate("Position size is required")
                    .addPropertyNode("size")
                    .addConstraintViolation();
        }

        if (Objects.nonNull(entry.getExitDate()) && Objects.nonNull(entry.getDate()) && entry.getExitDate().isBefore(entry.getDate())) {
            isValid = false;
            context.buildConstraintViolationWithTemplate("Exit date must be after entry date")
                    .addPropertyNode("exitDate")
                    .addConstraintViolation();
        }

        return isValid;
    }

    private static boolean validateExitTrading(Entry entry, ConstraintValidatorContext context) {
        boolean isValid = true;
        if ((Objects.nonNull(entry.getExitPrice()) || Objects.nonNull(entry.getExitDate()))
                && (Objects.isNull(entry.getExitPrice()) || Objects.isNull(entry.getExitDate()))) {
            isValid = false;
            String exitPrice = "exitPrice";
            String exitDate = "exitDate";
            String exitPriceLabel = "Exit Price";
            String exitDateLabel = "Exit Date";

            String nullFieldName = Objects.isNull(entry.getExitPrice()) ? exitPrice : exitDate;
            String message = (nullFieldName.equals(exitPrice) ? exitPriceLabel : exitDateLabel) + " is required";
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(nullFieldName)
                    .addConstraintViolation();
        }
        return isValid;
    }
}
