import { useEffect, useState } from 'react'
import { api, formatDate, formatMoney } from '../api.js'

const ALL = '__ALL__'

export default function Historial() {
  const [customers, setCustomers] = useState([])
  const [account, setAccount] = useState(ALL)
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    api.getCustomers().then(setCustomers).catch((e) => setError(e.message))
  }, [])

  // La API solo expone GET /api/transactions/{accountNumber}. Para ver "todas"
  // consultamos cada cuenta y unificamos por id (una transferencia aparece en
  // la cuenta origen y en la destino).
  const loadTransactions = async (selected, accounts) => {
    setLoading(true)
    setError(null)
    try {
      if (selected === ALL) {
        const lists = await Promise.all(
          accounts.map((c) => api.getTransactionsByAccount(c.accountNumber))
        )
        const byId = new Map()
        lists.flat().forEach((tx) => byId.set(tx.id, tx))
        setTransactions([...byId.values()])
      } else {
        setTransactions(await api.getTransactionsByAccount(selected))
      }
    } catch (e) {
      setError(e.message)
      setTransactions([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (account === ALL && customers.length === 0) {
      setTransactions([])
      return
    }
    loadTransactions(account, customers)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [account, customers])

  const titular = (accountNumber) => {
    const c = customers.find((x) => x.accountNumber === accountNumber)
    return c ? `${c.firstName} ${c.lastName}` : '—'
  }

  const sorted = [...transactions].sort((a, b) => {
    const ta = a.timestamp ? new Date(a.timestamp).getTime() : 0
    const tb = b.timestamp ? new Date(b.timestamp).getTime() : 0
    if (tb !== ta) return tb - ta
    return (b.id ?? 0) - (a.id ?? 0)
  })

  const total = sorted.reduce((acc, tx) => acc + Number(tx.amount ?? 0), 0)

  return (
    <section className="card">
      <div className="card-head">
        <div>
          <h2>Historial de transacciones</h2>
          <p className="muted">GET /api/transactions/{'{accountNumber}'}</p>
        </div>
        <div className="filters">
          <select value={account} onChange={(e) => setAccount(e.target.value)}>
            <option value={ALL}>Todas las cuentas</option>
            {customers.map((c) => (
              <option key={c.id} value={c.accountNumber}>
                {c.accountNumber} — {c.firstName} {c.lastName}
              </option>
            ))}
          </select>
          <button className="btn ghost" onClick={() => loadTransactions(account, customers)} disabled={loading}>
            Refrescar
          </button>
        </div>
      </div>

      {error && <div className="alert error">{error}</div>}

      <div className="stats">
        <div className="stat">
          <span className="stat-label">Movimientos</span>
          <strong>{sorted.length}</strong>
        </div>
        <div className="stat">
          <span className="stat-label">Monto total</span>
          <strong>{formatMoney(total)}</strong>
        </div>
      </div>

      {loading ? (
        <p className="muted">Cargando…</p>
      ) : sorted.length === 0 ? (
        <p className="muted">No hay transacciones para mostrar.</p>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Fecha</th>
                <th>Origen</th>
                <th>Destino</th>
                <th className="right">Monto</th>
                {account !== ALL && <th>Efecto</th>}
              </tr>
            </thead>
            <tbody>
              {sorted.map((tx) => {
                const isSender = tx.senderAccountNumber === account
                return (
                  <tr key={tx.id}>
                    <td>{tx.id}</td>
                    <td>{formatDate(tx.timestamp)}</td>
                    <td>
                      <code>{tx.senderAccountNumber}</code>
                      <span className="sub">{titular(tx.senderAccountNumber)}</span>
                    </td>
                    <td>
                      <code>{tx.receiverAccountNumber}</code>
                      <span className="sub">{titular(tx.receiverAccountNumber)}</span>
                    </td>
                    <td className="right">{formatMoney(tx.amount)}</td>
                    {account !== ALL && (
                      <td>
                        <span className={`badge ${isSender ? 'out' : 'in'}`}>
                          {isSender ? 'Enviado' : 'Recibido'}
                        </span>
                      </td>
                    )}
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
