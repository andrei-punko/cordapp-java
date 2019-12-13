package com.template.flows.iou;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.template.contracts.IOUContract;
import com.template.states.IOUState;
import java.util.Arrays;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.TransactionState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IOUFlowTest {

    private MockNetwork mockNetwork;
    private StartedMockNode notaryNode;
    private StartedMockNode nodeA;
    private StartedMockNode nodeB;
    private Party notaryParty;
    private Party aParty;
    private Party bParty;

    @Before
    public void setup() {
        MockNetworkParameters mockNetworkParameters = new MockNetworkParameters(ImmutableList.of(
            TestCordapp.findCordapp("com.template.contracts"),
            TestCordapp.findCordapp("com.template.flows")
        ));
        mockNetwork = new MockNetwork(mockNetworkParameters);

        notaryNode = mockNetwork.getNotaryNodes().get(0);
        nodeA = mockNetwork.createPartyNode(new CordaX500Name("NodeA", "London", "GB"));
        nodeB = mockNetwork.createPartyNode(new CordaX500Name("NodeB", "London", "GB"));

        notaryParty = notaryNode.getInfo().getLegalIdentities().get(0);
        aParty = nodeA.getInfo().getLegalIdentities().get(0);
        bParty = nodeB.getInfo().getLegalIdentities().get(0);

        mockNetwork.runNetwork();
    }

    @After
    public void tearDown() {
        mockNetwork.stopNodes();
    }

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
