import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, IconButton
} from '@mui/material'
import { Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'

interface Bonus {
  id: number
  name: string
  type: string
  amount: number
  percentage?: number
  minDeposit?: number
  maxBonus?: number
  status: string
  validFrom: string
  validTo: string
  usedCount: number
}

const Bonuses = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const [bonuses, setBonuses] = useState<Bonus[]>([])

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canViewBonuses) {
      navigate('/dashboard')
    }
  }, [isAuthenticated, permissions, navigate])

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
        {permissions.canManageBonuses && (
          <Button variant="contained" startIcon={<AddIcon />}>
            Create Bonus
          </Button>
        )}
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
                      <IconButton size="small"><EditIcon /></IconButton>
                      <IconButton size="small" color="error"><DeleteIcon /></IconButton>
                    </TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </AdminLayout>
  )
}

export default Bonuses
