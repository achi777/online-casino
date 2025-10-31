import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import {
  Box, Typography, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Chip, TextField, InputAdornment,
  Dialog, DialogTitle, DialogContent, DialogActions, Button, Paper,
  Tabs, Tab, TablePagination, Grid, IconButton
} from '@mui/material'
import {
  Search as SearchIcon,
  Check as CheckIcon,
  Close as CloseIcon,
  Visibility as VisibilityIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import axios from 'axios'

interface Transaction {
  id: number
  userId: number
  userEmail: string
  type: string
  amount: number
  status: string
  paymentMethod?: string
  createdAt: string
  processedAt?: string
  notes?: string
}

const Transactions = () => {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedTransaction, setSelectedTransaction] = useState<Transaction | null>(null)
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false)
  const [actionDialogOpen, setActionDialogOpen] = useState(false)
  const [actionType, setActionType] = useState<'approve' | 'reject' | null>(null)
  const [actionNotes, setActionNotes] = useState('')
  const [tabValue, setTabValue] = useState(searchParams.get('filter') === 'withdrawals' ? 1 : 0)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(25)
  const [totalTransactions, setTotalTransactions] = useState(0)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else {
      fetchTransactions()
    }
  }, [isAuthenticated, navigate, tabValue, page, rowsPerPage, searchTerm])

  const fetchTransactions = async () => {
    try {
      const type = tabValue === 0 ? 'DEPOSIT' : 'WITHDRAWAL'
      const params = {
        type,
        page,
        size: rowsPerPage,
        search: searchTerm || undefined
      }
      const response = await axios.get('/api/admin/transactions', { params })
      setTransactions(response.data.content || response.data)
      setTotalTransactions(response.data.totalElements || response.data.length)
    } catch (err) {
      console.error('Failed to fetch transactions:', err)
    }
  }

  const handleViewDetails = (transaction: Transaction) => {
    setSelectedTransaction(transaction)
    setDetailsDialogOpen(true)
  }

  const handleOpenActionDialog = (transaction: Transaction, action: 'approve' | 'reject') => {
    setSelectedTransaction(transaction)
    setActionType(action)
    setActionNotes('')
    setActionDialogOpen(true)
  }

  const handleProcessTransaction = async () => {
    if (!selectedTransaction || !actionType) return

    try {
      const status = actionType === 'approve' ? 'COMPLETED' : 'REJECTED'
      await axios.put(`/api/admin/transactions/${selectedTransaction.id}/status`, {
        status,
        notes: actionNotes
      })
      setActionDialogOpen(false)
      setActionNotes('')
      fetchTransactions()
    } catch (err) {
      console.error('Failed to process transaction:', err)
    }
  }

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue)
    setPage(0)
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
      case 'COMPLETED': return 'success'
      case 'PENDING': return 'warning'
      case 'REJECTED': return 'error'
      case 'PROCESSING': return 'info'
      default: return 'default'
    }
  }

  const formatCurrency = (amount: number) => {
    return `₾${amount.toFixed(2)}`
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-GB')
  }

  return (
    <AdminLayout title="Transactions">

        <Tabs value={tabValue} onChange={handleTabChange} sx={{ mb: 2 }}>
          <Tab label="Deposits" />
          <Tab label="Withdrawals" />
        </Tabs>

        <Box sx={{ mb: 3 }}>
          <TextField
            fullWidth
            placeholder="Search by transaction ID, user email, or amount..."
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
                <TableCell>User</TableCell>
                <TableCell>Type</TableCell>
                <TableCell align="right">Amount</TableCell>
                <TableCell>Payment Method</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Created</TableCell>
                <TableCell>Processed</TableCell>
                <TableCell align="center">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.map((transaction) => (
                <TableRow key={transaction.id} hover>
                  <TableCell>{transaction.id}</TableCell>
                  <TableCell>{transaction.userEmail}</TableCell>
                  <TableCell>
                    <Chip
                      label={transaction.type}
                      color={transaction.type === 'DEPOSIT' ? 'success' : 'warning'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell align="right">{formatCurrency(transaction.amount)}</TableCell>
                  <TableCell>{transaction.paymentMethod || '-'}</TableCell>
                  <TableCell>
                    <Chip
                      label={transaction.status}
                      color={getStatusColor(transaction.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{formatDate(transaction.createdAt)}</TableCell>
                  <TableCell>{transaction.processedAt ? formatDate(transaction.processedAt) : '-'}</TableCell>
                  <TableCell align="center">
                    <IconButton
                      size="small"
                      onClick={() => handleViewDetails(transaction)}
                      title="View Details"
                    >
                      <VisibilityIcon />
                    </IconButton>
                    {transaction.status === 'PENDING' && transaction.type === 'WITHDRAWAL' && (
                      <>
                        <IconButton
                          size="small"
                          color="success"
                          onClick={() => handleOpenActionDialog(transaction, 'approve')}
                          title="Approve"
                        >
                          <CheckIcon />
                        </IconButton>
                        <IconButton
                          size="small"
                          color="error"
                          onClick={() => handleOpenActionDialog(transaction, 'reject')}
                          title="Reject"
                        >
                          <CloseIcon />
                        </IconButton>
                      </>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <TablePagination
            component="div"
            count={totalTransactions}
            page={page}
            onPageChange={handleChangePage}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={handleChangeRowsPerPage}
            rowsPerPageOptions={[10, 25, 50, 100]}
          />
        </TableContainer>

        {/* Transaction Details Dialog */}
        <Dialog open={detailsDialogOpen} onClose={() => setDetailsDialogOpen(false)} maxWidth="md" fullWidth>
          <DialogTitle>Transaction Details</DialogTitle>
          <DialogContent>
            {selectedTransaction && (
              <Box sx={{ pt: 2 }}>
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Transaction ID</Typography>
                    <Typography variant="body1" gutterBottom>{selectedTransaction.id}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">User ID</Typography>
                    <Typography variant="body1" gutterBottom>{selectedTransaction.userId}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">User Email</Typography>
                    <Typography variant="body1" gutterBottom>{selectedTransaction.userEmail}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Type</Typography>
                    <Box sx={{ mt: 0.5 }}>
                      <Chip
                        label={selectedTransaction.type}
                        color={selectedTransaction.type === 'DEPOSIT' ? 'success' : 'warning'}
                        size="small"
                      />
                    </Box>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Amount</Typography>
                    <Typography variant="h6" color="primary.main" gutterBottom>
                      {formatCurrency(selectedTransaction.amount)}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Status</Typography>
                    <Box sx={{ mt: 0.5 }}>
                      <Chip
                        label={selectedTransaction.status}
                        color={getStatusColor(selectedTransaction.status) as any}
                        size="small"
                      />
                    </Box>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Payment Method</Typography>
                    <Typography variant="body1" gutterBottom>{selectedTransaction.paymentMethod || '-'}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">Created At</Typography>
                    <Typography variant="body1" gutterBottom>{formatDate(selectedTransaction.createdAt)}</Typography>
                  </Grid>
                  {selectedTransaction.processedAt && (
                    <Grid item xs={12} sm={6}>
                      <Typography variant="body2" color="text.secondary">Processed At</Typography>
                      <Typography variant="body1" gutterBottom>{formatDate(selectedTransaction.processedAt)}</Typography>
                    </Grid>
                  )}
                  {selectedTransaction.notes && (
                    <Grid item xs={12}>
                      <Typography variant="body2" color="text.secondary">Notes</Typography>
                      <Typography variant="body1">{selectedTransaction.notes}</Typography>
                    </Grid>
                  )}
                </Grid>
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDetailsDialogOpen(false)}>Close</Button>
          </DialogActions>
        </Dialog>

        {/* Action Dialog (Approve/Reject) */}
        <Dialog open={actionDialogOpen} onClose={() => setActionDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>
            {actionType === 'approve' ? 'Approve Withdrawal' : 'Reject Withdrawal'}
          </DialogTitle>
          <DialogContent>
            {selectedTransaction && (
              <Box sx={{ pt: 2 }}>
                <Typography variant="body1" gutterBottom>
                  Transaction ID: <strong>{selectedTransaction.id}</strong>
                </Typography>
                <Typography variant="body1" gutterBottom>
                  User: <strong>{selectedTransaction.userEmail}</strong>
                </Typography>
                <Typography variant="body1" gutterBottom sx={{ mb: 2 }}>
                  Amount: <strong>{formatCurrency(selectedTransaction.amount)}</strong>
                </Typography>

                <TextField
                  fullWidth
                  label="Notes (optional)"
                  value={actionNotes}
                  onChange={(e) => setActionNotes(e.target.value)}
                  multiline
                  rows={3}
                  placeholder="Add any notes about this transaction..."
                />
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setActionDialogOpen(false)}>Cancel</Button>
            <Button
              onClick={handleProcessTransaction}
              variant="contained"
              color={actionType === 'approve' ? 'success' : 'error'}
            >
              {actionType === 'approve' ? 'Approve' : 'Reject'}
            </Button>
          </DialogActions>
        </Dialog>
    </AdminLayout>
  )
}

export default Transactions
