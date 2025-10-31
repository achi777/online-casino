import { useAuth } from '../context/AuthContext'

type AdminRole = 'OWNER' | 'ADMIN' | 'FINANCE' | 'SUPPORT' | 'CONTENT' | 'ANALYST' | 'COMPLIANCE'

interface Permissions {
  // User management
  canViewUsers: boolean
  canEditUsers: boolean
  canManageKYC: boolean

  // Financial
  canViewTransactions: boolean
  canApproveWithdrawals: boolean
  canManageDeposits: boolean
  canViewFinancialReports: boolean

  // Games & Content
  canViewGames: boolean
  canEditGames: boolean
  canManageProviders: boolean

  // Reports & Analytics
  canViewReports: boolean
  canViewDashboard: boolean

  // Admin management
  canManageAdmins: boolean
  canManageSystemSettings: boolean

  // Compliance
  canViewAuditLogs: boolean
  canManageCompliance: boolean

  // Bonuses & Promotions
  canViewBonuses: boolean
  canManageBonuses: boolean

  // VIP Management
  canViewVIP: boolean
  canManageVIP: boolean

  // Support
  canViewSupport: boolean
  canManageSupport: boolean

  // CMS
  canViewCMS: boolean
  canManageCMS: boolean

  // Payment Methods
  canViewPaymentMethods: boolean
  canManagePaymentMethods: boolean
}

const getRolePermissions = (role: AdminRole): Permissions => {
  const permissions: Record<AdminRole, Permissions> = {
    OWNER: {
      canViewUsers: true,
      canEditUsers: true,
      canManageKYC: true,
      canViewTransactions: true,
      canApproveWithdrawals: true,
      canManageDeposits: true,
      canViewFinancialReports: true,
      canViewGames: true,
      canEditGames: true,
      canManageProviders: true,
      canViewReports: true,
      canViewDashboard: true,
      canManageAdmins: true,
      canManageSystemSettings: true,
      canViewAuditLogs: true,
      canManageCompliance: true,
      canViewBonuses: true,
      canManageBonuses: true,
      canViewVIP: true,
      canManageVIP: true,
      canViewSupport: true,
      canManageSupport: true,
      canViewCMS: true,
      canManageCMS: true,
      canViewPaymentMethods: true,
      canManagePaymentMethods: true,
    },
    ADMIN: {
      canViewUsers: true,
      canEditUsers: true,
      canManageKYC: true,
      canViewTransactions: true,
      canApproveWithdrawals: true,
      canManageDeposits: true,
      canViewFinancialReports: true,
      canViewGames: true,
      canEditGames: true,
      canManageProviders: true,
      canViewReports: true,
      canViewDashboard: true,
      canManageAdmins: false,
      canManageSystemSettings: false,
      canViewAuditLogs: true,
      canManageCompliance: true,
      canViewBonuses: true,
      canManageBonuses: true,
      canViewVIP: true,
      canManageVIP: true,
      canViewSupport: true,
      canManageSupport: true,
      canViewCMS: true,
      canManageCMS: true,
      canViewPaymentMethods: true,
      canManagePaymentMethods: false,
    },
    FINANCE: {
      canViewUsers: true,
      canEditUsers: false,
      canManageKYC: false,
      canViewTransactions: true,
      canApproveWithdrawals: true,
      canManageDeposits: true,
      canViewFinancialReports: true,
      canViewGames: false,
      canEditGames: false,
      canManageProviders: false,
      canViewReports: true,
      canViewDashboard: true,
      canManageAdmins: false,
      canManageSystemSettings: false,
      canViewAuditLogs: false,
      canManageCompliance: false,
      canViewBonuses: true,
      canManageBonuses: false,
      canViewVIP: true,
      canManageVIP: false,
      canViewSupport: false,
      canManageSupport: false,
      canViewCMS: false,
      canManageCMS: false,
      canViewPaymentMethods: true,
      canManagePaymentMethods: true,
    },
    SUPPORT: {
      canViewUsers: true,
      canEditUsers: true,
      canManageKYC: true,
      canViewTransactions: true,
      canApproveWithdrawals: false,
      canManageDeposits: false,
      canViewFinancialReports: false,
      canViewGames: true,
      canEditGames: false,
      canManageProviders: false,
      canViewReports: false,
      canViewDashboard: true,
      canManageAdmins: false,
      canManageSystemSettings: false,
      canViewAuditLogs: false,
      canManageCompliance: false,
      canViewBonuses: true,
      canManageBonuses: false,
      canViewVIP: true,
      canManageVIP: true,
      canViewSupport: true,
      canManageSupport: true,
      canViewCMS: false,
      canManageCMS: false,
      canViewPaymentMethods: false,
      canManagePaymentMethods: false,
    },
    CONTENT: {
      canViewUsers: false,
      canEditUsers: false,
      canManageKYC: false,
      canViewTransactions: false,
      canApproveWithdrawals: false,
      canManageDeposits: false,
      canViewFinancialReports: false,
      canViewGames: true,
      canEditGames: true,
      canManageProviders: true,
      canViewReports: false,
      canViewDashboard: true,
      canManageAdmins: false,
      canManageSystemSettings: false,
      canViewAuditLogs: false,
      canManageCompliance: false,
      canViewBonuses: true,
      canManageBonuses: true,
      canViewVIP: false,
      canManageVIP: false,
      canViewSupport: false,
      canManageSupport: false,
      canViewCMS: true,
      canManageCMS: true,
      canViewPaymentMethods: false,
      canManagePaymentMethods: false,
    },
    ANALYST: {
      canViewUsers: true,
      canEditUsers: false,
      canManageKYC: false,
      canViewTransactions: true,
      canApproveWithdrawals: false,
      canManageDeposits: false,
      canViewFinancialReports: true,
      canViewGames: true,
      canEditGames: false,
      canManageProviders: false,
      canViewReports: true,
      canViewDashboard: true,
      canManageAdmins: false,
      canManageSystemSettings: false,
      canViewAuditLogs: false,
      canManageCompliance: false,
      canViewBonuses: true,
      canManageBonuses: false,
      canViewVIP: true,
      canManageVIP: false,
      canViewSupport: true,
      canManageSupport: false,
      canViewCMS: true,
      canManageCMS: false,
      canViewPaymentMethods: true,
      canManagePaymentMethods: false,
    },
    COMPLIANCE: {
      canViewUsers: true,
      canEditUsers: false,
      canManageKYC: true,
      canViewTransactions: true,
      canApproveWithdrawals: false,
      canManageDeposits: false,
      canViewFinancialReports: false,
      canViewGames: false,
      canEditGames: false,
      canManageProviders: false,
      canViewReports: true,
      canViewDashboard: true,
      canManageAdmins: false,
      canManageSystemSettings: false,
      canViewAuditLogs: true,
      canManageCompliance: true,
      canViewBonuses: false,
      canManageBonuses: false,
      canViewVIP: true,
      canManageVIP: false,
      canViewSupport: true,
      canManageSupport: false,
      canViewCMS: false,
      canManageCMS: false,
      canViewPaymentMethods: false,
      canManagePaymentMethods: false,
    },
  }

  return permissions[role]
}

export const usePermissions = () => {
  const { admin } = useAuth()

  if (!admin || !admin.role) {
    return getRolePermissions('ANALYST') // Most restrictive by default
  }

  const permissions = getRolePermissions(admin.role as AdminRole)

  return {
    ...permissions,
    role: admin.role,
  }
}
