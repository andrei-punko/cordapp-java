package com.template.states;

import com.template.contracts.XoGameContract;
import com.template.model.XoGameField;
import java.util.Arrays;
import java.util.List;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

@BelongsToContract(XoGameContract.class)
public class XoGameState implements LinearState {

    private final UniqueIdentifier linearId;
    private final String gameId;
    private final Party player1;
    private final Party player2;
    private final XoGameField gameField;

    public XoGameState(String gameId, Party player1, Party player2) {
        this.linearId = new UniqueIdentifier(gameId);
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
        this.gameField = new XoGameField();
    }

    /**
     * If more than one constructor is provided, the serialization framework needs to know which one to use. The @ConstructorForDeserialization annotation can
     * be used to indicate which one. See https://docs.corda.net/serialization.html for details
     */
    @ConstructorForDeserialization
    public XoGameState(String gameId, Party player1, Party player2, XoGameField gameField) {
        this.linearId = new UniqueIdentifier(gameId);
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
        this.gameField = gameField;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public String getGameId() {
        return gameId;
    }

    public Party getPlayer1() {
        return player1;
    }

    public Party getPlayer2() {
        return player2;
    }

    public XoGameField getGameField() {
        return gameField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        XoGameState that = (XoGameState) o;

        return new EqualsBuilder()
            .append(gameId, that.gameId)
            .append(player1, that.player1)
            .append(player2, that.player2)
            .append(gameField, that.gameField)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(gameId)
            .append(player1)
            .append(player2)
            .append(gameField)
            .toHashCode();
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(player1, player2);
    }
}
