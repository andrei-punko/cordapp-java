package com.template.states;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

public class IOUStateTest {

    private final Party alice = new TestIdentity(new CordaX500Name("Alice", "", "GB")).getParty();
    private final Party bob = new TestIdentity(new CordaX500Name("Bob", "", "GB")).getParty();

    @Test
    public void iouStateHasIssuerOwnerAndAmountParamsOfCorrectTypeInConstructor() {
        new IOUState(alice, bob, 1);
    }

    @Test
    public void iouStateHasGettersForIssuerOwnerAndAmount() {
        IOUState iouState = new IOUState(alice, bob, 1);
        assertEquals(alice, iouState.getLender());
        assertEquals(bob, iouState.getBorrower());
        assertEquals(1, iouState.getValue());
    }

    @Test
    public void iouStateImplementsContractState() {
        assertTrue(new IOUState(alice, bob, 1) instanceof IOUState);
    }

    @Test
    public void iouStateHasTwoParticipantsTheIssuerAndTheOwner() {
        IOUState iouState = new IOUState(alice, bob, 1);
        assertEquals(2, iouState.getParticipants().size());
        assertTrue(iouState.getParticipants().contains(alice));
        assertTrue(iouState.getParticipants().contains(bob));
    }
}
