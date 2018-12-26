package java_bootcamp;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.List;

public class ContainerState implements ContractState
{
    private int width;
    private int height;
    private int depth;

    private String contents;

    private Party owner;
    private Party carrier;

    public ContainerState(int width, int height, int depth, String contents, Party owner, Party carrier) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.contents = contents;
        this.owner = owner;
        this.carrier = carrier;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getDepth()
    {
        return depth;
    }

    public String getContents()
    {
        return contents;
    }

    public Party getOwner()
    {
        return owner;
    }

    public Party getCarrier()
    {
        return carrier;
    }


    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner, carrier);
    }

    public static void main(String[] args)
    {
        Party SirKalleem = null;
        Party AliBaba = null;

        ContainerState container = new ContainerState(
                2,
                4,
                2,
                "Arduinos",
                SirKalleem,
                AliBaba);


    }

}
