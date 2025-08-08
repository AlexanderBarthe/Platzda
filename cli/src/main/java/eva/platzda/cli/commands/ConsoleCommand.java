package eva.platzda.cli.commands;

public interface ConsoleCommand {

    String command();
    int requiredArguments();
    String execute(String[] args);

}
