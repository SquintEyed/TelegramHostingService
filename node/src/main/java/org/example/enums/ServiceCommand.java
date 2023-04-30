package org.example.enums;

public enum ServiceCommand {

    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start"),
    HELP("/help");

    private final String cmd;

    ServiceCommand(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {

        return cmd;
    }

    public static ServiceCommand fromValue(String v) {

        for(ServiceCommand command : ServiceCommand.values()) {

            if (command.cmd.equals(v)) {
                return command;
            }
        }
        return null;
    }
}
