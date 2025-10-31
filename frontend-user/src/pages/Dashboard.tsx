import { Container, Typography, Box, Paper, Grid, CircularProgress } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { useQuery } from 'react-query'
import Navbar from '../components/Navbar'
import SportsEsportsIcon from '@mui/icons-material/SportsEsports'
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet'
import SecurityIcon from '@mui/icons-material/Security'
import AccountCircleIcon from '@mui/icons-material/AccountCircle'
import HistoryIcon from '@mui/icons-material/History'
import TrendingUpIcon from '@mui/icons-material/TrendingUp'
import AttachMoneyIcon from '@mui/icons-material/AttachMoney'

const Dashboard = () => {
  const navigate = useNavigate()

  const { data: balance, isLoading: balanceLoading } = useQuery('balance', async () => {
    const response = await axios.get('/api/user/wallet/balance')
    return response.data
  })

  const { data: transactions } = useQuery('recentTransactions', async () => {
    const response = await axios.get('/api/user/wallet/transactions')
    return response.data.content?.slice(0, 5) || []
  })

  const { data: gameSessions } = useQuery('recentSessions', async () => {
    const response = await axios.get('/api/user/game-history')
    return response.data.content?.slice(0, 5) || []
  })

  return (
    <>
      <Navbar />
      <Container maxWidth="lg">
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Welcome to Your Dashboard
          </Typography>
          <Typography color="text.secondary">
            Quick overview of your casino account
          </Typography>
        </Box>

        {/* Balance Overview */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <AccountBalanceWalletIcon sx={{ mr: 1, fontSize: 32 }} />
                <Typography variant="h6">Current Balance</Typography>
              </Box>
              {balanceLoading ? (
                <CircularProgress size={24} />
              ) : (
                <Typography variant="h3">₾{balance?.toFixed(2) || '0.00'}</Typography>
              )}
            </Paper>
          </Grid>
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <TrendingUpIcon sx={{ mr: 1, fontSize: 32 }} />
                <Typography variant="h6">Total Bets</Typography>
              </Box>
              <Typography variant="h3">{gameSessions?.length || 0}</Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <AttachMoneyIcon sx={{ mr: 1, fontSize: 32 }} />
                <Typography variant="h6">Transactions</Typography>
              </Box>
              <Typography variant="h3">{transactions?.length || 0}</Typography>
            </Paper>
          </Grid>
        </Grid>

        {/* Quick Actions */}
        <Typography variant="h5" gutterBottom sx={{ mb: 2 }}>
          Quick Actions
        </Typography>
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={6} md={3}>
            <Paper
              sx={{
                p: 3,
                cursor: 'pointer',
                transition: 'transform 0.2s',
                '&:hover': { transform: 'translateY(-5px)' }
              }}
              onClick={() => navigate('/games')}
            >
              <SportsEsportsIcon sx={{ fontSize: 48, mb: 1, color: 'primary.main' }} />
              <Typography variant="h6">Play Games</Typography>
              <Typography color="text.secondary">Browse casino games</Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Paper
              sx={{
                p: 3,
                cursor: 'pointer',
                transition: 'transform 0.2s',
                '&:hover': { transform: 'translateY(-5px)' }
              }}
              onClick={() => navigate('/wallet')}
            >
              <AccountBalanceWalletIcon sx={{ fontSize: 48, mb: 1, color: 'secondary.main' }} />
              <Typography variant="h6">Wallet</Typography>
              <Typography color="text.secondary">Manage your balance</Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Paper
              sx={{
                p: 3,
                cursor: 'pointer',
                transition: 'transform 0.2s',
                '&:hover': { transform: 'translateY(-5px)' }
              }}
              onClick={() => navigate('/game-history')}
            >
              <HistoryIcon sx={{ fontSize: 48, mb: 1, color: 'info.main' }} />
              <Typography variant="h6">Game History</Typography>
              <Typography color="text.secondary">View your sessions</Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <Paper
              sx={{
                p: 3,
                cursor: 'pointer',
                transition: 'transform 0.2s',
                '&:hover': { transform: 'translateY(-5px)' }
              }}
              onClick={() => navigate('/profile')}
            >
              <AccountCircleIcon sx={{ fontSize: 48, mb: 1, color: 'success.main' }} />
              <Typography variant="h6">My Profile</Typography>
              <Typography color="text.secondary">Manage account</Typography>
            </Paper>
          </Grid>
        </Grid>

        {/* Recent Activity */}
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                Recent Transactions
              </Typography>
              {transactions?.length === 0 ? (
                <Typography color="text.secondary">No transactions yet</Typography>
              ) : (
                transactions?.map((tx: any) => (
                  <Box key={tx.id} sx={{ mb: 2, pb: 2, borderBottom: '1px solid #444' }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography>{tx.type}</Typography>
                      <Typography color={tx.type === 'DEPOSIT' || tx.type === 'WIN' ? 'success.main' : 'error.main'}>
                        {tx.type === 'DEPOSIT' || tx.type === 'WIN' ? '+' : '-'}₾{tx.amount}
                      </Typography>
                    </Box>
                    <Typography variant="caption" color="text.secondary">
                      {new Date(tx.createdAt).toLocaleString()}
                    </Typography>
                  </Box>
                ))
              )}
            </Paper>
          </Grid>
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                Recent Game Sessions
              </Typography>
              {gameSessions?.length === 0 ? (
                <Typography color="text.secondary">No game sessions yet</Typography>
              ) : (
                gameSessions?.map((session: any) => (
                  <Box key={session.id} sx={{ mb: 2, pb: 2, borderBottom: '1px solid #444' }}>
                    <Typography>{session.gameName}</Typography>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography variant="caption" color="text.secondary">
                        Status: {session.status}
                      </Typography>
                      <Typography variant="caption">
                        Bet: ₾{session.totalBet || 0}
                      </Typography>
                    </Box>
                  </Box>
                ))
              )}
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </>
  )
}

export default Dashboard
