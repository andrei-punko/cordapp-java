package com.template.flows;

import com.google.common.collect.ImmutableList;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractFlowTest {

    protected MockNetwork mockNetwork;
    protected StartedMockNode notaryNode;
    protected StartedMockNode nodeA;
    protected StartedMockNode nodeB;
    protected Party notaryParty;
    protected Party aParty;
    protected Party bParty;

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
}
