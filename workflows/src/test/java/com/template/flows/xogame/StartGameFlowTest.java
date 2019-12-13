package com.template.flows.xogame;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.template.contracts.XoGameContract;
import com.template.flows.AbstractFlowTest;
import com.template.states.XoGameState;
import java.util.Arrays;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.transactions.SignedTransaction;
import org.junit.Test;

public class StartGameFlowTest extends AbstractFlowTest {

    @Test
    public void testTransaction() throws Exception {
        final String gameId = "Game Id";
        StartGameFlow.Initiator flow = new StartGameFlow.Initiator(gameId, bParty);
        CordaFuture<SignedTransaction> future = nodeA.startFlow(flow);
        mockNetwork.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertThat("No inputs expected", signedTransaction.getTx().getInputs().isEmpty(), is(true));
        assertThat("One output expected", signedTransaction.getTx().getOutputStates().size(), is(1));

        TransactionState output = signedTransaction.getTx().getOutputs().get(0);
        assertThat("Wrong notary", output.getNotary(), is(notaryParty));
        assertThat("Wrong contract", output.getContract(), is(XoGameContract.ID));

        XoGameState xoGameState = signedTransaction.getTx().outputsOfType(XoGameState.class).get(0);
        assertThat("Wrong game id", xoGameState.getGameId(), is(gameId));
        assertThat("Wrong player1", xoGameState.getPlayer1(), is(aParty));
        assertThat("Wrong player2", xoGameState.getPlayer2(), is(bParty));

        assertThat("One command expected", signedTransaction.getTx().getCommands().size(), is(1));
        Command command = signedTransaction.getTx().getCommands().get(0);
        assertThat("Wrong instance type", command.getValue(), instanceOf(XoGameContract.Commands.StartGame.class));

        assertThat("Only two signers expected", command.getSigners().size(), is(2));
        assertThat("Keys of NodeA and NodeB should be present in signers list",
            command.getSigners().containsAll(Arrays.asList(aParty.getOwningKey(), bParty.getOwningKey())), is(true));
    }
}