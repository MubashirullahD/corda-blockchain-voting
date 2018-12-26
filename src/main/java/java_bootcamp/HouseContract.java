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

public class HouseContract implements Contract
{
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        // Keeping it empty means it allows everything
        // The below code means nothing is acceptable
        // throw new IllegalArgumentException("Accepts nothing");

        if (tx.getCommands().size() != 1)
        {
            throw new IllegalArgumentException("Transaction must have one command");
        }

        Command command = tx.getCommand(0);
        List<PublicKey> requiredSigners = command.getSigners();
        CommandData commandType = command.getValue();

        if (commandType instanceof Register)
        {
            // "Shape" constraints.
            if (tx.getInputStates().size() != 0)
            {
                throw new IllegalArgumentException("Registration transaction must have no inputs.");
            }
            if (tx.getOutputStates().size() != 1)
            {
                throw new IllegalArgumentException("Registration transaction must have one output.");
            }

            // Content constraints
            ContractState outputState = tx.getOutput(0);
            if(!(outputState instanceof HouseState))
            {
                throw new IllegalArgumentException("Output must be a HouseState");
            }
            HouseState houseState = (HouseState)outputState;
            if (houseState.getAddress().length() <= 3)
            {
                throw new IllegalArgumentException("Address must be longer than 3 characters.");
            }
            if (houseState.getOwner().getName().getCountry().equals("Israel"))
            {
                throw new IllegalArgumentException("Not a legitimate country. No hard feelings.");
            }

            // Required signer constraints.
            Party owner = houseState.getOwner();
            PublicKey ownerskey = owner.getOwningKey();
            if (!(requiredSigners.contains(ownerskey)))
                throw new IllegalArgumentException("Owner of the house must sign registration.");

        }
        else if (commandType instanceof Transfer)
        {
            // "Shape" constraints
            if (tx.getInputStates().size() != 1)
            {
                throw new IllegalArgumentException("Must have one input.");
            }
            if (tx.getOutputStates().size() != 1)
            {
                throw new IllegalArgumentException("Must have one output.");
            }


            // Content constraints
            ContractState input = tx.getInput(0);
            ContractState output = tx.getOutput(0);

            if(!(input instanceof HouseState))
                throw new IllegalArgumentException("Input state must be instance of HouseState");
            if(!(output instanceof HouseState))
                throw new IllegalArgumentException("Output state must be instance of HouseState");

            HouseState inputHouse = (HouseState) input;
            HouseState outputHouse = (HouseState) output;

            if(!(inputHouse.getAddress().equals(outputHouse.getAddress())))
                throw new IllegalArgumentException("In a transfer, the address can not change");
            if(!(inputHouse.getOwner().equals(outputHouse.getOwner())))
                throw new IllegalArgumentException("In a transfer, the owner must change.");


            // Required signer constraints
            Party newOwner = outputHouse.getOwner();
            Party oldOwner = inputHouse.getOwner();

            PublicKey newOwnerKey = newOwner.getOwningKey();
            PublicKey oldOwnerKey = oldOwner.getOwningKey();

            if(!(requiredSigners.contains(newOwnerKey)))
                throw new IllegalArgumentException("New owner must sign transaction.");
            if (!(requiredSigners.contains(oldOwnerKey)))
                throw new IllegalArgumentException("Old owner must sign transaction.");
        }
        else
        {
            throw new IllegalArgumentException("Command type not recognised");
        }
    }

    public static class Register implements CommandData {}
    public static class Transfer implements CommandData {}

}
