package com.wms.api.cadastros;

import com.wms.domain.cadastros.Categoria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoriaControllerTest {
    private EntityManager entityManager;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        TypedQuery<Long> uniquenessQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(uniquenessQuery);
        when(uniquenessQuery.setParameter(anyString(), any())).thenReturn(uniquenessQuery);
        when(uniquenessQuery.getSingleResult()).thenReturn(0L);
        mockMvc = MockMvcBuilders.standaloneSetup(new CategoriaController(entityManager)).build();
    }

    @Test
    void exposesInheritedCrudEndpoints() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setId(7L);
        categoria.setCode("ALIMENTOS");
        categoria.setDescription("Alimentos");
        categoria.setActive(true);
        when(entityManager.find(Categoria.class, 7L)).thenReturn(categoria);

        mockMvc.perform(get("/api/categorias/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.code").value("ALIMENTOS"));
    }

    @Test
    void createsEntityAndReturnsItsLocation() throws Exception {
        doAnswer(invocation -> {
            Categoria categoria = invocation.getArgument(0);
            categoria.setId(12L);
            return null;
        }).when(entityManager).persist(any(Categoria.class));

        mockMvc.perform(post("/api/categorias")
                        .contentType("application/json")
                        .content("""
                                {
                                  "id": 99,
                                  "code": "ALIMENTOS",
                                  "description": "Alimentos",
                                  "userNameCreator": "controller-test",
                                  "userNameLastChange": "controller-test",
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/categorias/12"))
                .andExpect(jsonPath("$.id").value(12));

        verify(entityManager).persist(any(Categoria.class));
    }

    @Test
    void updatesEntityUsingPathId() throws Exception {
        when(entityManager.find(Categoria.class, 8L)).thenReturn(new Categoria());
        when(entityManager.merge(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/categorias/8")
                        .contentType("application/json")
                        .content("""
                                {
                                  "id": 99,
                                  "code": "BEBIDAS",
                                  "description": "Bebidas",
                                  "userNameCreator": "controller-test",
                                  "userNameLastChange": "controller-test",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.code").value("BEBIDAS"));

        verify(entityManager).merge(any(Categoria.class));
    }

    @Test
    void returnsNotFoundWhenDeletingUnknownEntity() throws Exception {
        when(entityManager.find(eq(Categoria.class), eq(123L))).thenReturn(null);

        mockMvc.perform(delete("/api/categorias/123"))
                .andExpect(status().isNotFound());
    }
}
