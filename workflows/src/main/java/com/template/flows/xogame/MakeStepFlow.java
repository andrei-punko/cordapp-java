package com.template.flows.xogame;

import static com.template.flows.tracker.ProgressTrackerBuilder.FINALISING_TRANSACTION;
import static com.template.flows.tracker.ProgressTrackerBuilder.GATHERING_SIGS;
import static com.template.flows.tracker.ProgressTrackerBuilder.GENERATING_TRANSACTION;
import static com.template.flows.tracker.ProgressTrackerBuilder.SIGNING_TRANSACTION;
import static com.template.flows.tracker.ProgressTrackerBuilder.VERIFYING_TRANSACTION;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.template.contracts.XoGameContract;
import com.template.contracts.XoGameContract.Commands.StartGame;
import com.template.flows.tracker.ProgressTrackerBuilder;
import com.template.model.XoGameField;
import com.template.states.XoGameState;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import kotlin.Pair;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
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
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

public class MakeStepFlow {

    // ******************
    // * Initiator flow *
    // ******************
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private final String gameId;
        private final Party opponent;
        private final String newField;

        public Initiator(String gameId, Party opponent, String newField) {
            this.gameId = gameId;
            this.opponent = opponent;
            this.newField = newField;
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
            QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null, //ImmutableList.of(me, opponent),
                null,
                ImmutableList.of(gameId),
                Vault.StateStatus.UNCONSUMED,
                null //ImmutableSet.of(XoGameState.class)
                );

            Vault.Page<XoGameState> results = getServiceHub().getVaultService().queryBy(XoGameState.class, queryCriteria);
            List<StateAndRef<XoGameState>> states = results.getStates();
            if (states.isEmpty()) {
                throw new IllegalArgumentException("Required state not found");
            } else if (states.size() > 1) {
                throw new IllegalArgumentException("There are more than one required state");
            }
            StateAndRef<XoGameState> inputStateAndRef = states.get(0);
            XoGameState inputState = inputStateAndRef.getState().getData();
            getLogger().info(inputState.toString());
            XoGameState outputState = new XoGameState(gameId, me, opponent, new XoGameField(newField));;
            getLogger().info(outputState.toString());

            final Command<XoGameContract.Commands.MakeStep> txCommand = new Command<>(
                new XoGameContract.Commands.MakeStep(),
                ImmutableList.of(me.getOwningKey(), opponent.getOwningKey()));

            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addInputState(inputStateAndRef)
                .addOutputState(outputState, XoGameContract.ID)
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
    @InitiatedBy(MakeStepFlow.Initiator.class)
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
                        require.using("This must be an XoGameState transaction.", output instanceof XoGameState);
                        XoGameState xoGameState = (XoGameState) output;
                        // TODO: add required checks

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
