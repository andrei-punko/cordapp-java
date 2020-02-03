package com.template.webserver;

import com.template.flows.xogame.MakeStepFlow;
import com.template.flows.xogame.StartGameFlow;
import com.template.states.XoGameState;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Define your API endpoints here. Current set of actions taken from https://docs.corda.net/tutorial-cordapp.html
 */
@RestController
@RequestMapping("/xo")
public class XoController {

    private final CordaRPCOps proxy;
    private final CordaX500Name myLegalName;
    private final static Logger logger = LoggerFactory.getLogger(XoController.class);

    public XoController(NodeRPCConnection rpc) {
        this.proxy = rpc.proxy;
        this.myLegalName = rpc.proxy.nodeInfo().getLegalIdentities().get(0).getName();
    }

    /**
     * Displays all games that exist in the node's vault.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StateAndRef<XoGameState>>> getAllGames() {
        return ResponseEntity.ok(proxy.vaultQuery(XoGameState.class).getStates());
    }

    /**
     * Get game by id
     */
    @GetMapping(value = "{gameId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StateAndRef<XoGameState>> getGameById(@PathVariable("gameId") String gameId) {
        StateAndRef<XoGameState> myGame = proxy.vaultQuery(XoGameState.class).getStates().stream()
            .filter(it -> {
                return it.getState().getData().getGameId().equals(gameId);
            }).collect(Collectors.toList()).get(0);
        return ResponseEntity.ok(myGame);
    }

    /**
     * Creates the game by gameId and opponent.
     */
    @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE, headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<String> startGame(HttpServletRequest request) {
        String gameId = request.getParameter("gameId");
        String partyName = request.getParameter("opponent");
        if (gameId == null) {
            return ResponseEntity.badRequest().body("Query parameter 'gameId' must not be null.\n");
        }
        if (partyName == null) {
            return ResponseEntity.badRequest().body("Query parameter 'opponent' must not be null.\n");
        }
        CordaX500Name partyX500Name = CordaX500Name.parse(partyName);
        Party opponent = proxy.wellKnownPartyFromX500Name(partyX500Name);
        if (opponent == null) {
            return ResponseEntity.badRequest().body("Party named $opponent cannot be found.\n");
        }

        try {
            SignedTransaction signedTx = proxy.startTrackedFlowDynamic(StartGameFlow.Initiator.class, gameId, opponent)
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
     * Makes game step
     */
    @PutMapping(produces = MediaType.TEXT_PLAIN_VALUE, headers = "Content-Type=application/x-www-form-urlencoded")
    public ResponseEntity<String> makeStep(HttpServletRequest request) {
        String gameId = request.getParameter("gameId");
        String partyName = request.getParameter("opponent");
        String newField = request.getParameter("newField");
        if (gameId == null) {
            return ResponseEntity.badRequest().body("Query parameter 'gameId' must not be null.\n");
        }
        if (partyName == null) {
            return ResponseEntity.badRequest().body("Query parameter 'opponent' must not be null.\n");
        }
        if (newField == null) {
            return ResponseEntity.badRequest().body("Query parameter 'newField' must not be null.\n");
        }
        CordaX500Name partyX500Name = CordaX500Name.parse(partyName);
        Party opponent = proxy.wellKnownPartyFromX500Name(partyX500Name);
        if (opponent == null) {
            return ResponseEntity.badRequest().body("Party named $opponent cannot be found.\n");
        }

        try {
            SignedTransaction signedTx = proxy.startTrackedFlowDynamic(MakeStepFlow.Initiator.class, gameId, opponent, newField)
                .getReturnValue().get();
            return ResponseEntity.status(HttpStatus.OK)
                .body("Transaction id ${signedTx.id} committed to ledger.\n");
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            assert ex.getMessage() != null;
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
