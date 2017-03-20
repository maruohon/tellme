package fi.dy.masa.tellme.command;

public class SubCommandListCsv extends SubCommandDump
{
    public SubCommandListCsv(CommandTellme baseCommand)
    {
        super(baseCommand);
    }

    @Override
    public String getName()
    {
        return "list-csv";
    }
}
