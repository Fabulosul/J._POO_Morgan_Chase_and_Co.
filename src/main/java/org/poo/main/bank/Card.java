package org.poo.main.bank;

import lombok.Getter;
import lombok.Setter;
import org.poo.utils.Utils;

@Getter
@Setter
public final class Card {
    private String cardNumber;
    private String status;
    private String type;

    public Card(final String type) {
        this.cardNumber = Utils.generateCardNumber();
        this.status = "active";
        this.type = type;
    }

    /**
     * Helper method to check if the card is a one-time card
     *
     * @return true if the card is a one-time card, false otherwise
     */
    public boolean isOneTimeCard() {
        return this.type.equals("oneTime");
    }

    /**
     * Helper method used to check if the card is frozen
     *
     * @return true if the card is frozen, false otherwise
     */
    public boolean isFrozen() {
        return this.status.equals("frozen");
    }

    /**
     * Helper method used to freeze the card.
     * It sets the status of the card to "frozen".
     */
    public void freezeCard() {
        this.status = "frozen";
    }
}
