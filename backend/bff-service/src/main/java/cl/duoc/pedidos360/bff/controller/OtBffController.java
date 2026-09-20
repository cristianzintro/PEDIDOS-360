package cl.duoc.pedidos360.bff.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/bff/ots")
public class OtBffController {

    private final RestClient otClient;

    public OtBffController(@Qualifier("otRestClient") RestClient otClient) {
        this.otClient = otClient;
    }

    @GetMapping
    public JsonNode findAll() {
        return otClient.get().uri("/api/ots").retrieve().body(JsonNode.class);
    }

    @GetMapping("/{id}")
    public JsonNode findById(@PathVariable String id) {
        return otClient.get().uri("/api/ots/{id}", id).retrieve().body(JsonNode.class);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JsonNode create(@RequestBody JsonNode ot) {
        return otClient.post().uri("/api/ots").body(ot).retrieve().body(JsonNode.class);
    }

    @GetMapping("/{id}/items")
    public JsonNode findItems(@PathVariable String id) {
        return otClient.get().uri("/api/ots/{id}/items", id).retrieve().body(JsonNode.class);
    }

    @PostMapping("/{id}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public JsonNode createItem(@PathVariable String id, @RequestBody JsonNode item) {
        return otClient.post().uri("/api/ots/{id}/items", id)
                .body(item).retrieve().body(JsonNode.class);
    }
}
