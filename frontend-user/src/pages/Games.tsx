import { useState } from 'react'
import {
  Container,
  Typography,
  Box,
  Grid,
  Card,
  CardMedia,
  CardContent,
  Button,
  TextField,
  InputAdornment,
  Chip,
  CircularProgress,
  Alert,
  ButtonGroup,
  Skeleton,
  Dialog,
  DialogContent,
  IconButton
} from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import VisibilityIcon from '@mui/icons-material/Visibility'
import CloseIcon from '@mui/icons-material/Close'
import axios from 'axios'
import { useQuery } from 'react-query'
import Navbar from '../components/Navbar'

const Games = () => {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('ALL')
  const [error, setError] = useState('')
  const [gameUrl, setGameUrl] = useState<string | null>(null)

  const { data: games, isLoading } = useQuery('games', async () => {
    const response = await axios.get('/api/user/games')
    return response.data.content
  })

  const handlePlayGame = async (gameId: number, demoMode: boolean = false) => {
    setError('')
    try {
      const response = await axios.post('/api/user/games/launch', { gameId, demoMode })
      // Append JWT token to game URL so iframe can use it
      const token = localStorage.getItem('accessToken')
      const launchUrl = response.data.launchUrl + (token ? `&token=${token}` : '')
      setGameUrl(launchUrl)
    } catch (error: any) {
      setError(error.response?.data?.error || 'Failed to launch game. Please try again.')
    }
  }

  const handleCloseGame = () => {
    setGameUrl(null)
  }

  // Filter games based on search and category
  const filteredGames = games?.filter((game: any) => {
    const matchesSearch = game.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         game.providerName.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesCategory = selectedCategory === 'ALL' || game.category === selectedCategory
    return matchesSearch && matchesCategory
  })

  // Get unique categories
  const categories = ['ALL', ...new Set(games?.map((game: any) => game.category) || [])]

  return (
    <>
      <Navbar />
      <Container maxWidth="lg">
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" gutterBottom>
            Casino Games
          </Typography>
          <Typography color="text.secondary">
            Choose from our wide selection of games
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError('')}>
            {error}
          </Alert>
        )}

        {/* Search and Filters */}
        <Box sx={{ mb: 4 }}>
          <TextField
            fullWidth
            placeholder="Search games..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
            sx={{ mb: 2 }}
          />

          {/* Category Filter */}
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {categories.map((category: string) => (
              <Chip
                key={category}
                label={category}
                onClick={() => setSelectedCategory(category)}
                color={selectedCategory === category ? 'primary' : 'default'}
                sx={{ cursor: 'pointer' }}
              />
            ))}
          </Box>
        </Box>

        {/* Games Grid */}
        {isLoading ? (
          <Grid container spacing={3}>
            {[1, 2, 3, 4, 5, 6].map((n) => (
              <Grid item xs={12} sm={6} md={4} key={n}>
                <Card>
                  <Skeleton variant="rectangular" height={200} />
                  <CardContent>
                    <Skeleton variant="text" height={32} />
                    <Skeleton variant="text" height={24} />
                    <Skeleton variant="rectangular" height={36} sx={{ mt: 2 }} />
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        ) : filteredGames?.length === 0 ? (
          <Box sx={{ textAlign: 'center', py: 8 }}>
            <Typography variant="h6" color="text.secondary">
              No games found
            </Typography>
            <Typography color="text.secondary">
              Try adjusting your search or filters
            </Typography>
          </Box>
        ) : (
          <Grid container spacing={3}>
            {filteredGames?.map((game: any) => (
              <Grid item xs={12} sm={6} md={4} key={game.id}>
                <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                  <CardMedia
                    component="img"
                    height="200"
                    image={game.thumbnailUrl || 'https://via.placeholder.com/300x200?text=Casino+Game'}
                    alt={game.name}
                    sx={{ objectFit: 'cover' }}
                  />
                  <CardContent sx={{ flexGrow: 1 }}>
                    <Typography variant="h6" gutterBottom noWrap>
                      {game.name}
                    </Typography>
                    <Box sx={{ display: 'flex', gap: 1, mb: 2, flexWrap: 'wrap' }}>
                      <Chip label={game.providerName} size="small" />
                      <Chip label={game.category} size="small" color="primary" />
                    </Box>
                    <ButtonGroup fullWidth variant="contained" sx={{ mt: 'auto' }}>
                      <Button
                        startIcon={<PlayArrowIcon />}
                        onClick={() => handlePlayGame(game.id, false)}
                      >
                        Play
                      </Button>
                      <Button
                        startIcon={<VisibilityIcon />}
                        onClick={() => handlePlayGame(game.id, true)}
                        color="secondary"
                      >
                        Demo
                      </Button>
                    </ButtonGroup>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        )}

        {/* Results count */}
        {!isLoading && filteredGames && (
          <Box sx={{ mt: 4, textAlign: 'center' }}>
            <Typography color="text.secondary">
              Showing {filteredGames.length} {filteredGames.length === 1 ? 'game' : 'games'}
            </Typography>
          </Box>
        )}
      </Container>

      {/* Game Modal */}
      <Dialog
        open={!!gameUrl}
        onClose={handleCloseGame}
        maxWidth={false}
        fullScreen
        PaperProps={{
          sx: {
            backgroundColor: '#000',
          }
        }}
      >
        <Box sx={{ position: 'relative', width: '100%', height: '100%' }}>
          <IconButton
            onClick={handleCloseGame}
            sx={{
              position: 'absolute',
              top: 16,
              right: 16,
              zIndex: 1,
              backgroundColor: 'rgba(0, 0, 0, 0.7)',
              color: 'white',
              '&:hover': {
                backgroundColor: 'rgba(0, 0, 0, 0.9)',
              }
            }}
          >
            <CloseIcon />
          </IconButton>
          {gameUrl && (
            <iframe
              src={gameUrl}
              style={{
                width: '100%',
                height: '100%',
                border: 'none',
              }}
              title="Casino Game"
            />
          )}
        </Box>
      </Dialog>
    </>
  )
}

export default Games
