package blackjack.strategy;

public enum Action {
    STAND("S"), HIT("H"), DOUBLE("D"), SPLIT("Y"), SURRENDER("SUR"),
    TAKE_INSURANCE("INS"), DECLINE_INSURANCE("NoINS");

    public final String shortCode;

    Action(String shortCode) {
        this.shortCode = shortCode;
    }
}
