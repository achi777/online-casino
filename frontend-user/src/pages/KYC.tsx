import { useState, useEffect } from 'react'
import {
  Container,
  Typography,
  Box,
  Paper,
  TextField,
  Button,
  Grid,
  Alert,
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Stepper,
  Step,
  StepLabel
} from '@mui/material'
import axios from 'axios'
import { useQuery } from 'react-query'
import Navbar from '../components/Navbar'
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser'
import PendingIcon from '@mui/icons-material/Pending'
import CancelIcon from '@mui/icons-material/Cancel'
import UploadFileIcon from '@mui/icons-material/UploadFile'

interface KYCStatus {
  id: number
  status: 'PENDING' | 'VERIFIED' | 'REJECTED'
  firstName: string
  lastName: string
  dateOfBirth: string
  nationality: string
  addressLine1: string
  addressLine2: string
  city: string
  country: string
  postalCode: string
  documentType: 'PASSPORT' | 'NATIONAL_ID' | 'DRIVERS_LICENSE'
  documentNumber: string
  documentIssueDate: string
  documentExpiryDate: string
  documentIssuingCountry: string
  documentFrontImageUrl: string
  documentBackImageUrl: string
  selfieImageUrl: string
  proofOfAddressUrl: string
  submittedAt: string
  reviewedAt?: string
  rejectionReason?: string
}

