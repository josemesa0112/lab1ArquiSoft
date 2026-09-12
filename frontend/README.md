# Frontend Lab 1 — Arquitectura de Software (UdeA Bank)

Frontend en React + Vite que consume la API Spring Boot de `lab1arq`. Tres vistas:

| Vista | Ruta | Endpoints que usa |
|---|---|---|
| Registrar cliente | `/clientes` | `GET /api/customers`, `POST /api/customers`, `PUT /api/customers/{id}`, `DELETE /api/customers/{id}` |
| Realizar transacción | `/transacciones` | `POST /api/transactions`, `GET /api/customers` |
| Historial | `/historial` | `GET /api/transactions/{accountNumber}`, `GET /api/customers` |

## Cómo ejecutarlo

1. **Levanta la base de datos y el backend.** MySQL con la base `udeabank` y el
   usuario configurado en `src/main/resources/application.properties`
   (`jose` / `jose123`). Luego, desde la raíz del proyecto:

   ```bash
   ./mvnw spring-boot:run
   ```

   La API queda en `http://localhost:8088`.

2. **Levanta el frontend** (en otra terminal):

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

   Abre `http://localhost:5173`.

## Sobre CORS

No hay que tocar el backend: Vite hace de proxy y reenvía todo lo que empieza
por `/api` a `http://localhost:8088` (ver `vite.config.js`), así que para el
navegador todo viaja en el mismo origen y no se dispara CORS.

Si el backend corre en otro puerto o host:

```bash
# PowerShell
$env:VITE_API_TARGET = "http://localhost:9090"; npm run dev
```

Si en algún momento quieres consumir la API directamente (sin proxy, por
ejemplo desde el build de producción servido en otro dominio), tienes que
habilitar CORS en Spring. Basta con agregar esta clase al backend:

```java
package com.udea.lab1arq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
```

...y compilar el frontend apuntando a la API con `VITE_API_URL`:

```bash
$env:VITE_API_URL = "http://localhost:8088"; npm run build
```

## Notas de integración con la API actual

- `TransactionService.transferMoney` guarda el `timestamp` **que llega en el
  request** (no lo genera el servidor). El frontend envía la hora local en el
  formato que espera Jackson para `LocalDateTime` (`2026-09-04T14:35:12`); si no
  se enviara, la transacción quedaría con fecha `null` en la BD.
- `POST /api/transactions` devuelve el mensaje de error como **texto plano** con
  status 400 (p. ej. "Saldo insuficiente en la cuenta del remitente."). El
  cliente HTTP (`src/api.js`) lo maneja y lo muestra tal cual en la UI.
- No existe un endpoint "todas las transacciones": solo
  `GET /api/transactions/{accountNumber}`. La vista de historial, cuando eliges
  "Todas las cuentas", consulta cada cuenta y unifica los resultados por `id`
  (una transferencia aparece tanto en la cuenta origen como en la destino).
- `PUT /api/customers/{id}` actualiza **solo los campos que llegan** en el body;
  los que van en `null` se dejan como estaban. Devuelve 400 con el mensaje en
  texto plano si el id no existe, si el nuevo número de cuenta ya está tomado o
  si el saldo es negativo.
- `DELETE /api/customers/{id}` devuelve **204 No Content** (sin cuerpo) cuando
  borra, o 400 con el mensaje en texto plano si el id no existe. Borrar un
  cliente **no** borra sus transacciones: la tabla `transactions` guarda números
  de cuenta como texto, sin llave foránea, así que el historial se conserva y esa
  cuenta simplemente aparece sin titular.
- El frontend valida antes de enviar (saldo suficiente, cuentas distintas, monto
  mayor a cero, cuenta no duplicada) para dar feedback inmediato, pero la
  validación real sigue estando en el backend.

## Estructura

```
frontend/
├── index.html
├── package.json
├── vite.config.js          # proxy /api -> localhost:8088
└── src/
    ├── main.jsx            # bootstrap + router
    ├── App.jsx             # layout y navegación entre las 3 vistas
    ├── api.js              # cliente HTTP + formateo de moneda/fecha
    ├── styles.css
    └── pages/
        ├── RegistrarCliente.jsx
        ├── Transaccion.jsx
        └── Historial.jsx
```
