# Revisión técnica de IsaFlor para integración con React

**Fecha:** 27 de noviembre de 2026  
**Alcance:** revisión del contrato REST y de su preparación para React 19, Vite y React Router.  
**Estado:** solo análisis; no se modificó código ni se implementó checkout o reserva de stock.

## Resumen ejecutivo

El backend expone una API versionada bajo `/api/v1`, usa JWT stateless y reconoce únicamente los roles `CLIENTE` y `ADMINISTRADOR`. Hay rutas utilizables para catálogo de productos y categorías, autenticación, pedidos, detalles y pagos.

Los principales bloqueos o decisiones pendientes para el frontend son:

- No hay endpoints de perfil propio ni de logout/revocación de JWT.
- La compra se crea en varias solicitudes: primero un pedido vacío y después sus detalles. No hay checkout integral ni control de stock al comprar.
- El total del pedido no suma el costo del envío.
- No existe API de estadísticas para el dashboard.
- El DTO de respuesta de producto está aplanado, pero `POST` y `PUT` reciben la entidad `Producto`, que espera la categoría anidada.
- CORS no incluye el origen de producción de Vercel.
- Varias rutas devuelven `204` sin cuerpo y los errores no siguen un formato JSON uniforme.

La lógica de ofertas existente se revisó, pero no se cambió. Tampoco se implementaron reservas de inventario ni checkout integral.

## Convenciones y autenticación

- Prefijo base: `/api/v1`.
- Encabezado JWT: `Authorization: Bearer <accessToken>`.
- Roles/autoridades: `ROLE_CLIENTE` y `ROLE_ADMINISTRADOR`. No se admite `VENDEDOR`.
- La autenticación es stateless. El token incluye identidad, rol y expiración; el valor `expiresIn` de la respuesta de login está expresado en segundos.
- En las tablas, “público” significa que Spring Security no exige autenticación. Los permisos de propietario indicados son verificaciones adicionales del servicio.
- Los listados suelen responder `200` con contenido o `204 No Content` si no hay resultados. El frontend debe tratar `204` como respuesta sin cuerpo y no intentar parsearlo como JSON.

## Inventario de endpoints existentes

Los JSON referenciados por nombre se describen en la sección **Contratos JSON**.

