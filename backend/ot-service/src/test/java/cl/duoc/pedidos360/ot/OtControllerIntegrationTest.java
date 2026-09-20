package cl.duoc.pedidos360.ot;

import cl.duoc.pedidos360.ot.model.Ot;
import cl.duoc.pedidos360.ot.repository.OtItemRepository;
import cl.duoc.pedidos360.ot.repository.OtRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OtControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OtRepository otRepository;

    @Autowired
    private OtItemRepository itemRepository;

    @BeforeEach
    void cleanDatabase() {
        itemRepository.deleteAll();
        otRepository.deleteAll();
    }

    @Test
    void shouldListOts() throws Exception {
        otRepository.saveAndFlush(new Ot("OT-2026-900001", "CLI-TEST", "TEST01",
                "OT de prueba", new BigDecimal("62000")));

        mockMvc.perform(get("/api/ots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("OT-2026-900001"))
                .andExpect(jsonPath("$[0].clienteId").value("CLI-TEST"));
    }

    @Test
    void shouldCreateOtWithOfficialIdFormat() throws Exception {
        String body = """
                {
                  "clienteId": "CLI-003",
                  "patente": "CCDD33",
                  "descripcion": "Diagnostico general",
                  "total": 30000
                }
                """;

        mockMvc.perform(post("/api/ots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern(
                        "/api/ots/OT-[0-9]{4}-[0-9]{6}")))
                .andExpect(jsonPath("$.id", org.hamcrest.Matchers.matchesPattern(
                        "OT-[0-9]{4}-[0-9]{6}")))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldCreateAndCalculateItemSubtotal() throws Exception {
        String otId = "OT-2026-900002";
        otRepository.saveAndFlush(new Ot(otId, "CLI-TEST", "TEST02", null, BigDecimal.ZERO));

        String body = """
                {
                  "concepto": "MO-HH",
                  "cantidad": 2,
                  "precioUnit": 25000
                }
                """;

        mockMvc.perform(post("/api/ots/{id}/items", otId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.otId").value(otId))
                .andExpect(jsonPath("$.subtotal").value(50000));
    }
}
