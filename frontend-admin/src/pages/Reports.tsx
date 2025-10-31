import { useState } from 'react'
import {
  Box, Typography, Paper, Grid, Card, CardContent, TextField, Button,
  FormControl, InputLabel, Select, MenuItem
} from '@mui/material'
import {
  Assessment as ReportsIcon,
  Download as DownloadIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  AttachMoney as MoneyIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import axios from 'axios'

interface ReportData {
  totalDeposits: number
  totalWithdrawals: number
  totalBets: number
  totalWins: number
  ggr: number
  ngr: number
  activeUsers: number
  newUsers: number
  totalGames: number
  topGames: Array<{
    gameId: number
    gameName: string
    totalBets: number
    totalWins: number
    ggr: number
  }>
  topUsers: Array<{
    userId: number
    userEmail: string
    totalBets: number
    totalWins: number
    ggr: number
  }>
}

const Reports = () => {
  const [reportType, setReportType] = useState('financial')
  const [dateFrom, setDateFrom] = useState(new Date().toISOString().split('T')[0])
  const [dateTo, setDateTo] = useState(new Date().toISOString().split('T')[0])
  const [reportData, setReportData] = useState<ReportData | null>(null)
  const [loading, setLoading] = useState(false)

  const generateReport = async () => {
    setLoading(true)
    try {
      const response = await axios.get('/api/admin/reports', {
        params: {
          type: reportType,
          dateFrom,
          dateTo
        }
      })
      setReportData(response.data)
    } catch (err) {
      console.error('Failed to generate report:', err)
    } finally {
      setLoading(false)
    }
  }

  const exportReport = async (format: 'csv' | 'pdf') => {
    try {
      const response = await axios.get(`/api/admin/reports/export`, {
        params: {
          type: reportType,
          dateFrom,
          dateTo,
          format
        },
        responseType: 'blob'
      })

      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `report_${dateFrom}_${dateTo}.${format}`)
      document.body.appendChild(link)
      link.click()
      link.remove()
    } catch (err) {
      console.error('Failed to export report:', err)
    }
  }

  const formatCurrency = (amount: number) => {
    return `₾${amount.toFixed(2)}`
  }

  return (
    <AdminLayout title="Reports & Analytics">

        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" gutterBottom>
            Generate Report
          </Typography>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={3}>
              <FormControl fullWidth>
                <InputLabel>Report Type</InputLabel>
                <Select
                  value={reportType}
                  label="Report Type"
                  onChange={(e) => setReportType(e.target.value)}
                >
                  <MenuItem value="financial">Financial Report</MenuItem>
                  <MenuItem value="users">User Activity Report</MenuItem>
                  <MenuItem value="games">Games Performance Report</MenuItem>
                  <MenuItem value="summary">Summary Report</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField
                fullWidth
                type="date"
                label="From Date"
                value={dateFrom}
                onChange={(e) => setDateFrom(e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={3}>
              <TextField
                fullWidth
                type="date"
                label="To Date"
                value={dateTo}
                onChange={(e) => setDateTo(e.target.value)}
                InputLabelProps={{ shrink: true }}
              />
            </Grid>
            <Grid item xs={12} sm={3}>
              <Button
                fullWidth
                variant="contained"
                onClick={generateReport}
                disabled={loading}
                sx={{ height: 56 }}
              >
                {loading ? 'Generating...' : 'Generate Report'}
              </Button>
            </Grid>
          </Grid>
        </Paper>

        {reportData && (
          <>
            <Box sx={{ mb: 2, display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
              <Button
                variant="outlined"
                startIcon={<DownloadIcon />}
                onClick={() => exportReport('csv')}
              >
                Export CSV
              </Button>
              <Button
                variant="outlined"
                startIcon={<DownloadIcon />}
                onClick={() => exportReport('pdf')}
              >
                Export PDF
              </Button>
            </Box>

            <Grid container spacing={3}>
              {/* Financial Overview */}
              <Grid item xs={12}>
                <Typography variant="h5" gutterBottom>
                  Financial Overview
                </Typography>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <TrendingUpIcon color="success" />
                      <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                        Total Deposits
                      </Typography>
                    </Box>
                    <Typography variant="h4">
                      {formatCurrency(reportData.totalDeposits)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <TrendingDownIcon color="error" />
                      <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                        Total Withdrawals
                      </Typography>
                    </Box>
                    <Typography variant="h4">
                      {formatCurrency(reportData.totalWithdrawals)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <MoneyIcon color="primary" />
                      <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                        Total Bets
                      </Typography>
                    </Box>
                    <Typography variant="h4">
                      {formatCurrency(reportData.totalBets)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={3}>
                <Card>
                  <CardContent>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <TrendingUpIcon color="success" />
                      <Typography variant="body2" color="text.secondary" sx={{ ml: 1 }}>
                        GGR
                      </Typography>
                    </Box>
                    <Typography variant="h4" color="success.main">
                      {formatCurrency(reportData.ggr)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              {/* User Statistics */}
              <Grid item xs={12} sx={{ mt: 3 }}>
                <Typography variant="h5" gutterBottom>
                  User Statistics
                </Typography>
              </Grid>

              <Grid item xs={12} sm={6} md={4}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      Active Users
                    </Typography>
                    <Typography variant="h4">{reportData.activeUsers}</Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={4}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      New Users
                    </Typography>
                    <Typography variant="h4" color="success.main">
                      {reportData.newUsers}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              <Grid item xs={12} sm={6} md={4}>
                <Card>
                  <CardContent>
                    <Typography variant="body2" color="text.secondary">
                      NGR (Net Gaming Revenue)
                    </Typography>
                    <Typography variant="h4" color="primary.main">
                      {formatCurrency(reportData.ngr)}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>

              {/* Top Games */}
              {reportData.topGames && reportData.topGames.length > 0 && (
                <>
                  <Grid item xs={12} sx={{ mt: 3 }}>
                    <Typography variant="h5" gutterBottom>
                      Top Performing Games
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <Paper sx={{ p: 2 }}>
                      {reportData.topGames.map((game, index) => (
                        <Box key={game.gameId} sx={{ mb: 2, pb: 2, borderBottom: index < reportData.topGames.length - 1 ? 1 : 0, borderColor: 'divider' }}>
                          <Typography variant="h6">{game.gameName}</Typography>
                          <Grid container spacing={2} sx={{ mt: 1 }}>
                            <Grid item xs={4}>
                              <Typography variant="body2" color="text.secondary">Total Bets</Typography>
                              <Typography variant="body1">{formatCurrency(game.totalBets)}</Typography>
                            </Grid>
                            <Grid item xs={4}>
                              <Typography variant="body2" color="text.secondary">Total Wins</Typography>
                              <Typography variant="body1">{formatCurrency(game.totalWins)}</Typography>
                            </Grid>
                            <Grid item xs={4}>
                              <Typography variant="body2" color="text.secondary">GGR</Typography>
                              <Typography variant="body1" color="success.main">{formatCurrency(game.ggr)}</Typography>
                            </Grid>
                          </Grid>
                        </Box>
                      ))}
                    </Paper>
                  </Grid>
                </>
              )}

              {/* Top Users */}
              {reportData.topUsers && reportData.topUsers.length > 0 && (
                <>
                  <Grid item xs={12} sx={{ mt: 3 }}>
                    <Typography variant="h5" gutterBottom>
                      Top Users by Activity
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <Paper sx={{ p: 2 }}>
                      {reportData.topUsers.map((user, index) => (
                        <Box key={user.userId} sx={{ mb: 2, pb: 2, borderBottom: index < reportData.topUsers.length - 1 ? 1 : 0, borderColor: 'divider' }}>
                          <Typography variant="h6">{user.userEmail}</Typography>
                          <Grid container spacing={2} sx={{ mt: 1 }}>
                            <Grid item xs={4}>
                              <Typography variant="body2" color="text.secondary">Total Bets</Typography>
                              <Typography variant="body1">{formatCurrency(user.totalBets)}</Typography>
                            </Grid>
                            <Grid item xs={4}>
                              <Typography variant="body2" color="text.secondary">Total Wins</Typography>
                              <Typography variant="body1">{formatCurrency(user.totalWins)}</Typography>
                            </Grid>
                            <Grid item xs={4}>
                              <Typography variant="body2" color="text.secondary">Net Gaming Revenue</Typography>
                              <Typography variant="body1" color="success.main">{formatCurrency(user.ggr)}</Typography>
                            </Grid>
                          </Grid>
                        </Box>
                      ))}
                    </Paper>
                  </Grid>
                </>
              )}
            </Grid>
          </>
        )}

        {!reportData && !loading && (
          <Paper sx={{ p: 4, textAlign: 'center' }}>
            <ReportsIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
            <Typography variant="h6" color="text.secondary">
              Select report parameters and click "Generate Report" to view analytics
            </Typography>
          </Paper>
        )}
    </AdminLayout>
  )
}

export default Reports
