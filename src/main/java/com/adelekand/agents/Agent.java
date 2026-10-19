package com.adelekand.agents;

import com.adelekand.messages.*;
import com.adelekand.models.Model;
import com.adelekand.tools.Tool;
import com.adelekand.tools.ToolCall;
import com.adelekand.tools.ToolSpec;
import com.adelekand.tools.Tools;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class Agent implements RunnableEntity {
    protected final Model model;
    protected String name = "agent";
    protected String description = "";
    protected String instructions;
    protected final Map<String, Tool> tools = new LinkedHashMap<>();
    protected int maxSteps = 10;

    public Agent(Model model) {
        this.model = model;
    }

    public Agent name(String n) { this.name = n; return this; }
    public Agent description(String d) { this.description = d; return this; }
    public Agent instructions(String i) { this.instructions = i; return this; }
    public Agent maxSteps(int n) { this.maxSteps = n; return this; }
    public Agent tool(Tool t) { tools.put(t.spec().name(), t); return this; }
    public Agent tools(Object target) { Tools.from(target).forEach(this::tool); return this; }

    @Override public String name() { return name; }
    @Override public String description() { return description; }

    @Override
    public RunOutput run(RunInput req) { return runStream(req, e -> {}); }

    public RunOutput runStream(RunInput req, Consumer<AgentEvent> onEvent) {
        RunContext ctx = start(req);
        List<ToolSpec> specs = tools.values().stream().map(Tool::spec).toList();
        Usage usage = Usage.ZERO;

        for (int step = 0; step < maxSteps; step++) {
            StringBuilder text = new StringBuilder();
            TreeMap<Integer, PartialCall> partial = new TreeMap<>();
            Usage[] stepUsage = { Usage.ZERO };

            model.chatStream(ctx.messages, specs, chunk -> {
                switch (chunk) {
                    case StreamChunk.Text t -> {
                        text.append(t.text());
                        onEvent.accept(new AgentEvent.Text(t.text()));
                    }
                    case StreamChunk.ToolCallDelta d ->
                            partial.computeIfAbsent(d.index(), k -> new PartialCall()).append(d);
                    case StreamChunk.UsageChunk u -> stepUsage[0] = stepUsage[0].plus(u.usage());
                }
            });
            usage = usage.plus(stepUsage[0]);

            List<ToolCall> calls = partial.values().stream().map(PartialCall::build).toList();
            ctx.messages.add(new Message.AssistantMessage(text.isEmpty() ? null : text.toString(), calls));

            if (calls.isEmpty()) {
                return complete(ctx, text.toString(), usage, onEvent);
            }

            calls.forEach(c -> onEvent.accept(new AgentEvent.ToolCallStarted(c.name(), c.arguments())));
            List<String> results = runTools(calls);

            String stopResult = null;
            for (int i = 0; i < calls.size(); i++) {
                ToolCall c = calls.get(i);
                String r = results.get(i);
                onEvent.accept(new AgentEvent.ToolCallCompleted(c.name(), r));
                ctx.messages.add(new Message.ToolMessage(c.id(), r));
                Tool t = tools.get(c.name());
                if (t != null && t.stopsRun()) stopResult = r;
            }
            if (stopResult != null) {
                return complete(ctx, stopResult, usage, onEvent);
            }
        }
        throw new AgentException("max steps (" + maxSteps + ") reached without a final answer");
    }

    private List<String> runTools(List<ToolCall> calls) {
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = calls.stream()
                    .map(c -> exec.submit(() -> invoke(c)))
                    .toList();
            return futures.stream().map(f -> {
                try { return f.get(); }
                catch (Exception e) { return "Error: " + e.getMessage(); }
            }).toList();
        }
    }

    private String invoke(ToolCall c) {
        Tool t = tools.get(c.name());
        if (t == null) return "Error: unknown tool '" + c.name() + "'";
        try { return t.call(c.arguments()); }
        catch (Exception e) { return "Error: " + e.getMessage(); }
    }

    // ---- Hooks: Part 5 replaces these to add sessions and memory ----

    protected RunContext start(RunInput req) {
        RunContext ctx = new RunContext(req);
        if (instructions != null) ctx.messages.add(new Message.SystemMessage(instructions));
        ctx.runStart = ctx.messages.size();
        ctx.messages.add(new Message.UserMessage(req.input()));
        return ctx;
    }

    protected RunOutput complete(RunContext ctx, String content, Usage usage, Consumer<AgentEvent> onEvent) {
        RunOutput out = new RunOutput(content, List.copyOf(ctx.messages), usage);
        onEvent.accept(new AgentEvent.Completed(out));
        return out;
    }
}
