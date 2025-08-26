package eva.platzda.cli.commands.scripts;

import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.commands.execution.ConsoleManager;
import eva.platzda.cli.notification_management.receivers.NotificationReceiver;
import eva.platzda.cli.notification_management.SubscriptionService;
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

        ConcurrentHashMap<Long, CompletableFuture<String>> futures = new ConcurrentHashMap<>();

        for(String command : commands){
            if(command.startsWith("await")) {

                String[] awaitArgs = command.split(" ");

                if(awaitArgs.length < 3){
                    System.out.println("Not enough arguments in command: " + command);
                    continue;
                }

                SocketNotificationType awaitedType = SocketNotificationType.fromString(awaitArgs[1]);
                if(awaitedType == null){
                    System.out.println("Invalid socket notification type: " + awaitArgs[1] + "in command: " + command);
                    continue;
                }

                List<Long> awaitedIds = resolveAwaitedIds(awaitArgs[2]);

                for(long awaitedId : awaitedIds){

                    CompletableFuture<String> future = new CompletableFuture<>();

                    //schedule a timeout that completes the future exceptionally after TIMEOUT_SECONDS
                    ScheduledFuture<?> timeoutTask = TIMEOUT_SCHEDULER.schedule(
                            () -> future.completeExceptionally(new TimeoutException(
                                    "Await timed out after " + timeoutSeconds + " seconds for id " + awaitedId)),
                            timeoutSeconds, TimeUnit.SECONDS);

                    //cancel the scheduled timeout if the future completes before timeout
                    future.whenComplete((res, ex) -> timeoutTask.cancel(false));

                    NotificationReceiver receiver = new NotificationReceiver(awaitedId, awaitedType, line -> {
                        if (line == null) return;

                        if ("__CONNECTION_CLOSED__".equals(line)) {
                            future.completeExceptionally(new IllegalStateException("Connection closed"));
                            return;
                        }

                        future.complete(line);
                    });
                    subscriptionService.subscribeToObject(receiver);

                    futures.put(awaitedId, future);

                }

            }
            else {
                consoleManager.runCommand(command);
            }
        }

        for(CompletableFuture<String> future : futures.values()){
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw  new RuntimeException(e.getMessage());
            }
        }

        return "Script executed.";
    }

    private List<Long> resolveAwaitedIds(String input) {

        String[] split = input.split(",");

        List<Long> ids = new ArrayList<>();
        for(String s : split){
            ids.addAll(resolveAwaitedIdRanges(s));
        }

        return ids;

    }

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
