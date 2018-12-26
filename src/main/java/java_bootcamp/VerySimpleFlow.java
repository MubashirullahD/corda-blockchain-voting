package java_bootcamp;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;

@InitiatingFlow
//@InitiatedBy()
@StartableByRPC
public class VerySimpleFlow extends FlowLogic<Integer> {

    @Suspendable
    public Integer call() throws FlowException {
        int a = 1;
        int b = 2;

        return a + b;
    }

    public Integer returnOne()
    {
        return 1;
    }
}
