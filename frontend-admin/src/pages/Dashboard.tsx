import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Grid, Card, CardContent, Button
} from '@mui/material'
import {
  People as PeopleIcon,
  SportsEsports as GamesIcon,
  Payment as PaymentIcon,
  AttachMoney as MoneyIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import axios from 'axios'

interface DashboardStats {
  totalUsers: number
  activeUsers: number
  pendingKYC: number
  totalGames: number
  activeGames: number
  totalDepositsToday: number
  totalWithdrawalsToday: number
  totalBetsToday: number
  ggrToday: number
  totalDepositsMonth: number
  totalWithdrawalsMonth: number
  totalBetsMonth: number
  ggrMonth: number
  pendingWithdrawals: number
}

const Dashboard = () => {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [stats, setStats] = useState<DashboardStats | null>(null)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else {
      fetchStats()
    }
  }, [isAuthenticated, navigate])

  const fetchStats = async () => {
    try {
      const response = await axios.get('/api/admin/dashboard/stats')
      setStats(response.data)
    } catch (err) {
      console.error('Failed to fetch stats:', err)
    }
  }

  const formatCurrency = (amount: number) => {
    return `₾${amount.toFixed(2)}`
  }

  return (
    <AdminLayout title="Dashboard">

        {stats ? (
          <Grid container spacing={3}>
            {/* Today's Stats */}
            <Grid item xs={12}>
              <Typography variant="h5" gutterBottom>
                Today's Statistics
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <TrendingUpIcon color="success" />
                    <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                      Deposits Today
                    </Typography>
                  </Box>
                  <Typography variant="h4">
                    {formatCurrency(stats.totalDepositsToday)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <TrendingDownIcon color="error" />
                    <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                      Withdrawals Today
                    </Typography>
                  </Box>
                  <Typography variant="h4">
                    {formatCurrency(stats.totalWithdrawalsToday)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <MoneyIcon color="primary" />
                    <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                      Bets Today
                    </Typography>
                  </Box>
                  <Typography variant="h4">
                    {formatCurrency(stats.totalBetsToday)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <TrendingUpIcon color="success" />
                    <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                      GGR Today
                    </Typography>
                  </Box>
                  <Typography variant="h4" color="success.main">
                    {formatCurrency(stats.ggrToday)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            {/* Monthly Stats */}
            <Grid item xs={12} sx={{ mt: 3 }}>
              <Typography variant="h5" gutterBottom>
                This Month
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography variant="body2" color="text.secondary">
                    Deposits
                  </Typography>
                  <Typography variant="h5">
                    {formatCurrency(stats.totalDepositsMonth)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography variant="body2" color="text.secondary">
                    Withdrawals
                  </Typography>
                  <Typography variant="h5">
                    {formatCurrency(stats.totalWithdrawalsMonth)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography variant="body2" color="text.secondary">
                    Total Bets
                  </Typography>
                  <Typography variant="h5">
                    {formatCurrency(stats.totalBetsMonth)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Typography variant="body2" color="text.secondary">
                    GGR Month
                  </Typography>
                  <Typography variant="h5" color="success.main">
                    {formatCurrency(stats.ggrMonth)}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            {/* Users & Games */}
            <Grid item xs={12} sx={{ mt: 3 }}>
              <Typography variant="h5" gutterBottom>
                Platform Overview
              </Typography>
            </Grid>

            <Grid item xs={12} sm={6} md={4}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <PeopleIcon color="primary" />
                    <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                      Users
                    </Typography>
                  </Box>
                  <Typography variant="h4">{stats.totalUsers}</Typography>
                  <Typography variant="body2" color="success.main">
                    Active: {stats.activeUsers}
                  </Typography>
                  <Typography variant="body2" color="warning.main">
                    Pending KYC: {stats.pendingKYC}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={4}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <GamesIcon color="primary" />
                    <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                      Games
                    </Typography>
                  </Box>
                  <Typography variant="h4">{stats.totalGames}</Typography>
                  <Typography variant="body2" color="success.main">
                    Active: {stats.activeGames}
                  </Typography>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={4}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                    <PaymentIcon color="warning" />
                    <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                      Pending Withdrawals
                    </Typography>
                  </Box>
                  <Typography variant="h4" color="warning.main">
                    {stats.pendingWithdrawals}
                  </Typography>
                  <Button
                    variant="outlined"
                    size="small"
                    sx={{ mt: 1 }}
                    onClick={() => navigate('/transactions?filter=withdrawals')}
                  >
                    Review
                  </Button>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        ) : (
          <Typography>Loading statistics...</Typography>
        )}
    </AdminLayout>
  )
}

export default Dashboard
