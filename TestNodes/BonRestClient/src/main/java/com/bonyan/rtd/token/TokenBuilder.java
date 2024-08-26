package com.bonyan.rtd.token;

import java.util.MissingFormatArgumentException;
import java.util.Objects;

public class TokenBuilder<T extends TokenInterface> {

    public static TokenInterface buildNormalToken(TokenType type, String tokenValue) throws MissingFormatArgumentException {
        if (Objects.requireNonNull(type) == TokenType.PARDIS_TOKEN) {
            return new Token(tokenValue);
        }
        throw new MissingFormatArgumentException("missing or invalid token type");
    }

    private static TokenInterface buildPardisToken(String tokenValue, TokenDurationType tokenDurationType,
                                                   int expirationDuration, int expirationPenaltyPercentage) {
        return new Token(tokenValue,tokenDurationType,expirationDuration,expirationPenaltyPercentage);
    }

}
