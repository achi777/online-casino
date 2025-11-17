import { AppBar, Toolbar, Typography, Button, Box, IconButton, Menu, MenuItem, Badge, Chip } from '@mui/material'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useState } from 'react'
import AccountCircleIcon from '@mui/icons-material/AccountCircle'
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet'
import SportsEsportsIcon from '@mui/icons-material/SportsEsports'
import DashboardIcon from '@mui/icons-material/Dashboard'
import HistoryIcon from '@mui/icons-material/History'
import SecurityIcon from '@mui/icons-material/Security'
import LogoutIcon from '@mui/icons-material/Logout'
import StarsIcon from '@mui/icons-material/Stars'
import { useQuery } from 'react-query'
import axios from 'axios'

const Navbar = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { logout } = useAuth()
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null)

  const { data: balance } = useQuery('balance', async () => {
    const response = await axios.get('/api/user/wallet/balance')
    return response.data
  }, {
    refetchInterval: 30000, // Refetch every 30 seconds
  })

  const { data: vipStatus } = useQuery('vipStatus', async () => {
    const response = await axios.get('/api/user/vip/my-status')
    return response.data
  }, {
    refetchInterval: 60000, // Refetch every 60 seconds
  })

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget)
  }

  const handleClose = () => {
    setAnchorEl(null)
  }

  const handleLogout = () => {
    handleClose()
    logout()
    navigate('/login')
  }

  const isActive = (path: string) => location.pathname === path

  return (
    <AppBar position="static" sx={{ mb: 3 }}>
      <Toolbar>
        <SportsEsportsIcon sx={{ mr: 2 }} />
        <Typography variant="h6" component="div" sx={{ flexGrow: 0, mr: 4 }}>
          Casino
        </Typography>

        <Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
          <Button
            color="inherit"
            startIcon={<DashboardIcon />}
            onClick={() => navigate('/dashboard')}
            sx={{
              borderBottom: isActive('/dashboard') ? '2px solid white' : 'none',
              borderRadius: 0
            }}
          >
            Dashboard
          </Button>
          <Button
            color="inherit"
            startIcon={<SportsEsportsIcon />}
            onClick={() => navigate('/games')}
            sx={{
              borderBottom: isActive('/games') ? '2px solid white' : 'none',
              borderRadius: 0
            }}
          >
            Games
          </Button>
          <Button
            color="inherit"
            startIcon={<AccountBalanceWalletIcon />}
            onClick={() => navigate('/wallet')}
            sx={{
              borderBottom: isActive('/wallet') ? '2px solid white' : 'none',
              borderRadius: 0
            }}
          >
            Wallet
          </Button>
          <Button
            color="inherit"
            startIcon={<HistoryIcon />}
            onClick={() => navigate('/game-history')}
            sx={{
              borderBottom: isActive('/game-history') ? '2px solid white' : 'none',
              borderRadius: 0
            }}
          >
            History
          </Button>
          <Button
            color="inherit"
            startIcon={<StarsIcon />}
            onClick={() => navigate('/vip')}
            sx={{
              borderBottom: isActive('/vip') ? '2px solid white' : 'none',
              borderRadius: 0,
              position: 'relative'
            }}
          >
            VIP
            {vipStatus?.currentTier && (
              <Chip
                label={vipStatus.currentTier.name}
                size="small"
                sx={{
                  ml: 1,
                  height: 20,
                  fontSize: '0.7rem',
                  backgroundColor: vipStatus.currentTier.color,
                  color: '#000'
                }}
              />
            )}
          </Button>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Badge
            badgeContent={`₾${balance?.toFixed(2) || '0.00'}`}
            color="secondary"
            sx={{ mr: 3 }}
          >
            <AccountBalanceWalletIcon />
          </Badge>

          <IconButton
            size="large"
            onClick={handleMenu}
            color="inherit"
          >
            <AccountCircleIcon />
          </IconButton>
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleClose}
          >
            <MenuItem onClick={() => { handleClose(); navigate('/profile'); }}>
              <AccountCircleIcon sx={{ mr: 1 }} /> Profile
            </MenuItem>
            <MenuItem onClick={() => { handleClose(); navigate('/responsible-gaming'); }}>
              <SecurityIcon sx={{ mr: 1 }} /> Responsible Gaming
            </MenuItem>
            <MenuItem onClick={handleLogout}>
              <LogoutIcon sx={{ mr: 1 }} /> Logout
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  )
}

export default Navbar
