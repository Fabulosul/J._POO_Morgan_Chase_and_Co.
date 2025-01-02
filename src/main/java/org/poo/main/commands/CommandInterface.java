package org.poo.main.commands;

public interface CommandInterface {
    /**
     * Method designed to be overridden by the subclasses
     * of the Command Class that implement the CommandInterface
     * to make a specific action depending on the command.
     * It was created to be used in the Command Pattern.
     */
    void execute();
}
