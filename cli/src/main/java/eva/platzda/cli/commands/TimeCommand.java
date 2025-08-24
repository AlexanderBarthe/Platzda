package eva.platzda.cli.commands;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.commands.scripts.ScriptLoader;
import eva.platzda.cli.notification_management.SubscriptionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TimeCommand implements ConsoleCommand {

    private final SubscriptionService subscriptionService;
    private final ScriptLoader scriptLoader;

    private static final List<Long> responseTimes_us = Collections.synchronizedList(new ArrayList<>());

    public TimeCommand(SubscriptionService subscriptionService, ScriptLoader scriptLoader) {
        this.subscriptionService = subscriptionService;
        this.scriptLoader = scriptLoader;
    }


    @Override
    public String command() {
        return "time";
    }

    @Override
    public String executeCommand(String[] args) {

        if(args.length < 1) throw new IllegalArgumentException("Not enough arguments");

        if(args[0].equals("--flush")) {
            flushResponseTimes();
            return "Flushed.";
        }
        else if(args[0].equals("--avg")) {
            return "Average response times of recent requests: " + (float)getAverageResponseTime()/1000 + " ms. (" + responseTimes_us.size() + " data points)";
        }
        else if(args[0].equals("--mdn") || args[0].equals("--med")) {
            return "Median response time of recent requests: " + (float)getMedianResponseTime()/1000 + " ms. (" + responseTimes_us.size() + " data points)";
        }
        else if(args[0].equals("--max")) {
            return "Highest response time of recent requests: " + (float)getHighestResponseTime()/1000 + " ms. (" + responseTimes_us.size() + " data points)";
        }
        else if(args[0].startsWith("--")) {
            throw new IllegalArgumentException("Unknown option: " + args[0]);
        }


        ConsoleManager executor = new ConsoleManager(subscriptionService, scriptLoader);
        String command = String.join(" ", args);

        long start = System.nanoTime();

        executor.runCommand(command);

        long end = System.nanoTime();
        long time_us = (end - start) / 1000;

        appendResponseTime(time_us);

        return String.format("Execution time: %f ms", ((float)time_us)/1000);

    }

    public void appendResponseTime(long responseTime) {
        responseTimes_us.add(responseTime);
    }

    public List<Long> getResponseTimes() {
        return responseTimes_us;
    }

    public long getAverageResponseTime() {
        if(responseTimes_us.isEmpty()) return 0;

        long amount = 0;
        long sum = 0;
        for(Long responseTime : responseTimes_us) {
            sum += responseTime;
            amount++;
        }
        return sum / amount;
    }

    public long getMedianResponseTime() {
        if(responseTimes_us.isEmpty()) return 0;
        return responseTimes_us.stream().sorted().toList().get(responseTimes_us.size()/2);
    }

    public long getHighestResponseTime() {
        if(responseTimes_us.isEmpty()) return 0;
        return responseTimes_us.stream().max(Long::compareTo).get();
    }

    public void flushResponseTimes() {
        responseTimes_us.clear();
    }

}
