package bisq.http_api.web_socket.domain.language;

import bisq.bonded_roles.BondedRolesService;
import bisq.common.locale.LanguageRepository;
import bisq.common.observable.Pin;
import bisq.http_api.web_socket.domain.BaseWebSocketService;
import bisq.http_api.web_socket.subscription.Subscriber;
import bisq.http_api.web_socket.subscription.SubscriberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static bisq.http_api.web_socket.subscription.Topic.*;

@Slf4j
public class I18NWebSocketService extends BaseWebSocketService {
    private final Set<Pin> pins = new HashSet<>();

    public I18NWebSocketService(ObjectMapper objectMapper,
                                SubscriberRepository subscriberRepository,
                                BondedRolesService bondedRolesService) {
        super(objectMapper, subscriberRepository, I18N_PAIRS);

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
        Optional<Set<Subscriber>> subscribersOpt = subscriberRepository.findSubscribers(I18N_PAIRS);

        if (subscribersOpt.isEmpty()) {
            log.warn("No subscribers found for topic: {}", I18N_PAIRS);
        }

        Set<Subscriber> _subscribers = subscribersOpt.orElse(Collections.emptySet());
        log.info("Found {} subscribers", _subscribers.size());

        for (Subscriber sub : _subscribers) {
            log.info("Subscriber: ID={} Parameter={}, Seq={}", sub.getSubscriberId(), sub.getParameter(), sub.getSequenceNumber());
        }

        String languageCode = subscriberRepository.findSubscribers(I18N_PAIRS)
                .flatMap(subscribers -> subscribers.stream()
                        .map(Subscriber::getParameter)
                        .flatMap(Optional::stream)
                        .peek(param -> log.info("Extracted language parameter: {}", param))
                        .findFirst())
                .orElse("en");

        // TODO: How to get the latest subscription/message and it's langugeCode request parameter?
        LanguageRepository.setDefaultLanguage(languageCode);

        Map<String, String> languageMap = LanguageRepository.I18N_CODES.stream()
                .collect(Collectors.toMap(code -> code, LanguageRepository::getDisplayString));
        return toJson(languageMap);
    }
}
