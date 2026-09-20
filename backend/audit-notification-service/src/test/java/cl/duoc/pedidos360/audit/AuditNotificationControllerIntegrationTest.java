package cl.duoc.pedidos360.audit;

import cl.duoc.pedidos360.audit.model.NotifyLog;
import cl.duoc.pedidos360.audit.model.OtEvent;
import cl.duoc.pedidos360.audit.repository.NotifyLogRepository;
import cl.duoc.pedidos360.audit.repository.OtEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditNotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OtEventRepository eventRepository;

    @Autowired
    private NotifyLogRepository notificationRepository;

    @BeforeEach
    void cleanDatabase() {
        eventRepository.deleteAll();
        notificationRepository.deleteAll();
    }

    @Test
    void shouldListEventsForOt() throws Exception {
        eventRepository.saveAndFlush(new OtEvent("OT-2026-000001", "OtCreada", "{\"total\":62000}"));

        mockMvc.perform(get("/api/ots/OT-2026-000001/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("OtCreada"))
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    void shouldListNotifications() throws Exception {
        notificationRepository.saveAndFlush(new NotifyLog("OT-2026-000001", "CLI-001", "email",
                "{\"type\":\"NotificarClienteOtCreada\"}"));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId").value("CLI-001"))
                .andExpect(jsonPath("$[0].canal").value("email"));
    }
}
