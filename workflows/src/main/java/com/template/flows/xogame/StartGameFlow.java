package com.template.flows.xogame;

import static com.template.flows.tracker.ProgressTrackerBuilder.FINALISING_TRANSACTION;
import static com.template.flows.tracker.ProgressTrackerBuilder.GATHERING_SIGS;
import static com.template.flows.tracker.ProgressTrackerBuilder.GENERATING_TRANSACTION;
import static com.template.flows.tracker.ProgressTrackerBuilder.SIGNING_TRANSACTION;
import static com.template.flows.tracker.ProgressTrackerBuilder.VERIFYING_TRANSACTION;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableSet;
import com.template.contracts.XoGameContract;
import com.template.contracts.XoGameContract.Commands.StartGame;
import com.template.flows.tracker.ProgressTrackerBuilder;
import com.template.states.XoGameState;
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
import org.jetbrains.annotations.NotNull;

public class StartGameFlow {

    // ******************
    // * Initiator flow *
    // ******************
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private final String gameId;
        private final Party opponent;

        public Initiator(String gameId, Party opponent) {
            this.gameId = gameId;
            this.opponent = opponent;
        }

        private final ProgressTracker progressTracker = ProgressTrackerBuilder.build();

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            Party me = getOurIdentity();

            // Step 1
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            XoGameState initialState = new XoGameState(gameId, me, opponent);

            List<PublicKey> requiredSigners = Arrays.asList(me.getOwningKey(), opponent.getOwningKey());
            final Command<StartGame> txCommand = new Command<>(new XoGameContract.Commands.StartGame(), requiredSigners);

            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(initialState, XoGameContract.ID)
                .addCommand(txCommand);

            // Step 2
            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
            // Verifying the transaction.
            txBuilder.verify(getServiceHub());

            // Stage 3.
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            // Signing the transaction.
            SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Stage 4.
            progressTracker.setCurrentStep(GATHERING_SIGS);
            // Creating a session with the other party.
            FlowSession otherPartySession = initiateFlow(opponent);
            // Obtaining the counterparty's signature.
            SignedTransaction fullySignedTx = subFlow(
                new CollectSignaturesFlow(partSignedTx, ImmutableSet.of(otherPartySession), CollectSignaturesFlow.Companion.tracker())
            );

            // Stage 5.
            progressTracker.setCurrentStep(FINALISING_TRANSACTION);
            // Finalising the transaction.
            return subFlow(new FinalityFlow(fullySignedTx, ImmutableSet.of(otherPartySession)));
        }
    }

    // ******************
    // * Responder flow *
    // ******************
    @InitiatedBy(StartGameFlow.Initiator.class)
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
                protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
                    ContractState outputState = stx.getTx().getOutputs().get(0).getData();
                    if (!(outputState instanceof XoGameState)) {
                        throw new FlowException("Wrong output state type");
                    }
                }
            }

            SignTxFlow signTxFlow = new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker());
            SecureHash expectedTxId = subFlow(signTxFlow).getId();
            return subFlow(new ReceiveFinalityFlow(otherPartySession, expectedTxId));
        }
    }
}