const KYC = () => {
  const [activeStep, setActiveStep] = useState(0)
  const [message, setMessage] = useState('')
  const [messageType, setMessageType] = useState<'success' | 'error' | 'info'>('info')
  const [loading, setLoading] = useState(false)

  // Form fields
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [dateOfBirth, setDateOfBirth] = useState('')
  const [nationality, setNationality] = useState('')
  const [addressLine1, setAddressLine1] = useState('')
  const [addressLine2, setAddressLine2] = useState('')
  const [city, setCity] = useState('')
  const [country, setCountry] = useState('')
  const [postalCode, setPostalCode] = useState('')
  const [documentType, setDocumentType] = useState<'PASSPORT' | 'NATIONAL_ID' | 'DRIVERS_LICENSE'>('NATIONAL_ID')
  const [documentNumber, setDocumentNumber] = useState('')
  const [documentIssueDate, setDocumentIssueDate] = useState('')
  const [documentExpiryDate, setDocumentExpiryDate] = useState('')
  const [documentIssuingCountry, setDocumentIssuingCountry] = useState('')
  const [documentFrontImageUrl, setDocumentFrontImageUrl] = useState('')
  const [documentBackImageUrl, setDocumentBackImageUrl] = useState('')
  const [selfieImageUrl, setSelfieImageUrl] = useState('')
  const [proofOfAddressUrl, setProofOfAddressUrl] = useState('')
  const [notes, setNotes] = useState('')
  const [uploading, setUploading] = useState<string | null>(null)
  const [showResubmitForm, setShowResubmitForm] = useState(false)

  const { data: kycStatus, refetch: refetchKYC, isLoading: kycLoading } = useQuery<KYCStatus | null>(
    'kycStatus',
    async () => {
      try {
        const response = await axios.get('/api/user/kyc/status')
        return response.data
      } catch (error: any) {
        if (error.response?.status === 404) {
          return null
        }
        throw error
      }
    }
  )

  const steps = ['Personal Information', 'Address', 'Document Details', 'Upload Documents']

  const handleNext = () => {
    setActiveStep((prevActiveStep) => prevActiveStep + 1)
  }

  const handleBack = () => {
    setActiveStep((prevActiveStep) => prevActiveStep - 1)
  }

  const handleFileUpload = async (file: File, fieldName: string) => {
    setUploading(fieldName)
    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await axios.post('/api/files/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      const fileUrl = 'http://localhost:8080' + response.data.url

      // Update the appropriate state based on fieldName
      switch (fieldName) {
        case 'documentFront':
          setDocumentFrontImageUrl(fileUrl)
          break
        case 'documentBack':
          setDocumentBackImageUrl(fileUrl)
          break
        case 'selfie':
          setSelfieImageUrl(fileUrl)
          break
        case 'proofOfAddress':
          setProofOfAddressUrl(fileUrl)
          break
      }

      setMessage(`File uploaded successfully!`)
      setMessageType('success')
    } catch (error: any) {
      setMessage(error.response?.data?.error || 'File upload failed')
      setMessageType('error')
    } finally {
      setUploading(null)
    }
  }

  const handleSubmit = async () => {
    setLoading(true)
    setMessage('')

    try {
      await axios.post('/api/user/kyc/submit', {
        firstName,
        lastName,
        dateOfBirth,
        nationality,
        addressLine1,
        addressLine2,
        city,
        country,
        postalCode,
        documentType,
        documentNumber,
        documentIssueDate,
        documentExpiryDate,
        documentIssuingCountry,
        documentFrontImageUrl,
        documentBackImageUrl,
        selfieImageUrl,
        proofOfAddressUrl,
        notes
      })
      setMessage('KYC submitted successfully! Your documents will be reviewed shortly.')
      setMessageType('success')
      setShowResubmitForm(false)
      refetchKYC()
      setActiveStep(0)
    } catch (error: any) {
      setMessage(error.response?.data?.error || 'KYC submission failed')
      setMessageType('error')
    } finally {
      setLoading(false)
    }
  }

  if (kycLoading) {
    return (
      <>
        <Navbar />
        <Container maxWidth="lg">
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
            <CircularProgress />
          </Box>
        </Container>
      </>
    )
  }

  // Show status if KYC is PENDING or VERIFIED (not REJECTED)
  if (kycStatus && kycStatus.status !== 'REJECTED') {
    return (
      <>
        <Navbar />
        <Container maxWidth="md">
          <Box sx={{ mb: 4 }}>
            <Typography variant="h4" gutterBottom>
              KYC Verification Status
            </Typography>
          </Box>

          <Paper sx={{ p: 4 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
              {kycStatus.status === 'VERIFIED' && (
                <>
                  <VerifiedUserIcon sx={{ fontSize: 48, color: 'success.main', mr: 2 }} />
                  <Box>
                    <Typography variant="h5">Verified</Typography>
                    <Chip label="VERIFIED" color="success" sx={{ mt: 1 }} />
                  </Box>
                </>
              )}
              {kycStatus.status === 'PENDING' && (
                <>
                  <PendingIcon sx={{ fontSize: 48, color: 'warning.main', mr: 2 }} />
                  <Box>
                    <Typography variant="h5">Pending Review</Typography>
                    <Chip label="PENDING" color="warning" sx={{ mt: 1 }} />
                  </Box>
                </>
              )}
            </Box>

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Name</Typography>
                <Typography variant="body1">{kycStatus.firstName} {kycStatus.lastName}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Date of Birth</Typography>
                <Typography variant="body1">{new Date(kycStatus.dateOfBirth).toLocaleDateString()}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Nationality</Typography>
                <Typography variant="body1">{kycStatus.nationality}</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <Typography variant="body2" color="text.secondary">Document Type</Typography>
                <Typography variant="body1">{kycStatus.documentType.replace('_', ' ')}</Typography>
              </Grid>
              <Grid item xs={12}>
                <Typography variant="body2" color="text.secondary">Submitted At</Typography>
                <Typography variant="body1">{new Date(kycStatus.submittedAt).toLocaleString()}</Typography>
              </Grid>
              {kycStatus.reviewedAt && (
                <Grid item xs={12}>
                  <Typography variant="body2" color="text.secondary">Reviewed At</Typography>
                  <Typography variant="body1">{new Date(kycStatus.reviewedAt).toLocaleString()}</Typography>
                </Grid>
              )}
            </Grid>

            {kycStatus.status === 'PENDING' && (
              <Alert severity="info" sx={{ mt: 3 }}>
                Your KYC verification is being reviewed. This typically takes 24-48 hours. You will be notified once the review is complete.
              </Alert>
            )}
          </Paper>
        </Container>
      </>
    )
  }

  // Show rejection message and allow resubmission
  if (kycStatus && kycStatus.status === 'REJECTED' && !showResubmitForm) {
    return (
      <>
        <Navbar />
        <Container maxWidth="md">
          <Box sx={{ mb: 4 }}>
            <Typography variant="h4" gutterBottom>
              KYC Verification
            </Typography>
          </Box>

          <Paper sx={{ p: 4, mb: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
              <CancelIcon sx={{ fontSize: 48, color: 'error.main', mr: 2 }} />
              <Box>
                <Typography variant="h5">Verification Rejected</Typography>
                <Chip label="REJECTED" color="error" sx={{ mt: 1 }} />
              </Box>
            </Box>

            <Alert severity="error" sx={{ mb: 2 }}>
              <Typography variant="body1" fontWeight="bold" gutterBottom>
                Rejection Reason:
              </Typography>
              <Typography variant="body2">
                {kycStatus.rejectionReason || 'No reason provided'}
              </Typography>
            </Alert>

            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Please review the rejection reason and submit your KYC verification again with the correct information.
            </Typography>

            <Button
              variant="contained"
              fullWidth
              onClick={() => setShowResubmitForm(true)}
            >
              Submit New KYC Application
            </Button>
          </Paper>
        </Container>
      </>
    )
  }

  // Show KYC submission form
  return (
    <>
      <Navbar />
      <Container maxWidth="md">
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            KYC Verification
          </Typography>
          <Typography color="text.secondary">
            Complete your KYC verification to enable withdrawals
          </Typography>
        </Box>

        {message && (
          <Alert severity={messageType} sx={{ mb: 3 }} onClose={() => setMessage('')}>
            {message}
          </Alert>
        )}

        {kycStatus?.status === 'REJECTED' && showResubmitForm && kycStatus.rejectionReason && (
          <Alert severity="warning" sx={{ mb: 3 }}>
            <Typography variant="body2" fontWeight="bold" gutterBottom>
              Previous KYC was rejected. Reason:
            </Typography>
            <Typography variant="body2">
              {kycStatus.rejectionReason}
            </Typography>
            <Typography variant="body2" sx={{ mt: 1 }}>
              Please correct the issues mentioned above before resubmitting.
            </Typography>
          </Alert>
        )}

        <Paper sx={{ p: 3 }}>
          <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
            {steps.map((label) => (
              <Step key={label}>
                <StepLabel>{label}</StepLabel>
              </Step>
            ))}
          </Stepper>

          {activeStep === 0 && (
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>Personal Information</Typography>
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="First Name"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Last Name"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Date of Birth"
                  type="date"
                  value={dateOfBirth}
                  onChange={(e) => setDateOfBirth(e.target.value)}
                  InputLabelProps={{ shrink: true }}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Nationality"
                  value={nationality}
                  onChange={(e) => setNationality(e.target.value)}
                  required
                />
              </Grid>
            </Grid>
          )}

          {activeStep === 1 && (
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>Address Information</Typography>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Address Line 1"
                  value={addressLine1}
                  onChange={(e) => setAddressLine1(e.target.value)}
                  required
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Address Line 2"
                  value={addressLine2}
                  onChange={(e) => setAddressLine2(e.target.value)}
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="City"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Country"
                  value={country}
                  onChange={(e) => setCountry(e.target.value)}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Postal Code"
                  value={postalCode}
                  onChange={(e) => setPostalCode(e.target.value)}
                  required
                />
              </Grid>
            </Grid>
          )}

          {activeStep === 2 && (
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>Document Details</Typography>
              </Grid>
              <Grid item xs={12}>
                <FormControl fullWidth>
                  <InputLabel>Document Type</InputLabel>
                  <Select
                    value={documentType}
                    label="Document Type"
                    onChange={(e) => setDocumentType(e.target.value as any)}
                  >
                    <MenuItem value="PASSPORT">Passport</MenuItem>
                    <MenuItem value="NATIONAL_ID">National ID</MenuItem>
                    <MenuItem value="DRIVERS_LICENSE">Driver's License</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Document Number"
                  value={documentNumber}
                  onChange={(e) => setDocumentNumber(e.target.value)}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Issue Date"
                  type="date"
                  value={documentIssueDate}
                  onChange={(e) => setDocumentIssueDate(e.target.value)}
                  InputLabelProps={{ shrink: true }}
                  required
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  label="Expiry Date"
                  type="date"
                  value={documentExpiryDate}
                  onChange={(e) => setDocumentExpiryDate(e.target.value)}
                  InputLabelProps={{ shrink: true }}
                  required
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Issuing Country"
                  value={documentIssuingCountry}
                  onChange={(e) => setDocumentIssuingCountry(e.target.value)}
                  required
                />
              </Grid>
            </Grid>
          )}

          {activeStep === 3 && (
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>Upload Documents</Typography>
                <Alert severity="info" sx={{ mb: 2 }}>
                  Please upload images of your documents. Accepted formats: JPG, PNG. Max size: 10MB.
                </Alert>
              </Grid>

              {/* Document Front Image */}
              <Grid item xs={12}>
                <Box sx={{ border: '1px dashed #ccc', borderRadius: 1, p: 2 }}>
                  <Typography variant="body2" fontWeight="bold" gutterBottom>
                    Document Front Image *
                  </Typography>
                  <Button
                    variant="outlined"
                    component="label"
                    startIcon={<UploadFileIcon />}
                    disabled={uploading === 'documentFront'}
                    fullWidth
                  >
                    {uploading === 'documentFront' ? 'Uploading...' : 'Choose File'}
                    <input
                      type="file"
                      hidden
                      accept="image/*"
                      onChange={(e) => {
                        const file = e.target.files?.[0]
                        if (file) handleFileUpload(file, 'documentFront')
                      }}
                    />
                  </Button>
                  {documentFrontImageUrl && (
                    <Box sx={{ mt: 1 }}>
                      <Typography variant="caption" color="success.main">✓ File uploaded</Typography>
                      <Box component="img" src={documentFrontImageUrl} sx={{ width: '100%', maxWidth: 200, mt: 1, borderRadius: 1 }} />
                    </Box>
                  )}
                </Box>
              </Grid>

              {/* Document Back Image */}
              <Grid item xs={12}>
                <Box sx={{ border: '1px dashed #ccc', borderRadius: 1, p: 2 }}>
                  <Typography variant="body2" fontWeight="bold" gutterBottom>
                    Document Back Image (Optional)
                  </Typography>
                  <Button
                    variant="outlined"
                    component="label"
                    startIcon={<UploadFileIcon />}
                    disabled={uploading === 'documentBack'}
                    fullWidth
                  >
                    {uploading === 'documentBack' ? 'Uploading...' : 'Choose File'}
                    <input
                      type="file"
                      hidden
                      accept="image/*"
                      onChange={(e) => {
                        const file = e.target.files?.[0]
                        if (file) handleFileUpload(file, 'documentBack')
                      }}
                    />
                  </Button>
                  {documentBackImageUrl && (
                    <Box sx={{ mt: 1 }}>
                      <Typography variant="caption" color="success.main">✓ File uploaded</Typography>
                      <Box component="img" src={documentBackImageUrl} sx={{ width: '100%', maxWidth: 200, mt: 1, borderRadius: 1 }} />
                    </Box>
                  )}
                </Box>
              </Grid>

              {/* Selfie with Document */}
              <Grid item xs={12}>
                <Box sx={{ border: '1px dashed #ccc', borderRadius: 1, p: 2 }}>
                  <Typography variant="body2" fontWeight="bold" gutterBottom>
                    Selfie with Document *
                  </Typography>
                  <Button
                    variant="outlined"
                    component="label"
                    startIcon={<UploadFileIcon />}
                    disabled={uploading === 'selfie'}
                    fullWidth
                  >
                    {uploading === 'selfie' ? 'Uploading...' : 'Choose File'}
                    <input
                      type="file"
                      hidden
                      accept="image/*"
                      onChange={(e) => {
                        const file = e.target.files?.[0]
                        if (file) handleFileUpload(file, 'selfie')
                      }}
                    />
                  </Button>
                  {selfieImageUrl && (
                    <Box sx={{ mt: 1 }}>
                      <Typography variant="caption" color="success.main">✓ File uploaded</Typography>
                      <Box component="img" src={selfieImageUrl} sx={{ width: '100%', maxWidth: 200, mt: 1, borderRadius: 1 }} />
                    </Box>
                  )}
                </Box>
              </Grid>

              {/* Proof of Address */}
              <Grid item xs={12}>
                <Box sx={{ border: '1px dashed #ccc', borderRadius: 1, p: 2 }}>
                  <Typography variant="body2" fontWeight="bold" gutterBottom>
                    Proof of Address *
                  </Typography>
                  <Button
                    variant="outlined"
                    component="label"
                    startIcon={<UploadFileIcon />}
                    disabled={uploading === 'proofOfAddress'}
                    fullWidth
                  >
                    {uploading === 'proofOfAddress' ? 'Uploading...' : 'Choose File'}
                    <input
                      type="file"
                      hidden
                      accept="image/*"
                      onChange={(e) => {
                        const file = e.target.files?.[0]
                        if (file) handleFileUpload(file, 'proofOfAddress')
                      }}
                    />
                  </Button>
                  {proofOfAddressUrl && (
                    <Box sx={{ mt: 1 }}>
                      <Typography variant="caption" color="success.main">✓ File uploaded</Typography>
                      <Box component="img" src={proofOfAddressUrl} sx={{ width: '100%', maxWidth: 200, mt: 1, borderRadius: 1 }} />
                    </Box>
                  )}
                </Box>
              </Grid>

              {/* Additional Notes */}
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Additional Notes (Optional)"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  multiline
                  rows={3}
                />
              </Grid>
            </Grid>
          )}

          <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
            <Button
              disabled={activeStep === 0}
              onClick={handleBack}
            >
              Back
            </Button>
            <Box>
              {activeStep === steps.length - 1 ? (
                <Button
                  variant="contained"
                  onClick={handleSubmit}
                  disabled={loading || !documentFrontImageUrl || !selfieImageUrl || !proofOfAddressUrl}
                  startIcon={loading ? <CircularProgress size={20} /> : null}
                >
                  {loading ? 'Submitting...' : 'Submit KYC'}
                </Button>
              ) : (
                <Button
                  variant="contained"
                  onClick={handleNext}
                >
                  Next
                </Button>
              )}
            </Box>
          </Box>
        </Paper>
      </Container>
    </>
  )
}

export default KYC
