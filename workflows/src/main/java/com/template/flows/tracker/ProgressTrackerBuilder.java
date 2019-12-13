package com.template.flows.tracker;

import net.corda.core.flows.CollectSignaturesFlow;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

public class ProgressTrackerBuilder {

    public static final Step GENERATING_TRANSACTION = new Step("Generating transaction based on new state.");
    public static final Step VERIFYING_TRANSACTION = new Step("Verifying contract constraints.");
    public static final Step SIGNING_TRANSACTION = new Step("Signing transaction with our private key.");
    public static final Step GATHERING_SIGS = new Step("Gathering the counterparty's signatures.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return CollectSignaturesFlow.Companion.tracker();
        }
    };
    public static final Step FINALISING_TRANSACTION = new Step("Obtaining notary signature and recording transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.Companion.tracker();
        }
    };

    /**
     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
     */
    public static ProgressTracker build() {
        return new ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION
        );
    }
}
