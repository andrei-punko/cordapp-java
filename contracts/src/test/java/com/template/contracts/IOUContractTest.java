package com.template.contracts;

import static net.corda.testing.node.NodeTestUtils.ledger;

import com.template.states.IOUState;
import java.util.Arrays;
import kotlin.Unit;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

public class IOUContractTest {

    private final TestIdentity alice = new TestIdentity(new CordaX500Name("Alice", "London", "GB"));
    private final TestIdentity bob = new TestIdentity(new CordaX500Name("Bob", "Glasgow", "GB"));
    private MockServices ledgerServices = new MockServices(
        new TestIdentity(new CordaX500Name("TestId", "New York", "US")));

    private IOUState iouState = new IOUState(alice.getParty(), bob.getParty(), 1);

    @Test
    public void iouContractImplementsContract() {
        assert (new IOUContract() instanceof Contract);
    }

    @Test
    public void iouContractRequiresZeroInputsInTheTransaction() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has an input, will fail.
                tx.input(IOUContract.ID, iouState);
                tx.output(IOUContract.ID, iouState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
                return tx.failsWith("No inputs should be consumed when issuing an IOU.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void iouContractRequiresOneOutputInTheTransaction() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has two outputs, will fail.
                tx.output(IOUContract.ID, iouState);
                tx.output(IOUContract.ID, iouState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
                return tx.failsWith("There should be one output state of type IOUState.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void iouContractRequiresOneCommandInTheTransaction() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(IOUContract.ID, iouState);
                // Has two commands, will fail.
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
                return tx.fails();
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void iouContractRequiresTheTransactionsOutputToBeAIouState() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has wrong output type, will fail.
                tx.output(IOUContract.ID, new DummyState());
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
                return tx.fails();
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void iouContractRequiresTheTransactionsOutputToHaveAPositiveAmount() {
        IOUState zeroIouState = new IOUState(alice.getParty(), bob.getParty(), 0);
        IOUState negativeIouState = new IOUState(alice.getParty(), bob.getParty(), -1);
        IOUState positiveIouState = new IOUState(alice.getParty(), bob.getParty(), 2);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has zero-amount IouState, will fail.
                tx.output(IOUContract.ID, zeroIouState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
                return tx.failsWith("The IOU's value must be non-negative.");
            });
            return Unit.INSTANCE;
        });

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has negative-amount IouState, will fail.
                tx.output(IOUContract.ID, negativeIouState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
                return tx.failsWith("The IOU's value must be non-negative.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void iouContractRequiresTheTransactionsCommandToBeAnIssueCommand() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Has wrong command type, will fail.
                tx.output(IOUContract.ID, iouState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), DummyCommandData.INSTANCE);
                return tx.fails();
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void iouContractRequiresTheIssuerToBeARequiredSignerInTheTransaction() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Lender and borrower are the same, will fail.
                tx.output(IOUContract.ID, new IOUState(alice.getParty(), alice.getParty(), 1));
                tx.command(alice.getPublicKey(), new IOUContract.Commands.Action());
                return tx.failsWith("The lender and the borrower cannot be the same entity.");
            });
            return Unit.INSTANCE;
        });

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Only one signer, will fail.
                tx.output(IOUContract.ID, iouState);
                tx.command(bob.getPublicKey(), new IOUContract.Commands.Action());
                return tx.failsWith("There must be two signers.");
            });
            return Unit.INSTANCE;
        });

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Two same signers, Bob not presents, will fail.
                tx.output(IOUContract.ID, iouState);
                tx.command(Arrays.asList(alice.getPublicKey(), alice.getPublicKey()),
                    new IOUContract.Commands.Action());
                return tx.failsWith("The borrower and lender must be signers.");
            });
            return Unit.INSTANCE;
        });
    }

    @Test
    public void testIouContract() {
        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                // Issuer is a required signer, will verify.
                tx.output(IOUContract.ID, iouState);
                tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
                return tx.verifies();
            });
            return Unit.INSTANCE;
        });
    }
}