| Ruta | Método | Acceso | Entrada | Salida y estados principales |
|---|---|---|---|---|
| `/api/v1/auth/login` | POST | Público | `LoginRequest` | `AuthResponse`, 200; credenciales incorrectas, 401. |
| `/api/v1/auth/registro` | POST | Público | `RegistroUsuarioDTO` | `UsuarioDTO`, 201; validación 400; RUN/correo duplicado 409; comuna inválida 400. |
| `/api/v1/usuarios` | GET | ADMINISTRADOR | — | Lista `UsuarioDTO`, 200 o 204. |
| `/api/v1/usuarios/{id}` | GET | ADMINISTRADOR | — | `UsuarioDTO`, 200 o 404. |
| `/api/v1/usuarios` | POST | ADMINISTRADOR | Entidad `Usuario` | `UsuarioDTO`, 201; errores capturados como 400. |
| `/api/v1/usuarios/{id}` | PUT | ADMINISTRADOR | Entidad `Usuario` | `UsuarioDTO`, 200; errores capturados como 400. |
| `/api/v1/usuarios/{id}` | DELETE | ADMINISTRADOR | — | Sin cuerpo, 204; errores capturados como 404. |
| `/api/v1/productos` | GET | Público | — | Lista `ProductoDTO`, 200 o 204. |
| `/api/v1/productos/{id}` | GET | Público | — | `ProductoDTO`, 200 o 404. |
| `/api/v1/productos` | POST | ADMINISTRADOR | Entidad `Producto` | `ProductoDTO`, 201; errores de validación/servicio 400. El controlador documenta 409 para código duplicado, pero en la práctica captura el error como 400. |
| `/api/v1/productos/{id}` | PUT | ADMINISTRADOR | Entidad `Producto` | `ProductoDTO`, 200; errores del servicio 400. |
| `/api/v1/productos/{id}/oferta` | PATCH | ADMINISTRADOR | `OfertaDTO` | `ProductoDTO`, 200; inválido 400; producto no encontrado 404. |
| `/api/v1/productos/{id}` | DELETE | ADMINISTRADOR | — | Mensaje `String`, 200; errores capturados como 404. |
| `/api/v1/categorias` | GET | Público | — | Lista `CategoriaDTO`, 200 o 204. |
| `/api/v1/categorias/{id}` | GET | Público | — | `CategoriaDTO`, 200 o 404. |
| `/api/v1/categorias` | POST | ADMINISTRADOR | Entidad `Categoria` | `CategoriaDTO`, 201; inválido 400; duplicado 409. |
| `/api/v1/categorias/{id}` | PUT | ADMINISTRADOR | Entidad `Categoria` | `CategoriaDTO`, 200; inválido 400; no encontrada 404; duplicado 409. |
| `/api/v1/categorias/{id}` | DELETE | ADMINISTRADOR | — | Mensaje `String`, 200; no encontrada 404; asociada a productos 409. |
| `/api/v1/pedidos` | GET | CLIENTE, ADMINISTRADOR | — | Lista `PedidoDTO`; CLIENTE recibe los propios y ADMINISTRADOR todos; 200 o 204. |
| `/api/v1/pedidos/{id}` | GET | CLIENTE, ADMINISTRADOR | — | `PedidoDTO`, 200; ajeno 403; inexistente 404. |
| `/api/v1/pedidos` | POST | CLIENTE | `CrearPedidoRequest` | `PedidoDTO`, 201; inválido 400; método de envío/tipo de documento inexistente 404. |
| `/api/v1/pedidos/{id}/estado` | PATCH | ADMINISTRADOR | `ActualizarEstadoPedidoRequest` | `PedidoDTO`, 200; estado inválido 400; sin permiso 403; transición inválida 409. |
| `/api/v1/pedidos/{id}` | DELETE | CLIENTE, ADMINISTRADOR | — | Mensaje `String`, 200; ajeno 403; inexistente 404; con detalles o pago 409. |
| `/api/v1/detalles-pedido` | GET | CLIENTE, ADMINISTRADOR | — | Lista `DetallePedidoDTO`, filtrada por propiedad para clientes; 200 o 204. |
| `/api/v1/detalles-pedido/{id}` | GET | CLIENTE, ADMINISTRADOR | — | `DetallePedidoDTO`, 200; ajeno 403; inexistente 404. |
| `/api/v1/detalles-pedido` | POST | CLIENTE, ADMINISTRADOR | `CrearDetallePedidoRequest` | `DetallePedidoDTO`, 201; inválido 400; ajeno 403; pedido/producto inexistente 404; pedido no modificable o pagado 409. |
| `/api/v1/detalles-pedido/{id}` | PUT | CLIENTE, ADMINISTRADOR | `ActualizarDetallePedidoRequest` | `DetallePedidoDTO`, 200; inválido 400; ajeno 403; inexistente 404; pedido no modificable o pagado 409. |
| `/api/v1/detalles-pedido/{id}` | DELETE | CLIENTE, ADMINISTRADOR | — | Mensaje `String`, 200; ajeno 403; inexistente 404; pedido no modificable o pagado 409. |
| `/api/v1/pagos` | GET | CLIENTE, ADMINISTRADOR | — | Lista `PagoDTO`, propia para cliente y global para administrador; 200 o 204. |
| `/api/v1/pagos/{id}` | GET | CLIENTE, ADMINISTRADOR | — | `PagoDTO`, 200; ajeno 403; inexistente 404. |
| `/api/v1/pagos/{id}/historial` | GET | CLIENTE, ADMINISTRADOR | — | Lista `PagoEstadoHistorialDTO`, 200 o 204; ajeno 403; pago inexistente 404. |
| `/api/v1/pagos` | POST | CLIENTE | `CrearPagoRequest` | `PagoDTO`, 201; inválido 400; pedido ajeno 403; pedido/método inexistente 404; pago existente o total no pagable 409. |
| `/api/v1/pagos/{id}/estado` | PATCH | ADMINISTRADOR | `ActualizarEstadoPagoRequest` | `PagoDTO`, 200; inválido 400; sin permiso 403; inexistente 404; transición inválida 409. |
| `/api/v1/pagos`, `/api/v1/pagos/{id}`, `/api/v1/pagos/{id}/**` | DELETE | Público por Security | — | Siempre responde 409: los pagos no se pueden eliminar. |
| `/api/v1/regiones` | GET | Público | — | Lista `RegionDTO`, 200 o 204. |
| `/api/v1/regiones/{id}` | GET | Público | — | `RegionDTO`, 200 o 404. |
| `/api/v1/regiones` | POST | ADMINISTRADOR | Entidad `Region` | `RegionDTO`, 201; inválido 400; duplicado 409. |
| `/api/v1/regiones/{id}` | PUT | ADMINISTRADOR | Entidad `Region` | `RegionDTO`, 200; inválido 400; inexistente 404; duplicado 409. |
| `/api/v1/regiones/{id}` | DELETE | ADMINISTRADOR | — | Mensaje `String`, 200; inexistente 404; con comunas 409. |
| `/api/v1/comunas` | GET | Público | — | Lista de entidades `Comuna`, 200 o 204. |
| `/api/v1/comunas/{id}` | GET | Público | — | Entidad `Comuna`, 200 o 404. |
| `/api/v1/comunas` | POST | ADMINISTRADOR | Entidad `Comuna` | Entidad `Comuna`, 201; inválido 400. |
| `/api/v1/comunas/{id}` | PUT | ADMINISTRADOR | Entidad `Comuna` | Entidad `Comuna`, 200; inexistente 404; otros errores 400. |
| `/api/v1/comunas/{id}` | DELETE | ADMINISTRADOR | — | Mensaje `String`, 200; errores capturados como 404. |
| `/api/v1/tipos-documento` | GET | Público | — | Lista `TipoDocumentoDTO`, 200 o 204. |
| `/api/v1/tipos-documento/{id}` | GET | Público | — | `TipoDocumentoDTO`, 200 o 404. |
| `/api/v1/tipos-documento` | POST | ADMINISTRADOR | Entidad `TipoDocumento` | `TipoDocumentoDTO`, 201; inválido 400; duplicado 409. |
| `/api/v1/tipos-documento/{id}` | PUT | ADMINISTRADOR | Entidad `TipoDocumento` | `TipoDocumentoDTO`, 200; inválido 400; inexistente 404; duplicado 409. |
| `/api/v1/tipos-documento/{id}` | DELETE | ADMINISTRADOR | — | Mensaje `String`, 200; inexistente 404; asociado a pedidos 409. |
| `/api/v1/metodos-envio` | GET | Público | — | Lista de entidades `MetodoEnvio`, 200 o 204. |
| `/api/v1/metodos-envio/{id}` | GET | Público | — | Entidad `MetodoEnvio`, 200 o 404. |
| `/api/v1/metodos-envio` | POST | ADMINISTRADOR | Entidad `MetodoEnvio` | Entidad `MetodoEnvio`, 201; inválido 400. |
| `/api/v1/metodos-envio/{id}` | PUT | ADMINISTRADOR | Entidad `MetodoEnvio` | Entidad `MetodoEnvio`, 200; inválido 400; inexistente 404. |
| `/api/v1/metodos-envio/{id}` | DELETE | ADMINISTRADOR | — | Mensaje `String`, 200; inexistente 404; asociado a pedidos 409. |
| `/api/v1/metodos-pago` | GET | Público | — | Lista de entidades `MetodoPago`: visitantes/clientes ven activos; ADMINISTRADOR puede ver todos. 200 o 204. |
| `/api/v1/metodos-pago/{id}` | GET | Público | — | Entidad `MetodoPago`, 200 o 404 si no existe/no es visible. |
| `/api/v1/metodos-pago` | POST | ADMINISTRADOR | Entidad `MetodoPago` | Entidad `MetodoPago`, 201; inválido 400. |
| `/api/v1/metodos-pago/{id}` | PUT | ADMINISTRADOR | Entidad `MetodoPago` | Entidad `MetodoPago`, 200; inválido 400; inexistente 404. |
| `/api/v1/metodos-pago/{id}` | DELETE | ADMINISTRADOR | — | Mensaje `String`, 200; inexistente 404; asociado a pagos 409. |
| `/api/v1/imagenes` | GET | Público | — | Lista de entidades `Imagen`, 200 o 204. |
| `/api/v1/imagenes/{id}` | GET | Público | — | Entidad `Imagen`, 200 o 404. |
| `/api/v1/imagenes/subir/{idProducto}` | POST | ADMINISTRADOR | Multipart: `archivo` obligatorio, `descripcion` opcional | Entidad `Imagen`, 201; error de subida 400. |
| `/api/v1/imagenes/{id}` | DELETE | ADMINISTRADOR | — | Mensaje `String`, 200; errores capturados como 404. |
| `/api/v1/imagenes-blog` | GET | Público | — | Lista `ImagenBlogResponseDTO`; administrador ve todas y visitante solo publicadas; 200 o 204. |
| `/api/v1/imagenes-blog/{id}` | GET | Público | — | `ImagenBlogResponseDTO`, 200 o 404 si no existe/no es visible. |
| `/api/v1/imagenes-blog/subir/{idBlog}` | POST | ADMINISTRADOR | Multipart: `archivo` obligatorio, `descripcion` opcional | `ImagenBlogResponseDTO`, 201; 400, 401, 403, 404 o 502 según error. |
| `/api/v1/imagenes-blog/{id}` | DELETE | ADMINISTRADOR | — | Mensaje `String`, 200; 401, 403 o 404. |
| `/api/v1/blogs` | GET | Público | — | Lista `BlogResponseDTO`; administrador ve todos los estados; 200 o 204. |
| `/api/v1/blogs/{id}` | GET | Público | — | `BlogResponseDTO`; visitante ve publicado y administrador también borradores; 200 o 404. |
| `/api/v1/blogs` | POST | ADMINISTRADOR | `BlogRequestDTO` | `BlogResponseDTO`, 201; inválido 400. |
| `/api/v1/blogs/{id}` | PUT | ADMINISTRADOR | `BlogRequestDTO` | `BlogResponseDTO`, 200; inexistente 404. |
| `/api/v1/blogs/{id}/estado` | PATCH | ADMINISTRADOR | `BlogEstadoRequestDTO` | `BlogResponseDTO`, 200; estado inválido 400; inexistente 404. |
| `/api/v1/blogs/{id}` | DELETE | ADMINISTRADOR | — | Sin cuerpo, 204; inexistente 404; contenido asociado 409. |
| `/api/v1/videos` | GET | Público | — | Lista `VideoResponseDTO`; administrador puede ver todos; 200 o 204. |
| `/api/v1/videos/{id}` | GET | Público | — | `VideoResponseDTO`, 200 o 404 si no existe/no es visible. |
| `/api/v1/videos` | POST | ADMINISTRADOR | `VideoRequestDTO` | `VideoResponseDTO`, 201; inválido 400; permisos 403; blog inexistente 404. |
| `/api/v1/videos/{id}` | PUT | ADMINISTRADOR | `VideoRequestDTO` | `VideoResponseDTO`, 200; inválido 400; permisos 403; inexistente 404. |
| `/api/v1/videos/{id}` | DELETE | ADMINISTRADOR | — | Mensaje `String`, 200; permisos 403; inexistente 404. |

