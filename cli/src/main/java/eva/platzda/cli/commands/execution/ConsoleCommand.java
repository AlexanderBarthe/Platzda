package eva.platzda.cli.commands.execution;

public interface ConsoleCommand {

    String command();
    String executeCommand(String[] args) throws Exception;

}
