
package java_bootcamp;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.confidential.SwapIdentitiesFlow;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public class BallotTransferFlow extends FlowLogic<SignedTransaction> {
    private final Party owner;

    public BallotTransferFlow(Party owner)
    {
        this.owner = owner;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }


    @Suspendable
    public SignedTransaction call() throws FlowException {
        // We get a reference to our own identity.
        AbstractParty issuer = getOurIdentity();

        // We extract all the `VoteStates from the vault.
        List<StateAndRef<VoteState>> voteStateAndRefs = getServiceHub().getVaultService().queryBy(VoteState.class).getStates();

        // We find the `VoteState` of the issuer.
        StateAndRef<VoteState> inputVoteStateAndRef = voteStateAndRefs
                .stream().filter(voteStateAndRef -> {
                    VoteState voteState = voteStateAndRef.getState().getData();
                    return voteState.getOwner().equals(issuer) && voteState.getVote()==1;
                }).findAny().orElseThrow(() -> new IllegalArgumentException("The Ballot State for this owner was not found."));
        VoteState inputVoteState = inputVoteStateAndRef.getState().getData();


        // We find the `VoteState` of the candidate.
        StateAndRef<VoteState> inputVoteStateAndRef1 = voteStateAndRefs
                .stream().filter(voteStateAndRef -> {
                    VoteState voteState = voteStateAndRef.getState().getData();
                    return voteState.getOwner().equals(owner);
                }).findAny().orElseThrow(() -> new IllegalArgumentException("The Candidate State for this owner was not found."));
        VoteState oldCandidateState = inputVoteStateAndRef1.getState().getData();


        // We throw an exception if the flow was not started by the ballot's current owner.
        if (!(issuer.equals(inputVoteState.getOwner())))
            throw new IllegalStateException("This flow must be started by the current owner.");

        // We use the notary used by the input state.
        Party notary = inputVoteStateAndRef1.getState().getNotary();

        // We build a transaction using a `TransactionBuilder`.
        TransactionBuilder txBuilder = new TransactionBuilder(notary);


        VoteContract.Commands.Transfer commandData = new VoteContract.Commands.Transfer();
        final HashMap<Party, AnonymousParty> txKeys = subFlow(new SwapIdentitiesFlow(owner));
        if (txKeys.size() != 2) {
            throw new IllegalStateException("Something went wrong when generating confidential identities.");
        } else if (!txKeys.containsKey(getOurIdentity())) {
            throw new FlowException("Couldn't create our conf. identity.");
        } else if (!txKeys.containsKey(owner)) {
            throw new FlowException("Couldn't create lender's conf. identity.");
        }

        final AnonymousParty anonymousMe = txKeys.get(getOurIdentity());
        final AnonymousParty anonymousLender = txKeys.get(owner);

        // New Candidate State
        VoteState newCandidateState = new VoteState(oldCandidateState.getVote()+1, anonymousLender, anonymousMe);

        txBuilder.addInputState(inputVoteStateAndRef);
        txBuilder.addOutputState(newCandidateState, VoteContract.ID );
        List<PublicKey> requiredSigners = ImmutableList.of(inputVoteState.getOwner().getOwningKey());

        txBuilder.addCommand(commandData, requiredSigners);

        // We check that the transaction builder we've created meets the
        // contracts of the input and output states.
        txBuilder.verify(getServiceHub());


        // We sign the transaction with our private key, making it immutable.
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(txBuilder);

        // We get the transaction notarised and recorded automatically by the platform.
        return subFlow(new FinalityFlow(signedTransaction));


    }
}