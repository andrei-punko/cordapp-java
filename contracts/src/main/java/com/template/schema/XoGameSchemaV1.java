package com.template.schema;

import com.google.common.collect.ImmutableList;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;

@CordaSerializable
public class XoGameSchemaV1 extends MappedSchema {

    public XoGameSchemaV1() {
        super(XoGameSchema.class, 1, ImmutableList.of(PersistentXoGame.class));
    }

    @Entity
    @Table(name = "xo_games")
    public static class PersistentXoGame extends PersistentState {

        @Column(name = "linear_id")
        private final UUID linearId;
        @Column(name = "game_id")
        private final String gameId;
        @Column(name = "player1")
        private final String player1;
        @Column(name = "player2")
        private final String player2;
        @Column(name = "next_turn_owner")
        private final String nextTurnOwner;
        @Column(name = "game_field")
        private final String gameField;

        public PersistentXoGame(UUID linearId, String gameId, String player1, String player2, String nextTurnOwner, String gameField) {
            this.linearId = linearId;
            this.gameId = gameId;
            this.player1 = player1;
            this.player2 = player2;
            this.nextTurnOwner = nextTurnOwner;
            this.gameField = gameField;
        }

        public PersistentXoGame() {
            this.linearId = null;
            this.gameId = null;
            this.player1 = null;
            this.player2 = null;
            this.nextTurnOwner = null;
            this.gameField = null;
        }

        public UUID getLinearId() {
            return linearId;
        }

        public String getGameId() {
            return gameId;
        }

        public String getPlayer1() {
            return player1;
        }

        public String getPlayer2() {
            return player2;
        }

        public String getNextTurnOwner() {
            return nextTurnOwner;
        }

        public String getGameField() {
            return gameField;
        }
    }
}
