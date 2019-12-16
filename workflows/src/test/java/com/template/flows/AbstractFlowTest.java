package com.template.flows;

import com.google.common.collect.ImmutableList;
import java.util.concurrent.TimeUnit;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

public abstract class AbstractFlowTest {

    @Rule
    public Timeout globalTimeout = new Timeout(2, TimeUnit.MINUTES);

    protected final MockNetwork mockNetwork = new MockNetwork(new MockNetworkParameters(ImmutableList.of(
        TestCordapp.findCordapp("com.template.contracts"),
        TestCordapp.findCordapp("com.template.flows")
    )));
    protected StartedMockNode notaryNode = mockNetwork.getNotaryNodes().get(0);
    protected final StartedMockNode nodeA = mockNetwork.createPartyNode(new CordaX500Name("NodeA", "London", "GB"));
    protected final StartedMockNode nodeB = mockNetwork.createPartyNode(new CordaX500Name("NodeB", "Minsk", "BY"));

    protected Party notaryParty = notaryNode.getInfo().getLegalIdentities().get(0);
    protected Party aParty = nodeA.getInfo().getLegalIdentities().get(0);
    protected Party bParty = nodeB.getInfo().getLegalIdentities().get(0);

    @Before
    public void setup() {
        mockNetwork.runNetwork();
    }

    @After
    public void tearDown() {
        mockNetwork.stopNodes();
    }
}
