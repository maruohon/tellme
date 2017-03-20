package fi.dy.masa.tellme.command;

public class SubCommandDumpCsv extends SubCommandDump
{
    public SubCommandDumpCsv(CommandTellme baseCommand)
    {
        super(baseCommand);
    }

    @Override
    public String getName()
    {
        return "dump-csv";
    }
}
