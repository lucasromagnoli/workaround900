package br.com.lucaromagnoli.workaround900.websocket;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.concurrent.Queues;

import javax.annotation.PostConstruct;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@EnableScheduling
public class MyWebSocketHandler implements WebSocketHandler {

    private final static Set<WebSocketSession> SESSIONS = new CopyOnWriteArraySet<>();

    Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer(Queues.XS_BUFFER_SIZE, false);

    @PostConstruct
    public void listenerSink() {
        sink.asFlux().subscribe(msg -> System.out.println(msg));
    }

    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .doOnSubscribe(subscription -> {
                    this.join(session);
                    sink.asFlux().subscribe(msg -> session.send(Mono.just(msg).map(session::textMessage)).doOnSuccess(unused -> System.out.println("mandado")).subscribe());
                })
                .doOnComplete(() -> this.left(session))
                .doOnNext(webSocketMessage -> this.broadcast(webSocketMessage.getPayloadAsText()))
                .then()
                ;
    }

    private void join(WebSocketSession session) {
        SESSIONS.add(session);
        this.broadcast("Usuário entrou do chat! =" + session.getId());
    }

    private void left(WebSocketSession session) {
        SESSIONS.remove(session);
        this.broadcast("Usuário saiu do chat! =" + session.getId());
    }

    private void broadcast(String message) {
        sink.tryEmitNext(message);
//        Flux.fromIterable(SESSIONS)
//                .flatMap(webSocketSession -> webSocketSession
//                        .send(Mono.just(message).map(webSocketSession::textMessage)))
//                .subscribe();
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 1000)
    public void sendMessage() {
        final var msg = UUID.randomUUID().toString();
        Flux.fromIterable(SESSIONS)
                .flatMap(webSocketSession -> webSocketSession.send(Flux.just(msg)
                        .map(webSocketSession::textMessage)))
                .subscribe();
    }

}
