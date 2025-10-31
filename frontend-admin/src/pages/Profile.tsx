import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Paper, TextField, Button, Grid, Alert,
  Divider, Card, CardContent
} from '@mui/material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import axios from 'axios'

const Profile = () => {
  const { isAuthenticated, admin } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')

  const [profileData, setProfileData] = useState({
    firstName: '',
    lastName: '',
    email: ''
  })

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (admin) {
      setProfileData({
        firstName: admin.firstName || '',
        lastName: admin.lastName || '',
        email: admin.email || ''
      })
    }
  }, [isAuthenticated, navigate, admin])

  const handleUpdateProfile = async () => {
    try {
      setLoading(true)
      setErrorMessage('')
      setSuccessMessage('')

      await axios.put('/api/admin/auth/profile', profileData)
      setSuccessMessage('Profile updated successfully')
      
      // Optionally refresh admin data
      window.location.reload()
    } catch (err: any) {
      setErrorMessage(err.response?.data?.message || 'Failed to update profile')
    } finally {
      setLoading(false)
    }
  }

  const handleChangePassword = async () => {
    try {
      setLoading(true)
      setErrorMessage('')
      setSuccessMessage('')

      if (passwordData.newPassword !== passwordData.confirmPassword) {
        setErrorMessage('New password and confirmation do not match')
        setLoading(false)
        return
      }

      await axios.put('/api/admin/auth/password', passwordData)
      setSuccessMessage('Password changed successfully')
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      })
    } catch (err: any) {
      setErrorMessage(err.response?.data?.message || 'Failed to change password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AdminLayout title="My Profile">
      {successMessage && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccessMessage('')}>
          {successMessage}
        </Alert>
      )}
      {errorMessage && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setErrorMessage('')}>
          {errorMessage}
        </Alert>
      )}

      <Grid container spacing={3}>
        {/* Profile Information Card */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Profile Information
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <TextField
                  label="Username"
                  value={admin?.username || ''}
                  disabled
                  fullWidth
                  helperText="Username cannot be changed"
                />
                <TextField
                  label="Role"
                  value={admin?.role || ''}
                  disabled
                  fullWidth
                  helperText="Role is assigned by Owner"
                />
                <TextField
                  label="First Name"
                  value={profileData.firstName}
                  onChange={(e) => setProfileData({ ...profileData, firstName: e.target.value })}
                  fullWidth
                  required
                />
                <TextField
                  label="Last Name"
                  value={profileData.lastName}
                  onChange={(e) => setProfileData({ ...profileData, lastName: e.target.value })}
                  fullWidth
                  required
                />
                <TextField
                  label="Email"
                  type="email"
                  value={profileData.email}
                  onChange={(e) => setProfileData({ ...profileData, email: e.target.value })}
                  fullWidth
                  required
                />
                <Button
                  variant="contained"
                  onClick={handleUpdateProfile}
                  disabled={loading}
                  fullWidth
                >
                  Update Profile
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Change Password Card */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Change Password
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <TextField
                  label="Current Password"
                  type="password"
                  value={passwordData.currentPassword}
                  onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
                  fullWidth
                  required
                />
                <TextField
                  label="New Password"
                  type="password"
                  value={passwordData.newPassword}
                  onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                  fullWidth
                  required
                  helperText="Minimum 6 characters"
                />
                <TextField
                  label="Confirm New Password"
                  type="password"
                  value={passwordData.confirmPassword}
                  onChange={(e) => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
                  fullWidth
                  required
                />
                <Button
                  variant="contained"
                  onClick={handleChangePassword}
                  disabled={loading}
                  fullWidth
                  color="secondary"
                >
                  Change Password
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Account Information Card */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Account Information
              </Typography>
              <Divider sx={{ mb: 2 }} />

              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Account ID</Typography>
                  <Typography variant="body1">{admin?.id}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Status</Typography>
                  <Typography variant="body1">{admin?.status}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Last Login</Typography>
                  <Typography variant="body1">
                    {admin?.lastLoginAt ? new Date(admin.lastLoginAt).toLocaleString('en-GB') : 'Never'}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Account Created</Typography>
                  <Typography variant="body1">
                    {admin?.createdAt ? new Date(admin.createdAt).toLocaleString('en-GB') : '-'}
                  </Typography>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </AdminLayout>
  )
}

export default Profile