Rutas auxiliares declaradas en seguridad: `GET /actuator/health` es público; `/swagger-ui/**`, `/swagger-ui.html` y `/v3/api-docs/**` también son públicos. `OPTIONS /**` está permitido para preflight.

## Contratos JSON principales

### Login y registro

Login (`LoginRequest`):

```json
{ "correo": "cliente@ejemplo.cl", "password": "..." }
```

Respuesta (`AuthResponse`):

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "usuario": {
    "idUsuario": 12,
    "run": "...",
    "nombre": "...",
    "apellidoPaterno": "...",
    "apellidoMaterno": "...",
    "correo": "cliente@ejemplo.cl",
    "fechaNacimiento": "2000-01-01",
    "tipoUsuario": "cliente",
    "direccion": "...",
    "idComuna": 4
  }
}
```

Registro (`RegistroUsuarioDTO`):

```json
{
  "run": "...",
  "nombre": "...",
  "apellidoPaterno": "...",
  "apellidoMaterno": "...",
  "correo": "...",
  "password": "...",
  "fechaNacimiento": "2000-01-01",
  "direccion": "...",
  "idComuna": 4
}
```

El registro asigna el rol cliente desde el backend. `UsuarioDTO` no incluye contraseña.

### Productos, categorías e inventario

`ProductoDTO` de salida contiene `idProducto`, `codigoProducto`, `nombreProducto`, `descripcion`, `precioProducto`, `enOferta`, `precioOferta`, `stockProducto`, `stockCritico`, `pesoProducto`, `pesoVolumetrico`, `idCategoria` y `nombreCategoria`.

Aunque ese DTO expone `idCategoria` aplanado, los `POST` y `PUT` de producto reciben la entidad `Producto`, cuyo cuerpo espera la relación `categoria` anidada:

```json
{
  "codigoProducto": "FLOR001",
  "nombreProducto": "Ramo de rosas",
  "descripcion": "...",
  "precioProducto": 25000,
  "enOferta": false,
  "precioOferta": null,
  "stockProducto": 10,
  "stockCritico": 2,
  "pesoProducto": 1.5,
  "pesoVolumetrico": 2.0,
  "categoria": { "idCategoria": 1 }
}
```

Oferta (`OfertaDTO`): `{ "enOferta": true, "precioOferta": 20000 }`.

Otros contratos:

- `CategoriaDTO`: `idCategoria`, `nombreCategoria`, `descripcion`. Escrituras reciben entidad `Categoria`.
- `RegionDTO`: `idRegion`, `nombreRegion`. Escrituras reciben entidad `Region`.
- Entidad `Comuna`: `idComuna`, `nombreComuna`, `region: { idRegion, nombreRegion }`.
- `TipoDocumentoDTO`: `idTipoDocumento`, `nombreDocumento`, `descripcion`; escrituras reciben entidad `TipoDocumento`.
- Entidad `MetodoEnvio`: `idMetodoEnvio`, `nombre`, `descripcion`, `costo`, `tiempoEntrega`.
- Entidad `MetodoPago`: `idMetodoPago`, `nombre`, `descripcion`, `estado`.

### Pedidos, detalles y pagos

`CrearPedidoRequest`:

```json
{ "idMetodoEnvio": 1, "idTipoDocumento": 1 }
```

`PedidoDTO`: `idPedido`, `fechaPedido`, `estado`, `total`, `idUsuario`, `idMetodoEnvio`, `idTipoDocumento`.

`CrearDetallePedidoRequest`:

```json
{ "idPedido": 15, "idProducto": 5, "cantidad": 2 }
```

`ActualizarDetallePedidoRequest`: `{ "cantidad": 3 }`.

`DetallePedidoDTO`: `idDetalle`, `cantidad`, `precioUnitario`, `subTotal`, `idPedido`, `idProducto`, `nombreProducto`. El frontend no debe enviar precio ni subtotal.

`CrearPagoRequest`:

```json
{ "idPedido": 15, "idMetodoPago": 1 }
```

`PagoDTO`: `idPago`, `fechaPago`, `monto`, `estadoPago`, `idPedido`, `idMetodoPago`. El cambio de estado recibe `estadoPago` y `motivo` opcional. `PagoEstadoHistorialDTO` incluye `idHistorialPago`, `estadoAnterior`, `estadoNuevo`, `correoActor`, `fechaCambio`, `motivo`.

### Blogs, videos e imágenes

- `BlogRequestDTO`: `{ "titulo": "...", "contenido": "..." }`.
- `BlogEstadoRequestDTO`: `{ "estado": "BORRADOR" }` o `{ "estado": "PUBLICADO" }`.
- `BlogResponseDTO`: `idBlog`, `titulo`, `contenido`, `fechaPublicacion`, `estado`.
- `VideoRequestDTO`: `idBlog`, `titulo`, `urlVideo`, `descripcion`.
- `VideoResponseDTO`: `idVideo`, `idBlog`, `titulo`, `urlVideo`, `descripcion`.
- `ImagenBlogResponseDTO`: `idImagenBlog`, `nombre`, `urlImagen`, `descripcion`, `idBlog`.
- Subida de imagen: `multipart/form-data`, parte `archivo` y parte `descripcion` opcional.
- Las imágenes de producto se exponen como entidad `Imagen`, que incluye una relación `producto`.

## Funcionalidades para el frontend

| Requisito | Estado | Observación |
|---|---|---|
| Listar, consultar, crear, actualizar y eliminar productos | Implementado | Lectura pública, modificaciones ADMINISTRADOR. |
| Listar categorías | Implementado | Público; también existe CRUD administrativo. |
| Registro y login JWT | Implementado | El registro crea CLIENTE; login entrega JWT y usuario. |
| Logout JWT | No implementado como endpoint | Por diseño stateless, el frontend puede borrar su token local; el token emitido no se revoca en servidor. |
| Consultar/actualizar perfil propio | Pendiente | No hay ruta `/me`; `/usuarios/**` es ADMINISTRADOR. |
| Crear compras | Parcial | El pedido y sus detalles se envían en operaciones independientes. |
| Historial propio y pedidos globales para ADMIN | Implementado | `GET /pedidos` cambia el conjunto según rol. |
| Consultas de administración | Parcial | ADMIN puede leer productos y pedidos; no hay agregados ni endpoint de estadísticas. |
| Consultar stock, stock crítico y ofertas | Parcial | Están en `ProductoDTO` y listados públicos; no existe API de inventario separada. |

## Problemas de integración y consideraciones

1. **Perfil:** falta lectura/actualización autenticada del propio usuario. No se debe exponer el `PUT /usuarios/{id}` al cliente: es administrativo y acepta la entidad `Usuario`, incluido `tipoUsuario`.
2. **Formato de producto:** el request usa `categoria.idCategoria`, mientras la respuesta devuelve `idCategoria` y `nombreCategoria` a nivel raíz. No se puede reutilizar la respuesta como request sin adaptar el modelo.
3. **Formato de comuna:** el registro usa `idComuna`; otras escrituras que aceptan entidad `Usuario` reciben `comuna` anidada.
4. **Checkout separado:** el pedido se crea vacío como `PENDIENTE`; luego se agregan detalles individualmente. No hay una transacción que reúna esas solicitudes.
5. **Stock:** crear, aumentar o quitar detalles no consulta ni reserva stock. La pantalla no debe ser la única defensa contra cantidades superiores a existencias.
6. **Costo de envío:** `MetodoEnvio` tiene `costo`, pero el total del pedido se recalcula con subtotales de detalles y no incluye dicho costo. El pago toma el total existente del pedido.
7. **Dirección de entrega:** `Pedido` no guarda una dirección/comuna propia; el usuario mantiene esos datos en su perfil.
8. **Imágenes de producto:** `ProductoDTO` no lista imágenes y no existe ruta para consultar imágenes por producto. `GET /imagenes` devuelve entidades y sus relaciones, lo que puede generar respuestas anidadas/cíclicas y acoplar la salida al modelo JPA.
9. **Exposición de inventario:** el `GET /productos` público incluye `stockProducto` y `stockCritico`. Confirmar si ambos valores deben ser visibles para visitantes.
10. **CORS:** en `SecurityConfig` se permiten `http://localhost:5173` y `http://localhost:8080`, pero no el dominio de Vercel. El navegador puede bloquear llamadas desde producción.
11. **Errores inconsistentes:** no se encontró un `@ControllerAdvice` común. Algunos controladores devuelven cadenas simples, otros propagan `ResponseStatusException` y las fallas de `@Valid` usan el formato predeterminado del framework.
12. **Códigos HTTP reales y documentados:** hay discrepancias; por ejemplo, errores de producto duplicado se documentan como 409 pero el controlador los traduce a 400.
13. **Paginación:** los listados revisados no definen paginación ni filtros consistentes, lo que puede ser costoso en el panel administrativo cuando crezca el catálogo/historial.
14. **Eliminar pago:** Security permite anónimamente `DELETE /pagos...`; el controlador responde 409 siempre. Aunque no elimina datos, la regla de acceso es inconsistente.
15. **Errores de seguridad:** 401 y 403 se envían mediante los handlers de Spring Security; no comparten un sobre JSON con los errores de los controladores.

## Propuesta de contrato API para compartir con frontend

### Contrato vigente

- Base `/api/v1`.
- Enviar JWT en `Authorization: Bearer <accessToken>`.
- Comparar `usuario.tipoUsuario` con `cliente` o `administrador` para presentación de interfaz; el claim del token es mayúsculo.
- Usar los nombres camelCase exactos descritos arriba.
- Para producto, mapear `idCategoria` del estado de React a `categoria: { idCategoria }` al crear/actualizar.
- Para una compra con la API actual: crear pedido, añadir cada detalle y luego crear pago según corresponda. Nunca enviar precios ni totales desde React.
- Tratar `204` como cuerpo vacío.
- Logout: limpiar el token local; no existe endpoint de revocación.

### Contratos por acordar antes de una futura ampliación

Estos son puntos de diseño, **no endpoints implementados**:

- Perfil: `GET /api/v1/usuarios/me` y `PUT /api/v1/usuarios/me`, resolviendo identidad desde JWT y limitando el request a campos editables. No aceptar rol ni `idUsuario` del cliente.
- Compra atómica: definir una operación idempotente que reciba IDs y cantidades, no precio/subtotal/total. Debe acordarse antes la política de stock, dirección de entrega y costos de envío.
- Dashboard: una ruta de resumen administrativo como `/api/v1/admin/dashboard/resumen`, con métricas calculadas por el backend.
- Inventario: definir si el stock es público; si no, separar vista pública de producto y vista administrativa de inventario.
- Imágenes: devolver una colección de referencias de imagen por producto usando DTO, en lugar de serializar relaciones JPA.
- Errores: usar un formato uniforme, por ejemplo `{ "status": 400, "code": "...", "message": "...", "fieldErrors": [] }`, manteniendo códigos HTTP coherentes.

## Archivos revisados

### Controladores

- `src/main/java/cl/isaflor/isaflor/controller/AuthController.java`
- `src/main/java/cl/isaflor/isaflor/controller/UsuarioController.java`
- `src/main/java/cl/isaflor/isaflor/controller/ProductoController.java`
- `src/main/java/cl/isaflor/isaflor/controller/CategoriaController.java`
- `src/main/java/cl/isaflor/isaflor/controller/PedidoController.java`
- `src/main/java/cl/isaflor/isaflor/controller/DetallePedidoController.java`
- `src/main/java/cl/isaflor/isaflor/controller/PagoController.java`
- `src/main/java/cl/isaflor/isaflor/controller/RegionController.java`
- `src/main/java/cl/isaflor/isaflor/controller/ComunaController.java`
- `src/main/java/cl/isaflor/isaflor/controller/TipoDocumentoController.java`
- `src/main/java/cl/isaflor/isaflor/controller/MetodoEnvioController.java`
- `src/main/java/cl/isaflor/isaflor/controller/MetodoPagoController.java`
- `src/main/java/cl/isaflor/isaflor/controller/ImagenController.java`
- `src/main/java/cl/isaflor/isaflor/controller/ImagenBlogController.java`
- `src/main/java/cl/isaflor/isaflor/controller/BlogController.java`
- `src/main/java/cl/isaflor/isaflor/controller/VideoController.java`

### Seguridad, servicios, configuración y contratos

- `src/main/java/cl/isaflor/isaflor/config/SecurityConfig.java`
- `src/main/java/cl/isaflor/isaflor/config/JwtAuthenticationFilter.java`
- `src/main/java/cl/isaflor/isaflor/service/AuthService.java`
- `src/main/java/cl/isaflor/isaflor/service/JwtService.java`
- `src/main/java/cl/isaflor/isaflor/service/UsuarioService.java`
- `src/main/java/cl/isaflor/isaflor/service/UsuarioValidaciones.java`
- `src/main/java/cl/isaflor/isaflor/service/ProductoService.java`
- `src/main/java/cl/isaflor/isaflor/service/ProductoValidaciones.java`
- `src/main/java/cl/isaflor/isaflor/service/PedidoService.java`
- `src/main/java/cl/isaflor/isaflor/service/PedidoValidaciones.java`
- `src/main/java/cl/isaflor/isaflor/service/DetallePedidoService.java`
- `src/main/java/cl/isaflor/isaflor/service/PagoService.java`
- DTO de autenticación, usuarios, productos/ofertas, pedidos/detalles, pagos, categorías, regiones, tipos de documento, blogs, imágenes de blog y videos bajo `src/main/java/cl/isaflor/isaflor/dto/`.
- Entidades `Usuario`, `Producto`, `Pedido`, `DetallePedido`, `Pago`, `Categoria`, `Comuna`, `Region`, `MetodoEnvio`, `MetodoPago`, `TipoDocumento`, `Imagen`, `ImagenBlog`, `Blog` y `Video` bajo `src/main/java/cl/isaflor/isaflor/model/`.
- Repositorios de `Usuario`, `Producto`, `Pedido`, `DetallePedido` y `Pago` bajo `src/main/java/cl/isaflor/isaflor/repository/`.
- `src/main/resources/application.properties`.

## Estado de archivos y validación

El árbol de trabajo ya tenía cambios en `DetallePedidoService.java` y `DetallePedidoServiceTest.java` antes de generar este documento; se conservaron sin cambios. No se ejecutaron pruebas porque esta entrega es documentación de revisión y no modifica código.
