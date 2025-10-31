import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, IconButton, Dialog, DialogTitle,
  DialogContent, DialogActions, Button, Grid, Tabs, Tab
} from '@mui/material'
import {
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  Visibility as ViewIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'
import axios from 'axios'

interface KYCRequest {
  id: number
  userId: number
  userEmail: string
  userName: string
  documentType: string
  documentNumber: string
  status: string
  submittedAt: string
  reviewedAt?: string
}

const KYC = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const [requests, setRequests] = useState<KYCRequest[]>([])
  const [tabValue, setTabValue] = useState(0)
  const [selectedRequest, setSelectedRequest] = useState<KYCRequest | null>(null)
  const [detailsOpen, setDetailsOpen] = useState(false)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canManageKYC) {
      navigate('/dashboard')
    } else {
      fetchKYCRequests()
    }
  }, [isAuthenticated, permissions, navigate])

  const fetchKYCRequests = async () => {
    try {
      const response = await axios.get('/api/admin/kyc/requests')
      setRequests(response.data || [])
    } catch (err) {
      console.error('Failed to fetch KYC requests:', err)
      // Mock data for demonstration
      setRequests([])
    }
  }

  const handleApprove = async (request: KYCRequest) => {
    try {
      await axios.put(`/api/admin/users/${request.userId}/kyc`, { kycStatus: 'VERIFIED' })
      fetchKYCRequests()
    } catch (err) {
      console.error('Failed to approve KYC:', err)
    }
  }

  const handleReject = async (request: KYCRequest) => {
    try {
      await axios.put(`/api/admin/users/${request.userId}/kyc`, { kycStatus: 'REJECTED' })
      fetchKYCRequests()
    } catch (err) {
      console.error('Failed to reject KYC:', err)
    }
  }

  const handleViewDetails = (request: KYCRequest) => {
    setSelectedRequest(request)
    setDetailsOpen(true)
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'VERIFIED': return 'success'
      case 'REJECTED': return 'error'
      case 'PENDING': return 'warning'
      default: return 'default'
    }
  }

  const filteredRequests = requests.filter(req => {
    if (tabValue === 0) return req.status === 'PENDING'
    if (tabValue === 1) return req.status === 'VERIFIED'
    if (tabValue === 2) return req.status === 'REJECTED'
    return true
  })

  return (
    <AdminLayout title="KYC Verification">
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>KYC Verification Management</Typography>
        <Typography color="text.secondary">
          Manage user identity verification requests
        </Typography>
      </Box>

      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)}>
          <Tab label="Pending" />
          <Tab label="Verified" />
          <Tab label="Rejected" />
        </Tabs>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>User ID</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Document Type</TableCell>
              <TableCell>Document Number</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Submitted</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredRequests.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Typography color="text.secondary">No KYC requests found</Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredRequests.map((request) => (
                <TableRow key={request.id} hover>
                  <TableCell>{request.userId}</TableCell>
                  <TableCell>{request.userEmail}</TableCell>
                  <TableCell>{request.userName}</TableCell>
                  <TableCell>{request.documentType}</TableCell>
                  <TableCell>{request.documentNumber}</TableCell>
                  <TableCell>
                    <Chip
                      label={request.status}
                      color={getStatusColor(request.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{new Date(request.submittedAt).toLocaleDateString()}</TableCell>
                  <TableCell align="center">
                    <IconButton size="small" onClick={() => handleViewDetails(request)}>
                      <ViewIcon />
                    </IconButton>
                    {request.status === 'PENDING' && (
                      <>
                        <IconButton
                          size="small"
                          color="success"
                          onClick={() => handleApprove(request)}
                        >
                          <ApproveIcon />
                        </IconButton>
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleReject(request)}
                        >
                          <RejectIcon />
                        </IconButton>
                      </>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Details Dialog */}
      <Dialog open={detailsOpen} onClose={() => setDetailsOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>KYC Request Details</DialogTitle>
        <DialogContent>
          {selectedRequest && (
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">User Email</Typography>
                <Typography>{selectedRequest.userEmail}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Name</Typography>
                <Typography>{selectedRequest.userName}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Document Type</Typography>
                <Typography>{selectedRequest.documentType}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Document Number</Typography>
                <Typography>{selectedRequest.documentNumber}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Document Images
                </Typography>
                <Typography color="text.secondary">
                  Document upload feature will be implemented
                </Typography>
              </Grid>
            </Grid>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </AdminLayout>
  )
}

export default KYC
