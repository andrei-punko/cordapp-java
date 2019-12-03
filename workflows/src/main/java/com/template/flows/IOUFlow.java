package com.template.flows;

import static net.corda.core.contracts.ContractsDSL.requireThat;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.IOUContract;
import com.template.states.IOUState;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.CollectSignaturesFlow;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.ReceiveFinalityFlow;
import net.corda.core.flows.SignTransactionFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

public class IOUFlow {

    // ******************
    // * Initiator flow *
    // ******************
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {
        private final Party otherParty;
        private final Integer iouValue;

        private final Step GENERATING_TRANSACTION = new Step("Generating transaction based on new IOU.");
        private final Step VERIFYING_TRANSACTION = new Step("Verifying contract constraints.");
        private final Step SIGNING_TRANSACTION = new Step("Signing transaction with our private key.");
        private final Step GATHERING_SIGS = new Step("Gathering the counterparty's signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final Step FINALISING_TRANSACTION = new Step("Obtaining notary signature and recording transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        /**
         * The progress tracker provides checkpoints indicating the progress of the flow to observers.
         */
        private final ProgressTracker progressTracker = new ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION
        );

        public Initiator(Party otherParty, Integer iouValue) {
            this.otherParty = otherParty;
            this.iouValue = iouValue;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // We retrieve the notary identity from the network map.
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // Step 1
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            // We create the transaction components.
            IOUState outputState = new IOUState(getOurIdentity(), otherParty, iouValue);
            List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey(), otherParty.getOwningKey());
            Command command = new Command<>(new IOUContract.Commands.Action(), requiredSigners);

            // We create a transaction builder and add the components.
            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(outputState, IOUContract.ID)
                .addCommand(command);

            // Step 2
            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
            // Verifying the transaction.
            txBuilder.verify(getServiceHub());

            // Stage 3.
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            // Signing the transaction.
            SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Stage 4.
            progressTracker.setCurrentStep(GATHERING_SIGS);
            // Creating a session with the other party.
            FlowSession otherPartySession = initiateFlow(otherParty);
            // Obtaining the counterparty's signature.
            SignedTransaction fullySignedTx = subFlow(
                new CollectSignaturesFlow(signedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.tracker())
            );

            // Stage 5.
            progressTracker.setCurrentStep(FINALISING_TRANSACTION);
            // Finalising the transaction.
            return subFlow(new FinalityFlow(fullySignedTx, otherPartySession));
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<SignedTransaction> {
        private final FlowSession otherPartySession;

        public Responder(FlowSession otherPartySession) {
            this.otherPartySession = otherPartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartySession, ProgressTracker progressTracker) {
                    super(otherPartySession, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    requireThat(require -> {
                        ContractState output = stx.getTx().getOutputs().get(0).getData();
                        require.using("This must be an IOU transaction.", output instanceof IOUState);
                        IOUState iou = (IOUState) output;
                        require.using("The IOU's value can't be too high.", iou.getValue() < 100);
                        return null;
                    });
                }
            }

            SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
            SecureHash expectedTxId = subFlow(signTxFlow).getId();
            return subFlow(new ReceiveFinalityFlow(otherPartySession, expectedTxId));
        }
    }
}
