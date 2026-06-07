# Integration Testing Rules

- Las integrales prueban HTTP real con MockMvc.
- Cargan Spring con perfil `test`.
- Usan H2.
- Usan repositorios reales.
- No mockean application services.
- Validan status, JSON y base de datos.
- Validan seguridad cuando aplique.
- Validan que no se expongan datos sensibles.
- Cubren creación, consulta, duplicidad, error de validación y acceso no autorizado.

