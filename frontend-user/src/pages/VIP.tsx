import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  Divider,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Container
} from '@mui/material'
import {
  Stars as VIPIcon,
  TrendingUp as TrendingUpIcon,
  CardGiftcard as GiftIcon,
  LocalAtm as MoneyIcon,
  EmojiEvents as TrophyIcon,
  CheckCircle as CheckIcon
} from '@mui/icons-material'
import { useAuth } from '../context/AuthContext'
import Navbar from '../components/Navbar'
import axios from 'axios'

interface VIPTier {
  id: number
  name: string
  level: number
  minPoints: number
  cashbackPercentage: number
  bonusMultiplier: number
  monthlyBonus?: number
  color: string
}

interface VIPStatus {
  currentTier?: VIPTier
  currentPoints: number
  nextTier?: VIPTier
  pointsToNextTier: number
  totalWagered: number
  lifetimeDeposits: number
}

interface PointsTransaction {
  id: number
  points: number
  type: string
  description: string
  createdAt: string
  relatedAmount?: number
}

const VIP = () => {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const [vipStatus, setVIPStatus] = useState<VIPStatus | null>(null)
  const [allTiers, setAllTiers] = useState<VIPTier[]>([])
  const [pointsHistory, setPointsHistory] = useState<PointsTransaction[]>([])

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    } else {
      fetchVIPStatus()
      fetchAllTiers()
      fetchPointsHistory()
    }
  }, [isAuthenticated, navigate])

  const fetchVIPStatus = async () => {
    try {
      const response = await axios.get('/api/user/vip/my-status')
      console.log('VIP Status Response:', response.data)
      setVIPStatus(response.data)
    } catch (error) {
      console.error('Failed to fetch VIP status:', error)
    }
  }

  const fetchAllTiers = async () => {
    try {
      const response = await axios.get('/api/user/vip/tiers')
      console.log('VIP Tiers Response:', response.data)
      setAllTiers(response.data)
    } catch (error) {
      console.error('Failed to fetch VIP tiers:', error)
    }
  }

  const fetchPointsHistory = async () => {
    try {
      const response = await axios.get('/api/user/vip/my-history')
      console.log('VIP Points History Response:', response.data)
      console.log('Points History Length:', response.data?.length)
      setPointsHistory(response.data)
    } catch (error) {
      console.error('Failed to fetch points history:', error)
    }
  }

  const calculateProgress = () => {
    if (!vipStatus || !vipStatus.nextTier) return 100

    const currentTierMin = vipStatus.currentTier?.minPoints || 0
    const nextTierMin = vipStatus.nextTier.minPoints
    const currentPoints = vipStatus.currentPoints

    return ((currentPoints - currentTierMin) / (nextTierMin - currentTierMin)) * 100
  }

  const getTypeLabel = (type: string) => {
    const labels: { [key: string]: string } = {
      WAGERING: 'თამაშიდან',
      DEPOSIT: 'დეპოზიტიდან',
      BONUS: 'ბონუსი',
      LEVEL_UP: 'ლეველ-აპ ბონუსი',
      MANUAL_ADJUSTMENT: 'ადმინის კორექცია',
      PROMO: 'პრომოცია',
      REFERRAL: 'რეფერალი',
      EXPIRED: 'ვადაგასული'
    }
    return labels[type] || type
  }

  const getTypeColor = (type: string) => {
    const colors: { [key: string]: any } = {
      WAGERING: 'primary',
      DEPOSIT: 'success',
      BONUS: 'warning',
      LEVEL_UP: 'error',
      MANUAL_ADJUSTMENT: 'info',
      PROMO: 'secondary',
      REFERRAL: 'success',
      EXPIRED: 'default'
    }
    return colors[type] || 'default'
  }

  return (
    <>
      <Navbar />
      <Container maxWidth="lg">
        {/* Header */}
        <Box sx={{ mb: 4 }}>
          <Typography variant="h4" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TrophyIcon sx={{ fontSize: 40 }} />
            VIP & Loyalty Program
          </Typography>
          <Typography color="text.secondary">
            დააგროვე ქულები და მიიღე ექსკლუზიური혜택ები
          </Typography>
        </Box>

        {/* Current Status Card */}
        {vipStatus && (
          <Paper sx={{ mb: 4, p: 4, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                  <VIPIcon sx={{ fontSize: 50 }} />
                  <Box>
                    <Typography variant="h4">
                      {vipStatus.currentTier ? vipStatus.currentTier.name : 'Bronze'}
                    </Typography>
                    <Typography variant="body2" sx={{ opacity: 0.9 }}>
                      შენი მიმდინარე VIP დონე
                    </Typography>
                  </Box>
                </Box>

                <Box sx={{ mt: 3 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body2">
                      {vipStatus.currentPoints.toLocaleString()} ქულა
                    </Typography>
                    {vipStatus.nextTier && (
                      <Typography variant="body2">
                        {vipStatus.nextTier.name}: {vipStatus.nextTier.minPoints.toLocaleString()}
                      </Typography>
                    )}
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={calculateProgress()}
                    sx={{
                      height: 10,
                      borderRadius: 5,
                      backgroundColor: 'rgba(255,255,255,0.3)',
                      '& .MuiLinearProgress-bar': {
                        backgroundColor: '#ffd700'
                      }
                    }}
                  />
                  {vipStatus.nextTier && (
                    <Typography variant="body2" sx={{ mt: 1, opacity: 0.9 }}>
                      კიდევ {vipStatus.pointsToNextTier.toLocaleString()} ქულა {vipStatus.nextTier.name} დონემდე
                    </Typography>
                  )}
                </Box>
              </Grid>

              <Grid item xs={12} md={6}>
                <Grid container spacing={2}>
                  <Grid item xs={6}>
                    <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'rgba(255,255,255,0.1)', borderRadius: 2 }}>
                      <Typography variant="h5">{vipStatus.currentTier?.cashbackPercentage || 0}%</Typography>
                      <Typography variant="body2" sx={{ opacity: 0.9 }}>Cashback</Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6}>
                    <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'rgba(255,255,255,0.1)', borderRadius: 2 }}>
                      <Typography variant="h5">{vipStatus.currentTier?.bonusMultiplier || 1}x</Typography>
                      <Typography variant="body2" sx={{ opacity: 0.9 }}>Bonus Multiplier</Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6}>
                    <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'rgba(255,255,255,0.1)', borderRadius: 2 }}>
                      <Typography variant="h5">₾{vipStatus.totalWagered.toLocaleString()}</Typography>
                      <Typography variant="body2" sx={{ opacity: 0.9 }}>მთლიანი ვაგერი</Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={6}>
                    <Box sx={{ textAlign: 'center', p: 2, bgcolor: 'rgba(255,255,255,0.1)', borderRadius: 2 }}>
                      <Typography variant="h5">₾{vipStatus.lifetimeDeposits.toLocaleString()}</Typography>
                      <Typography variant="body2" sx={{ opacity: 0.9 }}>მთლიანი დეპოზიტი</Typography>
                    </Box>
                  </Grid>
                </Grid>
              </Grid>
            </Grid>
          </Paper>
        )}

        {/* How to Earn Points */}
        <Paper sx={{ mb: 4, p: 3 }}>
          <Typography variant="h5" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TrendingUpIcon />
            როგორ დავაგროვო ქულები?
          </Typography>
          <Divider sx={{ my: 2 }} />
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Paper
                sx={{
                  p: 3,
                  display: 'flex',
                  alignItems: 'center',
                  gap: 2,
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                }}
              >
                <MoneyIcon sx={{ fontSize: 48 }} />
                <Box>
                  <Typography variant="h6">1 ლარი Wagering = 1 ქულა</Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    თამაშის დროს ყოველი დახარჯული ლარისთვის
                  </Typography>
                </Box>
              </Paper>
            </Grid>
            <Grid item xs={12} md={6}>
              <Paper
                sx={{
                  p: 3,
                  display: 'flex',
                  alignItems: 'center',
                  gap: 2,
                  background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
                }}
              >
                <GiftIcon sx={{ fontSize: 48 }} />
                <Box>
                  <Typography variant="h6">1 ლარი Deposit = 0.1 ქულა</Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    ანგარიშის შევსებისთვის
                  </Typography>
                </Box>
              </Paper>
            </Grid>
          </Grid>
        </Paper>

        {/* VIP Tiers */}
        <Typography variant="h5" gutterBottom sx={{ mb: 2 }}>
          VIP დონეები
        </Typography>
        <Grid container spacing={2} sx={{ mb: 4 }}>
          {allTiers.map((tier) => (
            <Grid item xs={12} sm={6} md={4} lg={2.4} key={tier.id}>
              <Paper
                sx={{
                  p: 2,
                  bgcolor: `${tier.color}20`,
                  border: `2px solid ${tier.color}`,
                  position: 'relative',
                  opacity: vipStatus?.currentTier?.level === tier.level ? 1 : 0.7,
                  transition: 'transform 0.2s',
                  '&:hover': { transform: 'translateY(-5px)' }
                }}
              >
                {vipStatus?.currentTier?.level === tier.level && (
                  <Chip
                    label="მიმდინარე"
                    color="primary"
                    size="small"
                    sx={{ position: 'absolute', top: 8, right: 8 }}
                  />
                )}
                <Typography variant="h6" gutterBottom>
                  {tier.name}
                </Typography>
                <Typography variant="body2" color="text.secondary" gutterBottom>
                  Level {tier.level}
                </Typography>
                <Divider sx={{ my: 1 }} />
                <List dense>
                  <ListItem>
                    <ListItemIcon sx={{ minWidth: 30 }}>
                      <CheckIcon fontSize="small" />
                    </ListItemIcon>
                    <ListItemText primary={`${tier.minPoints.toLocaleString()} ქულა`} />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon sx={{ minWidth: 30 }}>
                      <CheckIcon fontSize="small" />
                    </ListItemIcon>
                    <ListItemText primary={`${tier.cashbackPercentage}% Cashback`} />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon sx={{ minWidth: 30 }}>
                      <CheckIcon fontSize="small" />
                    </ListItemIcon>
                    <ListItemText primary={`${tier.bonusMultiplier}x Bonus`} />
                  </ListItem>
                </List>
              </Paper>
            </Grid>
          ))}
        </Grid>

        {/* Points History */}
        <Typography variant="h5" gutterBottom sx={{ mb: 2 }}>
          ქულების ისტორია
        </Typography>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>თარიღი</TableCell>
                <TableCell>ტიპი</TableCell>
                <TableCell>აღწერა</TableCell>
                <TableCell align="right">ქულები</TableCell>
                <TableCell align="right">თანხა</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {pointsHistory.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    <Typography color="text.secondary" sx={{ py: 4 }}>
                      ქულების ისტორია ცარიელია
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                pointsHistory.map((transaction) => (
                  <TableRow key={transaction.id}>
                    <TableCell>
                      {new Date(transaction.createdAt).toLocaleDateString('ka-GE', {
                        year: 'numeric',
                        month: 'short',
                        day: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={getTypeLabel(transaction.type)}
                        color={getTypeColor(transaction.type)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{transaction.description}</TableCell>
                    <TableCell align="right">
                      <Typography
                        color={transaction.points > 0 ? 'success.main' : 'error.main'}
                        fontWeight="bold"
                      >
                        {transaction.points > 0 ? '+' : ''}{transaction.points}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      {transaction.relatedAmount ? `₾${transaction.relatedAmount}` : '-'}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Container>
    </>
  )
}

export default VIP
