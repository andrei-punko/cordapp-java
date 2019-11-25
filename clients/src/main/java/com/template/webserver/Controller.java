package com.template.webserver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.template.flows.IOUFlow;
import com.template.states.IOUState;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Define your API endpoints here. Current set of actions taken from https://docs.corda.net/tutorial-cordapp.html
 */
@RestController
@RequestMapping("/api/example/") // The paths for GET and POST requests are relative to this base path.
public class Controller {

    private final CordaRPCOps proxy;
    private final CordaX500Name myLegalName;
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);
    private final List<String> SERVICE_NAMES = Arrays.asList("Notary", "Network Map Service");

    public Controller(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        this.myLegalName = rpc.proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }

    /**
     * Returns the node's name.
     */
    @GetMapping(value = "me", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, CordaX500Name> getNodeName() {
        return ImmutableMap.of("me", myLegalName);
    }

    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using
     * the identity service.
     */
    @GetMapping(value = "peers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<CordaX500Name>> getPeers() {
        List<NodeInfo> nodesInfo = proxy.networkMapSnapshot();
        return ImmutableMap.of("peers", nodesInfo.stream().map(nodeInfo -> {
            return nodeInfo.getLegalIdentities().get(0).getName();
        }).filter(o -> {
            return !SERVICE_NAMES.contains(o.getOrganisation()) &&
                !myLegalName.getOrganisation().equals(o.getOrganisation());
        }).collect(Collectors.toList()));
    }

    /**
     * Displays all IOU states that exist in the node's vault.
     */
    @GetMapping(value = "ious", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StateAndRef<IOUState>>> getIOUs() {
        return ResponseEntity.ok(proxy.vaultQuery(IOUState.class).getStates());
    }

    /**
     * Initiates a flow to agree an IOU between two parties.
     * <p>
     * Once the flow finishes it will have written the IOU to ledger. Both the lender and the borrower will be able to
     * see it when calling /spring/api/ious on their respective nodes.
     * <p>
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     * <p>
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */

    @PostMapping(value = "create-iou", produces = MediaType.TEXT_PLAIN_VALUE, headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<String> createIOU(HttpServletRequest request) {
        Integer iouValue = Integer.valueOf(request.getParameter("iouValue"));
        String partyName = request.getParameter("partyName");
        if (partyName == null) {
            return ResponseEntity.badRequest().body("Query parameter 'partyName' must not be null.\n");
        }
        ;
        if (iouValue <= 0) {
            return ResponseEntity.badRequest().body("Query parameter 'iouValue' must be non-negative.\n");
        }
        CordaX500Name partyX500Name = CordaX500Name.parse(partyName);
        Party otherParty = proxy.wellKnownPartyFromX500Name(partyX500Name);
        if (otherParty == null) {
            return ResponseEntity.badRequest().body("Party named $partyName cannot be found.\n");
        }

        try {
            SignedTransaction signedTx = proxy.startTrackedFlowDynamic(IOUFlow.class, otherParty, iouValue)
                .getReturnValue().get();
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("Transaction id ${signedTx.id} committed to ledger.\n");
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            assert ex.getMessage() != null;
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Displays all IOU states that only this node has been involved in.
     */
    @GetMapping(value = "my-ious", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StateAndRef<IOUState>>> getMyIOUs() {
        List<StateAndRef<IOUState>> myIous = proxy.vaultQuery(IOUState.class).getStates().stream()
            .filter(it -> {
                return it.getState().getData().getLender().equals(proxy.nodeInfo().getLegalIdentities().get(0));
            }).collect(Collectors.toList());
        return ResponseEntity.ok(myIous);
    }
}
