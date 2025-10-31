import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Button, Grid, Card, CardContent, CardMedia,
  CardActions, IconButton, Dialog, DialogTitle, DialogContent,
  DialogActions, TextField, FormControl, InputLabel, Select,
  MenuItem, Switch, FormControlLabel, Tabs, Tab
} from '@mui/material'
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'

interface Banner {
  id: number
  title: string
  imageUrl: string
  link?: string
  position: string
  status: string
  displayOrder: number
}

interface Notification {
  id: number
  title: string
  message: string
  type: string
  status: string
  createdAt: string
}

const CMS = () => {
  const { isAuthenticated } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const [tabValue, setTabValue] = useState(0)
  const [banners, setBanners] = useState<Banner[]>([])
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [openDialog, setOpenDialog] = useState(false)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else if (!permissions.canViewCMS) {
      navigate('/dashboard')
    }
  }, [isAuthenticated, permissions, navigate])

  return (
    <AdminLayout title="CMS & Banners">
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>Content Management</Typography>
        <Typography color="text.secondary">
          Manage banners, notifications, and promotional content
        </Typography>
      </Box>

      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)}>
          <Tab label="Banners" />
          <Tab label="Notifications" />
          <Tab label="Promotions" />
        </Tabs>
      </Box>

      {/* Banners Tab */}
      {tabValue === 0 && (
        <Box>
          <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between' }}>
            <Typography variant="h6">Banners</Typography>
            {permissions.canManageCMS && (
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => setOpenDialog(true)}
              >
                Add Banner
              </Button>
            )}
          </Box>

          <Grid container spacing={3}>
            {banners.length === 0 ? (
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography align="center" color="text.secondary" sx={{ py: 4 }}>
                      No banners found. Create your first banner to get started.
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ) : (
              banners.map((banner) => (
                <Grid item xs={12} sm={6} md={4} key={banner.id}>
                  <Card>
                    <CardMedia
                      component="img"
                      height="200"
                      image={banner.imageUrl}
                      alt={banner.title}
                    />
                    <CardContent>
                      <Typography variant="h6" gutterBottom>{banner.title}</Typography>
                      <Typography variant="body2" color="text.secondary">
                        Position: {banner.position}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Order: {banner.displayOrder}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Status: {banner.status}
                      </Typography>
                    </CardContent>
                    {permissions.canManageCMS && (
                      <CardActions>
                        <IconButton size="small"><EditIcon /></IconButton>
                        <IconButton size="small" color="error"><DeleteIcon /></IconButton>
                      </CardActions>
                    )}
                  </Card>
                </Grid>
              ))
            )}
          </Grid>
        </Box>
      )}

      {/* Notifications Tab */}
      {tabValue === 1 && (
        <Box>
          <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between' }}>
            <Typography variant="h6">System Notifications</Typography>
            {permissions.canManageCMS && (
              <Button variant="contained" startIcon={<AddIcon />}>
                Add Notification
              </Button>
            )}
          </Box>

          <Grid container spacing={2}>
            {notifications.length === 0 ? (
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography align="center" color="text.secondary" sx={{ py: 4 }}>
                      No notifications found
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ) : (
              notifications.map((notification) => (
                <Grid item xs={12} key={notification.id}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Box>
                          <Typography variant="h6">{notification.title}</Typography>
                          <Typography color="text.secondary">{notification.message}</Typography>
                          <Typography variant="caption" color="text.secondary">
                            {new Date(notification.createdAt).toLocaleString()}
                          </Typography>
                        </Box>
                        {permissions.canManageCMS && (
                          <Box>
                            <IconButton><EditIcon /></IconButton>
                            <IconButton color="error"><DeleteIcon /></IconButton>
                          </Box>
                        )}
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              ))
            )}
          </Grid>
        </Box>
      )}

      {/* Promotions Tab */}
      {tabValue === 2 && (
        <Box>
          <Typography variant="h6" gutterBottom>Promotional Content</Typography>
          <Card>
            <CardContent>
              <Typography align="center" color="text.secondary" sx={{ py: 4 }}>
                Promotional content management will be implemented here
              </Typography>
            </CardContent>
          </Card>
        </Box>
      )}

      {/* Add Banner Dialog */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Banner</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField fullWidth label="Banner Title" />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="Image URL" />
            </Grid>
            <Grid item xs={12}>
              <TextField fullWidth label="Link URL (optional)" />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Position</InputLabel>
                <Select label="Position" defaultValue="">
                  <MenuItem value="HOME_TOP">Home - Top</MenuItem>
                  <MenuItem value="HOME_MIDDLE">Home - Middle</MenuItem>
                  <MenuItem value="GAMES_TOP">Games - Top</MenuItem>
                  <MenuItem value="SIDEBAR">Sidebar</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField fullWidth label="Display Order" type="number" defaultValue="0" />
            </Grid>
            <Grid item xs={12}>
              <FormControlLabel
                control={<Switch defaultChecked />}
                label="Active"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button variant="contained">Create</Button>
        </DialogActions>
      </Dialog>
    </AdminLayout>
  )
}

export default CMS
