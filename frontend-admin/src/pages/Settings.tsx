import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Grid, Card, CardContent, TextField, Switch,
  FormControlLabel, Button, Divider, Select, MenuItem, FormControl,
  InputLabel
} from '@mui/material'
import {
  Save as SaveIcon,
  Security as SecurityIcon,
  AttachMoney as MoneyIcon,
  Email as EmailIcon,
  Language as LanguageIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'

const Settings = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canManageSystemSettings) {
      navigate('/dashboard')
    }
  }, [isAuthenticated, permissions, navigate])

  return (
    <AdminLayout title="System Settings">
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>System Settings</Typography>
        <Typography color="text.secondary">
          Configure casino platform settings and parameters
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* General Settings */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <LanguageIcon sx={{ mr: 1 }} />
                <Typography variant="h6">General Settings</Typography>
              </Box>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField fullWidth label="Casino Name" defaultValue="Casino Platform" />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <FormControl fullWidth>
                    <InputLabel>Default Currency</InputLabel>
                    <Select label="Default Currency" defaultValue="GEL">
                      <MenuItem value="GEL">₾ GEL</MenuItem>
                      <MenuItem value="USD">$ USD</MenuItem>
                      <MenuItem value="EUR">€ EUR</MenuItem>
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12}>
                  <TextField fullWidth label="Support Email" defaultValue="support@casino.com" />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={<Switch defaultChecked />}
                    label="Maintenance Mode"
                  />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={<Switch defaultChecked />}
                    label="Allow New Registrations"
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Financial Settings */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <MoneyIcon sx={{ mr: 1 }} />
                <Typography variant="h6">Financial Settings</Typography>
              </Box>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Minimum Deposit"
                    type="number"
                    defaultValue="10"
                    InputProps={{ startAdornment: '₾' }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Minimum Withdrawal"
                    type="number"
                    defaultValue="20"
                    InputProps={{ startAdornment: '₾' }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Maximum Withdrawal per Day"
                    type="number"
                    defaultValue="10000"
                    InputProps={{ startAdornment: '₾' }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Withdrawal Fee (%)"
                    type="number"
                    defaultValue="0"
                  />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={<Switch defaultChecked />}
                    label="Auto-approve deposits under ₾100"
                  />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={<Switch />}
                    label="Auto-approve withdrawals under ₾1000"
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Security Settings */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <SecurityIcon sx={{ mr: 1 }} />
                <Typography variant="h6">Security Settings</Typography>
              </Box>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Session Timeout (minutes)"
                    type="number"
                    defaultValue="30"
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="Max Login Attempts"
                    type="number"
                    defaultValue="5"
                  />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={<Switch defaultChecked />}
                    label="Require Email Verification"
                  />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={<Switch />}
                    label="Enable Two-Factor Authentication"
                  />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={<Switch defaultChecked />}
                    label="Log All Admin Actions"
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Email Settings */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <EmailIcon sx={{ mr: 1 }} />
                <Typography variant="h6">Email Settings</Typography>
              </Box>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField fullWidth label="SMTP Server" placeholder="smtp.example.com" />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField fullWidth label="SMTP Port" type="number" defaultValue="587" />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField fullWidth label="SMTP Username" />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField fullWidth label="SMTP Password" type="password" />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={<Switch defaultChecked />}
                    label="Send Welcome Email"
                  />
                </Grid>
                <Grid item xs={12}>
                  <FormControlLabel
                    control={<Switch defaultChecked />}
                    label="Send Transaction Notifications"
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* Save Button */}
        <Grid item xs={12}>
          <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <Button variant="contained" size="large" startIcon={<SaveIcon />}>
              Save All Settings
            </Button>
          </Box>
        </Grid>
      </Grid>
    </AdminLayout>
  )
}

export default Settings
