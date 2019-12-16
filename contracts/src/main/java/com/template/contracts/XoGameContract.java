package com.template.contracts;

import static net.corda.core.contracts.ContractsDSL.requireThat;

import com.template.model.XoGameField;
import com.template.states.XoGameState;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class XoGameContract implements Contract {

    public static final String ID = "com.template.contracts.XoGameContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        List<CommandWithParties<CommandData>> commands = tx.getCommands();
        if (commands.size() != 1) {
            throw new IllegalArgumentException("Only one command required.");
        }
        CommandWithParties<CommandData> cmd = commands.get(0);
        if (cmd.getValue() instanceof Commands.StartGame) {
            verifyStartGameTransaction(tx, cmd);
        } else if (cmd.getValue() instanceof Commands.MakeStep) {
            verifyMakeStepTransaction(tx, cmd);
        } else {
            throw new IllegalArgumentException("Unknown command.");
        }
    }

    private void verifyStartGameTransaction(LedgerTransaction tx, CommandWithParties<CommandData> cmd) {
        requireThat(
            require -> {
                // Constraints on the shape of the transaction.
                require.using("No inputs should be consumed during game start.",
                    tx.getInputs().isEmpty());
                require.using("Only one output should be created during game start.",
                    tx.getOutputs().size() == 1);

                // XoGame-specific constraints.
                XoGameState out = tx.outputsOfType(XoGameState.class).get(0);
                require.using("Players should be different.",
                    !out.getPlayer1().equals(out.getPlayer2()));
                require.using("NextTurnOwner should be player1",
                    out.getNextTurnOwner().equals(out.getPlayer1()));
                require.using("Game id should be present",
                    StringUtils.isNoneBlank(out.getGameId()));
                require.using("Game field should be present",
                    out.getGameField() != null);

                // Constraints on the signers.
                checkSignersConstraint(cmd, out);
                return null;
            }
        );
    }

    private void verifyMakeStepTransaction(LedgerTransaction tx, CommandWithParties<CommandData> cmd) {
        requireThat(
            require -> {
                // Constraints on the shape of the transaction.
                require.using("Only one input expected.",
                    tx.getInputs().size() == 1);
                require.using("Only one output should be created.",
                    tx.getOutputs().size() == 1);

                // XoGame-specific constraints.
                XoGameState out = tx.outputsOfType(XoGameState.class).get(0);
                require.using("Output players should be different.",
                    !out.getPlayer1().equals(out.getPlayer2()));
                require.using("Output game id should be present",
                    StringUtils.isNoneBlank(out.getGameId()));
                require.using("Output game field should be present",
                    out.getGameField() != null);

                XoGameState in = tx.inputsOfType(XoGameState.class).get(0);
                require.using("NextTurnOwner should be different.",
                    !in.getNextTurnOwner().equals(out.getNextTurnOwner()));
                require.using("Game id should be the same",
                    in.getGameId().equals(out.getGameId()));
                require.using("Player1 should be the same",
                    in.getPlayer1().equals(out.getPlayer1()));
                require.using("Player2 should be the same",
                    in.getPlayer2().equals(out.getPlayer2()));
                require.using("Game field should be changed",
                    !in.getGameField().equals(out.getGameField()));
                require.using("Only one cell should be changed",
                    in.getGameField().checkIsOnlyOneCellChanged(out.getGameField()));

                // Constraints on the signers.
                checkSignersConstraint(cmd, out);
                return null;
            }
        );
    }

    private void checkSignersConstraint(CommandWithParties<CommandData> cmd, XoGameState out) {
        List<PublicKey> signers = cmd.getSigners();
        List<PublicKey> expectedSigners = Arrays.asList(out.getPlayer1().getOwningKey(), out.getPlayer2().getOwningKey());
        if (signers.size() != 2) {
            throw new IllegalArgumentException("There must be two signers.");
        }
        if (!signers.containsAll(expectedSigners)) {
            throw new IllegalArgumentException("Both players must be signers.");
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {

        class StartGame implements XoGameContract.Commands {

        }

        class MakeStep implements XoGameContract.Commands {

        }
    }
}
