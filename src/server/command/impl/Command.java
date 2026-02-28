package server.command.impl;

import server.command.response.CommandResponse;
import server.session.ClientSession;

public interface Command {
    CommandResponse<?> execute(String[] args, ClientSession session);
}