// Cliente HTTP contra la API de lab1arq (Spring Boot, puerto 8088).
// En dev las rutas /api/** las reenvía el proxy de Vite (ver vite.config.js).
const BASE = import.meta.env.VITE_API_URL ?? ''

async function request(path, options = {}) {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })

  const raw = await res.text()

  if (!res.ok) {
    // El TransactionController devuelve el mensaje de error como texto plano.
    let message = raw
    try {
      const parsed = JSON.parse(raw)
      message = parsed.message || parsed.error || raw
    } catch {
      /* respuesta en texto plano */
    }
    throw new Error(message || `Error ${res.status}`)
  }

  return raw ? JSON.parse(raw) : null
}

// Formato que espera Jackson para LocalDateTime: 2026-09-04T14:35:12
function localDateTimeNow() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return (
    `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` +
    `T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  )
}

export const api = {
  getCustomers: () => request('/api/customers'),

  getCustomer: (id) => request(`/api/customers/${id}`),

  createCustomer: (customer) =>
    request('/api/customers', {
      method: 'POST',
      body: JSON.stringify({
        firstName: customer.firstName,
        lastName: customer.lastName,
        accountNumber: customer.accountNumber,
        balance: Number(customer.balance),
      }),
    }),

  updateCustomer: (id, customer) =>
    request(`/api/customers/${id}`, {
      method: 'PUT',
      body: JSON.stringify({
        firstName: customer.firstName,
        lastName: customer.lastName,
        accountNumber: customer.accountNumber,
        balance: Number(customer.balance),
      }),
    }),

  deleteCustomer: (id) =>
    request(`/api/customers/${id}`, { method: 'DELETE' }),

  transfer: ({ senderAccountNumber, receiverAccountNumber, amount }) =>
    request('/api/transactions', {
      method: 'POST',
      body: JSON.stringify({
        senderAccountNumber,
        receiverAccountNumber,
        amount: Number(amount),
        timestamp: localDateTimeNow(),
      }),
    }),

  getTransactionsByAccount: (accountNumber) =>
    request(`/api/transactions/${encodeURIComponent(accountNumber)}`),
}

export const formatMoney = (value) =>
  new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 2,
  }).format(Number(value ?? 0))

export const formatDate = (value) => {
  if (!value) return '—'
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? value : d.toLocaleString('es-CO')
}
