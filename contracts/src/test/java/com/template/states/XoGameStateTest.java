package com.template.states;

import static com.template.model.XoState.E;
import static com.template.model.XoState.O;
import static com.template.model.XoState.X;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.template.model.XoGameField;
import com.template.model.XoState;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

public class XoGameStateTest {

    private final TestIdentity alice = new TestIdentity(new CordaX500Name("Alice", "London", "GB"));
    private final TestIdentity bob = new TestIdentity(new CordaX500Name("Bob", "Glasgow", "GB"));

    @Test
    public void testEquals() {
        XoGameState state1 = new XoGameState("12345", alice.getParty(), bob.getParty(), new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        }));
        XoGameState state2 = new XoGameState("12345", alice.getParty(), bob.getParty(), new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, X}
        }));
        XoGameState state3 = new XoGameState("12345", alice.getParty(), bob.getParty(), new XoGameField(new XoState[][]{
            {X, E, E},
            {O, X, E},
            {E, E, O}
        }));

        assertThat("State equals to itself", state1.equals(state1), is(true));
        assertThat("State equals to another same state", state1.equals(state2), is(true));
        assertThat("State unequals to another different state", state1.equals(state3), is(false));
    }
}
