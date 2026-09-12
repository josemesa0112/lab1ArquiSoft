import { NavLink, Navigate, Route, Routes } from 'react-router-dom'
import RegistrarCliente from './pages/RegistrarCliente.jsx'
import Transaccion from './pages/Transaccion.jsx'
import Historial from './pages/Historial.jsx'

const linkClass = ({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')

export default function App() {
  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <span className="brand-mark">UB</span>
          <div>
            <h1>UdeA Bank</h1>
            <p>Laboratorio 1 &middot; Arquitectura de Software</p>
          </div>
        </div>
        <nav className="nav">
          <NavLink to="/clientes" className={linkClass}>Registrar cliente</NavLink>
          <NavLink to="/transacciones" className={linkClass}>Transacción</NavLink>
          <NavLink to="/historial" className={linkClass}>Historial</NavLink>
        </nav>
      </header>

      <main className="content">
        <Routes>
          <Route path="/" element={<Navigate to="/clientes" replace />} />
          <Route path="/clientes" element={<RegistrarCliente />} />
          <Route path="/transacciones" element={<Transaccion />} />
          <Route path="/historial" element={<Historial />} />
          <Route path="*" element={<Navigate to="/clientes" replace />} />
        </Routes>
      </main>

      <footer className="footer">API: Spring Boot en http://localhost:8088</footer>
    </div>
  )
}
