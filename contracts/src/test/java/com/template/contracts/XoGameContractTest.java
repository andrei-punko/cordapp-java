package com.template.contracts;

import static com.template.model.XoState.E;
import static com.template.model.XoState.X;
import static net.corda.testing.node.NodeTestUtils.ledger;

import com.template.model.XoGameField;
import com.template.model.XoState;
import com.template.states.XoGameState;
import java.util.Arrays;
import kotlin.Unit;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Before;
import org.junit.Test;

public class XoGameContractTest {

    private TestIdentity alice = new TestIdentity(new CordaX500Name("Alice", "London", "GB"));
    private TestIdentity bob = new TestIdentity(new CordaX500Name("Bob", "Glasgow", "GB"));
    private TestIdentity incognito = new TestIdentity(new CordaX500Name("Incognito", "London", "GB"));
    private MockServices ledgerServices = new MockServices(new TestIdentity(new CordaX500Name("TestId", "New York", "US")));
    private XoGameState xoGameState;
    private XoGameState xoGameState2;

    @Before
    public void setup() {
        xoGameState = new XoGameState("First game", alice.getParty(), bob.getParty());
        xoGameState2 = new XoGameState("First game", alice.getParty(), bob.getParty(), bob.getParty(), new XoGameField(new XoState[][]{
            {E, E, E},
            {E, X, E},
            {E, E, E}
        }));
    }

    @Test
    public void xoGameContractImplementsContract() {
        assert (new XoGameContract() instanceof Contract);
    }

    @Test
    public void xoGameContractRequiresOnlyOneCommand() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has 2 commands, will fail.
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.StartGame());
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Only one command required.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractRequiresKnownCommand() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has 2 commands, will fail.
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
                return tx.failsWith("Unknown command.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractStartGameRequiresZeroInputs() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has an input, will fail.
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.StartGame());
                return tx.failsWith("No inputs should be consumed during game start.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepRequiresOneInput() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // No input, will fail.
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Only one input expected.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractStartGameRequiresOneOutput() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has 2 outputs, will fail.
                tx.output(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.StartGame());
                return tx.failsWith("Only one output should be created during game start.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepRequiresOneOutput() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has 2 outputs, will fail.
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Only one output should be created.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractStartGameRequiresTwoDifferentSigners() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey()), new XoGameContract.Commands.StartGame());
                return tx.failsWith("There must be two signers.");
            });
            return Unit.INSTANCE;
        });

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey(), alice.getPublicKey()), new XoGameContract.Commands.StartGame());
                return tx.failsWith("Both players must be signers.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepRequiresTwoDifferentSigners() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("There must be two signers.");
            });
            return Unit.INSTANCE;
        });

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), alice.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Both players must be signers.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractStartGameOutPlayersShouldBeDifferent() {
        xoGameState = new XoGameState(xoGameState.getGameId(), alice.getParty(), alice.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.StartGame());
                return tx.failsWith("Players should be different.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractStartGameNextTurnOwnerShouldBePlayer1() {
        xoGameState = new XoGameState(xoGameState.getGameId(), alice.getParty(), bob.getParty(), bob.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.StartGame());
                return tx.failsWith("NextTurnOwner should be player1");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractStartGameOutGameIdShouldBePresent() {
        xoGameState = new XoGameState(null, alice.getParty(), bob.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.StartGame());
                return tx.failsWith("Game id should be present");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractStartGameOutGameFieldShouldBePresent() {
        xoGameState = new XoGameState(xoGameState.getGameId(), alice.getParty(), bob.getParty(), alice.getParty(), null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.StartGame());
                return tx.failsWith("Game field should be present");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepOutputPlayersShouldBeDifferent() {
        xoGameState2 = new XoGameState(xoGameState.getGameId(), alice.getParty(), alice.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Output players should be different.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepOutputGameIdShouldBePresent() {
        xoGameState2 = new XoGameState(null, alice.getParty(), bob.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Output game id should be present");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepOutputGameFieldShouldBePresent() {
        xoGameState2 = new XoGameState(xoGameState.getGameId(), alice.getParty(), bob.getParty(), alice.getParty(), null);
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Output game field should be present");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepNextTurnOwnerShouldBeDifferent() {
        xoGameState2 = new XoGameState(xoGameState.getGameId(), alice.getParty(), bob.getParty(), alice.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("NextTurnOwner should be different.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepGameIdShouldBeTheSame() {
        xoGameState2 = new XoGameState("Another game", alice.getParty(), bob.getParty(), bob.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Game id should be the same");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepPlayer1ShouldBeTheSame() {
        xoGameState2 = new XoGameState(xoGameState.getGameId(), incognito.getParty(), bob.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Player1 should be the same");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepPlayer2ShouldBeTheSame() {
        xoGameState2 = new XoGameState(xoGameState.getGameId(), alice.getParty(), incognito.getParty(), incognito.getParty());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Player2 should be the same");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepGameFieldShouldBeChanged() {
        xoGameState2 = new XoGameState(xoGameState.getGameId(), alice.getParty(), bob.getParty(), bob.getParty(), xoGameState.getGameField());
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Game field should be changed");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStepOnlyOneCellOfGameFieldShouldBeChanged() {
        xoGameState2 = new XoGameState(xoGameState.getGameId(), alice.getParty(), bob.getParty(), bob.getParty(), new XoGameField(new XoState[][]{
            {E, E, X},
            {E, X, E},
            {X, E, E}
        }));
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.failsWith("Only one cell should be changed");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractStartGame() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(XoGameContract.ID, xoGameState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.StartGame());
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void xoGameContractMakeStep() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(XoGameContract.ID, xoGameState);
                tx.output(XoGameContract.ID, xoGameState2);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new XoGameContract.Commands.MakeStep());
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });
    }
}
