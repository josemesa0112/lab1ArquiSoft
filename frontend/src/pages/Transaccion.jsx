import { useEffect, useMemo, useState } from 'react'
import { api, formatMoney } from '../api.js'

const EMPTY = { senderAccountNumber: '', receiverAccountNumber: '', amount: '' }

export default function Transaccion() {
  const [form, setForm] = useState(EMPTY)
  const [customers, setCustomers] = useState([])
  const [sending, setSending] = useState(false)
  const [feedback, setFeedback] = useState(null)

  const loadCustomers = async () => {
    try {
      setCustomers(await api.getCustomers())
    } catch (e) {
      setFeedback({ type: 'error', text: `No se pudieron cargar las cuentas: ${e.message}` })
    }
  }

  useEffect(() => {
    loadCustomers()
  }, [])

  const sender = useMemo(
    () => customers.find((c) => c.accountNumber === form.senderAccountNumber),
    [customers, form.senderAccountNumber]
  )

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const validate = () => {
    if (!form.senderAccountNumber) return 'Selecciona la cuenta origen.'
    if (!form.receiverAccountNumber) return 'Selecciona la cuenta destino.'
    if (form.senderAccountNumber === form.receiverAccountNumber)
      return 'La cuenta origen y la destino no pueden ser la misma.'
    if (form.amount === '' || Number.isNaN(Number(form.amount))) return 'El monto debe ser un número.'
    if (Number(form.amount) <= 0) return 'El monto debe ser mayor a cero.'
    if (sender && Number(form.amount) > Number(sender.balance))
      return `Saldo insuficiente: la cuenta ${sender.accountNumber} tiene ${formatMoney(sender.balance)}.`
    return null
  }

  const onSubmit = async (e) => {
    e.preventDefault()
    const error = validate()
    if (error) {
      setFeedback({ type: 'error', text: error })
      return
    }

    setSending(true)
    setFeedback(null)
    try {
      const tx = await api.transfer(form)
      setFeedback({
        type: 'success',
        text: `Transferencia #${tx.id} realizada: ${formatMoney(tx.amount)} de ${tx.senderAccountNumber} a ${tx.receiverAccountNumber}.`,
      })
      setForm(EMPTY)
      loadCustomers() // los saldos cambiaron
    } catch (err) {
      setFeedback({ type: 'error', text: err.message })
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="grid two">
      <section className="card">
        <h2>Realizar transacción</h2>
        <p className="muted">Transfiere dinero entre dos cuentas (POST /api/transactions).</p>

        <form onSubmit={onSubmit} className="form">
          <label>
            Cuenta origen
            <select name="senderAccountNumber" value={form.senderAccountNumber} onChange={onChange}>
              <option value="">Selecciona una cuenta…</option>
              {customers.map((c) => (
                <option key={c.id} value={c.accountNumber}>
                  {c.accountNumber} — {c.firstName} {c.lastName} ({formatMoney(c.balance)})
                </option>
              ))}
            </select>
          </label>

          <label>
            Cuenta destino
            <select name="receiverAccountNumber" value={form.receiverAccountNumber} onChange={onChange}>
              <option value="">Selecciona una cuenta…</option>
              {customers
                .filter((c) => c.accountNumber !== form.senderAccountNumber)
                .map((c) => (
                  <option key={c.id} value={c.accountNumber}>
                    {c.accountNumber} — {c.firstName} {c.lastName}
                  </option>
                ))}
            </select>
          </label>

          <label>
            Monto
            <input name="amount" type="number" step="0.01" min="0" value={form.amount} onChange={onChange} placeholder="150000" />
          </label>

          {sender && (
            <p className="hint">
              Saldo disponible en <code>{sender.accountNumber}</code>: <strong>{formatMoney(sender.balance)}</strong>
            </p>
          )}

          <button type="submit" className="btn primary" disabled={sending}>
            {sending ? 'Procesando…' : 'Transferir'}
          </button>
        </form>

        {feedback && <div className={`alert ${feedback.type}`}>{feedback.text}</div>}
      </section>

      <section className="card">
        <div className="card-head">
          <h2>Saldos actuales</h2>
          <button className="btn ghost" onClick={loadCustomers}>Refrescar</button>
        </div>

        {customers.length === 0 ? (
          <p className="muted">Registra clientes antes de transferir.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Cuenta</th>
                  <th>Titular</th>
                  <th className="right">Saldo</th>
                </tr>
              </thead>
              <tbody>
                {customers.map((c) => (
                  <tr key={c.id}>
                    <td><code>{c.accountNumber}</code></td>
                    <td>{c.firstName} {c.lastName}</td>
                    <td className="right">{formatMoney(c.balance)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
