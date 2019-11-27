package com.template.contracts;

import static net.corda.testing.node.NodeTestUtils.transaction;

import com.template.states.IOUState;
import java.util.Arrays;
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
    private MockServices ledgerServices = new MockServices(new TestIdentity(new CordaX500Name("TestId", "New York", "US")));

    private IOUState iouState = new IOUState(alice.getParty(), bob.getParty(), 1);

    @Test
    public void iouContractImplementsContract() {
        assert(new IOUContract() instanceof Contract);
    }

    @Test
    public void iouContractRequiresZeroInputsInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Has an input, will fail.
            tx.input(IOUContract.ID, iouState);
            tx.output(IOUContract.ID, iouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.failsWith("No inputs should be consumed when issuing an IOU.");
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has no input, will verify.
            tx.output(IOUContract.ID, iouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void iouContractRequiresOneOutputInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Has two outputs, will fail.
            tx.output(IOUContract.ID, iouState);
            tx.output(IOUContract.ID, iouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.failsWith("There should be one output state of type IOUState.");
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one output, will verify.
            tx.output(IOUContract.ID, iouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void iouContractRequiresOneCommandInTheTransaction() {
        transaction(ledgerServices, tx -> {
            tx.output(IOUContract.ID, iouState);
            // Has two commands, will fail.
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(IOUContract.ID, iouState);
            // Has one command, will verify.
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void iouContractRequiresTheTransactionsOutputToBeAIouState() {
        transaction(ledgerServices, tx -> {
            // Has wrong output type, will fail.
            tx.output(IOUContract.ID, new DummyState());
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct output type, will verify.
            tx.output(IOUContract.ID, iouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void iouContractRequiresTheTransactionsOutputToHaveAPositiveAmount() {
        IOUState zeroIouState = new IOUState(alice.getParty(), bob.getParty(), 0);
        IOUState negativeIouState = new IOUState(alice.getParty(), bob.getParty(), -1);
        IOUState positiveIouState = new IOUState(alice.getParty(), bob.getParty(), 2);

        transaction(ledgerServices, tx -> {
            // Has zero-amount IouState, will fail.
            tx.output(IOUContract.ID, zeroIouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.failsWith("The IOU's value must be non-negative.");
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has negative-amount IouState, will fail.
            tx.output(IOUContract.ID, negativeIouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.failsWith("The IOU's value must be non-negative.");
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has positive-amount IouState, will verify.
            tx.output(IOUContract.ID, iouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Also has positive-amount IouState, will verify.
            tx.output(IOUContract.ID, positiveIouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void iouContractRequiresTheTransactionsCommandToBeAnIssueCommand() {
        transaction(ledgerServices, tx -> {
            // Has wrong command type, will fail.
            tx.output(IOUContract.ID, iouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct command type, will verify.
            tx.output(IOUContract.ID, iouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void iouContractRequiresTheIssuerToBeARequiredSignerInTheTransaction() {
        IOUState iouStateWhereBobIsIssuer = new IOUState(bob.getParty(), alice.getParty(), 1);

        transaction(ledgerServices, tx -> {
            // Lender and borrower are the same, will fail.
            tx.output(IOUContract.ID, new IOUState(alice.getParty(), alice.getParty(), 1));
            tx.command(alice.getPublicKey(), new IOUContract.Commands.Action());
            tx.failsWith("The lender and the borrower cannot be the same entity.");
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is not a required signer, will fail.
            tx.output(IOUContract.ID, iouState);
            tx.command(bob.getPublicKey(), new IOUContract.Commands.Action());
            tx.failsWith("There must be two signers.");
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also not a required signer, will fail.
            tx.output(IOUContract.ID, iouStateWhereBobIsIssuer);
            tx.command(alice.getPublicKey(), new IOUContract.Commands.Action());
            tx.failsWith("There must be two signers.");
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Two same signers, Bob not presents, will fail.
            tx.output(IOUContract.ID, iouState);
            tx.command(Arrays.asList(alice.getPublicKey(), alice.getPublicKey()), new IOUContract.Commands.Action());
            tx.failsWith("The borrower and lender must be signers.");
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is a required signer, will verify.
            tx.output(IOUContract.ID, iouState);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also a required signer, will verify.
            tx.output(IOUContract.ID, iouStateWhereBobIsIssuer);
            tx.command(Arrays.asList(alice.getPublicKey(), bob.getPublicKey()), new IOUContract.Commands.Action());
            tx.verifies();
            return null;
        });
    }
}
