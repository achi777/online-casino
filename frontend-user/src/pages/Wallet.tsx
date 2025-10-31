import { useState } from 'react'
import {
  Container,
  Typography,
  Box,
  Paper,
  TextField,
  Button,
  Grid,
  Alert,
  Tabs,
  Tab,
  CircularProgress,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem
} from '@mui/material'
import axios from 'axios'
import { useQuery } from 'react-query'
import Navbar from '../components/Navbar'
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet'
import AddIcon from '@mui/icons-material/Add'
import RemoveIcon from '@mui/icons-material/Remove'

interface TabPanelProps {
  children?: React.ReactNode
  index: number
  value: number
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props
  return (
    <div hidden={value !== index} {...other}>
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  )
}

const Wallet = () => {
  const [tabValue, setTabValue] = useState(0)
  const [depositAmount, setDepositAmount] = useState('')
  const [withdrawAmount, setWithdrawAmount] = useState('')
  const [paymentMethod, setPaymentMethod] = useState('CARD')
  const [message, setMessage] = useState('')
  const [messageType, setMessageType] = useState<'success' | 'error' | 'info'>('info')
  const [loading, setLoading] = useState(false)

  const { data: balance, refetch, isLoading: balanceLoading } = useQuery('balance', async () => {
    const response = await axios.get('/api/user/wallet/balance')
    return response.data
  })

  const { data: transactions, refetch: refetchTransactions } = useQuery('transactions', async () => {
    const response = await axios.get('/api/user/wallet/transactions')
    return response.data.content
  })

  const handleDeposit = async () => {
    if (!depositAmount || parseFloat(depositAmount) <= 0) {
      setMessage('Please enter a valid amount')
      setMessageType('error')
      return
    }

    setLoading(true)
    setMessage('')
    try {
      await axios.post('/api/user/wallet/deposit', {
        amount: parseFloat(depositAmount),
        paymentMethod,
      })
      setMessage('Deposit successful!')
      setMessageType('success')
      refetch()
      refetchTransactions()
      setDepositAmount('')
    } catch (error: any) {
      setMessage(error.response?.data?.error || 'Deposit failed')
      setMessageType('error')
    } finally {
      setLoading(false)
    }
  }

  const handleWithdraw = async () => {
    if (!withdrawAmount || parseFloat(withdrawAmount) <= 0) {
      setMessage('Please enter a valid amount')
      setMessageType('error')
      return
    }

    if (parseFloat(withdrawAmount) > balance) {
      setMessage('Insufficient balance')
      setMessageType('error')
      return
    }

    setLoading(true)
    setMessage('')
    try {
      await axios.post('/api/user/wallet/withdraw', {
        amount: parseFloat(withdrawAmount),
        paymentMethod,
      })
      setMessage('Withdrawal successful!')
      setMessageType('success')
      refetch()
      refetchTransactions()
      setWithdrawAmount('')
    } catch (error: any) {
      setMessage(error.response?.data?.error || 'Withdrawal failed')
      setMessageType('error')
    } finally {
      setLoading(false)
    }
  }

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue)
    setMessage('')
  }

  return (
    <>
      <Navbar />
      <Container maxWidth="lg">
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Wallet Management
          </Typography>
          <Typography color="text.secondary">
            Manage your deposits and withdrawals
          </Typography>
        </Box>

        <Grid container spacing={3}>
          {/* Balance Card */}
          <Grid item xs={12}>
            <Paper sx={{ p: 3, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <AccountBalanceWalletIcon sx={{ fontSize: 48, mr: 2 }} />
                  <Box>
                    <Typography variant="h6">Current Balance</Typography>
                    {balanceLoading ? (
                      <CircularProgress size={24} />
                    ) : (
                      <Typography variant="h3">₾{balance?.toFixed(2) || '0.00'}</Typography>
                    )}
                  </Box>
                </Box>
              </Box>
            </Paper>
          </Grid>

          {/* Deposit/Withdraw Section */}
          <Grid item xs={12} md={6}>
            <Paper>
              <Tabs value={tabValue} onChange={handleTabChange} centered>
                <Tab icon={<AddIcon />} label="Deposit" />
                <Tab icon={<RemoveIcon />} label="Withdraw" />
              </Tabs>

              {message && (
                <Alert severity={messageType} sx={{ m: 2 }} onClose={() => setMessage('')}>
                  {message}
                </Alert>
              )}

              <TabPanel value={tabValue} index={0}>
                <Box>
                  <FormControl fullWidth margin="normal">
                    <InputLabel>Payment Method</InputLabel>
                    <Select
                      value={paymentMethod}
                      label="Payment Method"
                      onChange={(e) => setPaymentMethod(e.target.value)}
                    >
                      <MenuItem value="CARD">Credit/Debit Card</MenuItem>
                      <MenuItem value="BANK_TRANSFER">Bank Transfer</MenuItem>
                      <MenuItem value="E_WALLET">E-Wallet</MenuItem>
                      <MenuItem value="CRYPTO">Cryptocurrency</MenuItem>
                    </Select>
                  </FormControl>
                  <TextField
                    fullWidth
                    label="Amount"
                    type="number"
                    value={depositAmount}
                    onChange={(e) => setDepositAmount(e.target.value)}
                    margin="normal"
                    InputProps={{
                      startAdornment: <InputAdornment position="start">₾</InputAdornment>,
                    }}
                  />
                  <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                    {[10, 50, 100, 500].map((amount) => (
                      <Button
                        key={amount}
                        variant="outlined"
                        size="small"
                        onClick={() => setDepositAmount(amount.toString())}
                      >
                        ₾{amount}
                      </Button>
                    ))}
                  </Box>
                  <Button
                    fullWidth
                    variant="contained"
                    onClick={handleDeposit}
                    disabled={loading}
                    startIcon={loading ? <CircularProgress size={20} /> : <AddIcon />}
                    size="large"
                  >
                    {loading ? 'Processing...' : 'Deposit'}
                  </Button>
                </Box>
              </TabPanel>

              <TabPanel value={tabValue} index={1}>
                <Box>
                  <FormControl fullWidth margin="normal">
                    <InputLabel>Payment Method</InputLabel>
                    <Select
                      value={paymentMethod}
                      label="Payment Method"
                      onChange={(e) => setPaymentMethod(e.target.value)}
                    >
                      <MenuItem value="CARD">Credit/Debit Card</MenuItem>
                      <MenuItem value="BANK_TRANSFER">Bank Transfer</MenuItem>
                      <MenuItem value="E_WALLET">E-Wallet</MenuItem>
                      <MenuItem value="CRYPTO">Cryptocurrency</MenuItem>
                    </Select>
                  </FormControl>
                  <TextField
                    fullWidth
                    label="Amount"
                    type="number"
                    value={withdrawAmount}
                    onChange={(e) => setWithdrawAmount(e.target.value)}
                    margin="normal"
                    InputProps={{
                      startAdornment: <InputAdornment position="start">₾</InputAdornment>,
                    }}
                    helperText={`Available: ₾${balance?.toFixed(2) || '0.00'}`}
                  />
                  <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                    {[10, 50, 100, 500].map((amount) => (
                      <Button
                        key={amount}
                        variant="outlined"
                        size="small"
                        onClick={() => setWithdrawAmount(amount.toString())}
                        disabled={amount > balance}
                      >
                        ₾{amount}
                      </Button>
                    ))}
                  </Box>
                  <Button
                    fullWidth
                    variant="contained"
                    onClick={handleWithdraw}
                    disabled={loading}
                    startIcon={loading ? <CircularProgress size={20} /> : <RemoveIcon />}
                    size="large"
                    color="secondary"
                  >
                    {loading ? 'Processing...' : 'Withdraw'}
                  </Button>
                </Box>
              </TabPanel>
            </Paper>
          </Grid>

          {/* Transaction History */}
          <Grid item xs={12} md={6}>
            <Paper sx={{ p: 3, height: '100%' }}>
              <Typography variant="h6" gutterBottom>
                Recent Transactions
              </Typography>
              <Box sx={{ maxHeight: 400, overflow: 'auto' }}>
                {!transactions || transactions.length === 0 ? (
                  <Typography color="text.secondary">No transactions yet</Typography>
                ) : (
                  transactions?.map((tx: any) => (
                    <Box key={tx.id} sx={{ mb: 2, pb: 2, borderBottom: '1px solid #444' }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                        <Typography variant="body1">{tx.type}</Typography>
                        <Typography
                          variant="body1"
                          fontWeight="bold"
                          color={
                            tx.type === 'DEPOSIT' || tx.type === 'WIN'
                              ? 'success.main'
                              : 'error.main'
                          }
                        >
                          {tx.type === 'DEPOSIT' || tx.type === 'WIN' ? '+' : '-'}₾{tx.amount}
                        </Typography>
                      </Box>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                        <Typography variant="caption" color="text.secondary">
                          {new Date(tx.createdAt).toLocaleString()}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {tx.status || 'COMPLETED'}
                        </Typography>
                      </Box>
                    </Box>
                  ))
                )}
              </Box>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </>
  )
}

export default Wallet
