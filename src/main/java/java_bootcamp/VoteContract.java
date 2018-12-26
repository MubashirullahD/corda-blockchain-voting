package java_bootcamp;

import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;
import java.util.stream.Collectors;

public class VoteContract implements Contract {
    // Used to reference the contract in transactions.
    public static final String ID = "java_bootcamp.VoteContract";

    public interface Commands extends CommandData
    {
        class IssueVote implements Commands {}
        class IssueBallot implements Commands{}
        class Transfer implements Commands {}
    }


    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        if (tx.getCommands().size() != 1)
        {
            throw new IllegalArgumentException("Transaction must have one command");
        }

        Command command = tx.getCommand(0);
        CommandData commandType = command.getValue();
        List<PublicKey> requiredSigners = command.getSigners();

        if(commandType instanceof Commands.IssueVote)
        {
            // "Shape" constraints
            if(tx.getInputStates().size() != 0)
                throw new IllegalArgumentException("Issue transaction must have zero input states.");

            if(tx.getOutputStates().size() != 1)
                throw new IllegalArgumentException("Issue transaction must have one output state.");

            // Content constraints
            ContractState outputState = tx.getOutput(0);

            if(!(outputState instanceof VoteState))
                throw new IllegalArgumentException("Output state must be a VoteState.");

            VoteState voteState = (VoteState) outputState;

            if(voteState.getVote() != 0)
                throw new IllegalArgumentException("VoteState should have zero votes during issue.");

            if(voteState.getIssuer() == voteState.getOwner())
                throw new IllegalArgumentException("Cannot issue vote to self.");

            if(voteState.getIssuer().nameOrNull().getOrganisation() != "PartyA")
                throw new IllegalArgumentException("Issuer must be partyA only.");

            // Required Signers constraints
            if(!(requiredSigners.contains(voteState.getIssuer().getOwningKey())))
                throw new IllegalArgumentException("Issuer must sign the transaction.");

        }
        else if(commandType instanceof Commands.IssueBallot)
        {
            // "Shape" constraints
            if(tx.getInputStates().size() != 0)
                throw new IllegalArgumentException("Issue transaction must have zero input states.");

            if(tx.getOutputStates().size() != 1)
                throw new IllegalArgumentException("Issue transaction must have one output state.");

            // Content constraints
            ContractState outputState = tx.getOutput(0);

            if(!(outputState instanceof VoteState))
                throw new IllegalArgumentException("Output state must be a VoteState.");

            VoteState voteState = (VoteState) outputState;

            if(voteState.getVote() != 1)
                throw new IllegalArgumentException("VoteState should have one vote potential during issue.");

            if(voteState.getIssuer() == voteState.getOwner())
                throw new IllegalArgumentException("Cannot issue vote to self.");

            if(voteState.getIssuer().nameOrNull().getOrganisation() != "PartyA")
                throw new IllegalArgumentException("Issuer must be partyA only.");

            // Required Signer constraints
            if(!(requiredSigners.contains(voteState.getIssuer().getOwningKey())))
                throw new IllegalArgumentException("Issuer must sign the transaction.");

        }
        else if(commandType instanceof Commands.Transfer)
        {
            // "shape" constraints
            if(tx.getInputStates().size() != 1)
                throw new IllegalArgumentException("Transfer transaction must have two input states.");

            if(tx.getOutputStates().size() != 1)
                throw new IllegalArgumentException("Transfer transaction must have two output states.");

            // Content constraints
            ContractState inputStateOne = tx.getInput(0);

            ContractState outputStateOne = tx.getOutput(0);

            if(!(inputStateOne instanceof VoteState ))
                throw new IllegalArgumentException("Input states must be VoteState.");

            if(!(outputStateOne instanceof VoteState))
                throw new IllegalArgumentException("Output states must be VoteState.");

            VoteState ballotInput  = (VoteState) inputStateOne;
            VoteState CandidateOutput = (VoteState) outputStateOne;


            if(ballotInput.getVote() != 1)
                throw new IllegalArgumentException("Invalid number of ballot/s to cast.");

            if(CandidateOutput.getVote() < 0)
                throw new IllegalArgumentException("Number of final votes can not be negative.");


            //  Signature constraints
            //if (requiredSigners.containsAll(CandidateOutput.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList())))
            //    throw new IllegalArgumentException("All of the participants must be signers.");
            if(!(requiredSigners.contains(ballotInput.getOwner().getOwningKey())))
                throw new IllegalArgumentException("Issuer must sign the transaction.");


        }
        else
            throw new IllegalArgumentException("Command not recognised");

    }
}
