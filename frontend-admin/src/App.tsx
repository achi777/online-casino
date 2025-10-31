import { Routes, Route, Navigate } from 'react-router-dom'
import { Box, ThemeProvider, createTheme, CssBaseline } from '@mui/material'
import { AuthProvider } from './context/AuthContext'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Users from './pages/Users'
import Games from './pages/Games'
import Transactions from './pages/Transactions'
import Reports from './pages/Reports'
import Providers from './pages/Providers'
import KYC from './pages/KYC'
import Bonuses from './pages/Bonuses'
import VIP from './pages/VIP'
import Support from './pages/Support'
import Admins from './pages/Admins'
import AuditLogs from './pages/AuditLogs'
import Settings from './pages/Settings'
import CMS from './pages/CMS'
import PaymentMethods from './pages/PaymentMethods'
import Profile from './pages/Profile'

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
})

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/users" element={<Users />} />
            <Route path="/kyc" element={<KYC />} />
            <Route path="/games" element={<Games />} />
            <Route path="/providers" element={<Providers />} />
            <Route path="/transactions" element={<Transactions />} />
            <Route path="/payment-methods" element={<PaymentMethods />} />
            <Route path="/bonuses" element={<Bonuses />} />
            <Route path="/vip" element={<VIP />} />
            <Route path="/support" element={<Support />} />
            <Route path="/reports" element={<Reports />} />
            <Route path="/cms" element={<CMS />} />
            <Route path="/admins" element={<Admins />} />
            <Route path="/audit-logs" element={<AuditLogs />} />
            <Route path="/settings" element={<Settings />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/" element={<Navigate to="/login" replace />} />
          </Routes>
        </Box>
      </AuthProvider>
    </ThemeProvider>
  )
}

export default App
