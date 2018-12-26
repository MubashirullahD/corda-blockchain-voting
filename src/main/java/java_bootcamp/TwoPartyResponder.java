package java_bootcamp;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.FlowSession;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.node.NodeInfo;
import net.corda.core.node.ServiceHub;

import java.util.List;

@InitiatedBy(TwoPartyFlow.class)
public class TwoPartyResponder extends FlowLogic<Void>
{
    private FlowSession counterPartySession;

    public TwoPartyResponder(FlowSession counterPartySession)
    {
        this.counterPartySession = counterPartySession;
    }

    @Suspendable
    public Void call() throws FlowException {
        ServiceHub serviceHub = getServiceHub();

        List<StateAndRef<HouseState>> statesFromVault =
                serviceHub.getVaultService().queryBy(HouseState.class).getStates();

        CordaX500Name aliceName = new CordaX500Name("Alice","Manchester","UK");
        NodeInfo alice = serviceHub.getNetworkMapCache().getNodeByLegalName(aliceName);

        int platformVersion = serviceHub.getMyInfo().getPlatformVersion();

        int recieveInt = counterPartySession.receive(Integer.class).unwrap(it -> {
            if (it > 3) throw new IllegalArgumentException("Number too high");
            return it;
            });

        int recievedIntPlusOne = recieveInt + 1;

        counterPartySession.send(recievedIntPlusOne);

        return null;
    }
}
