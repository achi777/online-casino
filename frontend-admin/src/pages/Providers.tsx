import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, IconButton, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, FormControl, InputLabel,
  Select, MenuItem, Grid
} from '@mui/material'
import { Add as AddIcon, Edit as EditIcon, Visibility as ViewIcon } from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'
import axios from 'axios'

interface Provider {
  id: number
  code: string
  name: string
  integrationType: string
  status: string
  apiUrl?: string
  logoUrl?: string
  createdAt: string
}

const Providers = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const [providers, setProviders] = useState<Provider[]>([])
  const [openDialog, setOpenDialog] = useState(false)
  const [selectedProvider, setSelectedProvider] = useState<Provider | null>(null)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canManageProviders) {
      navigate('/dashboard')
    } else {
      fetchProviders()
    }
  }, [isAuthenticated, permissions, navigate])

  const fetchProviders = async () => {
    try {
      const response = await axios.get('/api/admin/games/providers')
      setProviders(response.data)
    } catch (err) {
      console.error('Failed to fetch providers:', err)
    }
  }

  const handleAddProvider = () => {
    setSelectedProvider(null)
    setOpenDialog(true)
  }

  const handleEditProvider = (provider: Provider) => {
    setSelectedProvider(provider)
    setOpenDialog(true)
  }

  const getStatusColor = (status: string) => {
    return status === 'ACTIVE' ? 'success' : 'error'
  }

  return (
    <AdminLayout title="Game Providers">
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h5">Game Providers Management</Typography>
        {permissions.canManageProviders && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddProvider}
          >
            Add Provider
          </Button>
        )}
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Code</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Integration Type</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>API URL</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {providers.map((provider) => (
              <TableRow key={provider.id} hover>
                <TableCell>{provider.id}</TableCell>
                <TableCell>{provider.code}</TableCell>
                <TableCell>{provider.name}</TableCell>
                <TableCell>{provider.integrationType}</TableCell>
                <TableCell>
                  <Chip
                    label={provider.status}
                    color={getStatusColor(provider.status) as any}
                    size="small"
                  />
                </TableCell>
                <TableCell>{provider.apiUrl || 'N/A'}</TableCell>
                <TableCell align="center">
                  <IconButton
                    size="small"
                    onClick={() => handleEditProvider(provider)}
                    title="Edit Provider"
                  >
                    <EditIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Add/Edit Dialog - Placeholder */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          {selectedProvider ? 'Edit Provider' : 'Add Provider'}
        </DialogTitle>
        <DialogContent>
          <Typography color="text.secondary" sx={{ mt: 2 }}>
            Provider form will be implemented here
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button variant="contained">Save</Button>
        </DialogActions>
      </Dialog>
    </AdminLayout>
  )
}

export default Providers
