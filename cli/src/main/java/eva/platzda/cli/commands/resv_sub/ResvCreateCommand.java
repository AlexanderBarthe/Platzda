package eva.platzda.cli.commands.resv_sub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eva.platzda.cli.commands.execution.ConsoleCommand;
import eva.platzda.cli.notification_management.SubscriptionService;
import eva.platzda.cli.notification_management.receivers.ReservationSubscriber;
import eva.platzda.cli.rest_api.HttpMethod;
import eva.platzda.cli.rest_api.RestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResvCreateCommand implements ConsoleCommand {

    private final SubscriptionService subscriptionService;

    public ResvCreateCommand(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public String command(){return "create";}

    @Override
    public String executeCommand(String[] args) {
        if(args.length == 0) {
            return "Not enough arguments provided. See 'help resv' for more information.";
        }

        boolean noNotification = false;
        if(args[0].equals("--no-notification")) {
            noNotification = true;
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        Long restaurantId;
        Long userId;
        LocalDateTime start;
        int guests;

        try {
            restaurantId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid restaurant id");
        }

        try {
            userId = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user id");
        }

        try {
            start = LocalDateTime.parse(args[2]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid time");
        }
        try {
            guests = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount of guests");
        }

        String json = "{}";
        String url = "reservation?restaurantId=" +restaurantId
                + "&userId=" + userId
                + "&start=" + start
                + "&guests=" + guests;
        String response = RestClient.sendRequest(url, HttpMethod.POST, json);

        //Subscribe to created reservations if successful
        if(!noNotification) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode root = mapper.readTree(response);
                if (!root.isArray()) throw new Exception();

                List<String> ids = new ArrayList<>();
                for (JsonNode item : root) {
                    JsonNode idNode = item.get("id");
                    if (idNode != null && !idNode.isNull()) {
                        ids.add(idNode.asText());
                    }
                }

                for (String id : ids) {
                    Long idLong = Long.parseLong(id);
                    subscriptionService.subscribeToObject(new ReservationSubscriber(idLong, System.out::println));
                }
            } catch (Exception ignored) {
            }
        }

        return response;

    }


}
