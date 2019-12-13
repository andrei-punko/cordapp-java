package com.template.flows.iou;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.template.contracts.IOUContract;
import com.template.flows.AbstractFlowTest;
import com.template.states.IOUState;
import java.util.Arrays;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.transactions.SignedTransaction;
import org.junit.Test;

public class IOUFlowTest extends AbstractFlowTest {

    @Test
    public void testTransaction() throws Exception {
        IOUFlow.Initiator flow = new IOUFlow.Initiator(bParty, 99);
        CordaFuture<SignedTransaction> future = nodeA.startFlow(flow);
        mockNetwork.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertThat("No inputs expected", signedTransaction.getTx().getInputs().isEmpty(), is(true));
        assertThat("One output expected", signedTransaction.getTx().getOutputStates().size(), is(1));

        TransactionState output = signedTransaction.getTx().getOutputs().get(0);
        assertThat("Wrong notary", output.getNotary(), is(notaryParty));
        assertThat("Wrong contract", output.getContract(), is(IOUContract.ID));

        IOUState iouState = signedTransaction.getTx().outputsOfType(IOUState.class).get(0);
        assertThat("Wrong borrower", iouState.getBorrower(), is(bParty));
        assertThat("Wrong amount value", iouState.getValue(), is(99));

        assertThat("One command expected", signedTransaction.getTx().getCommands().size(), is(1));
        Command command = signedTransaction.getTx().getCommands().get(0);
        assertThat("Wrong instance type", command.getValue(), instanceOf(IOUContract.Commands.Action.class));

        assertThat("Only two signers expected", command.getSigners().size(), is(2));
        assertThat("Keys of NodeA and NodeB should be present in signers list",
            command.getSigners().containsAll(Arrays.asList(aParty.getOwningKey(), bParty.getOwningKey())), is(true));

        assertThat("The single attachment is the contract attachment.",
            signedTransaction.getTx().getAttachments().size(), is(1));
        assertNull("Time window isn't expected", signedTransaction.getTx().getTimeWindow());
    }
}
