package com.hoccer.talk.tool.command;

import better.cli.annotations.CLICommand;
import com.beust.jcommander.Parameter;
import com.hoccer.talk.tool.TalkToolContext;
import com.hoccer.talk.tool.client.TalkToolClient;
import com.hoccer.talk.tool.client.TalkToolClientCommand;

import java.util.List;

@CLICommand(name = "csetstatus", description = "Set the status of a client")
public class ClientSetStatus extends TalkToolClientCommand {

    @Parameter(description = "<status>", required = true)
    List<String> pStatus;

    @Override
    public void runOnClient(TalkToolContext context, TalkToolClient client) {
        client.getClient().setClientString(null, pStatus.get(0));
    }

}
