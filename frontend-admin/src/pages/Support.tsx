import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, IconButton, Tabs, Tab, Dialog,
  DialogTitle, DialogContent, DialogActions, Button, TextField
} from '@mui/material'
import {
  Visibility as ViewIcon,
  Reply as ReplyIcon,
  CheckCircle as ResolveIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'

interface Ticket {
  id: number
  userId: number
  userEmail: string
  subject: string
  category: string
  priority: string
  status: string
  createdAt: string
  updatedAt: string
}

const Support = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const [tickets, setTickets] = useState<Ticket[]>([])
  const [tabValue, setTabValue] = useState(0)
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null)
  const [detailsOpen, setDetailsOpen] = useState(false)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canViewSupport) {
      navigate('/dashboard')
    }
  }, [isAuthenticated, permissions, navigate])

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return 'error'
      case 'IN_PROGRESS': return 'warning'
      case 'RESOLVED': return 'success'
      case 'CLOSED': return 'default'
      default: return 'default'
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH': return 'error'
      case 'MEDIUM': return 'warning'
      case 'LOW': return 'info'
      default: return 'default'
    }
  }

  const filteredTickets = tickets.filter(ticket => {
    if (tabValue === 0) return ticket.status === 'OPEN'
    if (tabValue === 1) return ticket.status === 'IN_PROGRESS'
    if (tabValue === 2) return ticket.status === 'RESOLVED'
    return true
  })

  const handleViewTicket = (ticket: Ticket) => {
    setSelectedTicket(ticket)
    setDetailsOpen(true)
  }

  return (
    <AdminLayout title="Customer Support">
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>Support Tickets</Typography>
        <Typography color="text.secondary">
          Manage customer support requests and inquiries
        </Typography>
      </Box>

      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)}>
          <Tab label="Open" />
          <Tab label="In Progress" />
          <Tab label="Resolved" />
          <Tab label="All" />
        </Tabs>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Ticket ID</TableCell>
              <TableCell>User Email</TableCell>
              <TableCell>Subject</TableCell>
              <TableCell>Category</TableCell>
              <TableCell>Priority</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Created</TableCell>
              <TableCell>Updated</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredTickets.length === 0 ? (
              <TableRow>
                <TableCell colSpan={9} align="center">
                  <Typography color="text.secondary" sx={{ py: 4 }}>
                    No support tickets found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              filteredTickets.map((ticket) => (
                <TableRow key={ticket.id} hover>
                  <TableCell>#{ticket.id}</TableCell>
                  <TableCell>{ticket.userEmail}</TableCell>
                  <TableCell>{ticket.subject}</TableCell>
                  <TableCell>{ticket.category}</TableCell>
                  <TableCell>
                    <Chip
                      label={ticket.priority}
                      color={getPriorityColor(ticket.priority) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={ticket.status}
                      color={getStatusColor(ticket.status) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{new Date(ticket.createdAt).toLocaleString()}</TableCell>
                  <TableCell>{new Date(ticket.updatedAt).toLocaleString()}</TableCell>
                  <TableCell align="center">
                    <IconButton size="small" onClick={() => handleViewTicket(ticket)}>
                      <ViewIcon />
                    </IconButton>
                    {permissions.canManageSupport && (
                      <>
                        <IconButton size="small" color="primary">
                          <ReplyIcon />
                        </IconButton>
                        {ticket.status !== 'RESOLVED' && (
                          <IconButton size="small" color="success">
                            <ResolveIcon />
                          </IconButton>
                        )}
                      </>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Ticket Details Dialog */}
      <Dialog open={detailsOpen} onClose={() => setDetailsOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Ticket Details #{selectedTicket?.id}</DialogTitle>
        <DialogContent>
          {selectedTicket && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="body2" color="text.secondary">User Email</Typography>
              <Typography gutterBottom>{selectedTicket.userEmail}</Typography>

              <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>Subject</Typography>
              <Typography gutterBottom>{selectedTicket.subject}</Typography>

              <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>Message</Typography>
              <Typography paragraph>
                Ticket message content will be displayed here...
              </Typography>

              {permissions.canManageSupport && (
                <TextField
                  fullWidth
                  multiline
                  rows={4}
                  label="Reply"
                  placeholder="Type your response..."
                  sx={{ mt: 2 }}
                />
              )}
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsOpen(false)}>Close</Button>
          {permissions.canManageSupport && (
            <Button variant="contained">Send Reply</Button>
          )}
        </DialogActions>
      </Dialog>
    </AdminLayout>
  )
}

export default Support
