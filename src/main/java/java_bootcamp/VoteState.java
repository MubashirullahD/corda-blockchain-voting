package java_bootcamp;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VoteState implements ContractState {

    // The attributes that will be stored on the ledger as part of the state.
    private final int vote;
    private final AbstractParty owner;
    private final AbstractParty issuer;

    // The constructor used to create an instance of the state.
    public VoteState(int vote, AbstractParty owner, AbstractParty issuer)
    {
        this.vote   = vote;
        this.owner  = owner;
        this.issuer = issuer;
    }


    // Overrides `participants`, the only field defined by `ContractState`.
    // Defines which parties will store the state.
    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner, issuer);
    }


    // Getters for the state's attributes.
    public int getVote() {
        return vote;
    }

    public AbstractParty getOwner() {
        return owner;
    }

    public AbstractParty getIssuer() { return issuer; }
}
