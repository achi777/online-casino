import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Button, Grid, Card, CardContent, Switch,
  FormControlLabel, Divider, TextField, Chip
} from '@mui/material'
import {
  CreditCard as CardIcon,
  AccountBalance as BankIcon,
  AccountBalanceWallet as WalletIcon,
  CurrencyBitcoin as CryptoIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'

interface PaymentMethod {
  id: string
  name: string
  type: string
  enabled: boolean
  minAmount: number
  maxAmount: number
  fee: number
  processingTime: string
  icon: React.ReactNode
}

const PaymentMethods = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()

  const [paymentMethods, setPaymentMethods] = useState<PaymentMethod[]>([
    {
      id: 'bank_card',
      name: 'Bank Card (Visa/Mastercard)',
      type: 'CARD',
      enabled: true,
      minAmount: 10,
      maxAmount: 10000,
      fee: 2,
      processingTime: 'Instant',
      icon: <CardIcon />
    },
    {
      id: 'bank_transfer',
      name: 'Bank Transfer',
      type: 'BANK',
      enabled: true,
      minAmount: 50,
      maxAmount: 50000,
      fee: 0,
      processingTime: '1-2 business days',
      icon: <BankIcon />
    },
    {
      id: 'e_wallet',
      name: 'E-Wallet (Skrill, Neteller)',
      type: 'EWALLET',
      enabled: false,
      minAmount: 10,
      maxAmount: 5000,
      fee: 1.5,
      processingTime: 'Instant',
      icon: <WalletIcon />
    },
    {
      id: 'crypto',
      name: 'Cryptocurrency',
      type: 'CRYPTO',
      enabled: false,
      minAmount: 20,
      maxAmount: 100000,
      fee: 0.5,
      processingTime: '10-30 minutes',
      icon: <CryptoIcon />
    },
  ])

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canViewPaymentMethods) {
      navigate('/dashboard')
    }
  }, [isAuthenticated, permissions, navigate])

  const handleToggle = (methodId: string) => {
    if (!permissions.canManagePaymentMethods) return

    setPaymentMethods(prev =>
      prev.map(method =>
        method.id === methodId ? { ...method, enabled: !method.enabled } : method
      )
    )
  }

  const handleSave = () => {
    console.log('Saving payment methods...', paymentMethods)
  }

  return (
    <AdminLayout title="Payment Methods">
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>Payment Methods Configuration</Typography>
        <Typography color="text.secondary">
          Configure available payment methods for deposits and withdrawals
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {paymentMethods.map((method) => (
          <Grid item xs={12} md={6} key={method.id}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {method.icon}
                    <Box>
                      <Typography variant="h6">{method.name}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        {method.type}
                      </Typography>
                    </Box>
                  </Box>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={method.enabled}
                        onChange={() => handleToggle(method.id)}
                        disabled={!permissions.canManagePaymentMethods}
                      />
                    }
                    label={method.enabled ? 'Enabled' : 'Disabled'}
                  />
                </Box>

                <Divider sx={{ my: 2 }} />

                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">
                      Min Amount
                    </Typography>
                    <TextField
                      size="small"
                      type="number"
                      value={method.minAmount}
                      disabled={!permissions.canManagePaymentMethods}
                      InputProps={{ startAdornment: '₾' }}
                      sx={{ mt: 0.5 }}
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">
                      Max Amount
                    </Typography>
                    <TextField
                      size="small"
                      type="number"
                      value={method.maxAmount}
                      disabled={!permissions.canManagePaymentMethods}
                      InputProps={{ startAdornment: '₾' }}
                      sx={{ mt: 0.5 }}
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">
                      Fee
                    </Typography>
                    <TextField
                      size="small"
                      type="number"
                      value={method.fee}
                      disabled={!permissions.canManagePaymentMethods}
                      InputProps={{ endAdornment: '%' }}
                      sx={{ mt: 0.5 }}
                    />
                  </Grid>
                  <Grid item xs={6}>
                    <Typography variant="body2" color="text.secondary">
                      Processing Time
                    </Typography>
                    <Typography variant="body1" sx={{ mt: 1 }}>
                      {method.processingTime}
                    </Typography>
                  </Grid>
                </Grid>

                <Box sx={{ mt: 2 }}>
                  <Chip
                    label={method.enabled ? 'Active' : 'Inactive'}
                    color={method.enabled ? 'success' : 'default'}
                    size="small"
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {permissions.canManagePaymentMethods && (
        <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
          <Button variant="contained" size="large" onClick={handleSave}>
            Save Changes
          </Button>
        </Box>
      )}
    </AdminLayout>
  )
}

export default PaymentMethods
