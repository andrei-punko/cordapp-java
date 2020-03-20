package com.template.webserver.controllers;

import com.google.common.collect.ImmutableMap;
import com.template.webserver.NodeRPCConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Define your API endpoints here. Current set of actions taken from https://docs.corda.net/tutorial-cordapp.html
 */
@RestController
@RequestMapping("/node")
public class NodeController {

    private final CordaRPCOps proxy;
    private final CordaX500Name myLegalName;
    private final List<String> SERVICE_NAMES = Arrays.asList("Notary", "Network Map Service");

    public NodeController(NodeRPCConnection rpc) {
        this.proxy = rpc.getProxy();
        this.myLegalName = proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }

    /**
     * Returns the node's name.
     */
    @GetMapping(value = "me", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, CordaX500Name> getNodeName() {
        return ImmutableMap.of("me", myLegalName);
    }

    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using the identity service.
     */
    @GetMapping(value = "peers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<CordaX500Name>> getPeers() {
        List<NodeInfo> nodesInfo = proxy.networkMapSnapshot();
        return ImmutableMap.of("peers", nodesInfo.stream().map(nodeInfo -> {
            return nodeInfo.getLegalIdentities().get(0).getName();
        }).filter(o -> {
            return !SERVICE_NAMES.contains(o.getOrganisation())
                // && !myLegalName.getOrganisation().equals(o.getOrganisation())
                ;
        }).collect(Collectors.toList()));
    }
}
