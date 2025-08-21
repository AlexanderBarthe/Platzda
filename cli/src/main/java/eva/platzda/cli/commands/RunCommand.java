package eva.platzda.cli.commands;

import eva.platzda.cli.ExpressionEvaluator;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.websockets.SubscriptionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunCommand implements ConsoleCommand {

    private final SubscriptionService subscriptionService;


    private static final int THREAD_COUNT = 12;
    private static final Pattern BRACKET_EXPR = Pattern.compile("\\[([^\\]]+)]");
    private static final ConcurrentMap<String, List<String>> RPN_CACHE = new ConcurrentHashMap<>();

    public RunCommand(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }


    @Override
    public String command() {
        return "run";
    }

    @Override
    public String executeCommand(String[] args) {

        if(args.length < 3) {
            return "Not enough arguments provided. See 'help run' for more information.";
        }

        int executionCount;

        try {
            executionCount = Integer.parseInt(args[0]);
            if(executionCount < 1) throw new NumberFormatException();

        } catch (NumberFormatException e) {
            return "Please enter a valid number.";
        }

        boolean runParallel;

        if(args[1].equals("p") || args[1].equals("1") || args[1].equals("true") || args[1].equals("t")) {
            runParallel = true;
        }
        else if(args[1].equals("r") || args[1].equals("0") || args[1].equals("false")  || args[1].equals("f")) {
            runParallel = false;
        }
        else {
            return "Please sepcify parellelisation";
        }

        if(runParallel) {
            formatAndRunParallel(executionCount, Arrays.copyOfRange(args, 2, args.length), THREAD_COUNT, false);
        }
        else {
            formatAndRun(executionCount, Arrays.copyOfRange(args, 2, args.length));
        }

        return "Execution finished!";

    }

    public void formatAndRun(int executionCount, String[] args) {
        ConsoleManager consoleManager = new ConsoleManager(subscriptionService);

        for (int i = 0; i < executionCount; i++) {
            String[] processedArgs = new String[args.length];
            for (int a = 0; a < args.length; a++) {
                processedArgs[a] = replaceBracketExpressions(args[a], i);
            }

            // join and run the command as before (or pass processedArgs if runCommand accepts array)
            consoleManager.runCommand(String.join(" ", processedArgs));
        }
    }

    private static String replaceBracketExpressions(String arg, long xValue) {
        Matcher m = BRACKET_EXPR.matcher(arg);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String expr = m.group(1);
            long val = ExpressionEvaluator.evaluate(expr, xValue);
            m.appendReplacement(sb, Matcher.quoteReplacement(Long.toString(val)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Parallel version with explicit control.
     *
     * @param executionCount number of iterations (i from 0..executionCount-1)
     * @param args command arguments (may contain [expr] occurrences)
     * @param threadPoolSize number of worker threads to use
     * @param serializeRunCommand if true, calls to consoleManager.runCommand(...) are synchronized
     */
    public void formatAndRunParallel(int executionCount, String[] args, int threadPoolSize, boolean serializeRunCommand) {
        ConsoleManager consoleManager = new ConsoleManager(subscriptionService);

        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, threadPoolSize));
        List<Future<?>> futures = new ArrayList<>(executionCount);

        for (int i = 0; i < executionCount; i++) {
            final int idx = i;
            // submit task for this iteration
            futures.add(pool.submit(() -> {
                try {
                    String[] processedArgs = new String[args.length];
                    for (int a = 0; a < args.length; a++) {
                        processedArgs[a] = replaceBracketExpressionsUsingCache(args[a], idx);
                    }
                    Runnable runCmd = () -> consoleManager.runCommand(String.join(" ", processedArgs));
                    if (serializeRunCommand) {
                        // serialize calls to runCommand if it's not thread-safe
                        synchronized (consoleManager) {
                            runCmd.run();
                        }
                    } else {
                        runCmd.run();
                    }
                } catch (RuntimeException ex) {
                    // rethrow to surface in Future.get()
                    throw ex;
                }
            }));
        }

        // wait for all tasks and propagate exceptions (if any)
        pool.shutdown();
        try {
            for (Future<?> f : futures) {
                f.get(); // will throw ExecutionException if any task failed
            }
            // optionally wait termination (should be almost immediate after all futures done)
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                // if still not terminated, force shutdown
                pool.shutdownNow();
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            pool.shutdownNow();
            throw new RuntimeException("Interrupted while waiting for tasks", ie);
        } catch (ExecutionException ee) {
            // unwrap and rethrow cause for clarity
            Throwable cause = ee.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            else throw new RuntimeException("Task failed", cause);
        }
    }

    // replace occurrences of [expr] in arg using a parsed-RPN cache
    private static String replaceBracketExpressionsUsingCache(String arg, long xValue) {
        Matcher m = BRACKET_EXPR.matcher(arg);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String expr = m.group(1);
            // get parsed RPN from cache or parse and put
            List<String> rpn = RPN_CACHE.computeIfAbsent(expr, ExpressionEvaluator::parseToRPN);
            long val = ExpressionEvaluator.evalRPNWithX(rpn, xValue);
            m.appendReplacement(sb, Matcher.quoteReplacement(Long.toString(val)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

}
