package fi.dy.masa.tellme.command.argument;

import java.util.Arrays;
import fi.dy.masa.tellme.command.SubCommandLoaded.Grouping;

public class GroupingArgument extends EnumArgument<Grouping>
{
    public GroupingArgument()
    {
        super(Arrays.asList(Grouping.values()), Grouping::fromArgument, Grouping::getArgument);
    }

    public static GroupingArgument create()
    {
        return new GroupingArgument();
    }
}
