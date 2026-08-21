package com.bendercasino.model;


public record Card(String code, String value, String suit, String image) {

    public int points() {
        return switch (value) {
            case "ACE"                      -> 11;
            case "KING", "QUEEN", "JACK"    -> 10;
            default                         -> Integer.parseInt(value);
        };
    }

    public boolean isAce() {
        return "ACE".equals(value);
    }
}
