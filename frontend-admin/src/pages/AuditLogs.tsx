import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, TextField, InputAdornment, Grid,
  FormControl, InputLabel, Select, MenuItem, Chip
} from '@mui/material'
import { Search as SearchIcon } from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'

interface AuditLog {
  id: number
  adminId: number
  adminUsername: string
  action: string
  entityType: string
  entityId: number
  oldValue?: string
  newValue?: string
  ipAddress: string
  timestamp: string
}

const AuditLogs = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const [logs, setLogs] = useState<AuditLog[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [filterAction, setFilterAction] = useState('ALL')
  const [filterEntity, setFilterEntity] = useState('ALL')

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canViewAuditLogs) {
      navigate('/dashboard')
    }
  }, [isAuthenticated, permissions, navigate])

  const getActionColor = (action: string) => {
    switch (action) {
      case 'CREATE': return 'success'
      case 'UPDATE': return 'info'
      case 'DELETE': return 'error'
      case 'LOGIN': return 'default'
      case 'APPROVE': return 'success'
      case 'REJECT': return 'error'
      default: return 'default'
    }
  }

  return (
    <AdminLayout title="Audit Logs">
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>System Audit Logs</Typography>
        <Typography color="text.secondary">
          Track all administrative actions and system changes
        </Typography>
      </Box>

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            placeholder="Search by admin username, entity, or action..."
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
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <FormControl fullWidth>
            <InputLabel>Filter by Action</InputLabel>
            <Select
              value={filterAction}
              label="Filter by Action"
              onChange={(e) => setFilterAction(e.target.value)}
            >
              <MenuItem value="ALL">All Actions</MenuItem>
              <MenuItem value="CREATE">Create</MenuItem>
              <MenuItem value="UPDATE">Update</MenuItem>
              <MenuItem value="DELETE">Delete</MenuItem>
              <MenuItem value="LOGIN">Login</MenuItem>
              <MenuItem value="APPROVE">Approve</MenuItem>
              <MenuItem value="REJECT">Reject</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <FormControl fullWidth>
            <InputLabel>Filter by Entity</InputLabel>
            <Select
              value={filterEntity}
              label="Filter by Entity"
              onChange={(e) => setFilterEntity(e.target.value)}
            >
              <MenuItem value="ALL">All Entities</MenuItem>
              <MenuItem value="USER">User</MenuItem>
              <MenuItem value="GAME">Game</MenuItem>
              <MenuItem value="TRANSACTION">Transaction</MenuItem>
              <MenuItem value="BONUS">Bonus</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
            </Select>
          </FormControl>
        </Grid>
      </Grid>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Timestamp</TableCell>
              <TableCell>Admin</TableCell>
              <TableCell>Action</TableCell>
              <TableCell>Entity Type</TableCell>
              <TableCell>Entity ID</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>IP Address</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {logs.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} align="center">
                  <Typography color="text.secondary" sx={{ py: 4 }}>
                    No audit logs found
                  </Typography>
                </TableCell>
              </TableRow>
            ) : (
              logs.map((log) => (
                <TableRow key={log.id} hover>
                  <TableCell>{log.id}</TableCell>
                  <TableCell>{new Date(log.timestamp).toLocaleString()}</TableCell>
                  <TableCell>{log.adminUsername}</TableCell>
                  <TableCell>
                    <Chip
                      label={log.action}
                      color={getActionColor(log.action) as any}
                      size="small"
                    />
                  </TableCell>
                  <TableCell>{log.entityType}</TableCell>
                  <TableCell>{log.entityId}</TableCell>
                  <TableCell>
                    {log.oldValue && log.newValue
                      ? `Changed from "${log.oldValue}" to "${log.newValue}"`
                      : `${log.action} ${log.entityType}`}
                  </TableCell>
                  <TableCell>{log.ipAddress}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </AdminLayout>
  )
}

export default AuditLogs
