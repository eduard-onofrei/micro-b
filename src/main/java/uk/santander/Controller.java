package uk.santander;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class Controller {
    private static final int COMPLEXITY = 1000000;
    private static final String SERVICE1 = "http://localhost:8080";

    @Autowired
    private final AccountCrudRepository accountCrudRepository;

    @Autowired
    private WebClient webClient;

    @PostMapping
    public Mono<Account> createAccount(@RequestBody @Valid Account account){
        log.info("Request with {}", account);
        final Mono<Account> accountMono = accountCrudRepository.save(account);

        return accountMono.map(acc -> {
            accountCrudRepository.findAllByValue(acc.getValue())
                    .map(acc1 -> {
                        process(acc1);
                        return acc1;
                    });
            return acc;
        });
    }

    @GetMapping
    public Flux<Account> getAccount(@RequestParam @Valid String owner){
        return accountCrudRepository.findAllByOwner(owner)
                .map(account -> {
                    for(int i=0;i<COMPLEXITY;i++){
                        account.setResult(account.getValue()*Math.random());
                    }
                    return account;
                });
    }

    private void process(Account account) {
        webClient.post()
                .uri(SERVICE1)
                .bodyValue(account)
                .retrieve()
                .bodyToMono(Account.class);
    }
}
