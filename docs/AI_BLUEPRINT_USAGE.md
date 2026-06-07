# AI Blueprint Usage

Este blueprint permite crear un nuevo bounded context en minutos siguiendo una secuencia estable y repetible.

## 1. Detectar bounded context
Separar el caso de negocio por lenguaje ubicuo y responsabilidad principal.

## 2. Detectar aggregate principal
Elegir la entidad raíz que protege invariantes y coordina reglas.

## 3. Detectar value objects
Crear value objects para datos con formato o significado de negocio como email, documento, estado o código.

## 4. Detectar commands
Definir comandos para cada intención de escritura.

## 5. Detectar queries
Definir queries para consultas por colección, id o filtros.

## 6. Detectar reglas de negocio
Implementar reglas en aggregate/value objects y validar duplicidad o estados inválidos en command services.

## 7. Detectar endpoints
Mapear cada comando/query a endpoints REST en `interfaces/rest` con recursos y transformadores.

## 8. Detectar pruebas unitarias
Probar value objects, aggregates y command services con Mockito para reglas felices y tristes.

## 9. Detectar pruebas integrales
Probar endpoints con `@SpringBootTest`, `MockMvc`, `H2` y repositorios reales, validando HTTP + JSON + persistencia.

## 10. Detectar reglas de seguridad
Definir rutas públicas/protegidas, roles requeridos y validaciones de exposición de datos sensibles.

## Checklist operativo
- Crear paquetes del nuevo context con la misma estructura del blueprint.
- Implementar comandos, queries, services, repositorios, recursos y assemblers.
- Agregar tag de OpenAPI para el nuevo contexto.
- Agregar pruebas unitarias e integrales antes de cerrar la implementación.

