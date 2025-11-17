import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Paper, Chip, TextField, InputAdornment, Dialog, DialogTitle, DialogContent,
  DialogActions, Select, MenuItem, FormControl, InputLabel, Grid, TablePagination,
  IconButton, Button, Divider, Alert, CircularProgress
} from '@mui/material'
import {
  Search as SearchIcon,
  Edit as EditIcon,
  Visibility as VisibilityIcon,
  VerifiedUser as KYCIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import axios from 'axios'

interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  phone: string
  balance: number
  status: string
  kycStatus: string
  createdAt: string
}

interface KYCDocument {
  id: number
  userId: number
  status: 'PENDING' | 'VERIFIED' | 'REJECTED'
  firstName: string
  lastName: string
  dateOfBirth: string
  nationality: string
  addressLine1: string
  addressLine2?: string
  city: string
  country: string
  postalCode: string
  documentType: 'PASSPORT' | 'NATIONAL_ID' | 'DRIVERS_LICENSE'
  documentNumber: string
  documentIssueDate: string
  documentExpiryDate: string
  documentIssuingCountry?: string
  documentFrontImageUrl?: string
  documentBackImageUrl?: string
  selfieImageUrl?: string
  proofOfAddressUrl?: string
  submittedAt: string
  reviewedAt?: string
  reviewedByAdminId?: number
  rejectionReason?: string
  notes?: string
}

