import { useState } from 'react'
import { Container, Typography, Box, Paper, TextField, Button, Alert, InputAdornment, Grid } from '@mui/material'
import axios from 'axios'
import { useQuery } from 'react-query'
import Navbar from '../components/Navbar'
import SecurityIcon from '@mui/icons-material/Security'
import InfoIcon from '@mui/icons-material/Info'

const ResponsibleGaming = () => {
  const [limits, setLimits] = useState({
    dailyDepositLimit: '',
    weeklyDepositLimit: '',
    monthlyDepositLimit: '',
  })
  const [message, setMessage] = useState('')
  const [messageType, setMessageType] = useState<'success' | 'error' | 'info'>('info')

  const { data: currentLimits, refetch } = useQuery('limits', async () => {
    const response = await axios.get('/api/user/responsible-gaming/limits')
    return response.data
  })

  const handleSubmit = async () => {
    try {
      await axios.put('/api/user/responsible-gaming/limits', {
        dailyDepositLimit: parseFloat(limits.dailyDepositLimit),
        weeklyDepositLimit: parseFloat(limits.weeklyDepositLimit),
        monthlyDepositLimit: parseFloat(limits.monthlyDepositLimit),
      })
      setMessage('Limits updated successfully')
      setMessageType('success')
      refetch()
      setLimits({
        dailyDepositLimit: '',
        weeklyDepositLimit: '',
        monthlyDepositLimit: '',
      })
    } catch (error: any) {
      setMessage(error.response?.data?.error || 'Update failed')
      setMessageType('error')
    }
  }

  return (
    <>
      <Navbar />
      <Container maxWidth="md">
        <Box sx={{ mb: 4 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
            <SecurityIcon sx={{ fontSize: 32, mr: 1 }} />
            <Typography variant="h4" gutterBottom>
              Responsible Gaming
            </Typography>
          </Box>
          <Typography color="text.secondary">
            Set limits to help manage your gaming activity
          </Typography>
        </Box>

        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Paper sx={{ p: 3, mb: 3, bgcolor: 'info.dark' }}>
              <Box sx={{ display: 'flex', alignItems: 'flex-start' }}>
                <InfoIcon sx={{ mr: 2, mt: 0.5 }} />
                <Box>
                  <Typography variant="h6" gutterBottom>
                    Why Set Limits?
                  </Typography>
                  <Typography variant="body2">
                    Setting deposit limits helps you stay in control of your spending and ensures gaming remains fun and entertaining.
                    These limits will help prevent overspending and promote responsible gaming habits.
                  </Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>

          <Grid item xs={12}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom>
                Current Limits
              </Typography>
              {currentLimits && (
                <Box sx={{ mb: 3, p: 2, bgcolor: 'background.default', borderRadius: 1 }}>
                  <Grid container spacing={2}>
                    <Grid item xs={12} sm={4}>
                      <Typography variant="body2" color="text.secondary">Daily Limit</Typography>
                      <Typography variant="h6">
                        ₾{currentLimits.dailyDepositLimit || 'Not set'}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} sm={4}>
                      <Typography variant="body2" color="text.secondary">Weekly Limit</Typography>
                      <Typography variant="h6">
                        ₾{currentLimits.weeklyDepositLimit || 'Not set'}
                      </Typography>
                    </Grid>
                    <Grid item xs={12} sm={4}>
                      <Typography variant="body2" color="text.secondary">Monthly Limit</Typography>
                      <Typography variant="h6">
                        ₾{currentLimits.monthlyDepositLimit || 'Not set'}
                      </Typography>
                    </Grid>
                  </Grid>
                </Box>
              )}

              <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>
                Update Limits
              </Typography>
              {message && (
                <Alert severity={messageType} sx={{ mb: 2 }} onClose={() => setMessage('')}>
                  {message}
                </Alert>
              )}

              <TextField
                fullWidth
                label="Daily Deposit Limit"
                type="number"
                value={limits.dailyDepositLimit}
                onChange={(e) => setLimits({ ...limits, dailyDepositLimit: e.target.value })}
                margin="normal"
                InputProps={{
                  startAdornment: <InputAdornment position="start">₾</InputAdornment>,
                }}
                helperText="Maximum amount you can deposit per day"
              />
              <TextField
                fullWidth
                label="Weekly Deposit Limit"
                type="number"
                value={limits.weeklyDepositLimit}
                onChange={(e) => setLimits({ ...limits, weeklyDepositLimit: e.target.value })}
                margin="normal"
                InputProps={{
                  startAdornment: <InputAdornment position="start">₾</InputAdornment>,
                }}
                helperText="Maximum amount you can deposit per week"
              />
              <TextField
                fullWidth
                label="Monthly Deposit Limit"
                type="number"
                value={limits.monthlyDepositLimit}
                onChange={(e) => setLimits({ ...limits, monthlyDepositLimit: e.target.value })}
                margin="normal"
                InputProps={{
                  startAdornment: <InputAdornment position="start">₾</InputAdornment>,
                }}
                helperText="Maximum amount you can deposit per month"
              />
              <Button
                fullWidth
                variant="contained"
                onClick={handleSubmit}
                sx={{ mt: 3 }}
                size="large"
              >
                Update Limits
              </Button>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </>
  )
}

export default ResponsibleGaming
