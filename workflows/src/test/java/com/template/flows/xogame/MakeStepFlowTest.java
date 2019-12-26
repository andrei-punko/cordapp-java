package com.template.flows.xogame;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.template.contracts.XoGameContract;
import com.template.flows.AbstractFlowTest;
import com.template.model.XoGameField;
import com.template.states.XoGameState;
import java.util.Arrays;
import liquibase.util.StringUtils;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.StartedMockNode;
import org.junit.Test;

public class MakeStepFlowTest extends AbstractFlowTest {

    @Test
    public void testTransaction() throws Exception {
        final String gameId = StringUtils.randomIdentifer(10);
        final Party opponent = bParty;
        StartGameFlow.Initiator startGameFlow = new StartGameFlow.Initiator(gameId, opponent);
        CordaFuture<SignedTransaction> startGameFuture = nodeA.startFlow(startGameFlow);
        mockNetwork.runNetwork();
        startGameFuture.get();

        final String newField = "----X----";
        MakeStepFlow.Initiator flow = new MakeStepFlow.Initiator(gameId, opponent, newField);
        CordaFuture<SignedTransaction> makeStepFuture = nodeA.startFlow(flow);
        mockNetwork.runNetwork();
        SignedTransaction signedTransaction = makeStepFuture.get();

        // We check the recorded transaction in both vaults.
        for (StartedMockNode node : ImmutableList.of(nodeA, nodeB)) {
            SignedTransaction recordedTx = node.getServices().getValidatedTransactions().getTransaction(signedTransaction.getId());
            assertEquals(signedTransaction, recordedTx);

            assertThat("One input expected", recordedTx.getTx().getInputs().size(), is(1));
            assertThat("One output expected", recordedTx.getTx().getOutputStates().size(), is(1));

            TransactionState output = recordedTx.getTx().getOutputs().get(0);
            assertThat("Wrong notary", output.getNotary(), is(notaryParty));
            assertThat("Wrong contract", output.getContract(), is(XoGameContract.ID));

            XoGameState xoGameState = recordedTx.getTx().outputsOfType(XoGameState.class).get(0);
            assertThat("Wrong game id", xoGameState.getGameId(), is(gameId));
            assertThat("Wrong player1", xoGameState.getPlayer1(), is(aParty));
            assertThat("Wrong player2", xoGameState.getPlayer2(), is(bParty));
            assertThat("Wrong nextTurnOwner", xoGameState.getNextTurnOwner(), is(bParty));
            assertThat("Wrong game field", xoGameState.getGameField(), is(new XoGameField(newField)));

            assertThat("One command expected", recordedTx.getTx().getCommands().size(), is(1));
            Command command = recordedTx.getTx().getCommands().get(0);
            assertThat("Wrong instance type", command.getValue(), instanceOf(XoGameContract.Commands.MakeStep.class));

            assertThat("Only two signers expected", command.getSigners().size(), is(2));
            assertThat("Keys of NodeA and NodeB should be present in signers list",
                command.getSigners().containsAll(Arrays.asList(aParty.getOwningKey(), bParty.getOwningKey())), is(true));
        }
    }
}
