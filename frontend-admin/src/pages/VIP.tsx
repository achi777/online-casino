import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Grid, Card, CardContent, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Paper, Chip,
  IconButton, Button
} from '@mui/material'
import {
  Stars as VIPIcon,
  Edit as EditIcon,
  Visibility as ViewIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'

interface VIPTier {
  id: number
  name: string
  level: number
  minPoints: number
  cashbackPercentage: number
  bonusMultiplier: number
  color: string
}

interface VIPUser {
  id: number
  email: string
  name: string
  tier: string
  points: number
  totalWagered: number
  lifetimeDeposits: number
}

const VIP = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const [tiers] = useState<VIPTier[]>([
    { id: 1, name: 'Bronze', level: 1, minPoints: 0, cashbackPercentage: 1, bonusMultiplier: 1, color: '#CD7F32' },
    { id: 2, name: 'Silver', level: 2, minPoints: 1000, cashbackPercentage: 2, bonusMultiplier: 1.2, color: '#C0C0C0' },
    { id: 3, name: 'Gold', level: 3, minPoints: 5000, cashbackPercentage: 3, bonusMultiplier: 1.5, color: '#FFD700' },
    { id: 4, name: 'Platinum', level: 4, minPoints: 10000, cashbackPercentage: 5, bonusMultiplier: 2, color: '#E5E4E2' },
    { id: 5, name: 'Diamond', level: 5, minPoints: 50000, cashbackPercentage: 8, bonusMultiplier: 3, color: '#B9F2FF' },
  ])
  const [vipUsers, setVIPUsers] = useState<VIPUser[]>([])

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canViewVIP) {
      navigate('/dashboard')
    }
  }, [isAuthenticated, permissions, navigate])

  return (
    <AdminLayout title="VIP Management">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" gutterBottom>VIP & Loyalty Program</Typography>
        <Typography color="text.secondary">
          Manage VIP tiers, rewards, and high-value players
        </Typography>
      </Box>

      {/* VIP Tiers */}
      <Typography variant="h6" gutterBottom sx={{ mt: 4 }}>
        VIP Tiers Configuration
      </Typography>
      <Grid container spacing={2} sx={{ mb: 4 }}>
        {tiers.map((tier) => (
          <Grid item xs={12} sm={6} md={4} lg={2.4} key={tier.id}>
            <Card sx={{ bgcolor: `${tier.color}20`, border: `2px solid ${tier.color}` }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="h6">{tier.name}</Typography>
                  {permissions.canManageVIP && <IconButton size="small"><EditIcon /></IconButton>}
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Level {tier.level}
                </Typography>
                <Typography variant="body2" sx={{ mt: 1 }}>
                  Min Points: {tier.minPoints}
                </Typography>
                <Typography variant="body2">
                  Cashback: {tier.cashbackPercentage}%
                </Typography>
                <Typography variant="body2">
                  Bonus: {tier.bonusMultiplier}x
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* VIP Users */}
      <Typography variant="h6" gutterBottom sx={{ mt: 4 }}>
        VIP Users
      </Typography>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>User ID</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>VIP Tier</TableCell>
              <TableCell align="right">Points</TableCell>
              <TableCell align="right">Total Wagered</TableCell>
              <TableCell align="right">Lifetime Deposits</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {vipUsers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Typography color="text.secondary" sx={{ py: 4 }}>
                    No VIP users found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              vipUsers.map((user) => (
                <TableRow key={user.id} hover>
                  <TableCell>{user.id}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>{user.name}</TableCell>
                  <TableCell>
                    <Chip
                      icon={<VIPIcon />}
                      label={user.tier}
                      size="small"
                      color="primary"
                    />
                  </TableCell>
                  <TableCell align="right">{user.points.toLocaleString()}</TableCell>
                  <TableCell align="right">₾{user.totalWagered.toLocaleString()}</TableCell>
                  <TableCell align="right">₾{user.lifetimeDeposits.toLocaleString()}</TableCell>
                  <TableCell align="center">
                    <IconButton size="small"><ViewIcon /></IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </AdminLayout>
  )
}

export default VIP
