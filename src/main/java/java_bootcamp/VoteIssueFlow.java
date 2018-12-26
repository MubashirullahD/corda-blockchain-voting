package java_bootcamp;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public class VoteIssueFlow extends FlowLogic<SignedTransaction> {
    private final int vote;
    private final AbstractParty owner;

    public VoteIssueFlow(AbstractParty owner)
    {
        this.owner = owner;
        this.vote = 0;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        // We choose our transaction's notary (the notary prevents double-spends).
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        // We get a reference to our own identity.
        AbstractParty issuer = getOurIdentity();

        // We create our new VoteState for the individual voter.
        VoteState voteState = new VoteState(vote, owner, issuer);

        // We build our transaction.
        TransactionBuilder transactionBuilder = new TransactionBuilder();
        transactionBuilder.setNotary(notary);
        transactionBuilder.addOutputState(voteState, VoteContract.ID);


        // We add the Issue command to the transaction.
        // Note that we also specific who is required to sign the transaction.

        VoteContract.Commands.IssueVote commandData = new VoteContract.Commands.IssueVote();
        List<PublicKey> requiredSigners = ImmutableList.of(issuer.getOwningKey());

        transactionBuilder.addCommand(commandData, requiredSigners);

        // We check our transaction is valid based on its contracts.
        transactionBuilder.verify(getServiceHub());

        // We sign the transaction with our private key, making it immutable.
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

        // We get the transaction notarised and recorded automatically by the platform.
        return subFlow(new FinalityFlow(signedTransaction));

    }
}
