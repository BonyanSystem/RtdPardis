package com.bonyan.rtd.token;

import java.util.Calendar;
import java.util.Date;

public class Token implements TokenInterface {

    private String tokenValue;
    private int tokenDurationTypeCode;
    private int renewalMarginPercentage;
    private Date expirationTime;

    public Token(String tokenValue) {
        this.tokenValue = tokenValue;
        this.tokenDurationTypeCode = Calendar.SECOND;
        this.renewalMarginPercentage = 0;
        this.expirationTime = calculateExpirationTime(300);
    }

    public Token(String tokenValue, TokenDurationType tokenDurationType, int expirationDuration,
                 int renewalMarginPercentage) {

        this.tokenValue = tokenValue;
        this.expirationTime = calculateExpirationTime(expirationDuration);
        this.renewalMarginPercentage = renewalMarginPercentage;

        switch (tokenDurationType) {
            case MILLISECOND:
                this.tokenDurationTypeCode = Calendar.MILLISECOND;
                break;
            case MINUTE:
                this.tokenDurationTypeCode = Calendar.MINUTE;
                break;
            case HOUR:
                this.tokenDurationTypeCode = Calendar.HOUR;
                break;
            default:
                this.tokenDurationTypeCode = Calendar.SECOND;
                break;
        }
    }

    @Override
    public String getTokenValue() {
        return "";
    }

    @Override
    public boolean isTokenValid() {
        return false;
    }

    private Date calculateExpirationTime(int expirationDurationInMinutes) {
        int penaltyDuration = expirationDurationInMinutes - (expirationDurationInMinutes * renewalMarginPercentage / 100);
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(this.tokenDurationTypeCode, penaltyDuration);
        return calendar.getTime();
    }

    public boolean isValid() {
        Date now = new Date();
        return now.before(this.expirationTime);
    }

    public int getTokenDurationTypeCode() {
        return tokenDurationTypeCode;
    }

    public void setTokenDurationTypeCode(int tokenDurationTypeCode) {
        this.tokenDurationTypeCode = tokenDurationTypeCode;
    }

    public int getRenewalMarginPercentage() {
        return renewalMarginPercentage;
    }

    public void setRenewalMarginPercentage(int renewalMarginPercentage) {
        this.renewalMarginPercentage = renewalMarginPercentage;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }
}
