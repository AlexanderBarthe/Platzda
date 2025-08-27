package eva.platzda.cli.commands.scripts;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.notification_management.SocketNotificationReceiver;
import eva.platzda.cli.notification_management.SubscriptionService;
import eva.platzda.cli.notification_management.receivers.NotificationReceiver;
import eva.platzda.cli.notification_management.receivers.SocketNotificationType;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.*;
import java.util.concurrent.*;

public class ScriptCommand implements ConsoleCommand {

    private SubscriptionService subscriptionService;
    private ScriptLoader scriptLoader;

    private static int timeoutSeconds;

    //Scheduler for timeouts
    private static final ScheduledExecutorService TIMEOUT_SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "script-timeout-scheduler");
                t.setDaemon(true);
                return t;
            });


    /**
     * Create command handler with subscription service and script loader.
     * Reads await timeout from environment (falls back to 30s).
     */
    public ScriptCommand(SubscriptionService subscriptionService, ScriptLoader scriptLoader) {
        this.subscriptionService = subscriptionService;
        this.scriptLoader = scriptLoader;

        try {
            timeoutSeconds = Integer.parseInt(Dotenv.load().get("await-timeout"));
            if(timeoutSeconds <= 0) timeoutSeconds = 30;
        } catch (NumberFormatException e) {timeoutSeconds = 30;}

    }

    @Override
    public String command() {
        return "script";
    }

    /**
     * Execute a named script. Commands are read from the ScriptLoader and executed in order.
     * Supports 'await' commands which register listeners and wait for notifications.
     *
     * @param args command arguments; first arg is script name
     * @return fixed confirmation string on completion
     */
    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0){
            throw new IllegalArgumentException("Not enough arguments provided. See 'help' for more information.");
        }

        if(!scriptLoader.hasScript(args[0])){
            throw new IllegalArgumentException("Script not found: " + args[0]);
        }

        ConsoleManager consoleManager = new ConsoleManager(subscriptionService, scriptLoader);
        List<String> commands = scriptLoader.getScript(args[0]);

        Set<CompletableFuture<String>> futures = Collections.newSetFromMap(new ConcurrentHashMap<>());

        for(String command : commands){
            if(command.startsWith("await")) {

                Deque<String> awaitArgs = new ArrayDeque<>(List.of(command.split(" ")));

                awaitArgs.removeFirst();

                boolean silent;
                if(awaitArgs.peekFirst().equals("--silent")) {
                    silent = true;
                    awaitArgs.removeFirst();
                } else {
                    silent = false;
                }

                if(awaitArgs.size() < 2){
                    System.out.println("Not enough arguments in command: " + command);
                    continue;
                }

                SocketNotificationType awaitedType = SocketNotificationType.fromString(awaitArgs.peekFirst());
                if(awaitedType == null){
                    System.out.println("Invalid socket notification type: " + awaitArgs.peekFirst() + " in command: " + command);
                    continue;
                }
                awaitArgs.removeFirst();

                if(awaitArgs.peekFirst().equals("any")) {
                    CompletableFuture<String> future = new CompletableFuture<>();

                    //schedule a timeout that completes the future exceptionally after configured timeout
                    ScheduledFuture<?> timeoutTask = TIMEOUT_SCHEDULER.schedule(
                            () -> future.completeExceptionally(new TimeoutException(
                                    "Await timed out after " + timeoutSeconds + " seconds.")),
                            timeoutSeconds, TimeUnit.SECONDS);
                    future.whenComplete((res, ex) -> timeoutTask.cancel(false));

                    //Minimal hook that completes on any notification
                    SocketNotificationReceiver hook = new SocketNotificationReceiver() {
                        @Override
                        public void sendNotification(String message) {
                            if (message == null) return;

                            if ("__CONNECTION_CLOSED__".equals(message)) {
                                future.completeExceptionally(new IllegalStateException("Connection closed"));
                                return;
                            }

                            if(!silent) System.out.println("Received awaited message for type " + awaitedType.getTranslation() + ".");

                            future.complete(message);
                        }
                    };
                    subscriptionService.getSocketManager().addNotificationHook(hook);
                    futures.add(future);
                }
                else {

                    List<Long> awaitedIds = resolveAwaitedIds(awaitArgs.peekFirst());

                    for (long awaitedId : awaitedIds) {

                        CompletableFuture<String> future = new CompletableFuture<>();

                        //schedule a timeout that completes the future exceptionally after TIMEOUT_SECONDS
                        ScheduledFuture<?> timeoutTask = TIMEOUT_SCHEDULER.schedule(
                                () -> future.completeExceptionally(new TimeoutException(
                                        "Await timed out after " + timeoutSeconds + " seconds for id " + awaitedId)),
                                timeoutSeconds, TimeUnit.SECONDS);

                        //cancel the scheduled timeout if the future completes before timeout
                        future.whenComplete((res, ex) -> timeoutTask.cancel(false));

                        //create a receiver that completes the future when the specific id/type message arrives
                        NotificationReceiver receiver = new NotificationReceiver(awaitedId, awaitedType, line -> {
                            if (line == null) return;

                            if ("__CONNECTION_CLOSED__".equals(line)) {
                                future.completeExceptionally(new IllegalStateException("Connection closed"));
                                return;
                            }

                            if(!silent) System.out.println("Received awaited message for type " + awaitedType.getTranslation() + " on id " +  awaitedId);

                            future.complete(line);
                        });
                        // register and subscribe to remote notifications
                        subscriptionService.subscribeToObject(receiver);

                        futures.add(future);

                    }
                }

            }
            else {
                consoleManager.runCommand(command);
            }
        }

        //Wait for all futures to complete -> await commands to finish
        for(CompletableFuture<String> future : futures){
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw  new RuntimeException(e.getMessage());
            }
        }

        return "Script executed.";
    }

    /**
     * Parse a comma-separated list of ids and ranges into explicit ids.
     *
     * @param input comma separated ids/ranges (e.g. "1,3-5")
     * @return list of resolved ids
     */
    private List<Long> resolveAwaitedIds(String input) {

        String[] split = input.split(",");

        List<Long> ids = new ArrayList<>();
        for(String s : split){
            ids.addAll(resolveAwaitedIdRanges(s));
        }

        return ids;

    }

    /**
     * Resolve a single id or a hyphen range into a list of longs.
     *
     * @param input single id or range like "5" or "2-4"
     * @return list of ids or empty list on parse error
     */
    private List<Long> resolveAwaitedIdRanges(String input) {

        if(!input.contains("-")){
            try {
                long id = Integer.parseInt(input);
                return List.of(id);
            } catch (NumberFormatException e) {
                return List.of();
            }
        }

        String[] split = input.split("-");

        List<Long> ids = new ArrayList<>();

        try {
            long lower = Integer.parseInt(split[0]);
            long upper = Integer.parseInt(split[1]);
            for(long i = lower; i <= upper; i++){
                ids.add(i);
            }
        } catch (NumberFormatException e) {
            return List.of();
        }

        return ids;

    }
}