const Users = () => {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [users, setUsers] = useState<User[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedUser, setSelectedUser] = useState<User | null>(null)
  const [userKycDocument, setUserKycDocument] = useState<KYCDocument | null>(null)
  const [loadingKyc, setLoadingKyc] = useState(false)
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [totalUsers, setTotalUsers] = useState(0)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else {
      fetchUsers()
    }
  }, [isAuthenticated, navigate, page, rowsPerPage, searchTerm])

  const fetchUsers = async () => {
    try {
      const params = {
        page: page,
        size: rowsPerPage,
        search: searchTerm || undefined
      }
      const response = await axios.get('/api/admin/users', { params })
      setUsers(response.data.content || response.data)
      setTotalUsers(response.data.totalElements || response.data.length)
    } catch (err) {
      console.error('Failed to fetch users:', err)
    }
  }

  const handleEditUser = (user: User) => {
    setSelectedUser(user)
    setEditDialogOpen(true)
  }

  const handleViewDetails = async (user: User) => {
    setSelectedUser(user)
    setDetailsDialogOpen(true)
    setUserKycDocument(null)

    // Fetch user's KYC document if exists
    if (user.kycStatus !== 'NOT_SUBMITTED') {
      setLoadingKyc(true)
      try {
        // Get all KYC documents for this user and take the most recent one
        const response = await axios.get('/api/admin/kyc', {
          params: { page: 0, size: 1 }
        })

        // Filter by userId in frontend (since backend might not support user filtering)
        const userKyc = response.data.content?.find((kyc: KYCDocument) => kyc.userId === user.id)

        if (userKyc) {
          // Fetch full details
          const detailsResponse = await axios.get(`/api/admin/kyc/${userKyc.id}`)
          setUserKycDocument(detailsResponse.data)
        }
      } catch (err) {
        console.error('Failed to fetch KYC document:', err)
      } finally {
        setLoadingKyc(false)
      }
    }
  }

  const handleSaveUser = async () => {
    if (!selectedUser) return

    try {
      await axios.put(`/api/admin/users/${selectedUser.id}`, {
        status: selectedUser.status,
        kycStatus: selectedUser.kycStatus
      })
      setEditDialogOpen(false)
      fetchUsers()
    } catch (err) {
      console.error('Failed to update user:', err)
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
      case 'BLOCKED': return 'error'
      case 'SUSPENDED': return 'warning'
      default: return 'default'
    }
  }

  const getKYCStatusColor = (status: string) => {
    switch (status) {
      case 'VERIFIED': return 'success'
      case 'REJECTED': return 'error'
      case 'PENDING': return 'warning'
      default: return 'default'
    }
  }

  const formatCurrency = (amount: number) => {
    return `₾${amount.toFixed(2)}`
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-GB')
  }

  return (
    <AdminLayout title="Users Management">

        <Box sx={{ mb: 3 }}>
          <TextField
            fullWidth
            placeholder="Search by email, name, or phone..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
          />
        </Box>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Phone</TableCell>
                <TableCell align="right">Balance</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>KYC Status</TableCell>
                <TableCell>Registered</TableCell>
                <TableCell align="center">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map((user) => (
                <TableRow key={user.id} hover>
                  <TableCell>{user.id}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>{`${user.firstName} ${user.lastName}`}</TableCell>
                  <TableCell>{user.phone}</TableCell>
                  <TableCell align="right">{formatCurrency(user.balance)}</TableCell>
                  <TableCell>
                    <Chip
                      label={user.status}
                      color={getStatusColor(user.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={user.kycStatus}
                      color={getKYCStatusColor(user.kycStatus) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{formatDate(user.createdAt)}</TableCell>
                  <TableCell align="center">
                    <IconButton
                      size="small"
                      onClick={() => handleViewDetails(user)}
                      title="View Details"
                    >
                      <VisibilityIcon />
                    </IconButton>
                    <IconButton
                      size="small"
                      onClick={() => handleEditUser(user)}
                      title="Edit User"
                    >
                      <EditIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <TablePagination
            component="div"
            count={totalUsers}
            page={page}
            onPageChange={handleChangePage}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={handleChangeRowsPerPage}
            rowsPerPageOptions={[5, 10, 25, 50]}
          />
        </TableContainer>

        {/* Edit User Dialog */}
        <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Edit User</DialogTitle>
          <DialogContent>
            {selectedUser && (
              <Box sx={{ pt: 2 }}>
                <Typography variant="body2" gutterBottom>
                  <strong>Email:</strong> {selectedUser.email}
                </Typography>
                <Typography variant="body2" gutterBottom sx={{ mb: 3 }}>
                  <strong>Name:</strong> {selectedUser.firstName} {selectedUser.lastName}
                </Typography>

                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={selectedUser.status}
                    label="Status"
                    onChange={(e) => setSelectedUser({ ...selectedUser, status: e.target.value })}
                  >
                    <MenuItem value="ACTIVE">ACTIVE</MenuItem>
                    <MenuItem value="BLOCKED">BLOCKED</MenuItem>
                    <MenuItem value="SUSPENDED">SUSPENDED</MenuItem>
                  </Select>
                </FormControl>

                <FormControl fullWidth>
                  <InputLabel>KYC Status</InputLabel>
                  <Select
                    value={selectedUser.kycStatus}
                    label="KYC Status"
                    onChange={(e) => setSelectedUser({ ...selectedUser, kycStatus: e.target.value })}
                  >
                    <MenuItem value="PENDING">PENDING</MenuItem>
                    <MenuItem value="VERIFIED">VERIFIED</MenuItem>
                    <MenuItem value="REJECTED">REJECTED</MenuItem>
                  </Select>
                </FormControl>
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleSaveUser} variant="contained">Save</Button>
          </DialogActions>
        </Dialog>

        {/* User Details Dialog */}
        <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="lg" fullWidth>
          <DialogTitle>User Details</DialogTitle>
          <DialogContent>
            {selectedUser && (
              <Box sx={{ pt: 2 }}>
                <Typography variant="h6" gutterBottom>Account Information</Typography>
                <Grid container spacing={2} sx={{ mb: 3 }}>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">User ID</Typography>
                    <Typography variant="body1" gutterBottom>{selectedUser.id}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Email</Typography>
                    <Typography variant="body1" gutterBottom>{selectedUser.email}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">First Name</Typography>
                    <Typography variant="body1" gutterBottom>{selectedUser.firstName}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Last Name</Typography>
                    <Typography variant="body1" gutterBottom>{selectedUser.lastName}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Phone</Typography>
                    <Typography variant="body1" gutterBottom>{selectedUser.phone}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Balance</Typography>
                    <Typography variant="body1" gutterBottom color="success.main">
                      {formatCurrency(selectedUser.balance)}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Status</Typography>
                    <Box sx={{ mt: 0.5 }}>
                      <Chip
                        label={selectedUser.status}
                        color={getStatusColor(selectedUser.status) as any}
                        size="small"
                      />
                    </Box>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">KYC Status</Typography>
                    <Box sx={{ mt: 0.5 }}>
                      <Chip
                        label={selectedUser.kycStatus}
                        color={getKYCStatusColor(selectedUser.kycStatus) as any}
                        size="small"
                      />
                    </Box>
                  </Grid>
                  <Grid item xs={12}>
                    <Typography variant="body2" color="text.secondary">Registration Date</Typography>
                    <Typography variant="body1">{formatDate(selectedUser.createdAt)}</Typography>
                  </Grid>
                </Grid>

                <Divider sx={{ my: 3 }} />

                {/* KYC Information Section */}
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                  <KYCIcon sx={{ mr: 1 }} />
                  <Typography variant="h6">KYC Verification Details</Typography>
                </Box>

                {loadingKyc ? (
                  <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}>
                    <CircularProgress />
                  </Box>
                ) : userKycDocument ? (
                  <Grid container spacing={2}>
                    <Grid item xs={12}>
                      <Alert severity={
                        userKycDocument.status === 'VERIFIED' ? 'success' :
                        userKycDocument.status === 'REJECTED' ? 'error' : 'warning'
                      }>
                        Status: <strong>{userKycDocument.status}</strong>
                        {userKycDocument.reviewedAt && (
                          <> • Reviewed: {new Date(userKycDocument.reviewedAt).toLocaleString()}</>
                        )}
                      </Alert>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="text.secondary">Full Name (KYC)</Typography>
                      <Typography variant="body1">{userKycDocument.firstName} {userKycDocument.lastName}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="text.secondary">Date of Birth</Typography>
                      <Typography variant="body1">{new Date(userKycDocument.dateOfBirth).toLocaleDateString()}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="text.secondary">Nationality</Typography>
                      <Typography variant="body1">{userKycDocument.nationality}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="text.secondary">Document Type</Typography>
                      <Typography variant="body1">{userKycDocument.documentType.replace('_', ' ')}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="text.secondary">Document Number</Typography>
                      <Typography variant="body1">{userKycDocument.documentNumber}</Typography>
                    </Grid>
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="text.secondary">Submitted At</Typography>
                      <Typography variant="body1">{new Date(userKycDocument.submittedAt).toLocaleString()}</Typography>
                    </Grid>

                    <Grid item xs={12}>
                      <Typography variant="body2" color="text.secondary">Address</Typography>
                      <Typography variant="body1">
                        {userKycDocument.addressLine1}
                        {userKycDocument.addressLine2 && `, ${userKycDocument.addressLine2}`}
                        <br />
                        {userKycDocument.city}, {userKycDocument.country} {userKycDocument.postalCode}
                      </Typography>
                    </Grid>

                    {userKycDocument.rejectionReason && (
                      <Grid item xs={12}>
                        <Alert severity="error">
                          <Typography variant="body2" fontWeight="bold" gutterBottom>
                            Rejection Reason:
                          </Typography>
                          <Typography variant="body2">
                            {userKycDocument.rejectionReason}
                          </Typography>
                        </Alert>
                      </Grid>
                    )}

                    <Grid item xs={12}>
                      <Button
                        variant="outlined"
                        startIcon={<KYCIcon />}
                        onClick={() => navigate('/kyc')}
                        fullWidth
                      >
                        View Full KYC Details & Manage
                      </Button>
                    </Grid>
                  </Grid>
                ) : (
                  <Alert severity="info">
                    No KYC document submitted yet for this user.
                  </Alert>
                )}
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDetailsDialogOpen(false)}>Close</Button>
          </DialogActions>
        </Dialog>
    </AdminLayout>
  )
}

export default Users
