import { useEffect, useState } from 'react'
import { api, formatMoney } from '../api.js'

const EMPTY = { firstName: '', lastName: '', accountNumber: '', balance: '' }

export default function RegistrarCliente() {
  const [form, setForm] = useState(EMPTY)
  const [editingId, setEditingId] = useState(null)
  const [customers, setCustomers] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [deletingId, setDeletingId] = useState(null)
  const [feedback, setFeedback] = useState(null)

  const isEditing = editingId !== null

  const loadCustomers = async () => {
    setLoading(true)
    try {
      setCustomers(await api.getCustomers())
    } catch (e) {
      setFeedback({ type: 'error', text: `No se pudieron cargar los clientes: ${e.message}` })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCustomers()
  }, [])

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const startEdit = (customer) => {
    setEditingId(customer.id)
    setForm({
      firstName: customer.firstName ?? '',
      lastName: customer.lastName ?? '',
      accountNumber: customer.accountNumber ?? '',
      balance: customer.balance ?? '',
    })
    setFeedback(null)
  }

  const cancelEdit = () => {
    setEditingId(null)
    setForm(EMPTY)
    setFeedback(null)
  }

  const validate = () => {
    if (!form.firstName.trim()) return 'El nombre es obligatorio.'
    if (!form.lastName.trim()) return 'El apellido es obligatorio.'
    if (!form.accountNumber.trim()) return 'El número de cuenta es obligatorio.'
    if (form.balance === '' || Number.isNaN(Number(form.balance))) return 'El saldo debe ser un número.'
    if (Number(form.balance) < 0) return 'El saldo no puede ser negativo.'
    // Al editar, la propia cuenta del cliente no cuenta como duplicada.
    if (customers.some((c) => c.accountNumber === form.accountNumber.trim() && c.id !== editingId))
      return 'Ya existe un cliente con ese número de cuenta.'
    return null
  }

  const onSubmit = async (e) => {
    e.preventDefault()
    const error = validate()
    if (error) {
      setFeedback({ type: 'error', text: error })
      return
    }

    const payload = {
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      accountNumber: form.accountNumber.trim(),
      balance: form.balance,
    }

    setSaving(true)
    setFeedback(null)
    try {
      if (isEditing) {
        const updated = await api.updateCustomer(editingId, payload)
        setFeedback({
          type: 'success',
          text: `Cliente ${updated.firstName} ${updated.lastName} actualizado (cuenta ${updated.accountNumber}).`,
        })
        setEditingId(null)
      } else {
        const created = await api.createCustomer(payload)
        setFeedback({
          type: 'success',
          text: `Cliente ${created.firstName} ${created.lastName} registrado con la cuenta ${created.accountNumber}.`,
        })
      }
      setForm(EMPTY)
      loadCustomers()
    } catch (err) {
      setFeedback({ type: 'error', text: err.message })
    } finally {
      setSaving(false)
    }
  }

  const onDelete = async (customer) => {
    const ok = window.confirm(
      `¿Borrar a ${customer.firstName} ${customer.lastName} (cuenta ${customer.accountNumber})?\n\n` +
        'Esta acción no se puede deshacer. Las transacciones ya registradas se conservan en el historial.'
    )
    if (!ok) return

    setDeletingId(customer.id)
    setFeedback(null)
    try {
      await api.deleteCustomer(customer.id)
      setFeedback({
        type: 'success',
        text: `Cliente ${customer.firstName} ${customer.lastName} eliminado.`,
      })
      if (editingId === customer.id) {
        setEditingId(null)
        setForm(EMPTY)
      }
      loadCustomers()
    } catch (err) {
      setFeedback({ type: 'error', text: err.message })
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <div className="grid two">
      <section className="card">
        <h2>{isEditing ? `Editar cliente #${editingId}` : 'Registrar cliente'}</h2>
        <p className="muted">
          {isEditing
            ? `Actualiza los datos del cliente (PUT /api/customers/${editingId}).`
            : 'Crea una cuenta nueva en el banco (POST /api/customers).'}
        </p>

        <form onSubmit={onSubmit} className="form">
          <label>
            Nombre
            <input name="firstName" value={form.firstName} onChange={onChange} maxLength={50} placeholder="Jose" />
          </label>

          <label>
            Apellido
            <input name="lastName" value={form.lastName} onChange={onChange} maxLength={50} placeholder="Mesa" />
          </label>

          <label>
            Número de cuenta
            <input name="accountNumber" value={form.accountNumber} onChange={onChange} placeholder="1001" />
          </label>

          <label>
            {isEditing ? 'Saldo' : 'Saldo inicial'}
            <input name="balance" type="number" step="0.01" min="0" value={form.balance} onChange={onChange} placeholder="500000" />
          </label>

          <div className="form-actions">
            <button type="submit" className="btn primary" disabled={saving}>
              {saving ? 'Guardando…' : isEditing ? 'Guardar cambios' : 'Registrar cliente'}
            </button>
            {isEditing && (
              <button type="button" className="btn ghost" onClick={cancelEdit} disabled={saving}>
                Cancelar
              </button>
            )}
          </div>
        </form>

        {feedback && <div className={`alert ${feedback.type}`}>{feedback.text}</div>}
      </section>

      <section className="card">
        <div className="card-head">
          <h2>Clientes registrados</h2>
          <button className="btn ghost" onClick={loadCustomers} disabled={loading}>Refrescar</button>
        </div>

        {loading ? (
          <p className="muted">Cargando…</p>
        ) : customers.length === 0 ? (
          <p className="muted">Todavía no hay clientes registrados.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Cliente</th>
                  <th>Cuenta</th>
                  <th className="right">Saldo</th>
                  <th className="right">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {customers.map((c) => (
                  <tr key={c.id} className={c.id === editingId ? 'row-editing' : undefined}>
                    <td>{c.id}</td>
                    <td>{c.firstName} {c.lastName}</td>
                    <td><code>{c.accountNumber}</code></td>
                    <td className="right">{formatMoney(c.balance)}</td>
                    <td className="right">
                      <div className="row-actions">
                        <button
                          className="btn ghost small"
                          onClick={() => startEdit(c)}
                          disabled={saving || deletingId === c.id}
                        >
                          Editar
                        </button>
                        <button
                          className="btn danger small"
                          onClick={() => onDelete(c)}
                          disabled={deletingId === c.id}
                        >
                          {deletingId === c.id ? 'Borrando…' : 'Borrar'}
                        </button>
                      </div>
                    </td>
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