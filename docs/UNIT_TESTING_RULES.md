# Unit Testing Rules

- Las unitarias prueban dominio y application services sin Spring.
- Usar Mockito solo para dependencias externas.
- No mockear value objects ni aggregates si pueden crearse reales.
- Probar reglas de negocio, no detalles triviales.
- Probar camino feliz, alternativo y triste.
- Verificar que no se llame `save` cuando una regla falla.
- Usar `ArgumentCaptor` si importa validar lo persistido.
- No usar `@SpringBootTest`, `MockMvc`, H2 ni base de datos real en unitarias.

