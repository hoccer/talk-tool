package com.hoccer.talk.tool.command;

import better.cli.annotations.CLICommand;
import com.hoccer.talk.tool.TalkToolContext;
import com.hoccer.talk.tool.client.TalkToolClient;
import com.hoccer.talk.tool.client.TalkToolClientCommand;

@CLICommand(name = "cgcreate", description = "Creates a new group")
public class ClientGroupCreate extends TalkToolClientCommand {
    @Override
    public void runOnClient(TalkToolContext context, TalkToolClient client) {
        client.getClient().createGroup();
    }
}