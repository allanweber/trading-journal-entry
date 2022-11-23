package com.trading.journal.entry.entries.trade;

import lombok.NoArgsConstructor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@NoArgsConstructor
public class ValidTradeValidator implements ConstraintValidator<ValidTrade, Trade> {

    @Override
    public boolean isValid(Trade trade, ConstraintValidatorContext context) {
        return validateTrade(trade, context);
    }

    private boolean validateTrade(Trade trade, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        boolean isValid = validateExitTrading(trade, context);

        if (Objects.nonNull(trade.getExitDate()) && Objects.nonNull(trade.getDate()) && trade.getExitDate().isBefore(trade.getDate())) {
            isValid = false;
            context.buildConstraintViolationWithTemplate("Exit date must be after trade date")
                    .addPropertyNode("exitDate")
                    .addConstraintViolation();
        }

        return isValid;
    }

    private static boolean validateExitTrading(Trade trade, ConstraintValidatorContext context) {
        boolean isValid = true;
        if ((Objects.nonNull(trade.getExitPrice()) || Objects.nonNull(trade.getExitDate()))
                && (Objects.isNull(trade.getExitPrice()) || Objects.isNull(trade.getExitDate()))) {
            isValid = false;
            String exitPrice = "exitPrice";
            String exitDate = "exitDate";
            String exitPriceLabel = "Exit Price";
            String exitDateLabel = "Exit Date";

            String nullFieldName = Objects.isNull(trade.getExitPrice()) ? exitPrice : exitDate;
            String message = (nullFieldName.equals(exitPrice) ? exitPriceLabel : exitDateLabel) + " is required";
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(nullFieldName)
                    .addConstraintViolation();
        }
        return isValid;
    }
}
