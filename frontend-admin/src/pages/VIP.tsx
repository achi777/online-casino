import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Grid, Card, CardContent, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Paper, Chip,
  IconButton, Button, Dialog, DialogTitle, DialogContent, DialogActions,
  TextField, FormControl, InputLabel, Select, MenuItem
} from '@mui/material'
import {
  Stars as VIPIcon,
  Edit as EditIcon,
  Visibility as ViewIcon,
  Add as AddIcon,
  History as HistoryIcon,
  AttachMoney as MoneyIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'
import axios from 'axios'

interface VIPTier {
  id?: number
  name: string
  level: number
  minPoints: number
  cashbackPercentage: number
  bonusMultiplier: number
  monthlyBonus?: number
  prioritySupport?: number
  personalAccountManager?: boolean
  exclusivePromotions?: boolean
  withdrawalLimit?: number
  color: string
  sortOrder?: number
}

interface VIPUser {
  id: number
  email: string
  firstName: string
  lastName: string
  vipTier?: VIPTier
  vipPoints: number
  totalWagered: number
  lifetimeDeposits: number
}

const emptyTier: VIPTier = {
  name: '',
  level: 1,
  minPoints: 0,
  cashbackPercentage: 1,
  bonusMultiplier: 1,
  color: '#CD7F32',
  sortOrder: 1
}

const VIP = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()

  const [tiers, setTiers] = useState<VIPTier[]>([])
  const [vipUsers, setVIPUsers] = useState<VIPUser[]>([])

  const [tierDialogOpen, setTierDialogOpen] = useState(false)
  const [selectedTier, setSelectedTier] = useState<VIPTier | null>(null)
  const [tierFormData, setTierFormData] = useState<VIPTier>(emptyTier)

  const [pointsDialogOpen, setPointsDialogOpen] = useState(false)
  const [selectedUser, setSelectedUser] = useState<VIPUser | null>(null)
  const [pointsAdjustment, setPointsAdjustment] = useState({ points: 0, description: '' })

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canViewVIP) {
      navigate('/dashboard')
    } else {
      fetchTiers()
      fetchVIPUsers()
    }
  }, [isAuthenticated, permissions, navigate])

  const fetchTiers = async () => {
    try {
      const response = await axios.get('/api/admin/vip/tiers')
      setTiers(response.data)
    } catch (error) {
      console.error('Failed to fetch VIP tiers:', error)
    }
  }

  const fetchVIPUsers = async () => {
    try {
      const response = await axios.get('/api/admin/vip/users')
      setVIPUsers(response.data)
    } catch (error) {
      console.error('Failed to fetch VIP users:', error)
    }
  }

  const handleCreateTier = () => {
    setSelectedTier(null)
    setTierFormData(emptyTier)
    setTierDialogOpen(true)
  }

  const handleEditTier = (tier: VIPTier) => {
    setSelectedTier(tier)
    setTierFormData(tier)
    setTierDialogOpen(true)
  }

  const handleSaveTier = async () => {
    try {
      if (selectedTier?.id) {
        await axios.put(`/api/admin/vip/tiers/${selectedTier.id}`, tierFormData)
      } else {
        await axios.post('/api/admin/vip/tiers', tierFormData)
      }
      setTierDialogOpen(false)
      fetchTiers()
    } catch (error) {
      console.error('Failed to save tier:', error)
      alert('Failed to save tier')
    }
  }

  const handleAdjustPoints = (user: VIPUser) => {
    setSelectedUser(user)
    setPointsAdjustment({ points: 0, description: '' })
    setPointsDialogOpen(true)
  }

  const handleSavePointsAdjustment = async () => {
    if (!selectedUser) return

    try {
      await axios.post(`/api/admin/vip/users/${selectedUser.id}/points`, pointsAdjustment)
      setPointsDialogOpen(false)
      fetchVIPUsers()
      alert('Points adjusted successfully')
    } catch (error) {
      console.error('Failed to adjust points:', error)
      alert('Failed to adjust points')
    }
  }

  return (
    <AdminLayout title="VIP Management">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" gutterBottom>VIP & Loyalty Program</Typography>
        <Typography color="text.secondary">
          Manage VIP tiers, rewards, and high-value players
        </Typography>
      </Box>

      {/* VIP Tiers */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 4, mb: 2 }}>
        <Typography variant="h6">VIP Tiers Configuration</Typography>
        {permissions.canManageVIP && (
          <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreateTier}>
            Create Tier
          </Button>
        )}
      </Box>
      <Grid container spacing={2} sx={{ mb: 4 }}>
        {tiers.map((tier) => (
          <Grid item xs={12} sm={6} md={4} lg={2.4} key={tier.id}>
            <Card sx={{ bgcolor: `${tier.color}20`, border: `2px solid ${tier.color}` }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="h6">{tier.name}</Typography>
                  {permissions.canManageVIP && (
                    <IconButton size="small" onClick={() => handleEditTier(tier)}>
                      <EditIcon />
                    </IconButton>
                  )}
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Level {tier.level}
                </Typography>
                <Typography variant="body2" sx={{ mt: 1 }}>
                  Min Points: {tier.minPoints.toLocaleString()}
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
                  <TableCell>{user.firstName} {user.lastName}</TableCell>
                  <TableCell>
                    {user.vipTier ? (
                      <Chip
                        icon={<VIPIcon />}
                        label={user.vipTier.name}
                        size="small"
                        style={{ backgroundColor: `${user.vipTier.color}40`, color: '#000' }}
                      />
                    ) : (
                      <Chip label="None" size="small" />
                    )}
                  </TableCell>
                  <TableCell align="right">{user.vipPoints.toLocaleString()}</TableCell>
                  <TableCell align="right">₾{user.totalWagered.toLocaleString()}</TableCell>
                  <TableCell align="right">₾{user.lifetimeDeposits.toLocaleString()}</TableCell>
                  <TableCell align="center">
                    {permissions.canManageVIP && (
                      <IconButton
                        size="small"
                        onClick={() => handleAdjustPoints(user)}
                        title="Adjust Points"
                      >
                        <MoneyIcon />
                      </IconButton>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Tier Create/Edit Dialog */}
      <Dialog open={tierDialogOpen} onClose={() => setTierDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>{selectedTier ? 'Edit VIP Tier' : 'Create VIP Tier'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Tier Name"
                value={tierFormData.name}
                onChange={(e) => setTierFormData({ ...tierFormData, name: e.target.value })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Level"
                type="number"
                value={tierFormData.level}
                onChange={(e) => setTierFormData({ ...tierFormData, level: parseInt(e.target.value) })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Minimum Points"
                type="number"
                value={tierFormData.minPoints}
                onChange={(e) => setTierFormData({ ...tierFormData, minPoints: parseInt(e.target.value) })}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Cashback Percentage (%)"
                type="number"
                value={tierFormData.cashbackPercentage}
                onChange={(e) => setTierFormData({ ...tierFormData, cashbackPercentage: parseFloat(e.target.value) })}
                inputProps={{ step: 0.01 }}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Bonus Multiplier"
                type="number"
                value={tierFormData.bonusMultiplier}
                onChange={(e) => setTierFormData({ ...tierFormData, bonusMultiplier: parseFloat(e.target.value) })}
                inputProps={{ step: 0.1 }}
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Color (Hex)"
                value={tierFormData.color}
                onChange={(e) => setTierFormData({ ...tierFormData, color: e.target.value })}
                placeholder="#FFD700"
                required
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Monthly Bonus (₾)"
                type="number"
                value={tierFormData.monthlyBonus || ''}
                onChange={(e) => setTierFormData({ ...tierFormData, monthlyBonus: parseFloat(e.target.value) || undefined })}
                inputProps={{ step: 1 }}
              />
            </Grid>
            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Sort Order"
                type="number"
                value={tierFormData.sortOrder || 1}
                onChange={(e) => setTierFormData({ ...tierFormData, sortOrder: parseInt(e.target.value) })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTierDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleSaveTier} variant="contained">
            {selectedTier ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Points Adjustment Dialog */}
      <Dialog open={pointsDialogOpen} onClose={() => setPointsDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Adjust VIP Points</DialogTitle>
        <DialogContent>
          {selectedUser && (
            <Box sx={{ pt: 2 }}>
              <Typography variant="body2" gutterBottom>
                User: {selectedUser.firstName} {selectedUser.lastName} ({selectedUser.email})
              </Typography>
              <Typography variant="body2" gutterBottom color="text.secondary">
                Current Points: {selectedUser.vipPoints.toLocaleString()}
              </Typography>

              <TextField
                fullWidth
                label="Points Adjustment"
                type="number"
                value={pointsAdjustment.points}
                onChange={(e) => setPointsAdjustment({ ...pointsAdjustment, points: parseInt(e.target.value) })}
                helperText="Enter positive number to add points, negative to deduct"
                sx={{ mt: 2 }}
              />

              <TextField
                fullWidth
                label="Description"
                value={pointsAdjustment.description}
                onChange={(e) => setPointsAdjustment({ ...pointsAdjustment, description: e.target.value })}
                multiline
                rows={3}
                sx={{ mt: 2 }}
                required
              />
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPointsDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleSavePointsAdjustment} variant="contained" color="primary">
            Adjust Points
          </Button>
        </DialogActions>
      </Dialog>
    </AdminLayout>
  )
}

export default VIP
