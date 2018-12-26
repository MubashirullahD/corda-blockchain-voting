package java_bootcamp;

import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

/* Our contract, governing how our state will evolve over time.
 * See src/main/java/examples/ArtContract.java for an example. */
public class TokenContract implements Contract{
    public static String ID = "java_bootcamp.TokenContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        if(tx.getCommands().size() != 1)
            throw new IllegalArgumentException("Transaction must have one command.");

        Command command = tx.getCommand(0);
        List<PublicKey> requiredSigners = command.getSigners();
        CommandData commandType = command.getValue();

        if (commandType instanceof Commands.Issue)
        {
            // "Shape"
            if(tx.getInputStates().size() != 0)
                throw new IllegalArgumentException("Issue transaction must have no inputs.");
            if(tx.getOutputStates().size() != 1)
                throw new IllegalArgumentException("Issue transaction must have one output.");

            // Content
            ContractState outputState = tx.getOutput(0);
            if(!(outputState instanceof TokenState))
                throw new IllegalArgumentException("Output state must be of type TokenState.");
            TokenState tokenState = (TokenState) outputState;
            if (!(tokenState.getAmount() > 0))
                throw new IllegalArgumentException("Output state must have amount greater than 0.");

            // Signers
            Party issuer = tokenState.getIssuer();
            PublicKey issuerKey = issuer.getOwningKey();

            if(!(requiredSigners.contains(issuerKey)))
                throw new IllegalArgumentException("Issuer must sign the transaction.");


        }
        else
            throw new IllegalArgumentException("Command Type not instance of Issue.");
    }

    public interface Commands extends CommandData {
        class Issue implements Commands { }
    }
}