import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import axios from 'axios'

interface AuthContextType {
  isAuthenticated: boolean
  user: any | null
  login: (email: string, password: string) => Promise<void>
  register: (data: any) => Promise<void>
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
  const [user, setUser] = useState(null)

  useEffect(() => {
    // Check if user has a valid token
    const token = localStorage.getItem('accessToken')
    if (token) {
      // Set token in axios defaults
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

      // Verify token by making a test request
      axios.get('/api/user/wallet/balance')
        .then(() => {
          setIsAuthenticated(true)
        })
        .catch(() => {
          // Token is invalid or expired, clear everything
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          delete axios.defaults.headers.common['Authorization']
          setIsAuthenticated(false)
        })
    }
  }, [])

  const login = async (email: string, password: string) => {
    // Clear any existing tokens first
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    delete axios.defaults.headers.common['Authorization']

    const response = await axios.post('/api/auth/login', { email, password })
    const { accessToken, refreshToken } = response.data

    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`

    setIsAuthenticated(true)
  }

  const register = async (data: any) => {
    const response = await axios.post('/api/auth/register', data)
    setUser(response.data)
  }

  const logout = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    delete axios.defaults.headers.common['Authorization']
    setIsAuthenticated(false)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
