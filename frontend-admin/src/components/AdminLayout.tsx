import React, { ReactNode } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import {
  Box, AppBar, Toolbar, Typography, Drawer, List, ListItem,
  ListItemIcon, ListItemText, ListItemButton, Divider, IconButton, Chip
} from '@mui/material'
import {
  Dashboard as DashboardIcon,
  People as PeopleIcon,
  SportsEsports as GamesIcon,
  Payment as PaymentIcon,
  Assessment as ReportsIcon,
  ExitToApp as LogoutIcon,
  Menu as MenuIcon,
  AccountBalance as ProviderIcon,
  VerifiedUser as KYCIcon,
  CardGiftcard as BonusIcon,
  Stars as VIPIcon,
  Support as SupportIcon,
  SupervisorAccount as AdminIcon,
  History as AuditIcon,
  Settings as SettingsIcon,
  Announcement as CMSIcon,
  AccountBalanceWallet as PaymentMethodIcon
} from '@mui/icons-material'
import { useAuth } from '../context/AuthContext'
import { usePermissions } from '../hooks/usePermissions'

const drawerWidth = 240

interface AdminLayoutProps {
  children: ReactNode
  title: string
}

const AdminLayout = ({ children, title }: AdminLayoutProps) => {
  const { admin, logout } = useAuth()
  const permissions = usePermissions()
  const navigate = useNavigate()
  const location = useLocation()
  const [mobileOpen, setMobileOpen] = React.useState(false)

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen)
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  // Define all menu items with their permissions
  const allMenuItems = [
    {
      text: 'Dashboard',
      icon: <DashboardIcon />,
      path: '/dashboard',
      show: permissions.canViewDashboard
    },
    {
      text: 'Users',
      icon: <PeopleIcon />,
      path: '/users',
      show: permissions.canViewUsers
    },
    {
      text: 'KYC Verification',
      icon: <KYCIcon />,
      path: '/kyc',
      show: permissions.canManageKYC
    },
    {
      text: 'Games',
      icon: <GamesIcon />,
      path: '/games',
      show: permissions.canViewGames
    },
    {
      text: 'Providers',
      icon: <ProviderIcon />,
      path: '/providers',
      show: permissions.canManageProviders
    },
    {
      text: 'Transactions',
      icon: <PaymentIcon />,
      path: '/transactions',
      show: permissions.canViewTransactions
    },
    {
      text: 'Payment Methods',
      icon: <PaymentMethodIcon />,
      path: '/payment-methods',
      show: permissions.canViewPaymentMethods
    },
    {
      text: 'Bonuses',
      icon: <BonusIcon />,
      path: '/bonuses',
      show: permissions.canViewBonuses
    },
    {
      text: 'VIP Management',
      icon: <VIPIcon />,
      path: '/vip',
      show: permissions.canViewVIP
    },
    {
      text: 'Support',
      icon: <SupportIcon />,
      path: '/support',
      show: permissions.canViewSupport
    },
    {
      text: 'Reports',
      icon: <ReportsIcon />,
      path: '/reports',
      show: permissions.canViewReports
    },
    {
      text: 'CMS/Banners',
      icon: <CMSIcon />,
      path: '/cms',
      show: permissions.canViewCMS
    },
    {
      text: 'Admins',
      icon: <AdminIcon />,
      path: '/admins',
      show: permissions.canManageAdmins
    },
    {
      text: 'Audit Logs',
      icon: <AuditIcon />,
      path: '/audit-logs',
      show: permissions.canViewAuditLogs
    },
    {
      text: 'Settings',
      icon: <SettingsIcon />,
      path: '/settings',
      show: permissions.canManageSystemSettings
    },
  ]

  const menuItems = allMenuItems.filter(item => item.show)

  const drawer = (
    <Box>
      <Toolbar sx={{ bgcolor: 'primary.main', color: 'white' }}>
        <Typography variant="h6" noWrap>
          Casino Admin
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              onClick={() => navigate(item.path)}
              selected={location.pathname === item.path}
            >
              <ListItemIcon
                sx={{
                  color: location.pathname === item.path ? 'primary.main' : 'inherit'
                }}
              >
                {item.icon}
              </ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
      <Divider />
      <List>
        <ListItem disablePadding>
          <ListItemButton onClick={handleLogout}>
            <ListItemIcon><LogoutIcon /></ListItemIcon>
            <ListItemText primary="Logout" />
          </ListItemButton>
        </ListItem>
      </List>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          ml: { sm: `${drawerWidth}px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            {title}
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Chip
              label={admin?.role || 'ADMIN'}
              color="secondary"
              size="small"
              sx={{ fontWeight: 'bold' }}
            />
            <Typography variant="body2">
              {admin?.firstName} {admin?.lastName}
            </Typography>
          </Box>
        </Toolbar>
      </AppBar>

      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          minHeight: '100vh',
          bgcolor: 'background.default'
        }}
      >
        <Toolbar />
        {children}
      </Box>
    </Box>
  )
}

export default AdminLayout
