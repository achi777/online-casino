import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, IconButton, Dialog, DialogTitle,
  DialogContent, DialogActions, Button, Grid, Tabs, Tab, TextField,
  Alert, CircularProgress, Card, CardMedia, CardContent
} from '@mui/material'
import {
  CheckCircle as ApproveIcon,
  Cancel as RejectIcon,
  Visibility as ViewIcon,
  Close as CloseIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'
import axios from 'axios'

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
  documentIssuingCountry: string
  documentFrontImageUrl: string
  documentBackImageUrl?: string
  selfieImageUrl: string
  proofOfAddressUrl: string
  submittedAt: string
  reviewedAt?: string
  reviewedByAdminId?: number
  rejectionReason?: string
  notes?: string
  createdAt: string
  updatedAt: string
}

interface KYCStats {
  total: number
  pending: number
  verified: number
  rejected: number
}

const KYC = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const [documents, setDocuments] = useState<KYCDocument[]>([])
  const [stats, setStats] = useState<KYCStats | null>(null)
  const [tabValue, setTabValue] = useState(0)
  const [selectedDocument, setSelectedDocument] = useState<KYCDocument | null>(null)
  const [detailsOpen, setDetailsOpen] = useState(false)
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false)
  const [rejectionReason, setRejectionReason] = useState('')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [messageType, setMessageType] = useState<'success' | 'error'>('success')
  const [imageViewerOpen, setImageViewerOpen] = useState(false)
  const [selectedImage, setSelectedImage] = useState<string | null>(null)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else {
      fetchKYCDocuments()
      fetchKYCStats()
    }
  }, [isAuthenticated, navigate])

  const fetchKYCDocuments = async () => {
    try {
      const statusFilter = tabValue === 0 ? 'PENDING' : tabValue === 1 ? 'VERIFIED' : 'REJECTED'
      const response = await axios.get('/api/admin/kyc', {
        params: { status: statusFilter }
      })
      setDocuments(response.data.content || [])
    } catch (err) {
      console.error('Failed to fetch KYC documents:', err)
      setDocuments([])
    }
  }

  const fetchKYCStats = async () => {
    try {
      const response = await axios.get('/api/admin/kyc/stats')
      setStats(response.data)
    } catch (err) {
      console.error('Failed to fetch KYC stats:', err)
    }
  }

  useEffect(() => {
    if (isAuthenticated && permissions.canManageKYC) {
      fetchKYCDocuments()
    }
  }, [tabValue, isAuthenticated, permissions])

  const handleApprove = async (document: KYCDocument) => {
    setLoading(true)
    setMessage('')
    try {
      await axios.post(`/api/admin/kyc/${document.id}/review`, {
        status: 'VERIFIED'
      })
      setMessage('KYC approved successfully')
      setMessageType('success')
      fetchKYCDocuments()
      fetchKYCStats()
      setDetailsOpen(false)
    } catch (err: any) {
      setMessage(err.response?.data?.error || 'Failed to approve KYC')
      setMessageType('error')
    } finally {
      setLoading(false)
    }
  }

  const handleRejectClick = (document: KYCDocument) => {
    setSelectedDocument(document)
    setRejectDialogOpen(true)
    setRejectionReason('')
  }

  const handleRejectConfirm = async () => {
    if (!selectedDocument) return

    if (!rejectionReason.trim()) {
      setMessage('Rejection reason is required')
      setMessageType('error')
      return
    }

    setLoading(true)
    setMessage('')
    try {
      await axios.post(`/api/admin/kyc/${selectedDocument.id}/review`, {
        status: 'REJECTED',
        rejectionReason
      })
      setMessage('KYC rejected successfully')
      setMessageType('success')
      fetchKYCDocuments()
      fetchKYCStats()
      setRejectDialogOpen(false)
      setDetailsOpen(false)
      setRejectionReason('')
    } catch (err: any) {
      setMessage(err.response?.data?.error || 'Failed to reject KYC')
      setMessageType('error')
    } finally {
      setLoading(false)
    }
  }

  const handleViewDetails = async (document: KYCDocument) => {
    try {
      const response = await axios.get(`/api/admin/kyc/${document.id}`)
      setSelectedDocument(response.data)
      setDetailsOpen(true)
    } catch (err) {
      console.error('Failed to fetch KYC details:', err)
      setSelectedDocument(document)
      setDetailsOpen(true)
    }
  }

  const handleImageClick = (imageUrl: string) => {
    setSelectedImage(imageUrl)
    setImageViewerOpen(true)
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'VERIFIED': return 'success'
      case 'REJECTED': return 'error'
      case 'PENDING': return 'warning'
      default: return 'default'
    }
  }

  return (
    <AdminLayout title="KYC Verification">
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>KYC Verification Management</Typography>
        <Typography color="text.secondary">
          Manage user identity verification requests
        </Typography>
      </Box>

      {message && (
        <Alert severity={messageType} sx={{ mb: 3 }} onClose={() => setMessage('')}>
          {message}
        </Alert>
      )}

      {/* Stats Cards */}
      {stats && (
        <Grid container spacing={2} sx={{ mb: 3 }}>
          <Grid item xs={12} sm={3}>
            <Paper sx={{ p: 2, textAlign: 'center' }}>
              <Typography variant="h4">{stats.total}</Typography>
              <Typography color="text.secondary">Total</Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={3}>
            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: 'warning.dark' }}>
              <Typography variant="h4">{stats.pending}</Typography>
              <Typography>Pending</Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={3}>
            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: 'success.dark' }}>
              <Typography variant="h4">{stats.verified}</Typography>
              <Typography>Verified</Typography>
            </Paper>
          </Grid>
          <Grid item xs={12} sm={3}>
            <Paper sx={{ p: 2, textAlign: 'center', bgcolor: 'error.dark' }}>
              <Typography variant="h4">{stats.rejected}</Typography>
              <Typography>Rejected</Typography>
            </Paper>
          </Grid>
        </Grid>
      )}

      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)}>
          <Tab label={`Pending (${stats?.pending || 0})`} />
          <Tab label={`Verified (${stats?.verified || 0})`} />
          <Tab label={`Rejected (${stats?.rejected || 0})`} />
        </Tabs>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>User ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Document Type</TableCell>
              <TableCell>Nationality</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Submitted</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {documents.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Typography color="text.secondary">No KYC documents found</Typography>
                </TableCell>
              </TableRow>
            ) : (
              documents.map((doc) => (
                <TableRow key={doc.id} hover>
                  <TableCell>{doc.id}</TableCell>
                  <TableCell>{doc.userId}</TableCell>
                  <TableCell>{doc.firstName} {doc.lastName}</TableCell>
                  <TableCell>{doc.documentType.replace('_', ' ')}</TableCell>
                  <TableCell>{doc.nationality}</TableCell>
                  <TableCell>
                    <Chip
                      label={doc.status}
                      color={getStatusColor(doc.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{new Date(doc.submittedAt).toLocaleDateString()}</TableCell>
                  <TableCell align="center">
                    <IconButton size="small" onClick={() => handleViewDetails(doc)}>
                      <ViewIcon />
                    </IconButton>
                    {doc.status === 'PENDING' && (
                      <>
                        <IconButton
                          size="small"
                          color="success"
                          onClick={() => handleApprove(doc)}
                          disabled={loading}
                        >
                          <ApproveIcon />
                        </IconButton>
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleRejectClick(doc)}
                          disabled={loading}
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
      <Dialog open={detailsOpen} onClose={() => setDetailsOpen(false)} maxWidth="lg" fullWidth>
        <DialogTitle>KYC Document Details</DialogTitle>
        <DialogContent>
          {selectedDocument && (
            <Box sx={{ mt: 2 }}>
              <Grid container spacing={3}>
                {/* Personal Information */}
                <Grid item xs={12}>
                  <Typography variant="h6" gutterBottom>Personal Information</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Full Name</Typography>
                  <Typography>{selectedDocument.firstName} {selectedDocument.lastName}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Date of Birth</Typography>
                  <Typography>{new Date(selectedDocument.dateOfBirth).toLocaleDateString()}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Nationality</Typography>
                  <Typography>{selectedDocument.nationality}</Typography>
                </Grid>

                {/* Address Information */}
                <Grid item xs={12}>
                  <Typography variant="h6" gutterBottom>Address</Typography>
                </Grid>
                <Grid item xs={12}>
                  <Typography variant="body2" color="text.secondary">Address</Typography>
                  <Typography>
                    {selectedDocument.addressLine1}
                    {selectedDocument.addressLine2 && `, ${selectedDocument.addressLine2}`}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography variant="body2" color="text.secondary">City</Typography>
                  <Typography>{selectedDocument.city}</Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography variant="body2" color="text.secondary">Country</Typography>
                  <Typography>{selectedDocument.country}</Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography variant="body2" color="text.secondary">Postal Code</Typography>
                  <Typography>{selectedDocument.postalCode}</Typography>
                </Grid>

                {/* Document Information */}
                <Grid item xs={12}>
                  <Typography variant="h6" gutterBottom>Document Details</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Document Type</Typography>
                  <Typography>{selectedDocument.documentType.replace('_', ' ')}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Document Number</Typography>
                  <Typography>{selectedDocument.documentNumber}</Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography variant="body2" color="text.secondary">Issue Date</Typography>
                  <Typography>{new Date(selectedDocument.documentIssueDate).toLocaleDateString()}</Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography variant="body2" color="text.secondary">Expiry Date</Typography>
                  <Typography>{new Date(selectedDocument.documentExpiryDate).toLocaleDateString()}</Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography variant="body2" color="text.secondary">Issuing Country</Typography>
                  <Typography>{selectedDocument.documentIssuingCountry}</Typography>
                </Grid>

                {/* Document Images */}
                <Grid item xs={12}>
                  <Typography variant="h6" gutterBottom>Document Images</Typography>
                  <Alert severity="info" sx={{ mb: 2 }}>
                    Click on any image to view it in full size
                  </Alert>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Card sx={{ cursor: 'pointer' }} onClick={() => handleImageClick(selectedDocument.documentFrontImageUrl)}>
                    <CardMedia
                      component="img"
                      height="200"
                      image={selectedDocument.documentFrontImageUrl}
                      alt="Document Front"
                      sx={{ objectFit: 'contain', bgcolor: '#f5f5f5' }}
                    />
                    <CardContent>
                      <Typography variant="body2" color="text.secondary" align="center">
                        Document Front
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
                {selectedDocument.documentBackImageUrl && (
                  <Grid item xs={12} sm={6}>
                    <Card sx={{ cursor: 'pointer' }} onClick={() => handleImageClick(selectedDocument.documentBackImageUrl!)}>
                      <CardMedia
                        component="img"
                        height="200"
                        image={selectedDocument.documentBackImageUrl}
                        alt="Document Back"
                        sx={{ objectFit: 'contain', bgcolor: '#f5f5f5' }}
                      />
                      <CardContent>
                        <Typography variant="body2" color="text.secondary" align="center">
                          Document Back
                        </Typography>
                      </CardContent>
                    </Card>
                  </Grid>
                )}
                <Grid item xs={12} sm={6}>
                  <Card sx={{ cursor: 'pointer' }} onClick={() => handleImageClick(selectedDocument.selfieImageUrl)}>
                    <CardMedia
                      component="img"
                      height="200"
                      image={selectedDocument.selfieImageUrl}
                      alt="Selfie with Document"
                      sx={{ objectFit: 'contain', bgcolor: '#f5f5f5' }}
                    />
                    <CardContent>
                      <Typography variant="body2" color="text.secondary" align="center">
                        Selfie with Document
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Card sx={{ cursor: 'pointer' }} onClick={() => handleImageClick(selectedDocument.proofOfAddressUrl)}>
                    <CardMedia
                      component="img"
                      height="200"
                      image={selectedDocument.proofOfAddressUrl}
                      alt="Proof of Address"
                      sx={{ objectFit: 'contain', bgcolor: '#f5f5f5' }}
                    />
                    <CardContent>
                      <Typography variant="body2" color="text.secondary" align="center">
                        Proof of Address
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>

                {/* Additional Info */}
                {selectedDocument.notes && (
                  <Grid item xs={12}>
                    <Typography variant="body2" color="text.secondary">Notes</Typography>
                    <Typography>{selectedDocument.notes}</Typography>
                  </Grid>
                )}

                {/* Status Info */}
                <Grid item xs={12}>
                  <Typography variant="h6" gutterBottom>Status Information</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Status</Typography>
                  <Chip
                    label={selectedDocument.status}
                    color={getStatusColor(selectedDocument.status) as any}
                    sx={{ mt: 0.5 }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="body2" color="text.secondary">Submitted At</Typography>
                  <Typography>{new Date(selectedDocument.submittedAt).toLocaleString()}</Typography>
                </Grid>
                {selectedDocument.reviewedAt && (
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Reviewed At</Typography>
                    <Typography>{new Date(selectedDocument.reviewedAt).toLocaleString()}</Typography>
                  </Grid>
                )}
                {selectedDocument.rejectionReason && (
                  <Grid item xs={12}>
                    <Alert severity="error">
                      <Typography variant="body2" fontWeight="bold" gutterBottom>
                        Rejection Reason:
                      </Typography>
                      <Typography variant="body2">
                        {selectedDocument.rejectionReason}
                      </Typography>
                    </Alert>
                  </Grid>
                )}
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          {selectedDocument?.status === 'PENDING' && (
            <>
              <Button
                onClick={() => handleApprove(selectedDocument)}
                color="success"
                variant="contained"
                disabled={loading}
                startIcon={loading ? <CircularProgress size={20} /> : <ApproveIcon />}
              >
                Approve
              </Button>
              <Button
                onClick={() => handleRejectClick(selectedDocument)}
                color="error"
                variant="contained"
                disabled={loading}
                startIcon={<RejectIcon />}
              >
                Reject
              </Button>
            </>
          )}
          <Button onClick={() => setDetailsOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Reject Dialog */}
      <Dialog open={rejectDialogOpen} onClose={() => setRejectDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Reject KYC Verification</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Please provide a reason for rejecting this KYC verification:
          </Typography>
          <TextField
            fullWidth
            multiline
            rows={4}
            label="Rejection Reason"
            value={rejectionReason}
            onChange={(e) => setRejectionReason(e.target.value)}
            required
            error={!rejectionReason.trim()}
            helperText={!rejectionReason.trim() ? 'Rejection reason is required' : ''}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejectDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={handleRejectConfirm}
            color="error"
            variant="contained"
            disabled={loading || !rejectionReason.trim()}
            startIcon={loading ? <CircularProgress size={20} /> : <RejectIcon />}
          >
            {loading ? 'Rejecting...' : 'Confirm Rejection'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Image Viewer Dialog */}
      <Dialog
        open={imageViewerOpen}
        onClose={() => setImageViewerOpen(false)}
        maxWidth="lg"
        fullWidth
      >
        <DialogTitle>
          Document Image
          <IconButton
            onClick={() => setImageViewerOpen(false)}
            sx={{ position: 'absolute', right: 8, top: 8 }}
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent>
          {selectedImage && (
            <Box
              component="img"
              src={selectedImage}
              alt="Document"
              sx={{
                width: '100%',
                height: 'auto',
                maxHeight: '80vh',
                objectFit: 'contain'
              }}
            />
          )}
        </DialogContent>
      </Dialog>
    </AdminLayout>
  )
}

export default KYC
