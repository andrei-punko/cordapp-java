package com.template.states;

import static com.template.model.XoState.E;
import static com.template.model.XoState.O;
import static com.template.model.XoState.X;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.template.model.XoGameField;
import com.template.model.XoState;
import com.template.schema.XoGameSchemaV1;
import com.template.schema.XoGameSchemaV1.PersistentXoGame;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.schemas.PersistentState;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

public class XoGameStateTest {

    private final TestIdentity alice = new TestIdentity(new CordaX500Name("Alice", "London", "GB"));
    private final TestIdentity bob = new TestIdentity(new CordaX500Name("Bob", "Glasgow", "GB"));
    private final TestIdentity incognito = new TestIdentity(new CordaX500Name("Incognito", "Glasgow", "GB"));

    @Test
    public void testInitGameConstructor() {
        final String gameId = "12345";

        XoGameState gameState = new XoGameState(gameId, alice.getParty(), bob.getParty());

        assertThat(gameState.getLinearId(), is(notNullValue()));
        assertThat(gameState.getGameId(), is(gameId));
        assertThat(gameState.getPlayer1(), is(alice.getParty()));
        assertThat(gameState.getPlayer2(), is(bob.getParty()));
        assertThat(gameState.getNextTurnOwner(), is(alice.getParty()));
        assertThat(gameState.getGameField(), is(new XoGameField("---------")));
        assertThat(gameState.getParticipants().size(), is(2));
        assertThat(gameState.getParticipants(), hasItems(alice.getParty(), bob.getParty()));
    }

    @Test
    public void testConstructor() {
        final String gameId = "12345";
        final XoGameField gameField = new XoGameField("---X-O---");

        XoGameState gameState = new XoGameState(gameId, alice.getParty(), bob.getParty(), bob.getParty(), gameField);

        assertThat(gameState.getLinearId(), is(notNullValue()));
        assertThat(gameState.getGameId(), is(gameId));
        assertThat(gameState.getPlayer1(), is(alice.getParty()));
        assertThat(gameState.getPlayer2(), is(bob.getParty()));
        assertThat(gameState.getNextTurnOwner(), is(bob.getParty()));
        assertThat(gameState.getGameField(), is(gameField));
        assertThat(gameState.getParticipants().size(), is(2));
        assertThat(gameState.getParticipants(), hasItems(alice.getParty(), bob.getParty()));
    }

    @Test
    public void testConstructorWithWrongOpponent() {
        final String gameId = "12345";
        try {
            new XoGameState(gameId, alice.getParty(), bob.getParty(), incognito.getParty());
            fail("Exception should be thrown");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("NextTurnOwner should be one of players"));
        }
    }

    @Test
    public void testGenerateMappedObject() {
        final String gameId = "12345";
        final XoGameField gameField = new XoGameField("---X-O---");
        XoGameState gameState = new XoGameState(gameId, alice.getParty(), bob.getParty(), bob.getParty(), gameField);

        PersistentState mappedObject = gameState.generateMappedObject(new XoGameSchemaV1());

        assertThat(mappedObject instanceof XoGameSchemaV1.PersistentXoGame, is(true));
        PersistentXoGame persistentXoGame = (PersistentXoGame) mappedObject;
        assertThat(persistentXoGame.getLinearId(), is(notNullValue()));
        assertThat(persistentXoGame.getGameId(), is(gameId));
        assertThat(persistentXoGame.getPlayer1(), is(alice.getParty().toString()));
        assertThat(persistentXoGame.getPlayer2(), is(bob.getParty().toString()));
        assertThat(persistentXoGame.getNextTurnOwner(), is(bob.getParty().toString()));
        assertThat(persistentXoGame.getGameField(), is(gameField.toString()));
    }

    @Test
    public void testEquals() {
        XoGameState state1 = new XoGameState("12345", alice.getParty(), bob.getParty(), alice.getParty(), new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        }));
        XoGameState state2 = new XoGameState("12345", alice.getParty(), bob.getParty(), alice.getParty(), new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        }));
        XoGameState state3 = new XoGameState("12345", alice.getParty(), bob.getParty(), alice.getParty(), new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, O}
        }));

        assertThat("State equals to itself", state1.equals(state1), is(true));
        assertThat("State equals to another same state", state1.equals(state2), is(true));
        assertThat("State unequals to another different state", state1.equals(state3), is(false));
    }

    @Test
    public void testHashCode() {
        XoGameState state1 = new XoGameState("12345", alice.getParty(), bob.getParty(), alice.getParty(), new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        }));
        XoGameState state2 = new XoGameState("12345", alice.getParty(), bob.getParty(), alice.getParty(), new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        }));
        XoGameState state3 = new XoGameState("12345", alice.getParty(), bob.getParty(), alice.getParty(), new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, O}
        }));

        assertThat("State hashCode equals to another same state hashCode", state1.hashCode(), is(state2.hashCode()));
        assertThat("State hashCode unequals to another different state hashCode", state1.hashCode(), is(not(state3.hashCode())));
    }
}
