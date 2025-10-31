import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import axios from 'axios'

interface Admin {
  id: number
  username: string
  email: string
  firstName: string
  lastName: string
  role: string
  status: string
}

interface AuthContextType {
  isAuthenticated: boolean
  admin: Admin | null
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [admin, setAdmin] = useState<Admin | null>(null)

  useEffect(() => {
    // Check if admin has a valid token
    const token = localStorage.getItem('adminAccessToken')
    if (token) {
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      setIsAuthenticated(true)
    }
  }, [])

  const login = async (username: string, password: string) => {
    console.log('AuthContext: Starting login for:', username)
    // Clear any existing tokens first
    localStorage.removeItem('adminAccessToken')
    localStorage.removeItem('adminRefreshToken')
    delete axios.defaults.headers.common['Authorization']

    console.log('AuthContext: Making API request to /api/admin/auth/login')
    const response = await axios.post('/api/admin/auth/login', { username, password })
    console.log('AuthContext: API response:', response.data)

    const { accessToken, refreshToken } = response.data

    localStorage.setItem('adminAccessToken', accessToken)
    localStorage.setItem('adminRefreshToken', refreshToken)
    axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`

    setIsAuthenticated(true)
    console.log('AuthContext: Login completed successfully')
  }

  const logout = () => {
    localStorage.removeItem('adminAccessToken')
    localStorage.removeItem('adminRefreshToken')
    delete axios.defaults.headers.common['Authorization']
    setIsAuthenticated(false)
    setAdmin(null)
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, admin, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
