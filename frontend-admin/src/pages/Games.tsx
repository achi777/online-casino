import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Typography, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Chip, TextField, InputAdornment,
  Dialog, DialogTitle, DialogContent, DialogActions, Select, MenuItem,
  FormControl, InputLabel, Grid, Card, CardMedia, CardContent, CardActions,
  Button, Paper, Switch, FormControlLabel, IconButton
} from '@mui/material'
import {
  Search as SearchIcon,
  Edit as EditIcon,
  Visibility as VisibilityIcon
} from '@mui/icons-material'
import AdminLayout from '../components/AdminLayout'
import { useAuth } from '../context/AuthContext'
import axios from 'axios'

interface Game {
  id: number
  gameCode: string
  name: string
  description: string
  category: string
  provider?: {
    id: number
    name: string
    code: string
  }
  providerName?: string
  iframeUrl?: string
  thumbnailUrl: string
  rtp: number
  featured: boolean
  status: string
  sortOrder?: number
}

const Games = () => {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [games, setGames] = useState<Game[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedGame, setSelectedGame] = useState<Game | null>(null)
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [viewMode, setViewMode] = useState<'grid' | 'table'>('grid')

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else {
      fetchGames()
    }
  }, [isAuthenticated, navigate])

  const fetchGames = async () => {
    try {
      const response = await axios.get('/api/admin/games')
      console.log('API Response:', response.data)

      // Backend returns paginated response: {content: [], totalElements: 0, ...}
      let gamesData = response.data.content || response.data

      // Ensure we have an array
      if (!Array.isArray(gamesData)) {
        console.error('Expected array but got:', gamesData)
        gamesData = []
      }

      console.log('Games data:', gamesData)
      setGames(gamesData)
    } catch (err) {
      console.error('Failed to fetch games:', err)
      setGames([])
    }
  }

  const handleEditGame = (game: Game) => {
    setSelectedGame(game)
    setEditDialogOpen(true)
  }

  const handleSaveGame = async () => {
    if (!selectedGame) return

    try {
      await axios.put(`/api/admin/games/${selectedGame.id}`, selectedGame)
      setEditDialogOpen(false)
      fetchGames()
    } catch (err) {
      console.error('Failed to update game:', err)
    }
  }

  const handleToggleStatus = async (game: Game) => {
    try {
      const newStatus = game.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
      await axios.put(`/api/admin/games/${game.id}`, {
        ...game,
        status: newStatus
      })
      fetchGames()
    } catch (err) {
      console.error('Failed to toggle game status:', err)
    }
  }

  const handleToggleFeatured = async (game: Game) => {
    try {
      await axios.put(`/api/admin/games/${game.id}`, {
        ...game,
        featured: !game.featured
      })
      fetchGames()
    } catch (err) {
      console.error('Failed to toggle featured status:', err)
    }
  }

  const getStatusColor = (status: string) => {
    return status === 'ACTIVE' ? 'success' : 'default'
  }

  const getCategoryColor = (category: string) => {
    const colors: { [key: string]: any } = {
      SLOTS: 'primary',
      TABLE: 'secondary',
      LIVE: 'error',
      JACKPOT: 'warning',
      OTHER: 'default'
    }
    return colors[category] || 'default'
  }

  const getProviderName = (game: Game) => {
    return game.provider?.name || game.providerName || 'Unknown'
  }

  const filteredGames = games.filter(game => {
    if (!game || !game.name || !game.gameCode) return false
    const providerName = getProviderName(game)
    return (
      game.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      game.gameCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
      providerName.toLowerCase().includes(searchTerm.toLowerCase())
    )
  })

  return (
    <AdminLayout title="Games Management">

        <Box sx={{ mb: 3, display: 'flex', gap: 2 }}>
          <TextField
            fullWidth
            placeholder="Search games by name, code, or provider..."
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
          <Button
            variant="outlined"
            onClick={() => setViewMode(viewMode === 'grid' ? 'table' : 'grid')}
          >
            {viewMode === 'grid' ? 'Table View' : 'Grid View'}
          </Button>
        </Box>

        {viewMode === 'grid' ? (
          <Grid container spacing={3}>
            {filteredGames.map((game) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={game.id}>
                <Card>
                  <CardMedia
                    component="img"
                    height="200"
                    image={game.thumbnailUrl}
                    alt={game.name}
                  />
                  <CardContent>
                    <Typography gutterBottom variant="h6" component="div" noWrap>
                      {game.name}
                    </Typography>
                    <Typography variant="body2" color="text.secondary" noWrap>
                      {getProviderName(game)}
                    </Typography>
                    <Box sx={{ mt: 1, display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                      <Chip
                        label={game.category}
                        color={getCategoryColor(game.category)}
                        size="small"
                      />
                      <Chip
                        label={game.status}
                        color={getStatusColor(game.status) as any}
                        size="small"
                      />
                      {game.featured && (
                        <Chip label="Featured" color="warning" size="small" />
                      )}
                    </Box>
                    <Typography variant="body2" sx={{ mt: 1 }}>
                      RTP: {game.rtp}%
                    </Typography>
                  </CardContent>
                  <CardActions>
                    <Button
                      size="small"
                      onClick={() => window.open(game.iframeUrl + '?demo=true', '_blank')}
                      variant="outlined"
                    >
                      Preview
                    </Button>
                    <Button size="small" onClick={() => handleEditGame(game)}>
                      Edit
                    </Button>
                    <Button
                      size="small"
                      color={game.status === 'ACTIVE' ? 'error' : 'success'}
                      onClick={() => handleToggleStatus(game)}
                    >
                      {game.status === 'ACTIVE' ? 'Disable' : 'Enable'}
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
            ))}
          </Grid>
        ) : (
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Code</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Provider</TableCell>
                  <TableCell>Category</TableCell>
                  <TableCell align="center">RTP</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="center">Featured</TableCell>
                  <TableCell align="center">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredGames.map((game) => (
                  <TableRow key={game.id} hover>
                    <TableCell>{game.id}</TableCell>
                    <TableCell>{game.gameCode}</TableCell>
                    <TableCell>{game.name}</TableCell>
                    <TableCell>{getProviderName(game)}</TableCell>
                    <TableCell>
                      <Chip
                        label={game.category}
                        color={getCategoryColor(game.category)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">{game.rtp}%</TableCell>
                    <TableCell>
                      <Chip
                        label={game.status}
                        color={getStatusColor(game.status) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      <Switch
                        checked={game.featured}
                        onChange={() => handleToggleFeatured(game)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      <IconButton
                        size="small"
                        onClick={() => window.open(game.iframeUrl + '?demo=true', '_blank')}
                        title="Preview Game"
                        color="primary"
                      >
                        <VisibilityIcon />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => handleEditGame(game)}
                        title="Edit Game"
                      >
                        <EditIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        {/* Edit Game Dialog */}
        <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Edit Game</DialogTitle>
          <DialogContent>
            {selectedGame && (
              <Box sx={{ pt: 2 }}>
                <TextField
                  fullWidth
                  label="Game Name"
                  value={selectedGame.name}
                  onChange={(e) => setSelectedGame({ ...selectedGame, name: e.target.value })}
                  sx={{ mb: 2 }}
                />

                <TextField
                  fullWidth
                  label="Description"
                  value={selectedGame.description}
                  onChange={(e) => setSelectedGame({ ...selectedGame, description: e.target.value })}
                  multiline
                  rows={3}
                  sx={{ mb: 2 }}
                />

                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel>Category</InputLabel>
                  <Select
                    value={selectedGame.category}
                    label="Category"
                    onChange={(e) => setSelectedGame({ ...selectedGame, category: e.target.value })}
                  >
                    <MenuItem value="SLOTS">SLOTS</MenuItem>
                    <MenuItem value="TABLE">TABLE</MenuItem>
                    <MenuItem value="LIVE">LIVE</MenuItem>
                    <MenuItem value="JACKPOT">JACKPOT</MenuItem>
                    <MenuItem value="OTHER">OTHER</MenuItem>
                  </Select>
                </FormControl>

                <TextField
                  fullWidth
                  label="RTP (%)"
                  type="number"
                  value={selectedGame.rtp}
                  onChange={(e) => setSelectedGame({ ...selectedGame, rtp: parseFloat(e.target.value) })}
                  sx={{ mb: 2 }}
                  inputProps={{ min: 0, max: 100, step: 0.01 }}
                />

                <TextField
                  fullWidth
                  label="Sort Order"
                  type="number"
                  value={selectedGame.sortOrder}
                  onChange={(e) => setSelectedGame({ ...selectedGame, sortOrder: parseInt(e.target.value) })}
                  sx={{ mb: 2 }}
                />

                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel>Status</InputLabel>
                  <Select
                    value={selectedGame.status}
                    label="Status"
                    onChange={(e) => setSelectedGame({ ...selectedGame, status: e.target.value })}
                  >
                    <MenuItem value="ACTIVE">ACTIVE</MenuItem>
                    <MenuItem value="INACTIVE">INACTIVE</MenuItem>
                    <MenuItem value="MAINTENANCE">MAINTENANCE</MenuItem>
                  </Select>
                </FormControl>

                <FormControlLabel
                  control={
                    <Switch
                      checked={selectedGame.featured}
                      onChange={(e) => setSelectedGame({ ...selectedGame, featured: e.target.checked })}
                    />
                  }
                  label="Featured Game"
                />
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
            <Button onClick={handleSaveGame} variant="contained">Save</Button>
          </DialogActions>
        </Dialog>
    </AdminLayout>
  )
}

export default Games
