import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Chip, Button, Paper,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  MenuItem, TablePagination
} from '@mui/material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'
import axios from 'axios'

interface Administrator {
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
  const [administrators, setAdministrators] = useState<Administrator[]>([])
  const [createDialogOpen, setCreateDialogOpen] = useState(false)
  const [statusDialogOpen, setStatusDialogOpen] = useState(false)
  const [selectedAdmin, setSelectedAdmin] = useState<Administrator | null>(null)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(25)
  const [totalAdmins, setTotalAdmins] = useState(0)

  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    role: 'ADMIN'
  })

  const [newStatus, setNewStatus] = useState('')

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canManageAdmins) {
      navigate('/dashboard')
    } else {
      fetchAdministrators()
    }
  }, [isAuthenticated, permissions, navigate, page, rowsPerPage])

  const fetchAdministrators = async () => {
    try {
      const response = await axios.get('/api/admin/management/admins', {
        params: { page, size: rowsPerPage }
      })
      setAdministrators(response.data.content || response.data)
      setTotalAdmins(response.data.totalElements || response.data.length)
    } catch (err) {
      console.error('Failed to fetch administrators:', err)
    }
  }

  const handleCreateAdmin = async () => {
    try {
      await axios.post('/api/admin/management/admins', formData)
      setCreateDialogOpen(false)
      setFormData({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        role: 'ADMIN'
      })
      fetchAdministrators()
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to create admin')
    }
  }

  const handleOpenStatusDialog = (admin: Administrator) => {
    setSelectedAdmin(admin)
    setNewStatus(admin.status)
    setStatusDialogOpen(true)
  }

  const handleUpdateStatus = async () => {
    if (!selectedAdmin) return

    try {
      await axios.put(`/api/admin/management/admins/${selectedAdmin.id}/status`, {
        status: newStatus
      })
      setStatusDialogOpen(false)
      fetchAdministrators()
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update status')
    }
  }

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage)
  }

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10))
    setPage(0)
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'success'
      case 'INACTIVE': return 'default'
      case 'SUSPENDED': return 'error'
      default: return 'default'
    }
  }

  const getRoleColor = (role: string) => {
    switch (role) {
      case 'OWNER': return 'error'
      case 'ADMIN': return 'primary'
      case 'FINANCE': return 'success'
      case 'SUPPORT': return 'info'
      case 'CONTENT': return 'warning'
      case 'ANALYST': return 'default'
      case 'COMPLIANCE': return 'secondary'
      default: return 'default'
    }
  }

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-'
    return new Date(dateString).toLocaleString('en-GB')
  }

  return (
    <AdminLayout title="Administrators">
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h5">Administrator Management</Typography>
        <Button variant="contained" onClick={() => setCreateDialogOpen(true)}>
          Add Administrator
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Username</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Role</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Last Login</TableCell>
              <TableCell>Created</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {administrators.map((admin) => (
              <TableRow key={admin.id} hover>
                <TableCell>{admin.id}</TableCell>
                <TableCell>{admin.username}</TableCell>
                <TableCell>{admin.firstName} {admin.lastName}</TableCell>
                <TableCell>{admin.email}</TableCell>
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
                <TableCell>{formatDate(admin.lastLoginAt)}</TableCell>
                <TableCell>{formatDate(admin.createdAt)}</TableCell>
                <TableCell align="center">
                  {admin.role !== 'OWNER' && (
                    <Button
                      size="small"
                      variant="outlined"
                      onClick={() => handleOpenStatusDialog(admin)}
                    >
                      Change Status
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={totalAdmins}
          page={page}
          onPageChange={handleChangePage}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={handleChangeRowsPerPage}
          rowsPerPageOptions={[10, 25, 50, 100]}
        />
      </TableContainer>

      {/* Create Admin Dialog */}
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Administrator</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              label="Username"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Email"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Password"
              type="password"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="First Name"
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              fullWidth
              required
            />
            <TextField
              label="Last Name"
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              fullWidth
              required
            />
            <TextField
              select
              label="Role"
              value={formData.role}
              onChange={(e) => setFormData({ ...formData, role: e.target.value })}
              fullWidth
              required
            >
              <MenuItem value="ADMIN">Admin</MenuItem>
              <MenuItem value="FINANCE">Finance</MenuItem>
              <MenuItem value="SUPPORT">Support</MenuItem>
              <MenuItem value="CONTENT">Content Manager</MenuItem>
              <MenuItem value="ANALYST">Analyst</MenuItem>
              <MenuItem value="COMPLIANCE">Compliance Officer</MenuItem>
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleCreateAdmin} variant="contained">Create</Button>
        </DialogActions>
      </Dialog>

      {/* Change Status Dialog */}
      <Dialog open={statusDialogOpen} onClose={() => setStatusDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Change Administrator Status</DialogTitle>
        <DialogContent>
          {selectedAdmin && (
            <Box sx={{ pt: 2 }}>
              <Typography variant="body1" gutterBottom>
                Admin: <strong>{selectedAdmin.firstName} {selectedAdmin.lastName}</strong> ({selectedAdmin.username})
              </Typography>
              <TextField
                select
                label="Status"
                value={newStatus}
                onChange={(e) => setNewStatus(e.target.value)}
                fullWidth
                sx={{ mt: 2 }}
              >
                <MenuItem value="ACTIVE">Active</MenuItem>
                <MenuItem value="INACTIVE">Inactive</MenuItem>
                <MenuItem value="SUSPENDED">Suspended</MenuItem>
              </TextField>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStatusDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleUpdateStatus} variant="contained">Update</Button>
        </DialogActions>
      </Dialog>
    </AdminLayout>
  )
}

export default Admins
