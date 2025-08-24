package eva.platzda.cli.commands;

import eva.platzda.cli.ExpressionEvaluator;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.commands.scripts.ScriptLoader;
import eva.platzda.cli.notification_management.SubscriptionService;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunCommand implements ConsoleCommand {

    private final SubscriptionService subscriptionService;
    private final ScriptLoader scriptLoader;

    private static final int THREAD_COUNT = 12;
    private static final Pattern BRACKET_EXPR = Pattern.compile("\\[([^\\]]+)]");
    private static final ConcurrentMap<String, List<String>> RPN_CACHE = new ConcurrentHashMap<>();

    public RunCommand(SubscriptionService subscriptionService, ScriptLoader scriptLoader) {
        this.subscriptionService = subscriptionService;
        this.scriptLoader = scriptLoader;
    }


    @Override
    public String command() {
        return "run";
    }

    @Override
    public String executeCommand(String[] args) {

        Deque<String> arguments = new ArrayDeque<>(Arrays.asList(args));

        int executionCount;

        try {
            executionCount = Integer.parseInt(arguments.getFirst());
            if(executionCount < 1) throw new NumberFormatException();
            arguments.removeFirst();

        } catch (NumberFormatException e) {
            return "Please enter a valid number.";
        }

        boolean multithreaded = false;
        Integer rateLimit = null;

        while(!arguments.isEmpty() && arguments.getFirst().startsWith("--")) {
            String option = arguments.poll().substring(2);
            if(option.equals("mt")) {
                multithreaded = true;
            }
            else if (option.equals("rate")) {
                // consume next token as amount
                String amt = arguments.pollFirst();
                if (amt == null) return "Option --rate requires a numeric argument.";
                try {
                    int value = Integer.parseInt(amt);
                    if (value < 1) return "Rate must be >= 1.";
                    rateLimit = value;
                } catch (NumberFormatException nfe) {
                    return "Invalid rate value: " + amt;
                }
            }
            else {
                return "Unknown option: " + option;
            }

        }

        if(arguments.isEmpty()) return "Please enter a command.";

        if(multithreaded) {
            formatAndRunParallel(executionCount, rateLimit, arguments.toArray(new String[0]), THREAD_COUNT, false);
        }
        else {
            formatAndRun(executionCount, rateLimit, arguments.toArray(new String[0]));
        }

        return "Execution finished!";

    }

    public void formatAndRun(int executionCount, Integer rateLimit, String[] args) {
        ConsoleManager consoleManager = new ConsoleManager(subscriptionService, scriptLoader);

        TokenBucketRateLimiter limiter = null;
        if (rateLimit != null) {
            limiter = new TokenBucketRateLimiter(rateLimit);
        }

        try {
            for (int i = 0; i < executionCount; i++) {
                String[] processedArgs = new String[args.length];
                for (int a = 0; a < args.length; a++) {
                    processedArgs[a] = replaceBracketExpressions(args[a], i);
                }

                // rate-limit before actual run (if limiter != null)
                if (limiter != null) limiter.acquire();
                consoleManager.runCommand(String.join(" ", processedArgs));
            }
        } finally {
            if (limiter != null) limiter.shutdown();
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
     * Parallel version with explicit control and optional rate-limiter.
     *
     * @param executionCount number of iterations (i from 0..executionCount-1)
     * @param rateLimit if non-null, global limit of runCommand() calls per second
     * @param args command arguments (may contain [expr] occurrences)
     * @param threadPoolSize number of worker threads to use
     * @param serializeRunCommand if true, calls to consoleManager.runCommand(...) are synchronized
     */
    public void formatAndRunParallel(int executionCount, Integer rateLimit, String[] args, int threadPoolSize, boolean serializeRunCommand) {
        ConsoleManager consoleManager = new ConsoleManager(subscriptionService, scriptLoader);

        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, threadPoolSize));
        List<Future<?>> futures = new ArrayList<>(executionCount);

        TokenBucketRateLimiter limiter;
        if (rateLimit != null) {
            limiter = new TokenBucketRateLimiter(rateLimit);
        } else {
            limiter = null;
        }

        try {
            for (int i = 0; i < executionCount; i++) {
                final int idx = i;
                futures.add(pool.submit(() -> {
                    try {
                        String[] processedArgs = new String[args.length];
                        for (int a = 0; a < args.length; a++) {
                            processedArgs[a] = replaceBracketExpressionsUsingCache(args[a], idx);
                        }
                        Runnable runCmd = () -> consoleManager.runCommand(String.join(" ", processedArgs));

                        // Acquire rate permit if limiter present — this blocks the worker thread until allowed.
                        if (limiter != null) limiter.acquire();

                        if (serializeRunCommand) {
                            synchronized (consoleManager) {
                                runCmd.run();
                            }
                        } else {
                            runCmd.run();
                        }
                    } catch (RuntimeException ex) {
                        throw ex;
                    }
                }));
            }

            // wait for all tasks and propagate exceptions (if any)
            pool.shutdown();
            for (Future<?> f : futures) {
                try {
                    f.get(); // will throw ExecutionException if any task failed
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for tasks", ie);
                } catch (ExecutionException ee) {
                    Throwable cause = ee.getCause();
                    if (cause instanceof RuntimeException) throw (RuntimeException) cause;
                    else throw new RuntimeException("Task failed", cause);
                }
            }

            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException ignored) {}
        finally {
            if (limiter != null) limiter.shutdown();
            if (!pool.isShutdown()) pool.shutdownNow();
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

    static class TokenBucketRateLimiter {
        private final int ratePerSecond;
        private final Semaphore permits;
        private final ScheduledExecutorService refiller;
        private final int capacity;

        public TokenBucketRateLimiter(int ratePerSecond) {
            this.ratePerSecond = Math.max(1, ratePerSecond);
            this.capacity = this.ratePerSecond; // max burst = rate
            this.permits = new Semaphore(0);
            this.refiller = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "rate-refill");
                t.setDaemon(true);
                return t;
            });
            // refill every 1 second with `rate` permits (cap at capacity)
            refiller.scheduleAtFixedRate(this::refill, 0, 1, TimeUnit.SECONDS);
        }

        private void refill() {
            int toRelease;
            synchronized (permits) {
                int available = permits.availablePermits();
                toRelease = capacity - available;
                if (toRelease > 0) {
                    // release up to ratePerSecond but not exceeding capacity
                    int releaseCount = Math.min(ratePerSecond, toRelease);
                    permits.release(releaseCount);
                }
            }
        }

        /**
         * Blocks until a permit is available.
         */
        public void acquire() {
            try {
                permits.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for rate permit", e);
            }
        }

        /**
         * Shutdown the refill thread.
         */
        public void shutdown() {
            refiller.shutdownNow();
        }
    }

}
