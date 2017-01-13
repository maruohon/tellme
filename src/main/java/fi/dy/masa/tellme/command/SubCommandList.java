package fi.dy.masa.tellme.command;

public class SubCommandList extends SubCommandDump
{
    public SubCommandList(CommandTellme baseCommand)
    {
        super(baseCommand);
    }

    @Override
    public String getName()
    {
        return "list";
    }
}
