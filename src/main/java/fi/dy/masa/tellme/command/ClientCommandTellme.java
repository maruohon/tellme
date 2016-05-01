package fi.dy.masa.tellme.command;

public class ClientCommandTellme extends CommandTellme
{
    @Override
    public String getCommandName()
    {
        return "ctellme";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
}
