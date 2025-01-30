package bisq.http_api.web_socket.domain.language;

import bisq.bonded_roles.BondedRolesService;
import bisq.common.locale.LanguageRepository;
import bisq.common.observable.Pin;
import bisq.http_api.web_socket.domain.BaseWebSocketService;
import bisq.http_api.web_socket.subscription.SubscriberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static bisq.http_api.web_socket.subscription.Topic.LANGUAGE_PAIRS;

@Slf4j
public class LanguageWebSocketService extends BaseWebSocketService {
    private final Set<Pin> pins = new HashSet<>();

    public LanguageWebSocketService(ObjectMapper objectMapper,
                                  SubscriberRepository subscriberRepository,
                                  BondedRolesService bondedRolesService) {
        super(objectMapper, subscriberRepository, LANGUAGE_PAIRS);

    }

    @Override
    public CompletableFuture<Boolean> initialize() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> shutdown() {
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Optional<String> getJsonPayload() {
        Map<String, String> languageMap = LanguageRepository.CODES.stream()
                .collect(Collectors.toMap(code -> code, LanguageRepository::getDisplayString));
        return toJson(languageMap);
    }
}
