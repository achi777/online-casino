import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, IconButton, Dialog, DialogTitle,
  DialogContent, DialogActions, TextField, FormControl, InputLabel,
  Select, MenuItem, Grid
} from '@mui/material'
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Lock as LockIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'
import axios from 'axios'

interface AdminUser {
  id: number
  username: string
  email: string
  firstName: string
  lastName: string
  role: string
  status: string
  lastLoginAt?: string
  createdAt: string
}

const Admins = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const [admins, setAdmins] = useState<AdminUser[]>([])
  const [openDialog, setOpenDialog] = useState(false)
  const [selectedAdmin, setSelectedAdmin] = useState<AdminUser | null>(null)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canManageAdmins) {
      navigate('/dashboard')
    } else {
      fetchAdmins()
    }
  }, [isAuthenticated, permissions, navigate])

  const fetchAdmins = async () => {
    try {
      const response = await axios.get('/api/admin/admins')
      setAdmins(response.data || [])
    } catch (err) {
      console.error('Failed to fetch admins:', err)
      setAdmins([])
    }
  }

  const handleAddAdmin = () => {
    setSelectedAdmin(null)
    setOpenDialog(true)
  }

  const handleEditAdmin = (admin: AdminUser) => {
    setSelectedAdmin(admin)
    setOpenDialog(true)
  }

  const getRoleColor = (role: string) => {
    switch (role) {
      case 'OWNER': return 'error'
      case 'ADMIN': return 'warning'
      case 'FINANCE': return 'success'
      case 'SUPPORT': return 'info'
      case 'CONTENT': return 'secondary'
      default: return 'default'
    }
  }

  const getStatusColor = (status: string) => {
    return status === 'ACTIVE' ? 'success' : 'error'
  }

  return (
    <AdminLayout title="Admin Management">
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between' }}>
        <Box>
          <Typography variant="h5" gutterBottom>Admin Users</Typography>
          <Typography color="text.secondary">
            Manage administrative users and their roles
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleAddAdmin}
        >
          Add Admin
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Username</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Role</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Last Login</TableCell>
              <TableCell>Created</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {admins.map((admin) => (
              <TableRow key={admin.id} hover>
                <TableCell>{admin.id}</TableCell>
                <TableCell>{admin.username}</TableCell>
                <TableCell>{admin.email}</TableCell>
                <TableCell>{`${admin.firstName} ${admin.lastName}`}</TableCell>
                <TableCell>
                  <Chip
                    label={admin.role}
                    color={getRoleColor(admin.role) as any}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  <Chip
                    label={admin.status}
                    color={getStatusColor(admin.status) as any}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  {admin.lastLoginAt
                    ? new Date(admin.lastLoginAt).toLocaleString()
                    : 'Never'}
                </TableCell>
                <TableCell>{new Date(admin.createdAt).toLocaleDateString()}</TableCell>
                <TableCell align="center">
                  <IconButton size="small" onClick={() => handleEditAdmin(admin)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton size="small" color="warning">
                    <LockIcon />
                  </IconButton>
                  <IconButton size="small" color="error">
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Add/Edit Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          {selectedAdmin ? 'Edit Admin' : 'Add New Admin'}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField fullWidth label="Username" />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="Email" type="email" />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="First Name" />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Last Name" />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Role</InputLabel>
                <Select label="Role" defaultValue="">
                  <MenuItem value="OWNER">OWNER</MenuItem>
                  <MenuItem value="ADMIN">ADMIN</MenuItem>
                  <MenuItem value="FINANCE">FINANCE</MenuItem>
                  <MenuItem value="SUPPORT">SUPPORT</MenuItem>
                  <MenuItem value="CONTENT">CONTENT</MenuItem>
                  <MenuItem value="ANALYST">ANALYST</MenuItem>
                  <MenuItem value="COMPLIANCE">COMPLIANCE</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            {!selectedAdmin && (
              <Grid item xs={12}>
                <TextField fullWidth label="Password" type="password" />
              </Grid>
            )}
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button variant="contained">
            {selectedAdmin ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </AdminLayout>
  )
}

export default Admins
