package com.template.states;

import com.google.common.collect.ImmutableList;
import com.template.contracts.XoGameContract;
import com.template.model.XoGameField;
import com.template.model.XoState;
import com.template.schema.XoGameSchemaV1;
import java.util.Arrays;
import java.util.List;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

@BelongsToContract(XoGameContract.class)
public class XoGameState implements LinearState, QueryableState {

    private final UniqueIdentifier linearId;
    private final String gameId;
    private final Party player1;
    private final Party player2;
    private final Party nextTurnOwner;
    private final XoGameField gameField;

    public XoGameState(String gameId, Party player1, Party player2) {
        this(gameId, player1, player2, player1);
    }

    public XoGameState(String gameId, Party player1, Party player2, Party nextTurnOwner) {
        this(gameId, player1, player2, nextTurnOwner, new XoGameField());
    }

    /**
     * If more than one constructor is provided, the serialization framework needs to know which one to use. The @ConstructorForDeserialization annotation can
     * be used to indicate which one. See https://docs.corda.net/serialization.html for details
     */
    @ConstructorForDeserialization
    public XoGameState(String gameId, Party player1, Party player2, Party nextTurnOwner, XoGameField gameField) {
        if (!nextTurnOwner.equals(player1) && !nextTurnOwner.equals(player2)) {
            throw new IllegalArgumentException("NextTurnOwner should be one of players");
        }
        this.linearId = new UniqueIdentifier(gameId);
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
        this.nextTurnOwner = nextTurnOwner;
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

    public Party getNextTurnOwner() {
        return nextTurnOwner;
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
            .append(nextTurnOwner, that.nextTurnOwner)
            .append(gameField, that.gameField)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(gameId)
            .append(player1)
            .append(player2)
            .append(nextTurnOwner)
            .append(gameField)
            .toHashCode();
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(player1, player2);
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        if (schema instanceof XoGameSchemaV1) {
            return new XoGameSchemaV1.PersistentXoGame(
                this.linearId.getId(),
                this.gameId,
                this.player1.getName().toString(),
                this.player2.getName().toString(),
                this.nextTurnOwner.getName().toString(),
                this.gameField.toString());
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new XoGameSchemaV1());
    }

    @Override
    public String toString() {
        return "XoGameState{" +
            "gameId='" + gameId + '\'' +
            ", player1=" + player1 +
            ", player2=" + player2 +
            ", nextTurnOwner=" + nextTurnOwner +
            ", gameField=" + gameField +
            '}';
    }

    /**
     * We sit on convention that player1 uses 'X' and player2 - 'O' symbols.
     * @return
     */
    public XoState determineNextTurnSymbol() {
        if (nextTurnOwner.equals(player1)) {
            return XoState.X;
        } else if (nextTurnOwner.equals(player2)) {
            return XoState.O;
        } else {
            throw new IllegalStateException("NextTurnOwner should be one of players");
        }
    }
}
