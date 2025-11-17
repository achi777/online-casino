import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, IconButton, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, Select, MenuItem, FormControl,
  InputLabel, Grid
} from '@mui/material'
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'
import axios from 'axios'

interface Bonus {
  id?: number
  name: string
  description?: string
  type: string
  amount?: number
  percentage?: number
  minDeposit?: number
  maxBonus?: number
  wageringRequirement?: number
  status: string
  validFrom: string
  validTo: string
  usageLimit?: number
  usedCount?: number
  termsAndConditions?: string
}

const emptyBonus: Bonus = {
  name: '',
  description: '',
  type: 'DEPOSIT',
  status: 'ACTIVE',
  validFrom: new Date().toISOString().slice(0, 16),
  validTo: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().slice(0, 16),
}

const Bonuses = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const [bonuses, setBonuses] = useState<Bonus[]>([])
  const [dialogOpen, setDialogOpen] = useState(false)
  const [selectedBonus, setSelectedBonus] = useState<Bonus | null>(null)
  const [formData, setFormData] = useState<Bonus>(emptyBonus)

  useEffect(() => {
    console.log('Bonuses page - Permissions:', permissions)
    console.log('Can manage bonuses:', permissions.canManageBonuses)

    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canViewBonuses) {
      navigate('/dashboard')
    } else {
      fetchBonuses()
    }
  }, [isAuthenticated, permissions, navigate])

  const fetchBonuses = async () => {
    try {
      const response = await axios.get('/api/admin/bonuses')
      setBonuses(response.data)
    } catch (error) {
      console.error('Failed to fetch bonuses:', error)
    }
  }

  const handleCreate = () => {
    setSelectedBonus(null)
    setFormData(emptyBonus)
    setDialogOpen(true)
  }

  const handleEdit = (bonus: Bonus) => {
    setSelectedBonus(bonus)
    setFormData({
      ...bonus,
      validFrom: bonus.validFrom?.slice(0, 16) || '',
      validTo: bonus.validTo?.slice(0, 16) || '',
    })
    setDialogOpen(true)
  }

  const handleSave = async () => {
    try {
      if (selectedBonus?.id) {
        await axios.put(`/api/admin/bonuses/${selectedBonus.id}`, formData)
      } else {
        await axios.post('/api/admin/bonuses', formData)
      }
      setDialogOpen(false)
      fetchBonuses()
    } catch (error) {
      console.error('Failed to save bonus:', error)
      alert('Failed to save bonus')
    }
  }

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this bonus?')) {
      return
    }
    try {
      await axios.delete(`/api/admin/bonuses/${id}`)
      fetchBonuses()
    } catch (error) {
      console.error('Failed to delete bonus:', error)
      alert('Failed to delete bonus')
    }
  }

  const getStatusColor = (status: string) => {
    return status === 'ACTIVE' ? 'success' : 'default'
  }

  return (
    <AdminLayout title="Bonuses & Promotions">
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between' }}>
        <Box>
          <Typography variant="h5" gutterBottom>Bonuses & Promotions</Typography>
          <Typography color="text.secondary">
            Manage welcome bonuses, deposit bonuses, free spins, and promotions
          </Typography>
        </Box>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreate}>
          Create Bonus
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Type</TableCell>
              <TableCell>Amount/Percentage</TableCell>
              <TableCell>Min Deposit</TableCell>
              <TableCell>Max Bonus</TableCell>
              <TableCell>Valid Period</TableCell>
              <TableCell>Used</TableCell>
              <TableCell>Status</TableCell>
              {permissions.canManageBonuses && <TableCell align="center">Actions</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {bonuses.length === 0 ? (
              <TableRow>
                <TableCell colSpan={10} align="center">
                  <Typography color="text.secondary" sx={{ py: 4 }}>
                    No bonuses found. Create your first bonus to get started.
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              bonuses.map((bonus) => (
                <TableRow key={bonus.id} hover>
                  <TableCell>{bonus.id}</TableCell>
                  <TableCell>{bonus.name}</TableCell>
                  <TableCell>{bonus.type}</TableCell>
                  <TableCell>
                    {bonus.percentage ? `${bonus.percentage}%` : `₾${bonus.amount}`}
                  </TableCell>
                  <TableCell>₾{bonus.minDeposit || 'N/A'}</TableCell>
                  <TableCell>₾{bonus.maxBonus || 'N/A'}</TableCell>
                  <TableCell>
                    {new Date(bonus.validFrom).toLocaleDateString()} - {new Date(bonus.validTo).toLocaleDateString()}
                  </TableCell>
                  <TableCell>{bonus.usedCount}</TableCell>
                  <TableCell>
                    <Chip label={bonus.status} color={getStatusColor(bonus.status) as any} size="small" />
                  </TableCell>
                  {permissions.canManageBonuses && (
                    <TableCell align="center">
                      <IconButton size="small" onClick={() => handleEdit(bonus)}>
                        <EditIcon />
                      </IconButton>
                      <IconButton size="small" color="error" onClick={() => bonus.id && handleDelete(bonus.id)}>
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Create/Edit Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>{selectedBonus ? 'Edit Bonus' : 'Create Bonus'}</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Bonus Name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Description"
                value={formData.description || ''}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                multiline
                rows={2}
              />
            </Grid>

            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>Bonus Type</InputLabel>
                <Select
                  value={formData.type}
                  label="Bonus Type"
                  onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                >
                  <MenuItem value="WELCOME">Welcome Bonus</MenuItem>
                  <MenuItem value="DEPOSIT">Deposit Bonus</MenuItem>
                  <MenuItem value="NO_DEPOSIT">No Deposit Bonus</MenuItem>
                  <MenuItem value="FREE_SPINS">Free Spins</MenuItem>
                  <MenuItem value="CASHBACK">Cashback</MenuItem>
                  <MenuItem value="RELOAD">Reload Bonus</MenuItem>
                  <MenuItem value="REFERRAL">Referral Bonus</MenuItem>
                  <MenuItem value="VIP">VIP Bonus</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={6}>
              <FormControl fullWidth>
                <InputLabel>Status</InputLabel>
                <Select
                  value={formData.status}
                  label="Status"
                  onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                >
                  <MenuItem value="ACTIVE">Active</MenuItem>
                  <MenuItem value="INACTIVE">Inactive</MenuItem>
                  <MenuItem value="PAUSED">Paused</MenuItem>
                  <MenuItem value="EXPIRED">Expired</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Fixed Amount (₾)"
                type="number"
                value={formData.amount || ''}
                onChange={(e) => setFormData({ ...formData, amount: parseFloat(e.target.value) || undefined })}
                inputProps={{ min: 0, step: 0.01 }}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Percentage (%)"
                type="number"
                value={formData.percentage || ''}
                onChange={(e) => setFormData({ ...formData, percentage: parseFloat(e.target.value) || undefined })}
                inputProps={{ min: 0, max: 100, step: 0.01 }}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Minimum Deposit (₾)"
                type="number"
                value={formData.minDeposit || ''}
                onChange={(e) => setFormData({ ...formData, minDeposit: parseFloat(e.target.value) || undefined })}
                inputProps={{ min: 0, step: 0.01 }}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Maximum Bonus (₾)"
                type="number"
                value={formData.maxBonus || ''}
                onChange={(e) => setFormData({ ...formData, maxBonus: parseFloat(e.target.value) || undefined })}
                inputProps={{ min: 0, step: 0.01 }}
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Wagering Requirement (x)"
                type="number"
                value={formData.wageringRequirement || ''}
                onChange={(e) => setFormData({ ...formData, wageringRequirement: parseInt(e.target.value) || undefined })}
                inputProps={{ min: 0, step: 1 }}
                helperText="Times bonus must be wagered"
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Usage Limit"
                type="number"
                value={formData.usageLimit || ''}
                onChange={(e) => setFormData({ ...formData, usageLimit: parseInt(e.target.value) || undefined })}
                inputProps={{ min: 0, step: 1 }}
                helperText="Max number of claims (leave empty for unlimited)"
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Valid From"
                type="datetime-local"
                value={formData.validFrom}
                onChange={(e) => setFormData({ ...formData, validFrom: e.target.value })}
                InputLabelProps={{ shrink: true }}
                required
              />
            </Grid>

            <Grid item xs={6}>
              <TextField
                fullWidth
                label="Valid To"
                type="datetime-local"
                value={formData.validTo}
                onChange={(e) => setFormData({ ...formData, validTo: e.target.value })}
                InputLabelProps={{ shrink: true }}
                required
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Terms and Conditions"
                value={formData.termsAndConditions || ''}
                onChange={(e) => setFormData({ ...formData, termsAndConditions: e.target.value })}
                multiline
                rows={4}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">
            {selectedBonus ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </AdminLayout>
  )
}

export default Bonuses
