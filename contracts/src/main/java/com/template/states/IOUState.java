package com.template.states;

import com.template.contracts.IOUContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;

import java.util.Arrays;
import java.util.List;
import net.corda.core.identity.Party;

// *********
// * State *
// *********
@BelongsToContract(IOUContract.class)
public class IOUState implements ContractState {

    private final int value;
    private final Party lender;
    private final Party borrower;

    public IOUState(Party lender, Party borrower, int value) {
        this.value = value;
        this.lender = lender;
        this.borrower = borrower;
    }

    public int getValue() {
        return value;
    }

    public Party getLender() {
        return lender;
    }

    public Party getBorrower() {
        return borrower;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(lender, borrower);
    }
}
