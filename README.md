[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=coverage)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)
[![Maintainability issues](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=software_quality_maintainability_issues)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)
[![Reliability issues](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=software_quality_reliability_issues)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)
[![Security issues](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=software_quality_security_issues)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=josemesa0112_lab1ArquiSoft&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=josemesa0112_lab1ArquiSoft)

# lab1ArquiSoft

Primer laboratorio entregable de arquisoft con las operaciones básicas en transacciones de una app bancaria.

El proyecto tiene dos partes:

- **Backend:** API REST en Spring Boot + JPA sobre MySQL (carpeta `src/`).
- **Frontend:** SPA en React + Vite con tres vistas (carpeta `frontend/`).

## Stack

| Componente | Tecnología |
|---|---|
| Backend | Java 17, Spring Boot 4.0.8, Spring Data JPA, MapStruct, Lombok |
| Base de datos | MySQL (esquema `udeabank`) |
| Frontend | React 18, React Router, Vite |

## Requisitos

- JDK 17 o superior (con `JAVA_HOME` configurado)
- MySQL en `localhost:3306`
- Node.js 18 o superior

## Base de datos

Crea el esquema y el usuario que espera `src/main/resources/application.properties`:

```sql
CREATE DATABASE udeabank;
CREATE USER 'jose'@'localhost' IDENTIFIED BY 'jose123';
GRANT ALL PRIVILEGES ON udeabank.* TO 'jose'@'localhost';
FLUSH PRIVILEGES;
```

Las tablas `customers` y `transactions` las crea Hibernate solo al arrancar
(`spring.jpa.hibernate.ddl-auto=update`).

## Cómo ejecutarlo

**1. Backend** (desde la raíz del proyecto):

```bash
./mvnw spring-boot:run
```

La API queda en `http://localhost:8088`.

**2. Frontend** (en otra terminal):

```bash
cd frontend
npm install
npm run dev
```

La interfaz queda en `http://localhost:5173`.

No hay que configurar CORS: Vite reenvía todo lo que empieza por `/api` hacia
`http://localhost:8088`, así que para el navegador todo viaja en el mismo
origen. Los detalles están en [`frontend/README.md`](frontend/README.md).

## API

### Clientes — `/api/customers`

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/customers` | Lista todos los clientes |
| `GET` | `/api/customers/{id}` | Consulta un cliente por su id |
| `POST` | `/api/customers` | Registra un cliente nuevo |
| `PUT` | `/api/customers/{id}` | Actualiza los datos de un cliente |
| `DELETE` | `/api/customers/{id}` | Borra un cliente (204 No Content) |

Cuerpo de `POST` y `PUT`:

```json
{
  "firstName": "Ana",
  "lastName": "Ruiz",
  "accountNumber": "1001",
  "balance": 500000
}
```

El `PUT` es una actualización parcial: solo cambia los campos que llegan con
valor, los que van en `null` se dejan como estaban. Valida que el cliente
exista, que el número de cuenta no esté tomado por otro cliente (la columna es
`unique`) y que el saldo no sea negativo.

### Transacciones — `/api/transactions`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/transactions` | Transfiere dinero entre dos cuentas |
| `GET` | `/api/transactions/{accountNumber}` | Movimientos de una cuenta (enviados y recibidos) |

Cuerpo de `POST`:

```json
{
  "senderAccountNumber": "1001",
  "receiverAccountNumber": "1002",
  "amount": 150000,
  "timestamp": "2026-09-12T10:30:00"
}
```

La transferencia valida que ambas cuentas existan y que el remitente tenga saldo
suficiente; descuenta del origen, abona al destino y guarda el movimiento. Los
errores vuelven con status 400 y el mensaje en texto plano.

## Vistas del frontend

| Vista | Ruta | Qué hace |
|---|---|---|
| Registrar cliente | `/clientes` | Crea, edita y borra clientes; lista los registrados con su saldo |
| Realizar transacción | `/transacciones` | Transfiere entre cuentas, con los saldos actuales a la vista |
| Historial | `/historial` | Muestra las transacciones, filtrables por cuenta |

## Estructura

```
lab1arq/
├── pom.xml
├── src/main/java/com/udea/lab1arq/
│   ├── controller/     # CustomerController, TransactionController
│   ├── service/        # CustomerService, TransactionService
│   ├── repository/     # CustomerRepository, TransactionRepository
│   ├── entity/         # Customer, Transaction
│   ├── DTO/            # CustomerDTO, TransactionDTO, TransferRequestDTO
│   └── mapper/         # CustomerMapper, TransactionMapper (MapStruct)
├── src/main/resources/
│   └── application.properties
└── frontend/
    ├── vite.config.js  # proxy /api -> localhost:8088
    └── src/
        ├── App.jsx     # layout y navegación
        ├── api.js      # cliente HTTP
        └── pages/      # RegistrarCliente, Transaccion, Historial
```

## Notas

- La tabla `transactions` guarda los números de cuenta como texto, sin llave
  foránea hacia `customers`. Borrar un cliente no borra sus movimientos: el
  historial se conserva y esa cuenta aparece sin titular.
- El `timestamp` de una transacción es el que llega en el request, no lo genera
  el servidor. El frontend lo envía en el formato que espera Jackson para
  `LocalDateTime` (`2026-09-12T10:30:00`).