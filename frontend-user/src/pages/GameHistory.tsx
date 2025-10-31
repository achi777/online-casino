import { useState, useEffect } from 'react'
import { Container, Box, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Chip, CircularProgress } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import Navbar from '../components/Navbar'

interface GameSession {
  id: number
  sessionToken: string
  gameName: string
  gameCode: string
  totalBet: number
  totalWin: number
  totalRounds: number
  status: string
  startedAt: string
  endedAt: string
}

const GameHistory = () => {
  const navigate = useNavigate()
  const [sessions, setSessions] = useState<GameSession[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadGameHistory()
  }, [])

  const loadGameHistory = async () => {
    try {
      const response = await axios.get('/api/user/game-history')
      setSessions(response.data.content || [])
      setLoading(false)
    } catch (err) {
      console.error('Failed to load game history', err)
      setLoading(false)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'success'
      case 'COMPLETED':
        return 'default'
      case 'FAILED':
        return 'error'
      default:
        return 'default'
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString()
  }

  if (loading) {
    return (
      <>
        <Navbar />
        <Container maxWidth="lg">
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
            <CircularProgress />
          </Box>
        </Container>
      </>
    )
  }

  return (
    <>
      <Navbar />
      <Container maxWidth="lg">
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" gutterBottom>Game History</Typography>
          <Typography color="text.secondary">View all your past game sessions</Typography>
        </Box>

        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Game</TableCell>
                <TableCell>Started</TableCell>
                <TableCell>Ended</TableCell>
                <TableCell align="right">Rounds</TableCell>
                <TableCell align="right">Total Bet</TableCell>
                <TableCell align="right">Total Win</TableCell>
                <TableCell align="right">Profit/Loss</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {sessions.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} align="center">
                    No game history found
                  </TableCell>
                </TableRow>
              ) : (
                sessions.map((session) => {
                  const profitLoss = session.totalWin - session.totalBet
                  return (
                    <TableRow key={session.id}>
                      <TableCell>
                        <Typography variant="body2" fontWeight="bold">
                          {session.gameName}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {session.gameCode}
                        </Typography>
                      </TableCell>
                      <TableCell>{formatDate(session.startedAt)}</TableCell>
                      <TableCell>{session.endedAt ? formatDate(session.endedAt) : '-'}</TableCell>
                      <TableCell align="right">{session.totalRounds}</TableCell>
                      <TableCell align="right">₾{session.totalBet.toFixed(2)}</TableCell>
                      <TableCell align="right">₾{session.totalWin.toFixed(2)}</TableCell>
                      <TableCell
                        align="right"
                        sx={{
                          color: profitLoss >= 0 ? 'success.main' : 'error.main',
                          fontWeight: 'bold'
                        }}
                      >
                        {profitLoss >= 0 ? '+' : ''}₾{profitLoss.toFixed(2)}
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={session.status}
                          color={getStatusColor(session.status) as any}
                          size="small"
                        />
                      </TableCell>
                    </TableRow>
                  )
                })
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Container>
    </>
  )
}

export default GameHistory
