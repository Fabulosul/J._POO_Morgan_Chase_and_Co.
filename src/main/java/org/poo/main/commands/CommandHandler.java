package org.poo.main.commands;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CommandHandler {
    // The actual command object that will be executed
    private Command command;

    /**
     * Method used to execute any command that extends the Command class.
     * The execute method is implemented differently in each child class to
     * perform the desired action and since the command object is of type Command,
     * it can be used to execute any command that extends the Command class.
     */
    public void executeCommand() {
        command.execute();
    }
}
